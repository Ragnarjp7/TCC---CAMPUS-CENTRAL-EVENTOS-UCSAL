import { tokenStorage } from "../storage/tokenStorage";
import { API_URL, REQUEST_TIMEOUT_MS } from "./config";
import { ApiError } from "./errors";

type UnauthorizedHandler = () => void;

let unauthorizedHandler: UnauthorizedHandler | null = null;

export function setUnauthorizedHandler(handler: UnauthorizedHandler | null): void {
  unauthorizedHandler = handler;
}

type QueryParams = Record<string, string | number | boolean | null | undefined>;

interface RequestOptions {
  method?: "GET" | "POST" | "PUT" | "PATCH" | "DELETE";
  params?: QueryParams;
  body?: unknown;
  signal?: AbortSignal;
  /** Para endpoints em que 401 significa credencial inválida (ex.: login), sem encerrar a sessão. */
  skipUnauthorizedHandler?: boolean;
}

function buildUrl(path: string, params: QueryParams | undefined): string {
  const query = Object.entries(params ?? {})
    .filter((entry): entry is [string, string | number | boolean] => entry[1] != null && entry[1] !== "")
    .map(([key, value]) => `${encodeURIComponent(key)}=${encodeURIComponent(String(value))}`)
    .join("&");
  return `${API_URL}${path}${query ? `?${query}` : ""}`;
}

async function handleUnauthorized(tokenUsed: string | null): Promise<void> {
  if (!tokenUsed) return;
  // Só encerra a sessão se o token rejeitado ainda é o atual: um 401 atrasado não pode derrubar um login novo.
  if ((await tokenStorage.get()) !== tokenUsed) return;
  await tokenStorage.clear();
  unauthorizedHandler?.();
}

async function readErrorMessage(response: Response): Promise<string | undefined> {
  try {
    const data: unknown = await response.json();
    if (data && typeof data === "object") {
      // Backend responde { "mensagem": "..." }; "message" fica como fallback
      const msg = (data as Record<string, unknown>).mensagem ?? (data as Record<string, unknown>).message;
      if (typeof msg === "string") return msg;
    }
  } catch {
    // corpo ausente ou não-JSON
  }
  return undefined;
}

export async function apiRequest<T>(path: string, options: RequestOptions = {}): Promise<T> {
  const { method = "GET", params, body, signal, skipUnauthorizedHandler = false } = options;
  const token = await tokenStorage.get();

  const controller = new AbortController();
  let timedOut = false;
  const timer = setTimeout(() => {
    timedOut = true;
    controller.abort();
  }, REQUEST_TIMEOUT_MS);
  signal?.addEventListener("abort", () => controller.abort());

  let response: Response;
  try {
    response = await fetch(buildUrl(path, params), {
      method,
      signal: controller.signal,
      headers: {
        Accept: "application/json",
        ...(body !== undefined && { "Content-Type": "application/json" }),
        ...(token && { Authorization: `Bearer ${token}` }),
      },
      body: body !== undefined ? JSON.stringify(body) : undefined,
    });
  } catch {
    throw new ApiError(timedOut ? "timeout" : signal?.aborted ? "client" : "network");
  } finally {
    clearTimeout(timer);
  }

  if (!response.ok) {
    if (response.status === 401 && !skipUnauthorizedHandler) await handleUnauthorized(token);
    throw ApiError.fromStatus(response.status, await readErrorMessage(response));
  }

  if (response.status === 204) return undefined as T;
  return (await response.json()) as T;
}
