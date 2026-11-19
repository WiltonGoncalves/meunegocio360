import { getAccessToken } from "./accessToken.js";

const configuredApiBaseUrl = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

if (
  !configuredApiBaseUrl.startsWith("/") ||
  configuredApiBaseUrl.startsWith("//")
) {
  throw new Error(
    "VITE_API_BASE_URL deve ser um caminho relativo, como /api/v1.",
  );
}

const apiBaseUrl = configuredApiBaseUrl.replace(/\/+$/, "");
const requestTimeoutMs = 10000;
const authEndpointsWithoutRefresh = new Set([
  "/auth/login",
  "/auth/refresh",
  "/auth/logout",
]);
let refreshHandler = null;
let refreshFailureHandler = null;
let refreshPromise = null;

export class ApiError extends Error {
  constructor({ status, code, detail, errors = [], retryAfter = null }) {
    super(detail);
    this.name = "ApiError";
    this.status = status;
    this.code = code;
    this.detail = detail;
    this.errors = errors;
    this.retryAfter = retryAfter;
  }
}

export function registerSessionRefresh(onRefresh, onFailure) {
  refreshHandler = onRefresh;
  refreshFailureHandler = onFailure;

  return () => {
    if (refreshHandler === onRefresh) {
      refreshHandler = null;
      refreshFailureHandler = null;
    }
  };
}

export function refreshSessionOnce() {
  if (!refreshHandler) {
    return Promise.reject(
      new ApiError({
        status: 0,
        code: "SESSION_REFRESH_UNAVAILABLE",
        detail: "A renovação da sessão não está disponível.",
      }),
    );
  }

  if (!refreshPromise) {
    refreshPromise = Promise.resolve()
      .then(() => refreshHandler())
      .finally(() => {
        refreshPromise = null;
      });
  }

  return refreshPromise;
}

export async function apiRequest(
  path,
  {
    method = "GET",
    body,
    headers: requestHeaders = {},
    authenticated = true,
    retryUnauthorized = true,
  } = {},
) {
  const headers = new Headers(requestHeaders);
  headers.set("Accept", "application/json, application/problem+json");
  headers.set("X-Request-Id", createRequestId());

  if (body !== undefined) {
    headers.set("Content-Type", "application/json");
  }

  const token = authenticated ? getAccessToken() : null;
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }

  const controller = new AbortController();
  const timeoutId = window.setTimeout(
    () => controller.abort(),
    requestTimeoutMs,
  );

  let response;
  try {
    response = await fetch(`${apiBaseUrl}${path}`, {
      method,
      headers,
      body: body === undefined ? undefined : JSON.stringify(body),
      credentials: "same-origin",
      signal: controller.signal,
    });
  } catch (error) {
    if (error instanceof DOMException && error.name === "AbortError") {
      throw new ApiError({
        status: 0,
        code: "TIMEOUT",
        detail: "A solicitação excedeu o tempo limite.",
      });
    }

    throw new ApiError({
      status: 0,
      code: "NETWORK_ERROR",
      detail: "Não foi possível conectar à API.",
    });
  } finally {
    window.clearTimeout(timeoutId);
  }

  if (
    response.status === 401 &&
    authenticated &&
    retryUnauthorized &&
    !authEndpointsWithoutRefresh.has(path)
  ) {
    try {
      await refreshSessionOnce();
    } catch (error) {
      refreshFailureHandler?.(error);
      throw error;
    }

    return apiRequest(path, {
      method,
      body,
      headers: requestHeaders,
      authenticated,
      retryUnauthorized: false,
    });
  }

  if (!response.ok) {
    throw await createApiError(response);
  }

  if (response.status === 204) {
    return null;
  }

  try {
    return await response.json();
  } catch {
    throw new ApiError({
      status: response.status,
      code: "INVALID_RESPONSE",
      detail: "A API retornou uma resposta inválida.",
    });
  }
}

async function createApiError(response) {
  const responseText = await response.text();
  const problem = parseJson(responseText);

  return new ApiError({
    status: response.status,
    code: typeof problem?.code === "string" ? problem.code : "UNKNOWN_ERROR",
    detail:
      typeof problem?.detail === "string"
        ? problem.detail
        : "A solicitação não pôde ser concluída.",
    errors: Array.isArray(problem?.errors) ? problem.errors : [],
    retryAfter: response.headers.get("Retry-After"),
  });
}

function parseJson(value) {
  try {
    return JSON.parse(value);
  } catch (error) {
    if (error instanceof SyntaxError) {
      return null;
    }

    throw error;
  }
}

function createRequestId() {
  if (typeof crypto.randomUUID === "function") {
    return crypto.randomUUID();
  }

  const values = crypto.getRandomValues(new Uint8Array(16));
  return Array.from(values, (value) =>
    value.toString(16).padStart(2, "0"),
  ).join("");
}
