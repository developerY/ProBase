import { createRemoteJWKSet, jwtVerify, decodeJwt } from "jose";

export interface Env {
  GEMINI_API_KEY: string;
  FIREBASE_PROJECT_NUMBER: string;
}

// Cached at the Worker module level so Cloudflare doesn't re-fetch Google's keys on every request
const FIREBASE_APP_CHECK_JWKS = createRemoteJWKSet(
  new URL("https://firebaseappcheck.googleapis.com/v1/jwks")
);

interface ProxyRequestPayload {
  model?: string;
  prompt?: string;
  systemInstruction?: string;
  temperature?: number;
  maxOutputTokens?: number;
}

export default {
  async fetch(request: Request, env: Env): Promise<Response> {
    const startTime = Date.now();
    console.log(`[Proxy] Incoming ${request.method} request to ${request.url}`);

    // 1. Only allow POST requests
    if (request.method !== "POST") {
      return Response.json(
        { error: "Method Not Allowed. Use POST." },
        { status: 405 }
      );
    }

    // 2. Require the X-Firebase-AppCheck header from the Android app
    const appCheckToken = request.headers.get("X-Firebase-AppCheck");
    if (!appCheckToken) {
      return Response.json(
        { error: "Unauthorized: Missing X-Firebase-AppCheck header" },
        { status: 401 }
      );
    }

    // 3. Cryptographically verify the App Check JWT against Google's public JWKS
    const rawDecoded = decodeJwt(appCheckToken);
    const actualIssuer = (rawDecoded.iss as string) || "unknown";
    const actualProjectNumber = actualIssuer.split("/").pop() || "";

    // Extract project numbers from env (supports comma-separated list) plus your known projects
    const envNumbers = (env.FIREBASE_PROJECT_NUMBER || "")
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);

    const allowedProjectNumbers = Array.from(
      new Set([
        ...envNumbers,
        "20197827893",  // buildwithai-453716
        "10328426049",  // kocolor-dev (11-digit check)
        "315098361468", // kocolor-app
        "960844692488", // kocolor-ai
      ])
    );

    const allowedIssuers = allowedProjectNumbers.map(
      (num) => `https://firebaseappcheck.googleapis.com/${num}`
    );

    try {
      await jwtVerify(appCheckToken, FIREBASE_APP_CHECK_JWKS, {
        issuer: allowedIssuers,
        audience: allowedProjectNumbers.map((num) => `projects/${num}`),
        algorithms: ["RS256"],
      });
      console.log(`[AppCheck] Verification SUCCESS for project ${actualProjectNumber}!`);
    } catch (err: any) {
      console.error(
        `[AppCheck] Verification FAILED for issuer ${actualIssuer}: ${err?.message}`
      );
      return Response.json(
        {
          error: "Unauthorized: Invalid or expired App Check token",
          details: err?.message ?? "JWT verification failed",
          tokenIssuer: actualIssuer,
          tokenProjectNumber: actualProjectNumber,
          allowedProjects: allowedProjectNumbers,
        },
        { status: 401 }
      );
    }

    // 4. Parse incoming JSON body from the Android app
    let payload: ProxyRequestPayload;
    try {
      payload = (await request.json()) as ProxyRequestPayload;
    } catch {
      return Response.json({ error: "Invalid JSON body" }, { status: 400 });
    }

    const prompt = payload.prompt?.trim();
    if (!prompt) {
      return Response.json(
        { error: "Missing 'prompt' field in request body" },
        { status: 400 }
      );
    }

    const model = payload.model || "gemma-4-26b-a4b-it";
    const temperature = payload.temperature ?? 0.2;
    const maxOutputTokens = payload.maxOutputTokens ?? 4096; // Give Gemma room for thought blocks
    const isGeminiModel = model.toLowerCase().startsWith("gemini");
    const isGemmaModel = model.toLowerCase().includes("gemma");

    // 5. Construct the upstream Google Generative Language API request
    const upstreamUrl = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(
      model
    )}:generateContent`;

    // Force strict JSON output and prevent Gemma from writing 40-second Markdown monologues
    const finalPrompt = isGemmaModel
      ? `${prompt}\n\nCRITICAL INSTRUCTION: Do NOT write any analysis, bullet points, markdown, or reasoning. Output ONLY the raw, valid JSON object starting immediately with '{' and ending with '}'.`
      : prompt;

    // 5 & 6. Call Google's API with JSON mode (and automatic fallback if model rejects responseMimeType)
    const callGoogleApi = async (useJsonMimeType: boolean) => {
      const genConfig: Record<string, unknown> = {
        temperature,
        maxOutputTokens,
      };
      if (useJsonMimeType) {
        genConfig.responseMimeType = "application/json";
      }

      // Only attach thinkingConfig on Gemini models (Gemma models may reject it with HTTP 400)
      if (isGeminiModel) {
        genConfig.thinkingConfig = {
          thinkingLevel: "MINIMAL",
        };
      }

      const bodyObj: Record<string, unknown> = {
        contents: [
          {
            role: "user",
            parts: [{ text: finalPrompt }],
          },
        ],
        generationConfig: genConfig,
      };

      if (payload.systemInstruction && !isGemmaModel) {
        bodyObj.systemInstruction = {
          parts: [{ text: payload.systemInstruction }],
        };
      }

      return fetch(upstreamUrl, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "x-goog-api-key": env.GEMINI_API_KEY,
        },
        body: JSON.stringify(bodyObj),
      });
    };

    let upstreamResponse = await callGoogleApi(true);
    if (upstreamResponse.status === 400) {
      console.warn(`[Proxy] Model ${model} rejected responseMimeType; retrying without it...`);
      upstreamResponse = await callGoogleApi(false);
    }

    const responseText = await upstreamResponse.text();
    const duration = Date.now() - startTime;

    console.log(
      `[Proxy] Upstream responded with HTTP ${upstreamResponse.status} in ${duration}ms (Response Length: ${responseText.length} chars)`
    );

    return new Response(responseText, {
      status: upstreamResponse.status,
      headers: {
        "Content-Type": "application/json",
      },
    });
  },
};
