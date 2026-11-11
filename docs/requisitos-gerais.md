# MeuNegocio360 — Requisitos Gerais

| | |
| --- | --- |
| **Versão** | 2.0 |
| **Atualizado em** | 11/11/2026 |
| **Aplica-se a** | `meunegocio360-core` (backend) e `meunegocio360-js` (frontend) |
| **Documentos relacionados** | [`requisitos-be.md`](./requisitos-be.md) · [`requisitos-fe.md`](./requisitos-fe.md) |

## 1. Como usar este documento

Este é o documento-base do projeto. Ele define **o que vale para todo requisito**: stack, arquitetura, convenções, requisitos não funcionais (RNF), critérios de pronto e o fluxo de trabalho com IA. Os documentos de backend e frontend herdam tudo o que está aqui e acrescentam apenas o que é específico de cada camada.

Convenções de leitura:

- As palavras **DEVE**, **NÃO DEVE**, **DEVERIA** e **PODE** seguem o sentido da RFC 2119: *DEVE* é obrigatório, *DEVERIA* é fortemente recomendado (desvios exigem justificativa no Pull Request - PR) e *PODE* é opcional.
- Cada requisito tem um identificador estável e rastreável: `RNF-xxx` (não funcionais, válidos em todo o projeto), `RF-BE-xxx` (funcional de backend) e `RF-FE-xxx` (funcional de frontend). Commits, PRs, testes e documentação **DEVEM** citar o ID do requisito atendido.
- Os arquivos `pom.xml` e `package.json` são a fonte da verdade para versões de bibliotecas. Este documento só fixa versões quando isso é uma decisão de arquitetura.

## 2. Visão do produto

**MeuNegocio360** é um sistema comercial de gestão de clientes, serviços e suprimentos para pequenas e médias empresas, organizado em três módulos de negócio:

| Módulo | Objetivo | Funcionalidades previstas |
| --- | --- | --- |
| **Núcleo** | Base empresarial | Gestão de usuários, clientes, fornecedores, produtos, serviços, vendas, contas (a pagar e a receber), fluxo de caixa e dashboard. |
| **Serviços** | Operação de serviços | Gestão de técnicos, equipamentos, solicitações, ordens de serviço (OS), execução, evidências, aceite e faturamento da OS. |
| **Suprimentos** | Estoque e compras | Movimentações, estoque mínimo, inventário, compras, reposição, curva ABC, giro e alertas. |

## 3. Stack e decisões de arquitetura

| Camada | Tecnologia | Observações |
| --- | --- | --- |
| Arquitetura | **Microsserviços** (Núcleo, Serviços, Suprimentos), API REST | Ver seção 3.2. |
| Backend (cada serviço) | Java 17+, Spring Boot 4.x (Spring MVC), Spring Security, Spring Data JPA | Maven Wrapper (`./mvnw`) é a forma oficial de build. |
| Banco de dados | MySQL 8.x | Um banco por microsserviço |
| Frontend | React 19 + Vite, **JavaScript** | Gerenciador de pacotes: npm, com `package-lock.json` versionado. |
| Contrato de API | OpenAPI 3 (springdoc) | Fonte única do contrato entre front e back. |
| Execução local | Docker Compose | MySQL e demais dependências sobem com um único comando. |
| CI | GitHub Actions | Build, testes e lint a cada PR. |

### 3.1 Decisões vigentes (registro leve de decisões)

| # | Decisão | Motivo / revisão |
| --- | --- | --- |
| D-01 | Arquitetura de **microsserviços**: Núcleo (`meunegocio360-core`), Serviços (`meunegocio360-servicos`) e Suprimentos (`meunegocio360-suprimentos`). Cada serviço tem código, build, banco de dados e implantação independentes. | Cada módulo de negócio evolui e escala de forma autônoma. Dentro de cada serviço, o código é organizado por funcionalidade (*package by feature*), o que também facilita a extração de novas funcionalidades em serviços. |
| D-02 | API REST, **stateless**, versionada em `/api/v1`. | Facilita escala horizontal e múltiplos clientes. |
| D-03 | Autenticação por **JWT de acesso de curta duração + refresh token rotativo em cookie `HttpOnly`** (detalhes em `RF-BE-001`). O Núcleo é o **emissor** dos tokens. | Hoje assinado com HS256 (só o Núcleo valida). Antes de o primeiro serviço adicional validar tokens, DEVE migrar para chaves assimétricas (RS256) com publicação de JWKS, para que os demais serviços validem sem conhecer o segredo (RNF-094). |
| D-04 | Frontend em **JavaScript** (sem TypeScript). Contratos e tipos de dados são documentados via OpenAPI, *schemas* Zod e JSDoc quando agregar clareza. | Decisão do projeto, alinhada ao projeto gerado. |
| D-05 | Estilos com **CSS Modules + design tokens** (variáveis CSS), sem framework de UI obrigatório. | Mantém a paleta da marca centralizada em um só lugar. |
| D-06 | Um **único frontend** (SPA) consome todos os serviços por uma URL base única. Um *gateway*/proxy reverso será introduzido, por ADR, quando o segundo serviço existir. | Evita que o frontend conheça a topologia dos serviços. |

