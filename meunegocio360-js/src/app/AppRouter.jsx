import { lazy, Suspense } from "react";
import { BrowserRouter, Route, Routes } from "react-router";
import { Spinner } from "react-bootstrap";
import {
  loadHomePage,
  loadLoginPage,
  loadNotFoundPage,
  loadRestoreSessionErrorPage,
  RequireAuth,
  useAuth,
} from "../features/auth/index.js";

const LoginPage = lazy(() =>
  loadLoginPage().then((module) => ({
    default: module.LoginPage,
  })),
);
const HomePage = lazy(() =>
  loadHomePage().then((module) => ({
    default: module.HomePage,
  })),
);
const NotFoundPage = lazy(() =>
  loadNotFoundPage().then((module) => ({
    default: module.NotFoundPage,
  })),
);
const RestoreSessionErrorPage = lazy(() =>
  loadRestoreSessionErrorPage().then((module) => ({
    default: module.RestoreSessionErrorPage,
  })),
);

export function AppRouter() {
  const { status } = useAuth();

  return (
    <BrowserRouter>
      <Suspense fallback={<LoadingIndicator />}>
        {status === "loading" ? (
          <LoadingIndicator />
        ) : status === "restoreError" ? (
          <RestoreSessionErrorPage />
        ) : (
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route element={<RequireAuth />}>
              <Route path="/" element={<HomePage />} />
            </Route>
            <Route path="*" element={<NotFoundPage />} />
          </Routes>
        )}
      </Suspense>
    </BrowserRouter>
  );
}

function LoadingIndicator() {
  return (
    <main className="min-vh-100 d-flex align-items-center justify-content-center">
      <div role="status" className="d-flex align-items-center gap-3">
        <Spinner animation="border" aria-hidden="true" />
        <span>Restaurando sua sessão...</span>
      </div>
    </main>
  );
}
