import { useState } from "react";
import { useNavigate } from "react-router";
import { Button, Card, Container } from "react-bootstrap";
import { usePageTitle } from "../../../shared/lib/usePageTitle.js";
import { useAuth } from "../hooks/useAuth.js";

export function HomePage() {
  const { session, signOut } = useAuth();
  const navigate = useNavigate();
  const [isSigningOut, setIsSigningOut] = useState(false);
  usePageTitle("Início | MeuNegocio360");

  const handleSignOut = async () => {
    setIsSigningOut(true);
    const result = await signOut();
    navigate("/login", {
      replace: true,
      state: result.warning ? { logoutWarning: true } : undefined,
    });
  };

  return (
    <main className="min-vh-100 d-flex flex-column align-items-center py-5 px-3">
      <Container className="app-content">
        <header className="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
          <img
            src="/brand-logo.svg"
            alt="MeuNegocio360"
            width="240"
            height="60"
          />
          <Button
            type="button"
            variant="outline-dark"
            onClick={handleSignOut}
            disabled={isSigningOut}
          >
            {isSigningOut ? "Saindo..." : "Sair"}
          </Button>
        </header>

        <Card>
          <Card.Body className="p-4 p-md-5">
            <h1 className="h2">Olá, {session.usuario.nome}</h1>
            <p className="mb-0">
              Perfil: <strong>{session.usuario.perfil}</strong>
            </p>
          </Card.Body>
        </Card>
      </Container>
    </main>
  );
}
