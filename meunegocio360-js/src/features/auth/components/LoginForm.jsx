import { useEffect, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { Alert, Button, Card, Form, Spinner } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { ApiError } from "../../../shared/lib/http.js";
import { useAuth } from "../hooks/useAuth.js";
import { loginSchema } from "../schemas.js";

const initialValues = { email: "", senha: "" };

export function LoginForm({ sessionExpired, logoutWarning }) {
  const { signIn } = useAuth();
  const {
    clearErrors,
    formState: { errors, isSubmitting },
    handleSubmit,
    register,
    reset,
    setError,
    setFocus,
    setValue,
  } = useForm({
    resolver: zodResolver(loginSchema),
    defaultValues: initialValues,
    mode: "onSubmit",
    reValidateMode: "onChange",
  });
  const [alertMessage, setAlertMessage] = useState(
    sessionExpired ? "Sua sessão expirou. Entre novamente." : "",
  );
  const [lockDeadline, setLockDeadline] = useState(null);
  const [remainingSeconds, setRemainingSeconds] = useState(0);

  useEffect(() => {
    if (!lockDeadline) {
      return undefined;
    }

    const updateCountdown = () => {
      const remaining = Math.max(
        Math.ceil((lockDeadline - Date.now()) / 1000),
        0,
      );
      setRemainingSeconds(remaining);
      if (remaining === 0) {
        setLockDeadline(null);
      }
    };

    updateCountdown();
    const intervalId = window.setInterval(updateCountdown, 1000);
    return () => window.clearInterval(intervalId);
  }, [lockDeadline]);

  const submit = async (values) => {
    clearErrors();
    setAlertMessage("");

    try {
      await signIn(values);
      reset(initialValues);
    } catch (error) {
      if (error instanceof ApiError && error.code === "INVALID_CREDENTIALS") {
        setAlertMessage("E-mail ou senha inválidos.");
        setValue("senha", "");
        setFocus("senha");
        return;
      }

      if (
        error instanceof ApiError &&
        error.code === "ACCOUNT_TEMPORARILY_LOCKED"
      ) {
        const { deadline, seconds } = getRetryAfterDeadline(error.retryAfter);
        setLockDeadline(deadline);
        setRemainingSeconds(seconds);
        setAlertMessage("");
        return;
      }

      if (error instanceof ApiError && error.code === "VALIDATION_ERROR") {
        const hasFieldErrors = applyServerFieldErrors(error.errors, setError);
        if (!hasFieldErrors) {
          setAlertMessage(
            "Não foi possível validar os dados enviados. Revise os campos e tente novamente.",
          );
          return;
        }

        const firstInvalidField = error.errors.some(
          (fieldError) => fieldError.field === "email",
        )
          ? "email"
          : "senha";
        setFocus(firstInvalidField);
        return;
      }

      setAlertMessage(
        "Não foi possível entrar agora. Verifique sua conexão e tente novamente.",
      );
    }
  };

  const invalid = (formErrors) => {
    setFocus(formErrors.email ? "email" : "senha");
  };

  const minutesToWait = Math.max(Math.ceil(remainingSeconds / 60), 1);
  const lockMessage =
    remainingSeconds > 0
      ? `Muitas tentativas. Tente novamente em ${minutesToWait} ${
          minutesToWait === 1 ? "minuto" : "minutos"
        }.`
      : "";

  return (
    <Card className="login-card border-0 shadow-sm">
      <Card.Body className="p-4 p-md-5">
        <img
          className="login-logo mb-4"
          src="/brand-logo.svg"
          alt="MeuNegocio360"
          width="288"
          height="72"
        />
        <h1 className="h2 text-center mb-2">Entrar</h1>
        <p className="text-center text-secondary mb-4">
          Acesse sua conta para continuar.
        </p>

        {alertMessage && (
          <Alert
            variant="danger"
            className="mb-4"
            role="alert"
            aria-live="assertive"
          >
            {alertMessage}
          </Alert>
        )}
        {lockMessage && (
          <Alert
            variant="warning"
            className="mb-4"
            role="alert"
            aria-live="assertive"
          >
            {lockMessage}
          </Alert>
        )}
        {logoutWarning && (
          <Alert
            variant="warning"
            className="mb-4"
            role="alert"
            aria-live="assertive"
          >
            A sessão foi encerrada neste dispositivo, mas não foi possível
            confirmar o encerramento no servidor.
          </Alert>
        )}

        <Form noValidate onSubmit={handleSubmit(submit, invalid)}>
          <Form.Group className="mb-3" controlId="login-email">
            <Form.Label>E-mail</Form.Label>
            <Form.Control
              type="email"
              autoComplete="username"
              autoCapitalize="none"
              autoCorrect="off"
              aria-invalid={Boolean(errors.email)}
              aria-describedby={errors.email ? "login-email-error" : undefined}
              isInvalid={Boolean(errors.email)}
              {...register("email")}
            />
            {errors.email && (
              <Form.Control.Feedback id="login-email-error" type="invalid">
                {errors.email.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>

          <Form.Group className="mb-4" controlId="login-senha">
            <Form.Label>Senha</Form.Label>
            <Form.Control
              type="password"
              autoComplete="current-password"
              aria-invalid={Boolean(errors.senha)}
              aria-describedby={errors.senha ? "login-senha-error" : undefined}
              isInvalid={Boolean(errors.senha)}
              {...register("senha")}
            />
            {errors.senha && (
              <Form.Control.Feedback id="login-senha-error" type="invalid">
                {errors.senha.message}
              </Form.Control.Feedback>
            )}
          </Form.Group>

          <Button
            className="w-100"
            variant="primary"
            type="submit"
            disabled={isSubmitting || remainingSeconds > 0}
          >
            {isSubmitting && (
              <Spinner
                className="me-2"
                animation="border"
                size="sm"
                role="status"
                aria-label="Entrando"
              />
            )}
            {isSubmitting ? "Entrando..." : "Entrar"}
          </Button>
        </Form>
      </Card.Body>
    </Card>
  );
}

function getRetryAfterDeadline(value) {
  const seconds = getRetryAfterSeconds(value);
  return { deadline: Date.now() + seconds * 1000, seconds };
}

function getRetryAfterSeconds(value) {
  if (!value) {
    return 60;
  }

  const seconds = Number(value);
  if (Number.isFinite(seconds) && seconds > 0) {
    return Math.ceil(seconds);
  }

  const retryAt = Date.parse(value);
  if (Number.isNaN(retryAt)) {
    return 60;
  }

  return Math.max(Math.ceil((retryAt - Date.now()) / 1000), 1);
}

function applyServerFieldErrors(serverErrors, setError) {
  const fieldNames = new Set(["email", "senha"]);
  let hasFieldErrors = false;

  for (const fieldError of serverErrors) {
    if (
      fieldNames.has(fieldError.field) &&
      typeof fieldError.message === "string"
    ) {
      setError(fieldError.field, {
        type: "server",
        message: fieldError.message,
      });
      hasFieldErrors = true;
    }
  }

  return hasFieldErrors;
}
