import { apiRequest } from "../../../shared/lib/http.js";

export function login(credentials) {
  return apiRequest("/auth/login", {
    method: "POST",
    body: credentials,
    authenticated: false,
  });
}

export function refresh() {
  return apiRequest("/auth/refresh", {
    method: "POST",
    authenticated: false,
  });
}

export function logout() {
  return apiRequest("/auth/logout", {
    method: "POST",
    authenticated: false,
  });
}
