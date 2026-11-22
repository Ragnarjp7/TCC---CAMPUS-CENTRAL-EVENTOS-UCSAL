export type ApiErrorKind = "network" | "timeout" | "unauthorized" | "notFound" | "server" | "client";

const DEFAULT_MESSAGES: Record<ApiErrorKind, string> = {
  network: "Sem conexão com o servidor. Verifique sua internet e tente novamente.",
  timeout: "O servidor demorou para responder. Tente novamente.",
  unauthorized: "Sua sessão expirou. Entre novamente.",
  notFound: "Conteúdo não encontrado.",
  server: "Ocorreu um erro no servidor. Tente novamente em instantes.",
  client: "Não foi possível concluir a solicitação.",
};

export class ApiError extends Error {
  readonly status: number;
  readonly kind: ApiErrorKind;

  constructor(kind: ApiErrorKind, status = 0, message?: string) {
    super(message ?? DEFAULT_MESSAGES[kind]);
    this.name = "ApiError";
    this.kind = kind;
    this.status = status;
  }

  static fromStatus(status: number, message?: string): ApiError {
    if (status === 401) return new ApiError("unauthorized", status, message);
    if (status === 404) return new ApiError("notFound", status, message);
    if (status >= 500) return new ApiError("server", status, message);
    return new ApiError("client", status, message);
  }
}

export function getErrorMessage(error: unknown, fallback = DEFAULT_MESSAGES.client): string {
  if (error instanceof Error && error.message) return error.message;
  return fallback;
}
