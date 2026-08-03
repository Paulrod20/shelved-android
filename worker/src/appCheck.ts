interface AppCheckClaims {
  aud?: string | string[];
  exp?: number;
  iss?: string;
  sub?: string;
}

interface JwtHeader {
  alg?: string;
  kid?: string;
  typ?: string;
}

interface JsonWebKeySet {
  keys: AppCheckJwk[];
}

interface AppCheckJwk extends JsonWebKey {
  kid?: string;
}

let cachedKeys: {expiresAt: number; keys: AppCheckJwk[]} | null = null;

export async function verifyAppCheckToken(
  token: string | null,
  projectNumber: string,
  appId: string,
): Promise<string> {
  if (!token) throw new Error("Missing App Check token.");
  const parts = token.split(".");
  if (parts.length !== 3) throw new Error("Malformed App Check token.");

  const header = decodeJson<JwtHeader>(parts[0]);
  const claims = decodeJson<AppCheckClaims>(parts[1]);
  if (header.alg !== "RS256" || header.typ !== "JWT" || !header.kid) throw new Error("Invalid token header.");

  const key = (await appCheckKeys()).find((candidate) => candidate.kid === header.kid)
    ?? (await appCheckKeys(true)).find((candidate) => candidate.kid === header.kid);
  if (!key) throw new Error("Unknown App Check signing key.");
  const publicKey = await crypto.subtle.importKey(
    "jwk",
    key,
    {name: "RSASSA-PKCS1-v1_5", hash: "SHA-256"},
    false,
    ["verify"],
  );
  const isValid = await crypto.subtle.verify(
    "RSASSA-PKCS1-v1_5",
    publicKey,
    asArrayBuffer(decodeBase64Url(parts[2])),
    new TextEncoder().encode(`${parts[0]}.${parts[1]}`),
  );
  if (!isValid) throw new Error("Invalid App Check signature.");
  validateClaims(claims, projectNumber, appId, Math.floor(Date.now() / 1000));
  return claims.sub!;
}

export function validateClaims(
  claims: AppCheckClaims,
  projectNumber: string,
  appId: string,
  nowSeconds: number,
): void {
  const audience = Array.isArray(claims.aud) ? claims.aud : [claims.aud];
  if (claims.iss !== `https://firebaseappcheck.googleapis.com/${projectNumber}`) throw new Error("Invalid issuer.");
  if (!audience.includes(`projects/${projectNumber}`)) throw new Error("Invalid audience.");
  if (!claims.exp || claims.exp <= nowSeconds) throw new Error("Expired token.");
  if (claims.sub !== appId) throw new Error("Invalid app ID.");
}

async function appCheckKeys(forceRefresh = false): Promise<AppCheckJwk[]> {
  if (!forceRefresh && cachedKeys && cachedKeys.expiresAt > Date.now()) return cachedKeys.keys;
  const response = await fetch("https://firebaseappcheck.googleapis.com/v1/jwks");
  if (!response.ok) throw new Error("Unable to load App Check keys.");
  const keySet = await response.json<JsonWebKeySet>();
  const maxAge = /max-age=(\d+)/.exec(response.headers.get("Cache-Control") ?? "")?.[1];
  cachedKeys = {
    keys: keySet.keys,
    expiresAt: Date.now() + Math.min(Number(maxAge ?? 21_600), 21_600) * 1000,
  };
  return cachedKeys.keys;
}

function decodeJson<T>(value: string): T {
  return JSON.parse(new TextDecoder().decode(decodeBase64Url(value))) as T;
}

function decodeBase64Url(value: string): Uint8Array {
  const base64 = value.replace(/-/g, "+").replace(/_/g, "/").padEnd(Math.ceil(value.length / 4) * 4, "=");
  return Uint8Array.from(atob(base64), (character) => character.charCodeAt(0));
}

function asArrayBuffer(value: Uint8Array): ArrayBuffer {
  return value.buffer.slice(value.byteOffset, value.byteOffset + value.byteLength) as ArrayBuffer;
}
