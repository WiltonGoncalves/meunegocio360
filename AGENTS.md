# AGENTS.md — MeuNegocio360

Instruções para assistentes de IA (Copilot, Gemini Code Assist, Continue, Claude Code e similares) e para pessoas que trabalham neste repositório. Leia antes de gerar ou alterar código.

## 1. O que é este projeto

O **MeuNegocio360** é um sistema comercial de gestão de clientes, serviços e suprimentos, construído com uso assistido da IA nas diversas etapas do fluxo de desenvolvimento de software.

**Arquitetura: microsserviços.** Núcleo (`meunegocio360-core`), Serviços (`meunegocio360-servicos`) e Suprimentos (`meunegocio360-suprimentos`), consumidos por **um único frontend** (`meunegocio360-js`). Cada serviço tem código, banco de dados e implantação próprios. Nunca acople um serviço ao banco ou ao código interno de outro.

## 2. Fonte da verdade (leia antes de codar)

| Documento | Conteúdo |
| --- | --- |
| `docs/requisitos-gerais.md` | Regras válidas para tudo: arquitetura, RNFs, Definição de Pronto, fluxo por aula |
| `docs/requisitos-be.md` | Convenções e requisitos do backend |
| `docs/requisitos-fe.md` | Convenções, identidade visual e requisitos do frontend |

Regras de trabalho:

1. Antes de implementar, identifique o requisito (`RF-BE-xxx` / `RF-FE-xxx`) e leia **a seção inteira**, incluindo critérios de aceite, "Dependências introduzidas" e "Pontos de atenção".
2. Se algo estiver ambíguo ou em conflito, **pergunte** em vez de assumir.
3. Cite o ID do requisito em commits, PRs e nomes de testes.

## 3. Mapa do repositório

```
docs/                     requisitos e ADRs (docs/adr/)
meunegocio360-*/          microsserviços por módulo
meunegocio360-js/         frontend em módulo único
compose.yaml              infraestrutura local
.env.example              modelo de variáveis (copie para .env; nunca versione o .env)
```

Pacote raiz do backend: `br.com.meunegocio360.core`, organizado **por funcionalidade** (`<feature>/api|application|domain|infra`).

## 4. Comandos

Infraestrutura (na raiz):

```bash
cp .env.example .env          # preencha as senhas e o JWT_SECRET
docker compose up -d          # sobe o MySQL
docker compose down           # para (mantém dados) | down -v apaga os dados locais
```

Backend (em `meunegocio360-*`; no Windows use `mvnw.cmd`):

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw verify                 # build + testes + qualidade; DEVE passar antes de todo PR
./mvnw spotless:apply         # formata o código (disponível a partir de RF-BE-001)
```

Frontend (em `meunegocio360-js`):

```bash
npm install
npm run dev                   # servidor de desenvolvimento
npm run lint
npm test                      # disponível a partir de RF-FE-001
npm run build
```

> Ferramentas e scripts marcados "a partir de RF-…" são introduzidos pelo respectivo requisito. **Não instale dependências por conta própria**: elas entram no requisito que as exige (RNF-015) e precisam ser justificadas no PR.

## 5. Convenções do backend

- Camadas: `api → application → domain`; `infra` implementa interfaces do domínio. Controllers finos, regra de negócio no domínio/casos de uso.
- Injeção por construtor. Entidades JPA **nunca** são expostas na API; use DTOs (`record`).
- API em `/api/v1`, JSON `camelCase`, datas ISO 8601 UTC, IDs públicos UUID.
- Erros sempre em `application/problem+json` (RFC 9457) com `code` estável, tratados em um único `@RestControllerAdvice`.
- Banco: somente migrations Flyway (`V<n>__descricao.sql`), nunca edite migration já aplicada; `ddl-auto=validate`. Um banco por serviço.
- Segurança *deny-by-default*, sessão `STATELESS`. Senhas só com `PasswordEncoder` (BCrypt).
- Tempo sempre via `Clock` injetável (nada de `Instant.now()` solto) para tornar os testes determinísticos.
- Testes: JUnit 5, Mockito, AssertJ, `MockMvc`, Testcontainers (MySQL real, não banco em memória).

## 6. Convenções do frontend

- Estrutura `src/app`, `src/features/<feature>/{api,components,hooks,pages}`, `src/shared/{ui,lib,styles}`. Uma feature só importa outra pelo `index.js` público.
- Chamadas HTTP **somente** pelo cliente único de `shared/lib/http`; nunca `fetch` solto em componente.
- Cores, fontes e espaçamentos **só** por tokens CSS (`src/shared/styles/tokens.css`). Respeite as regras de contraste da seção 3.2 de `requisitos-fe.md` (ex.: texto preto sobre botão laranja).
- Formulários com React Hook Form + Zod; mensagens em pt-BR.
- Acessibilidade WCAG 2.2 AA: rótulos, foco visível, `role="alert"` em erros, navegação por teclado.
- Testes com Vitest + Testing Library (consultas por papel) + MSW (nunca *mock* manual de `fetch`).
- Fontes auto-hospedadas via `@fontsource`; nada de CDN de terceiros.

## 7. Segurança: proibições absolutas

- **Nunca** coloque segredos, senhas, chaves ou tokens no código, em arquivos versionados, em logs ou em prompts. Use variáveis de ambiente (`.env`).
- **Nunca** guarde token de acesso ou refresh em `localStorage`/`sessionStorage`. O token de acesso fica em memória; o refresh é cookie `HttpOnly` controlado pelo servidor.
- **Nunca** diferencie "usuário não existe" de "senha incorreta" em mensagens, status ou tempo de resposta.
- **Nunca** grave senha ou refresh token em claro, nem os registre em logs (nem e-mail completo).
- **Nunca** use `allowedOrigins("*")`, `permitAll()` amplo, `dangerouslySetInnerHTML` com conteúdo não confiável, nem desabilite verificação TLS.
- Trate toda entrada (inclusive respostas de API) como não confiável; valide no servidor.
- Dependências sugeridas por IA podem não existir ou estar desatualizadas: **confira nome, versão, licença e manutenção** antes de adicionar, e use as versões do `pom.xml`/`package.json`.

## 8. Como trabalhar com a IA neste projeto

1. **Planeje antes de codar:** peça/produza um plano em passos pequenos e verificáveis e revise-o.
2. **Testes junto com o código**, a partir dos cenários Gherkin do requisito.
3. **Incrementos pequenos** e commits frequentes; faça só o que o requisito pede (nada de funcionalidades, refatorações ou arquivos extras não solicitados).
4. **Revise tudo** que a IA gerar: o autor do PR responde pelo código. Use a lista "Pontos de atenção" do requisito.
5. Não invente APIs de bibliotecas: na dúvida, consulte a documentação da versão instalada.
6. No PR, registre o que foi **aceito, ajustado ou rejeitado** da sugestão da IA e por quê.

## 9. Git e commits

- `main` protegida; uma branch curta por requisito: `feat/rf-be-001-login`. Integração por PR somente após aprovação do líder técnico no GitHub.
- Conventional Commits citando o requisito: `feat(auth): autentica usuário por e-mail e senha [RF-BE-001]`.
- PR com pipeline vermelho não é integrado. Nunca versione `.env`, `target/` ou `node_modules/`.

## 10. Definição de Pronto (resumo)

Testes de aceite passando · CI verde · sem segredos/dados reais/código morto no diff · OpenAPI atualizado (se houve mudança de API) · acessibilidade verificada (se houve mudança de interface) · README/ADR atualizados · código gerado por IA revisado linha a linha. Detalhes em `docs/requisitos-gerais.md`, seção 7.
