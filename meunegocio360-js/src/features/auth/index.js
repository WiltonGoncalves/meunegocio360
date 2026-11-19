export { AuthProvider } from "./AuthProvider.jsx";
export { RequireAuth } from "./components/RequireAuth.jsx";
export { useAuth } from "./hooks/useAuth.js";

export const loadHomePage = () => import("./pages/HomePage.jsx");
export const loadLoginPage = () => import("./pages/LoginPage.jsx");
export const loadNotFoundPage = () => import("./pages/NotFoundPage.jsx");
export const loadRestoreSessionErrorPage = () =>
  import("./pages/RestoreSessionErrorPage.jsx");
