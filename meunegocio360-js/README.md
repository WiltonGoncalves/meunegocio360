# MeuNegocio360 — Frontend

Frontend React do MeuNegocio360, implementado com Vite e Bootstrap 5.

## Executar localmente

1. Inicie o backend Núcleo e o banco de dados seguindo o [README principal](../README.md).
2. Copie `.env.example` para `.env.local` se precisar alterar o caminho da API.
3. Execute `npm install` e `npm run dev`.
4. Abra a URL informada pelo Vite. O proxy encaminha `/api` para `http://localhost:8080`.

## Comandos

- `npm run dev`: inicia o servidor de desenvolvimento.
- `npm run build`: gera os arquivos estáticos em `dist/`.
- `npm run preview`: serve o build localmente.
- `npm run lint`: verifica o código com ESLint.
- `npm run format`: formata os arquivos do frontend com Prettier.

A API usa o caminho relativo `/api/v1`; não configure uma URL absoluta nem coloque segredos em variáveis `VITE_*`.

## Requisito implementado

- [RF-FE-001 — Fluxo de login do usuário](../docs/requisitos-fe.md#rf-fe-001--fluxo-de-login-do-usuário): login, restauração e renovação da sessão, rotas protegidas e logout.