### 3.2 Arquitetura de microsserviços

| Serviço | Pasta | Responsabilidade |
| --- | --- | --- |
| Núcleo | `meunegocio360-core` | Identidade e autenticação, usuários, clientes, fornecedores, produtos, serviços (catálogo), vendas, contas, fluxo de caixa e dashboard. |
| Serviços | `meunegocio360-servicos` | Técnicos, equipamentos, solicitações, OS, execução, evidências, aceite e faturamento da OS. |
| Suprimentos | `meunegocio360-suprimentos` | Movimentações, estoque mínimo, inventário, compras, reposição, curva ABC, giro e alertas. |

Regras que valem ao longo de todo o projeto:

| ID | Requisito |
| --- | --- |
| RNF-015 | Bibliotecas e ferramentas são acrescentadas ao `pom.xml` e ao `package.json` **no requisito que as exige**, e não antecipadamente. O projeto parte da base mínima gerada (Spring Initializr e Vite). Cada requisito lista, na seção "Dependências introduzidas", o que traz consigo. |
| RNF-016 | Requisitos não funcionais que dependem de uma ferramenta ainda não introduzida (por exemplo, cobertura com JaCoCo ou testes de acessibilidade com axe) passam a ser exigíveis **a partir do requisito que introduz a ferramenta**. Os demais valem desde o início. |
| RNF-090 | **Um banco de dados por serviço** (*database per service*). Nenhum serviço lê ou escreve nas tabelas de outro, nem existem chaves estrangeiras entre bancos de serviços diferentes. Referências a dados de outro serviço são feitas por identificador (UUID) e consultadas por API. |
| RNF-091 | A comunicação entre serviços é por **contrato versionado**: REST síncrono (OpenAPI) ou eventos assíncronos. A escolha de *broker* e do formato de eventos será registrada em ADR quando surgir a primeira necessidade. |
| RNF-092 | Chamadas a outro serviço DEVEM ter *timeout*, tratamento de falha (e, quando fizer sentido, *retry* com *backoff* e *circuit breaker*) e propagar `X-Request-Id`. Um serviço NÃO DEVE ficar indisponível só porque outro está. |
| RNF-093 | Cada serviço DEVE ser construído, testado e implantado de forma independente (pipeline, `compose` e variáveis próprios) e NÃO DEVE compartilhar código de negócio com outro. Bibliotecas compartilhadas se limitam a contratos (DTOs/OpenAPI) e utilitários técnicos sem regra de negócio. |
| RNF-094 | O Núcleo é o único serviço que autentica usuários e emite tokens. Os demais serviços apenas **validam** o token de acesso (assinatura via JWKS, emissor, expiração e `perfil`) e nunca recebem nem armazenam senhas. |
| RNF-095 | Cada serviço DEVE publicar o próprio OpenAPI, *health check* e logs com o mesmo formato e identificador de correlação (RNF-030, RNF-050, RNF-051). |

## 4. Estrutura do repositório e fluxo Git

```
meunegocio360/
├── docs/                         # requisitos, ADRs e demais documentos
├── meunegocio360-core/           # microsserviço Núcleo
├── meunegocio360-servicos/       # microsserviço Serviços
├── meunegocio360-suprimentos/    # microsserviço Suprimentos
├── meunegocio360-js/             # frontend único (React + Vite)
│   └── .env.example              # variáveis públicas do frontend (VITE_*)
├── compose.yaml                  # infraestrutura local
├── .env.example                  # variáveis de ambiente do backend e do compose
├── .gitignore                    # ignora .env e arquivos locais
├── AGENTS.md                     # instruções de contexto para assistentes de IA
└── README.md                     # documentação oficial do repositório
```

