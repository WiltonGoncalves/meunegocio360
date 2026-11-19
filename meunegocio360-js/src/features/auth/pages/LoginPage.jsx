import { Navigate, useLocation } from "react-router";
import { Container } from "react-bootstrap";
import { usePageTitle } from "../../../shared/lib/usePageTitle.js";
import { LoginForm } from "../components/LoginForm.jsx";
import { useAuth } from "../hooks/useAuth.js";

export function LoginPage() {
  const { status } = useAuth();
  const location = useLocation();
  const returnTo = getSafeReturnTo(location.state?.from);
  usePageTitle("Entrar | MeuNegocio360");

  if (status === "authenticated") {
    return <Navigate to={returnTo} replace />;
  }

  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center py-4">
      <Container className="d-flex justify-content-center">
        <LoginForm
          sessionExpired={Boolean(location.state?.sessionExpired)}
          logoutWarning={Boolean(location.state?.logoutWarning)}
        />
      </Container>
    </main>
  );
}

function getSafeReturnTo(from) {
  const path =
    typeof from === "string"
      ? from
      : from && typeof from.pathname === "string"
        ? `${from.pathname}${from.search ?? ""}${from.hash ?? ""}`
        : "/";

  if (!path.startsWith("/") || path.startsWith("//") || path.includes("\\")) {
    return "/";
  }

  return path;
}
