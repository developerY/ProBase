import { createRemoteJWKSet, jwtVerify } from "jose";

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
    const projectNumber = env.FIREBASE_PROJECT_NUMBER;
    try {
      await jwtVerify(appCheckToken, FIREBASE_APP_CHECK_JWKS, {
        issuer: `https://firebaseappcheck.googleapis.com/${projectNumber}`,
        audience: `projects/${projectNumber}`,
        algorithms: ["RS256"],
      });
    } catch (err: any) {
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
      return Response.json({ error: "Invalid JSON body" }, { status: 400 });
    }

    const prompt = payload.prompt?.trim();
    if (!prompt) {
      return Response.json(
        { error: "Missing 'prompt' field in request body" },
        { status: 400 }
      );
    }

    // Allow only Gemma / Gemini models
    const model = payload.model || "gemma-4-26b-a4b-it";
    const temperature = payload.temperature ?? 0.2;
    const maxOutputTokens = payload.maxOutputTokens ?? 2048;

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

    return new Response(responseText, {
      status: upstreamResponse.status,
      headers: {
        "Content-Type": "application/json",
      },
    });
  },
};
