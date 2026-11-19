import { Alert, Button, Container } from "react-bootstrap";
import { usePageTitle } from "../../../shared/lib/usePageTitle.js";
import { useAuth } from "../hooks/useAuth.js";

export function RestoreSessionErrorPage() {
  const { restoreSession } = useAuth();
  usePageTitle("Restaurar sessão | MeuNegocio360");

  return (
    <main className="min-vh-100 d-flex align-items-center">
      <Container className="login-card">
        <h1 className="h2">Não foi possível restaurar sua sessão</h1>
        <Alert variant="danger" role="alert" aria-live="assertive">
          A API está indisponível. Verifique sua conexão e tente novamente.
        </Alert>
        <Button type="button" variant="primary" onClick={restoreSession}>
          Tentar novamente
        </Button>
      </Container>
    </main>
  );
}
