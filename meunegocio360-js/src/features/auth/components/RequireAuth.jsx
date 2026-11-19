import { Navigate, Outlet, useLocation } from "react-router";
import { useAuth } from "../hooks/useAuth.js";

export function RequireAuth() {
  const { status, sessionExpired } = useAuth();
  const location = useLocation();

  if (status !== "authenticated") {
    return (
      <Navigate
        to="/login"
        replace
        state={{
          from: {
            pathname: location.pathname,
            search: location.search,
            hash: location.hash,
          },
          sessionExpired,
        }}
      />
    );
  }

  return <Outlet />;
}
