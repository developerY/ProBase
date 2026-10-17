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
      console.warn(`[Proxy] Rejected non-POST method: ${request.method}`);
      return Response.json(
        { error: "Method Not Allowed. Use POST." },
        { status: 405 }
      );
    }

    // 2. Require the X-Firebase-AppCheck header from the Android app
    const appCheckToken = request.headers.get("X-Firebase-AppCheck");
    if (!appCheckToken) {
      console.warn(`[Proxy] Rejected: Missing X-Firebase-AppCheck header`);
      return Response.json(
        { error: "Unauthorized: Missing X-Firebase-AppCheck header" },
        { status: 401 }
      );
    }

    // 3. Cryptographically verify the App Check JWT against Google's public JWKS
    const allowedProjectNumbers = [
      env.FIREBASE_PROJECT_NUMBER,
      "20197827893", // buildwithai-453716
      "10328426049", // kocolor-dev
      "315098361468", // kocolor-app
      "960844692488"  // kocolor-ai
    ].filter(Boolean);

    try {
      const unverified = decodeJwt(appCheckToken);
      const iss = (unverified.iss as string) || "";
      const matchedNumber = allowedProjectNumbers.find((num) => iss.endsWith(`/${num}`)) || env.FIREBASE_PROJECT_NUMBER;

      console.log(`[AppCheck] Token issuer: ${iss} | Matching project: ${matchedNumber}`);

      await jwtVerify(appCheckToken, FIREBASE_APP_CHECK_JWKS, {
        issuer: `https://firebaseappcheck.googleapis.com/${matchedNumber}`,
        audience: `projects/${matchedNumber}`,
        algorithms: ["RS256"],
      });
      console.log(`[AppCheck] Verification SUCCESS for project ${matchedNumber}!`);
    } catch (err: any) {
      console.error(`[AppCheck] Verification FAILED: ${err?.message}`);
      return Response.json(
        {
          error: "Unauthorized: Invalid or expired App Check token",
          details: err?.message ?? "JWT verification failed",
        },
        { status: 401 }
      );
    }

    // 4. Parse incoming JSON body from the Android app
    let payload: ProxyRequestPayload;
    try {
      payload = (await request.json()) as ProxyRequestPayload;
    } catch {
      console.error(`[Proxy] Failed to parse JSON request body`);
      return Response.json({ error: "Invalid JSON body" }, { status: 400 });
    }

    const prompt = payload.prompt?.trim();
    if (!prompt) {
      console.error(`[Proxy] Missing 'prompt' field in payload`);
      return Response.json(
        { error: "Missing 'prompt' field in request body" },
        { status: 400 }
      );
    }

    // Allow only Gemma / Gemini models
    const model = payload.model || "gemma-4-26b-a4b-it";
    const temperature = payload.temperature ?? 0.2;
    const maxOutputTokens = payload.maxOutputTokens ?? 2048;

    console.log(`[Proxy] Forwarding to Gemma 4 (Model: ${model}, Prompt Length: ${prompt.length} chars)`);

    // 5. Construct the upstream Google Generative Language API request
    const upstreamUrl = `https://generativelanguage.googleapis.com/v1beta/models/${encodeURIComponent(
      model
    )}:generateContent`;

    const googleRequestBody: Record<string, unknown> = {
      contents: [
        {
          role: "user",
          parts: [{ text: prompt }],
        },
      ],
      generationConfig: {
        temperature,
        maxOutputTokens,
        thinkingConfig: {
          thinkingLevel: "MINIMAL"
        }
      },
    };

    if (payload.systemInstruction) {
      googleRequestBody.systemInstruction = {
        parts: [{ text: payload.systemInstruction }],
      };
    }

    // 6. Call Google's API injecting the encrypted server-side GEMINI_API_KEY
    const upstreamResponse = await fetch(upstreamUrl, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "x-goog-api-key": env.GEMINI_API_KEY,
      },
      body: JSON.stringify(googleRequestBody),
    });

    const responseText = await upstreamResponse.text();
    const duration = Date.now() - startTime;

    console.log(`[Proxy] Upstream responded with HTTP ${upstreamResponse.status} in ${duration}ms (Response Length: ${responseText.length} chars)`);

    return new Response(responseText, {
      status: upstreamResponse.status,
      headers: {
        "Content-Type": "application/json",
      },
    });
  },
};
