import {ApiError} from "./catalog";
import {Env} from "./env";

interface CachedToken {
  expiresAtMillis: number;
  value: string;
}

export class TwitchTokenProvider {
  private cachedToken: CachedToken | null = null;
  private pendingToken: Promise<string> | null = null;

  async get(env: Env): Promise<string> {
    if (this.cachedToken && this.cachedToken.expiresAtMillis > Date.now() + 60_000) {
      return this.cachedToken.value;
    }
    if (this.pendingToken) return this.pendingToken;

    this.pendingToken = this.request(env);
    try {
      return await this.pendingToken;
    } finally {
      this.pendingToken = null;
    }
  }

  invalidate(): void {
    this.cachedToken = null;
  }

  private async request(env: Env): Promise<string> {
    const requestedAt = Date.now();
    const url = new URL("https://id.twitch.tv/oauth2/token");
    url.search = new URLSearchParams({
      client_id: env.TWITCH_CLIENT_ID,
      client_secret: env.TWITCH_CLIENT_SECRET,
      grant_type: "client_credentials",
    }).toString();
    const response = await fetch(url, {method: "POST", signal: AbortSignal.timeout(10_000)});
    if (!response.ok) {
      console.error("Twitch rejected the IGDB credentials.", {status: response.status});
      throw new ApiError(503, "IGDB credentials are not configured correctly.");
    }
    const result = await response.json<{access_token?: string; expires_in?: number}>();
    if (!result.access_token || !result.expires_in) {
      throw new ApiError(503, "IGDB returned an invalid access token.");
    }
    this.cachedToken = {
      value: result.access_token,
      expiresAtMillis: requestedAt + result.expires_in * 1000,
    };
    return this.cachedToken.value;
  }
}
