import { Link } from "react-router";
import { Container } from "react-bootstrap";
import { usePageTitle } from "../../../shared/lib/usePageTitle.js";

export function NotFoundPage() {
  usePageTitle("Página não encontrada | MeuNegocio360");

  return (
    <main className="min-vh-100 d-flex align-items-center">
      <Container className="app-content">
        <h1 className="h2">Página não encontrada</h1>
        <p className="mb-3">O endereço informado não existe.</p>
        <Link to="/">Ir para a página inicial</Link>
      </Container>
    </main>
  );
}
