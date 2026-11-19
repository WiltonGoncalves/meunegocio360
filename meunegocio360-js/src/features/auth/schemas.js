import { z } from "zod";

export const loginSchema = z.object({
  email: z
    .string()
    .trim()
    .min(1, "Informe seu e-mail.")
    .email("Informe um e-mail válido.")
    .max(160, "O e-mail deve ter no máximo 160 caracteres."),
  senha: z
    .string()
    .min(8, "A senha deve ter entre 8 e 72 caracteres.")
    .max(72, "A senha deve ter entre 8 e 72 caracteres."),
});