**Observação**: O repositório é um **monorepo** com uma pasta por serviço. Cada serviço é autocontido (RNF-093) e pode ser separado em repositório próprio no futuro, permitindo deploys independentes sem qualquer alteração de código..

| ID | Requisito |
| --- | --- |
| RNF-001 | O trabalho DEVE seguir **trunk-based development simplificado**: `main` protegida, uma branch curta por requisito (`feat/rf-be-001-login`) e *pull request* para integrar. |
| RNF-002 | Commits DEVEM seguir **Conventional Commits** (`feat:`, `fix:`, `test:`, `docs:`, `refactor:`, `chore:`) e citar o ID do requisito no escopo ou no corpo (ex.: `feat(auth): autentica usuário por e-mail e senha [RF-BE-001]`). |
| RNF-003 | PRs DEVEM ser pequenos e focados em um requisito, com descrição preenchida (o quê, por quê, como testar, trechos gerados por IA que exigiram correção). Merge APENAS após aprovação do líder técnico. |
| RNF-004 | O pipeline de CI DEVE executar `./mvnw verify` (backend) e `npm run lint && npm test && npm run build` (frontend). PR com pipeline vermelho NÃO DEVE ser integrado. |
| RNF-005 | O repositório NÃO DEVE conter segredos, credenciais, dumps de dados reais, `target/` nem `node_modules/`. O `.gitignore` DEVE cobrir esses itens e todo arquivo `.env*`, exceto `.env.example`. |
| RNF-006 | O `README.md` raiz DEVE permitir que um novo desenvolvedor suba o ambiente completo (banco, backend e frontend) em menos de 15 minutos. |

## 5. Requisitos não funcionais (válidos para todo o projeto)

Os requisitos desta seção são **aplicados progressivamente**: valem desde o início, exceto os que dependem de ferramentas que ainda não foram introduzidas (RNF-016).

### 5.1 Arquitetura e qualidade de código

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-010 | O código DEVE seguir arquitetura em camadas com **regra de dependência única**: `api → application → domain`, e `infra` implementa interfaces do domínio. Nenhuma camada interna conhece uma externa. | Revisão de PR e teste de arquitetura (ArchUnit) no backend. |
| RNF-011 | Cada funcionalidade DEVE ser agrupada em seu próprio pacote/pasta (*package by feature*), e não por tipo técnico global. | Revisão de PR. |
| RNF-012 | Código DEVE seguir princípios SOLID e *Clean Code*: nomes em português de domínio ou inglês técnico, de forma consistente; métodos curtos; sem duplicação relevante; sem código comentado. | Linters e revisão. |
| RNF-013 | Todo código gerado por IA DEVE ser **lido, entendido e revisado** por quem o submete. O autor do PR responde pelo código, independentemente de quem o escreveu. | Checklist do PR (seção 7). |
| RNF-014 | Dependências novas DEVEM ser justificadas no PR (necessidade, licença, manutenção ativa). A IA NÃO DEVE ser aceita como única justificativa. | Revisão de PR. |

### 5.2 Segurança e privacidade

Referência: **OWASP ASVS nível 1** e **OWASP Top 10**.

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-020 | Toda entrada externa DEVE ser validada no servidor (tipo, tamanho, formato). Validação no frontend é apenas experiência de uso. | Testes de validação. |
| RNF-021 | Endpoints DEVEM ser **negados por padrão**; só são públicos os explicitamente liberados. | Teste de integração de segurança. |
| RNF-022 | Segredos (chaves JWT, senhas de banco, tokens) DEVEM vir de variáveis de ambiente ou *secret manager*. NUNCA do código, de arquivos versionados ou de prompts enviados à IA. | Varredura de segredos no CI; revisão. |
| RNF-023 | Senhas DEVEM ser armazenadas apenas como *hash* adaptativo com *salt* (BCrypt via `DelegatingPasswordEncoder`). NÃO DEVEM aparecer em logs, respostas, exceções ou testes versionados com valores reais. | Revisão; testes. |
| RNF-024 | Mensagens de erro NÃO DEVEM vazar detalhes internos (stack trace, SQL, nomes de classe) nem permitir enumeração de usuários. | Testes de contrato de erro. |
| RNF-025 | Dados pessoais seguem a **LGPD**: coletar o mínimo necessário, não registrar dados pessoais em logs e permitir, no futuro, exclusão/anonimização. | Revisão de modelo de dados e logs. |
| RNF-026 | Dependências DEVEM ser auditadas continuamente (Dependabot e/ou `npm audit` / OWASP Dependency-Check no CI). Vulnerabilidades críticas bloqueiam o merge. | CI. |
| RNF-027 | CORS DEVE usar *allowlist* de origens configurável por ambiente; jamais `*` em produção. | Teste de integração. |

