import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
  ApiError,
  refreshSessionOnce,
  registerSessionRefresh,
} from "../../shared/lib/http.js";
import { setAccessToken } from "../../shared/lib/accessToken.js";
import * as authApi from "./api/authApi.js";
import { AuthContext } from "./AuthContext.js";

export function AuthProvider({ children }) {
  const queryClient = useQueryClient();
  const sessionGeneration = useRef(0);
  const [authState, setAuthState] = useState({
    status: "loading",
    session: null,
    restoreError: null,
    sessionExpired: false,
  });

  const setSession = useCallback((session) => {
    setAccessToken(session.accessToken);
    setAuthState({
      status: "authenticated",
      session,
      restoreError: null,
      sessionExpired: false,
    });
  }, []);

  const clearSession = useCallback(
    ({ sessionExpired = false } = {}) => {
      sessionGeneration.current += 1;
      setAccessToken(null);
      queryClient.clear();
      setAuthState({
        status: "anonymous",
        session: null,
        restoreError: null,
        sessionExpired,
      });
    },
    [queryClient],
  );

  const refreshCurrentSession = useCallback(async () => {
    const generation = sessionGeneration.current;
    const session = await authApi.refresh();
    if (generation !== sessionGeneration.current) {
      throw new ApiError({
        status: 401,
        code: "SESSION_CHANGED",
        detail: "A sessão foi encerrada durante a renovação.",
      });
    }

    setSession(session);
    return session;
  }, [setSession]);

  const handleRefreshFailure = useCallback(
    (error) => {
      if (error instanceof ApiError && error.code === "SESSION_CHANGED") {
        return;
      }

      clearSession({ sessionExpired: true });
    },
    [clearSession],
  );

  useEffect(
    () => registerSessionRefresh(refreshCurrentSession, handleRefreshFailure),
    [handleRefreshFailure, refreshCurrentSession],
  );

  const restoreSession = useCallback(async () => {
    setAuthState((current) => ({
      ...current,
      status: "loading",
      restoreError: null,
    }));

    try {
      await refreshSessionOnce();
    } catch (error) {
      if (error instanceof ApiError && error.status === 401) {
        clearSession();
        return;
      }

      setAuthState({
        status: "restoreError",
        session: null,
        restoreError: error,
        sessionExpired: false,
      });
    }
  }, [clearSession]);

  useEffect(() => {
    const timeoutId = window.setTimeout(() => {
      void restoreSession();
    }, 0);

    return () => window.clearTimeout(timeoutId);
  }, [restoreSession]);

  useEffect(() => {
    if (authState.status !== "authenticated" || !authState.session) {
      return undefined;
    }

    const renewInSeconds = Math.max(authState.session.expiresIn - 60, 1);
    const timeoutId = window.setTimeout(() => {
      refreshSessionOnce().catch((error) => {
        if (!(error instanceof ApiError && error.code === "SESSION_CHANGED")) {
          clearSession({ sessionExpired: true });
        }
      });
    }, renewInSeconds * 1000);

    return () => window.clearTimeout(timeoutId);
  }, [authState.session, authState.status, clearSession]);

  const signIn = useCallback(
    async (credentials) => {
      const generation = ++sessionGeneration.current;
      const session = await authApi.login(credentials);
      if (generation !== sessionGeneration.current) {
        throw new ApiError({
          status: 401,
          code: "SESSION_CHANGED",
          detail: "A sessão foi encerrada durante o login.",
        });
      }

      setSession(session);
      return session;
    },
    [setSession],
  );

  const signOut = useCallback(async () => {
    sessionGeneration.current += 1;
    let warning = false;

    try {
      await authApi.logout();
    } catch (error) {
      warning = !(
        error instanceof ApiError &&
        ["NETWORK_ERROR", "TIMEOUT"].includes(error.code)
      );
    } finally {
      clearSession();
    }

    return { warning };
  }, [clearSession]);

  const value = useMemo(
    () => ({
      ...authState,
      signIn,
      signOut,
      restoreSession,
    }),
    [authState, restoreSession, signIn, signOut],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