### 5.3 Contrato de API e integração

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-030 | Toda API DEVE ser documentada em OpenAPI (springdoc), com exemplos de sucesso e de erro, e disponível em `/swagger-ui` fora de produção. | Revisão; abertura da UI. |
| RNF-031 | Erros DEVEM seguir **RFC 9457 (Problem Details)**, com `Content-Type: application/problem+json` (formato detalhado em `requisitos-be.md`). | Testes de contrato. |
| RNF-032 | Mudanças incompatíveis no contrato DEVEM gerar nova versão de caminho (`/api/v2`). Mudanças compatíveis (campos opcionais novos) não. | Revisão. |
| RNF-033 | Datas e horas trafegam em **ISO 8601 UTC**; valores monetários como inteiros em centavos ou decimais em *string*, nunca `float`. | Testes de serialização. |

### 5.4 Testes e qualidade contínua

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-040 | Todo requisito DEVE ter testes automatizados que cubram seus critérios de aceite. Teste que "só passa" sem validar a regra de negócio NÃO conta. | Revisão de PR. |
| RNF-041 | Seguir a **pirâmide de testes**: muitos unitários, alguns de integração (banco real via Testcontainers; API real simulada via MSW no front), poucos de ponta a ponta. | Estrutura de testes. |
| RNF-042 | Cobertura de linhas do **código novo** DEVERIA ser ≥ 80% (JaCoCo no backend, Vitest no frontend). A cobertura é indicador, não objetivo: a qualidade das asserções prevalece. | Relatórios no CI. |
| RNF-043 | Testes DEVEM ser determinísticos, independentes entre si e executáveis offline (sem depender de serviços externos reais). | CI. |

### 5.5 Observabilidade, operação e desempenho

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-050 | Logs DEVEM ser estruturados (JSON) fora do ambiente de desenvolvimento, com nível adequado e **identificador de correlação** (`X-Request-Id`) propagado entre front e back. | Inspeção de logs. |
| RNF-051 | O backend DEVE expor *health checks* (Actuator `/actuator/health`) sem detalhes sensíveis. | Teste de integração. |
| RNF-052 | A configuração DEVE seguir *12-factor*: mesmo artefato para todos os ambientes, diferenças apenas por variáveis de ambiente e perfis (`dev`, `test`, `prod`). | Revisão. |
| RNF-053 | Endpoints de leitura DEVERIAM responder em p95 ≤ 500 ms e o login em p95 ≤ 800 ms (excluindo o custo intencional do hash de senha) em ambiente local com dados de teste. | Medição manual ou teste de carga leve. |
| RNF-054 | Listagens DEVEM ser **paginadas** no servidor; nunca retornar coleções ilimitadas. | Testes. |

### 5.6 Usabilidade e acessibilidade

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-060 | A interface DEVE atender **WCAG 2.2 nível AA**: contraste, foco visível, navegação por teclado, rótulos e mensagens associados a campos, alvos de toque adequados. | Verificação automatizada (axe) e roteiro manual. |
| RNF-061 | A interface DEVE ser responsiva (a partir de 360 px de largura) e em **português do Brasil**. | Revisão visual. |
| RNF-062 | Toda tela com dados remotos DEVE tratar explicitamente os estados de **carregamento, vazio, erro e sucesso**. | Testes de componente. |

### 5.7 Documentação

| ID | Requisito | Como verificar |
| --- | --- | --- |
| RNF-070 | Decisões arquiteturais relevantes DEVEM ser registradas como ADR curto em `docs/adr/NNNN-titulo.md` (contexto, decisão, consequências). | Revisão de PR. |
| RNF-071 | Cada requisito entregue DEVE atualizar o README/OpenAPI quando alterar como rodar ou como consumir o sistema. | Revisão de PR. |

## 6. Ambiente de desenvolvimento

| ID | Requisito |
| --- | --- |
| RNF-080 | IDE padrão: VS Code, com as extensões de IA e de linguagem do curso. |
| RNF-081 | O ambiente local DEVE subir com `docker compose up -d` (MySQL do Núcleo; cada novo serviço acrescenta seu próprio banco) e variáveis definidas a partir de `.env.example` (`cp .env.example .env`). O arquivo `.env` NUNCA é versionado. |
| RNF-082 | Formatação automática obrigatória: Spotless (google-java-format) no backend e Prettier + ESLint no frontend (ferramentas introduzidas por `RF-BE-001` e `RF-FE-001`, conforme RNF-015). |
| RNF-083 | O arquivo `AGENTS.md` na raiz DEVE descrever, para assistentes de IA, a arquitetura, os comandos de build/teste, as convenções e as proibições deste documento (por exemplo: "nunca colocar segredos no código", "nunca usar `localStorage` para tokens"). |

## 7. Definição de Pronto (*Definition of Done - DoD*)

Um requisito só é considerado entregue quando **todos** os itens abaixo estiverem marcados no PR:

- [ ] Todos os critérios de aceite do requisito estão cobertos por testes automatizados que passam.
- [ ] Pipeline de CI verde (build, testes, lint, auditoria de dependências).
- [ ] Nenhum segredo, dado pessoal real ou código morto no *diff*.
- [ ] Contrato OpenAPI atualizado e coerente com a implementação (quando há mudança de API).
- [ ] Critérios de acessibilidade verificados (quando há mudança de interface).
- [ ] README, OpenAPI e, se aplicável, ADR atualizados.
- [ ] O autor revisou linha a linha o código gerado por IA e registrou no PR o que foi **aceito, ajustado ou rejeitado**.
- [ ] Revisão de pelo menos uma pessoa (ou revisão assistida por IA mais revisão humana final, em atividades individuais).

## 8. Fluxo de trabalho por aula com IA como copiloto

Cada requisito segue o mesmo ciclo, que aplica na prática o *spec-driven development* do Módulo 2:

1. **Ler a especificação.** Entender o requisito, as regras de negócio, o contrato e os critérios de aceite. Dúvidas viram perguntas ao instrutor ou ao chat de IA **antes** de gerar código.
2. **Planejar com a IA.** Pedir um plano de implementação em passos pequenos e verificáveis (modelo de dados, contrato, camadas, testes). Revisar e ajustar o plano antes de executar.
3. **Escrever os testes de aceite primeiro** (ou em paralelo), a partir dos cenários Gherkin do requisito.
4. **Implementar em incrementos pequenos**, com commits frequentes. Contexto mínimo suficiente no prompt: o requisito, este documento, o trecho de código relevante e os arquivos de regras (`AGENTS.md`).
5. **Revisar criticamente** o que a IA gerou, usando a lista de "pontos de atenção" de cada requisito. Corrigir, simplificar e remover o que não foi pedido.
6. **Rodar a verificação completa** (build, testes, lint, execução manual do fluxo).
7. **Abrir o PR** com a descrição padrão e o registro de uso de IA (prompts úteis, erros encontrados, decisões humanas).

### 8.1 Template para escrever um novo requisito

Todo novo requisito DEVE usar esta estrutura, para que a IA e as pessoas recebam a mesma informação:

```markdown
## RF-<BE|FE>-NNN — <Título curto>

**História de usuário:** Como <perfil>, quero <ação>, para <benefício>.

**Contexto e dependências:** <o que já existe; requisitos dos quais depende>

**Regras de negócio:** RN-01 …, RN-02 …

**Contrato / Interface:** <endpoints e payloads (BE) ou telas e fluxos (FE)>

**Modelo de dados / Estado:** <tabelas e migrations (BE) ou estado e rotas (FE)>

**Critérios de aceite (Gherkin):**
Cenário: …
  Dado …
  Quando …
  Então …

**Requisitos não funcionais aplicáveis:** RNF-0xx, RNF-0yy (apenas os que merecem destaque)

**Testes exigidos:** <lista objetiva>

**Pontos de atenção ao revisar código gerado por IA:** <armadilhas conhecidas>

**Fora de escopo / extensões opcionais:** <o que NÃO fazer agora>
```

## 9. Requisitos específicos incluídos na base

| ID | Camada | Título | Documento |
| --- | --- | --- | --- |
| RF-BE-001 | Backend (Núcleo) | Autenticação de usuário (login) e sessão com refresh token | [`requisitos-be.md`](./requisitos-be.md#rf-be-001--autenticação-de-usuário-login) |
| RF-FE-001 | Frontend | Fluxo de login do usuário, com persistência de sessão | [`requisitos-fe.md`](./requisitos-fe.md#rf-fe-001--fluxo-de-login-do-usuário) |

Novos requisitos DEVEM ser acrescentados a esta tabela e aos documentos de sua camada.
