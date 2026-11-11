# MeuNegocio360 — Requisitos do Frontend

| | |
| --- | --- |
| **Versão** | 2.0 |
| **Atualizado em** | 11/11/2026 |
| **Projeto** | `meunegocio360-js` |
| **Herda de** | [`requisitos-gerais.md`](./requisitos-gerais.md) (todos os RNF se aplicam; aqui só há o que é específico do frontend) |
| **Contrato de API** | [`requisitos-be.md`](./requisitos-be.md) (OpenAPI do backend é a fonte da verdade) |

## 1. Stack e dependências

| Item | Definição |
| --- | --- |
| Framework / build | React 19 + Vite, em **JavaScript** (decisão D-04: sem TypeScript; contratos documentados por OpenAPI, *schemas* Zod e JSDoc quando agregar clareza) |
| Roteamento | React Router |
| Estado de servidor | TanStack Query |
| Formulários e validação | React Hook Form + Zod |
| Estilos | CSS Modules + design tokens em variáveis CSS (decisão D-05) |
| Fontes | `@fontsource/barlow` e `@fontsource/barlow-condensed` (auto-hospedadas) |
| Testes | Vitest, React Testing Library, `user-event`, `jest-dom`, MSW, `vitest-axe` |
| Qualidade | ESLint (configuração flat já existente) + Prettier |

### 1.1 Dependências: base e introdução ao longo do curso

O `package.json` gerado já contém a **base**: React, ReactDOM e a toolchain Vite/ESLint (scripts `dev`, `build`, `lint` e `preview`). Todo o resto é acrescentado **no requisito que o exige** (RNF-015). Nesta base, o único requisito é `RF-FE-001`, que introduz:

| Dependência | Para quê | Introduzida em |
| --- | --- | --- |
| `react-router` | Roteamento e rotas protegidas | RF-FE-001 |
| `@tanstack/react-query` | Estado de servidor (cache, carregamento, erro) | RF-FE-001 |
| `react-hook-form`, `zod`, `@hookform/resolvers` | Formulários e validação | RF-FE-001 |
| `@fontsource/barlow`, `@fontsource/barlow-condensed` | Fontes auto-hospedadas | RF-FE-001 |
| `vitest`, `@testing-library/*`, `msw`, `vitest-axe` | Testes de componente, API simulada e acessibilidade | RF-FE-001 |
| `prettier` | Formatação (RNF-082) | RF-FE-001 |

Scripts acrescentados por `RF-FE-001`: `format`, `test` e `test:coverage`. O comando `npm run lint && npm test && npm run build` DEVE passar no CI (RNF-004). Requisitos de aulas futuras introduzem suas próprias dependências (por exemplo, biblioteca de tabelas, gráficos ou Playwright), sempre justificadas no PR (RNF-014).

**Variáveis de ambiente:** `meunegocio360-js/.env.example` documenta `VITE_API_BASE_URL`. O arquivo `.env.local` real não é versionado.

## 2. Arquitetura

Organização por funcionalidade (*feature-based*), com páginas finas e regra de dependência clara:

```
src/
├── app/                    # bootstrap: providers, roteador, layout raiz, estilos globais
├── features/
│   └── <feature>/          # ex.: auth, clientes, produtos
│       ├── api/            # chamadas HTTP da feature
│       ├── components/     # componentes exclusivos da feature
│       ├── hooks/          # hooks da feature (useLogin, useAuth ...)
│       ├── pages/          # telas roteáveis (compõem componentes)
│       ├── schemas.js      # schemas Zod
│       └── index.js        # API pública da feature
├── shared/
│   ├── ui/                 # componentes de design system (Button, TextField, Alert ...)
│   ├── lib/                # http client, utilitários
│   └── styles/             # tokens, reset, tipografia
└── test/                   # setup do Vitest, handlers do MSW
```

| ID | Requisito |
| --- | --- |
| RNF-FE-01 | Uma feature NÃO DEVE importar arquivos internos de outra; só pelo `index.js` público. `shared/` NÃO DEVE importar de `features/`. |
| RNF-FE-02 | Componentes são funções com *hooks*; sem componentes de classe. Lógica de dados e efeitos ficam em *hooks* e não em JSX. |
| RNF-FE-03 | Chamadas HTTP NUNCA são feitas direto em componentes: passam pela camada `features/<feature>/api` sobre o cliente HTTP único de `shared/lib`. |
| RNF-FE-04 | Dados de servidor ficam no TanStack Query (cache, *loading*, erro). Estado global próprio de aplicação (ex.: sessão) via Context. Evitar `useEffect` para buscar dados e "prop drilling" além de dois níveis. |
| RNF-FE-05 | Rotas carregadas com *code splitting* (`React.lazy`) por página, com *fallback* acessível de carregamento. |
| RNF-FE-06 | Chaves de lista, `useEffect` com dependências corretas e ausência de avisos do `eslint-plugin-react-hooks` são obrigatórios. |

## 3. Identidade visual (design tokens)

A paleta, a tipografia e o espaçamento DEVEM ser definidos **uma única vez** como variáveis CSS em `src/shared/styles/tokens.css` e consumidos por todos os componentes. Valores de cor e fonte NÃO DEVEM ser escritos diretamente em componentes.

### 3.1 Paleta

| Token | Cor | Uso |
| --- | --- | --- |
| `--color-primary` (Laranja) | `#EC4E26` | Botões primários, barra de navegação, destaque de marca |
| `--color-surface` (Creme) | `#FCF5F0` | Fundo de páginas e cartões |
| `--color-muted` (Cinza) | `#4A3F39` | Linhas de tabelas, bordas de cartões e texto secundário |
| `--color-text` (Preto) | `#1F1612` | Texto principal |
| `--color-footer` (Laranja amarronzado claro) | `#CDBBAE` | Rodapé |

Tokens adicionais obrigatórios: `--color-error`, `--color-success` e `--color-focus` (anel de foco), definidos pela equipe com contraste AA contra o fundo em que forem usados.

### 3.2 Contraste (WCAG 2.2 AA, RNF-060)

Razões de contraste calculadas para os pares da paleta original:

| Texto sobre fundo | Razão | Texto normal (mín. 4,5:1) | Texto grande (mín. 3:1) |
| --- | --- | --- | --- |
| Preto sobre Creme | 16,5:1 | Passa | Passa |
| Cinza sobre Creme | 9,4:1 | Passa | Passa |
| Preto sobre Laranja | 4,8:1 | Passa | Passa |
| Cinza sobre Laranja amarronzado | 5,5:1 | Passa | Passa |
| **Creme sobre Laranja** | **3,4:1** | **Não passa** | Passa |
| **Laranja sobre Creme** | **3,4:1** | **Não passa** | Passa |

Consequências obrigatórias (ajustes em relação ao uso descrito na versão anterior da paleta):

- **Texto de botão primário:** usar **Preto** (`--color-text`) sobre Laranja. Creme sobre Laranja só é permitido em texto grande (≥ 24 px, ou ≥ 18,66 px em negrito).
- **Botões secundários e estados `:hover`** com texto Laranja sobre Creme: só com texto grande/negrito conforme acima, e sempre com borda visível. Para texto pequeno, usar Preto ou Cinza.
- Informação NUNCA DEVE ser transmitida apenas por cor (ex.: erro precisa de ícone ou texto, além de vermelho).

### 3.3 Tipografia

| Uso | Fonte | Pesos |
| --- | --- | --- |
| Títulos | Barlow Condensed | 500, 600 e 700 |
| Texto, tabelas, formulários e rodapé | Barlow | 400, 500 e 600 |

- As fontes DEVEM ser **auto-hospedadas** via `@fontsource` (importando apenas os pesos acima), e não carregadas do Google Fonts: evita requisição a terceiros (privacidade/LGPD) e melhora desempenho. Usar `font-display: swap`.
- Escala tipográfica e espaçamento definidos como tokens (`--font-size-*`, `--space-*`). Corpo de texto com no mínimo 16 px.

## 4. Camada HTTP, sessão e roteamento

| ID | Requisito |
| --- | --- |
| RNF-FE-10 | Um único cliente HTTP (`shared/lib/http`), baseado em `fetch`, lê a URL base de `VITE_API_BASE_URL` (única para todos os serviços, decisão D-06). Chamadas aos endpoints `/auth/*` DEVEM usar `credentials: "include"` para enviar/receber o cookie de refresh. Variáveis `VITE_*` são **públicas**: NUNCA colocar segredos nelas. |
| RNF-FE-11 | O cliente DEVE: injetar `Authorization: Bearer`, enviar `X-Request-Id`, aplicar *timeout* (`AbortController`), interpretar `application/problem+json` em um erro tipado (`ApiError` com `status`, `code`, `detail`, `errors`) e tratar `401` globalmente: tentar **uma única renovação** de sessão (`/auth/refresh`) e repetir a requisição original; se a renovação falhar, encerrar a sessão e redirecionar ao login. |
| RNF-FE-12 | Tokens NÃO DEVEM ser guardados em `localStorage` nem `sessionStorage` (acessíveis a qualquer script, inclusive um XSS). O **token de acesso** fica **em memória** (contexto de autenticação). O **refresh token** é um cookie `HttpOnly` gerenciado pelo navegador e **inacessível ao JavaScript**: o frontend nunca o lê nem o grava. A sessão sobrevive ao recarregamento porque, na inicialização, o app chama `/auth/refresh` para obter um novo token de acesso. |
| RNF-FE-15 | Renovações simultâneas dentro da mesma aba DEVEM ser **agrupadas** (*single-flight*): se várias requisições recebem `401` ao mesmo tempo, apenas uma chamada a `/auth/refresh` é feita e todas aguardam seu resultado. Como o refresh token é rotativo, duas chamadas paralelas com o mesmo cookie derrubariam a sessão (detecção de reuso do backend). |
| RNF-FE-13 | Rotas protegidas DEVEM usar um componente de guarda (`RequireAuth`). Controle de acesso no frontend é apenas UX; a autorização real é do backend. |
| RNF-FE-14 | A decisão de comportamento diante de erros usa o campo `code` do erro, nunca o texto da mensagem. |

## 5. Formulários, acessibilidade e UX

| ID | Requisito |
| --- | --- |
| RNF-FE-20 | Formulários com React Hook Form + schema Zod, mensagens em português. A validação do cliente espelha a do servidor, mas nunca a substitui (RNF-020). |
| RNF-FE-21 | Todo campo DEVE ter `<label>` visível associado, `autocomplete` correto, tipo de `input` adequado e mensagem de erro ligada por `aria-describedby`/`aria-invalid`. |
| RNF-FE-22 | Erros de formulário e de servidor DEVEM ser anunciados a leitores de tela (`role="alert"` ou região `aria-live`) e o foco levado ao primeiro campo inválido ou à mensagem. |
| RNF-FE-23 | Navegação completa por teclado, ordem de foco lógica, foco visível com contraste mínimo de 3:1 e alvos interativos com pelo menos 44×44 px. |
| RNF-FE-24 | HTML semântico (`main`, `nav`, `header`, `footer`, `h1` único por tela, botões `<button>`, links `<a>`). Atributo `lang="pt-BR"` no documento e título de página (`<title>`) por rota. |
| RNF-FE-25 | Botões que disparam requisições DEVEM ficar desabilitados e exibir estado de carregamento, impedindo envio duplicado. |
| RNF-FE-26 | Layout *mobile-first*, funcional a partir de 360 px, sem rolagem horizontal. Respeitar `prefers-reduced-motion`. |

## 6. Segurança, desempenho e testes

| ID | Requisito |
| --- | --- |
| RNF-FE-30 | NÃO DEVE usar `dangerouslySetInnerHTML` com conteúdo não sanitizado, `eval` ou injeção manual de HTML. Dados da API são sempre tratados como não confiáveis. |
| RNF-FE-31 | Dependências auditadas (`npm audit` no CI) e *lockfile* versionado. Dependências sugeridas por IA DEVEM ser conferidas quanto à existência, versão, licença e manutenção antes da instalação. |
| RNF-FE-32 | Orçamento de desempenho (build de produção): carga inicial de JavaScript idealmente ≤ 200 kB comprimidos; LCP ≤ 2,5 s em conexão simulada de 4G para telas públicas. |
| RNF-FE-33 | Consultas por papel e acessibilidade (`getByRole`, `getByLabelText`) são o padrão em testes. `data-testid` só como último recurso. |
| RNF-FE-34 | A API é simulada com **MSW** nos testes, usando respostas fiéis ao contrato do backend (inclusive `problem+json`). Não usar *mocks* manuais do `fetch`. |
| RNF-FE-35 | Toda página nova DEVE ter pelo menos uma verificação automática de acessibilidade com `vitest-axe`, sem violações. |

---

## RF-FE-001 — Fluxo de login do usuário

> **Dependências:** `RF-BE-001` (contrato de `POST /api/v1/auth/login`, `/auth/refresh`, `/auth/logout` e `GET /api/v1/auth/me`). Enquanto o backend não estiver pronto, o frontend DEVE ser desenvolvido contra handlers MSW que seguem o contrato, inclusive o comportamento do cookie de refresh.

### História de usuário

Como **usuário cadastrado**, quero **informar meu e-mail e minha senha em uma tela de login simples e acessível e continuar autenticado ao recarregar a página**, para **usar o sistema sem precisar entrar de novo a cada atualização**, vendo somente as áreas protegidas quando estiver autenticado.

### Escopo desta aula

**Dentro do escopo (obrigatório):** tela `/login`, validação do formulário, integração com a API, contexto de autenticação com token de acesso em memória, **restauração de sessão por refresh token (cookie `HttpOnly`)** na inicialização, renovação automática diante de `401`, rota protegida `/` (página inicial provisória), redirecionamento pós-login, botão de sair, tratamento de sessão expirada e de todos os estados de erro, remoção do conteúdo de exemplo do template Vite (`App.jsx`, contador, logos), aplicação dos tokens de identidade visual e testes.

**Fora do escopo:** cadastro de usuário, "esqueci minha senha", "lembrar de mim" (a duração da sessão é definida pelo backend), controle de acesso por perfil e menus definitivos.

### Dependências introduzidas

`react-router`, TanStack Query, React Hook Form + Zod, `@fontsource/*`, Vitest, Testing Library, MSW, `vitest-axe` e Prettier (ver seção 1.1).

### Telas e rotas

| Rota | Acesso | Descrição |
| --- | --- | --- |
| `/login` | Público (usuário já autenticado é redirecionado para `/`) | Formulário de login centralizado, dentro de um cartão Creme, com o título "Entrar" (Barlow Condensed) e o nome do produto. |
| `/` | Protegido | Página inicial provisória: saudação com o nome do usuário, perfil e botão **Sair**. Será substituída pelo dashboard em requisito futuro. |
| `*` | Público | Página "não encontrada" com link para a página inicial. |

### Ciclo de vida da sessão

1. **Inicialização do app:** o `AuthProvider` entra no estado `carregando` e chama `POST /auth/refresh` (com `credentials: "include"`). Se responder `200`, guarda `accessToken` e `usuario` em memória e passa a `autenticado`; se responder `401`, passa a `anônimo`. Durante `carregando`, o app exibe um indicador de carregamento acessível (`role="status"`) e **não** redireciona nem renderiza telas protegidas ou de login, para evitar "piscar" a tela de login a quem já tem sessão.
2. **Login:** `POST /auth/login` → `autenticado`.
3. **Requisição autenticada com `401`:** o cliente HTTP faz **uma** renovação (single-flight, RNF-FE-15) e repete a requisição. Se a renovação falhar, a sessão é encerrada com a mensagem de sessão expirada.
4. **Renovação preventiva (recomendada):** agendar a renovação cerca de 60 s antes de `expiresIn`, cancelando o agendamento no logout e ao desmontar o provider.
5. **Logout:** `POST /auth/logout` (o resultado é ignorado em caso de falha de rede), descarte do token e do cache do TanStack Query, e redirecionamento a `/login`.

### Campos e validações do formulário

| Campo | Controle | Regras (Zod) | Mensagens |
| --- | --- | --- | --- |
| E-mail | `input type="email"`, `autocomplete="username"` | Obrigatório; *trim*; formato de e-mail; máx. 160 caracteres | "Informe seu e-mail." / "Informe um e-mail válido." |
| Senha | `input type="password"`, `autocomplete="current-password"` | Obrigatório; 8 a 72 caracteres | "Informe sua senha." / "A senha deve ter entre 8 e 72 caracteres." |

Validação no envio e, depois do primeiro envio, a cada alteração. Botão **Entrar** com estado de carregamento. O campo de senha NÃO DEVE bloquear colar/gerenciadores de senha.

### Comportamentos e mensagens

| Situação (resposta da API) | Comportamento na interface |
| --- | --- |
| Login `200` | Guarda `accessToken` e `usuario` **em memória** (contexto), limpa o formulário e redireciona para a rota de origem (`from`) ou `/`. |
| Login `400 VALIDATION_ERROR` | Mostra os erros do servidor junto aos campos indicados em `errors[].field`. |
| Login `401 INVALID_CREDENTIALS` | Alerta no topo do formulário: "E-mail ou senha inválidos." Mantém o e-mail digitado, limpa a senha e devolve o foco ao campo de senha. |
| Login `429 ACCOUNT_TEMPORARILY_LOCKED` | Alerta: "Muitas tentativas. Tente novamente em X minutos." (calculado a partir de `Retry-After`). Botão desabilitado durante a contagem. |
| Falha de rede / *timeout* / `5xx` no login | Alerta: "Não foi possível entrar agora. Verifique sua conexão e tente novamente." com opção de tentar de novo. |
| Falha de rede / `5xx` na restauração de sessão (inicialização) | NÃO é tratada como sessão inválida: exibir tela de erro com "Tentar novamente", sem apagar nada. Somente `401` leva ao estado `anônimo`. |
| `401` em chamada autenticada e renovação bem-sucedida | Transparente para o usuário: a requisição é repetida com o novo token. |
| `401` em chamada autenticada e renovação falha (`INVALID_REFRESH_TOKEN`) | Encerra a sessão, redireciona para `/login` com a mensagem "Sua sessão expirou. Entre novamente." e preserva a rota de origem. |
| Clique em **Sair** | Conforme o ciclo de vida (item 5). |

Mensagens de erro NÃO DEVEM revelar se o e-mail existe (RN-03 do backend).

### Critérios de aceite

```gherkin
Cenário: Login com sucesso
  Dado que estou na tela /login e não estou autenticado
  Quando informo e-mail e senha válidos e envio o formulário
  Então vejo o botão em estado de carregamento durante a requisição
  E sou redirecionado para "/"
  E vejo meu nome na página inicial
  E nenhum token foi gravado em localStorage nem sessionStorage
  E o JavaScript da página não consegue ler o cookie de refresh (document.cookie não o contém)

Cenário: Sessão restaurada ao recarregar
  Dado que estou autenticado em "/"
  Quando recarrego a página
  Então vejo um indicador de carregamento, sem passar pela tela de login
  E volto a ver minha página inicial já autenticado

Cenário: Visitante sem sessão ao abrir o app
  Dado que não tenho cookie de refresh válido
  Quando abro "/"
  Então a restauração de sessão resulta em 401 e sou redirecionado para "/login"

Cenário: Renovação transparente do token de acesso
  Dado que estou autenticado e o token de acesso expirou
  Quando uma chamada autenticada recebe 401
  Então o app renova a sessão uma única vez e repete a chamada com sucesso
  E o usuário não percebe a renovação

Cenário: Várias chamadas expiradas ao mesmo tempo
  Dado que várias chamadas autenticadas recebem 401 simultaneamente
  Então é feita apenas uma chamada a /auth/refresh
  E todas as chamadas são repetidas com o novo token

Cenário: Sessão expirada de vez
  Dado que estou autenticado
  Quando a renovação responde 401 INVALID_REFRESH_TOKEN
  Então volto a "/login" com a mensagem de sessão expirada
  E, após entrar, sou levado à rota em que estava

Cenário: Falha de rede ao restaurar sessão
  Quando abro o app e a API está indisponível
  Então vejo uma tela de erro com "Tentar novamente"
  E não sou enviado ao login nem perco a sessão

Cenário: Validação no cliente
  Quando envio o formulário vazio ou com e-mail inválido
  Então nenhuma requisição é feita
  E as mensagens de erro aparecem associadas aos campos
  E o foco vai para o primeiro campo inválido

Cenário: Credenciais inválidas
  Quando a API responde 401 INVALID_CREDENTIALS
  Então vejo "E-mail ou senha inválidos." em um alerta anunciado por leitores de tela
  E o e-mail permanece preenchido e a senha é limpa

Cenário: Conta bloqueada
  Quando a API responde 429 com Retry-After
  Então vejo o tempo de espera estimado e não consigo reenviar antes do fim da contagem

Cenário: Rota protegida sem sessão
  Dado que não estou autenticado
  Quando acesso "/" diretamente
  Então sou redirecionado para "/login"
  E após entrar sou levado de volta a "/"

Cenário: Sair
  Dado que estou autenticado
  Quando clico em "Sair"
  Então a API de logout é chamada e sou levado a "/login"
  E, ao recarregar a página, continuo sem sessão

Cenário: Acessibilidade
  Quando uso apenas o teclado (Tab, Shift+Tab, Enter)
  Então consigo preencher e enviar o formulário com foco sempre visível
  E a tela não tem violações detectadas pelo axe
  E o contraste de todos os textos atende ao WCAG 2.2 AA

Cenário: Responsividade
  Quando a largura da tela é 360 px
  Então o formulário é totalmente utilizável sem rolagem horizontal
```

### Requisitos não funcionais com destaque neste requisito

RNF-020, RNF-060, RNF-061, RNF-062, RNF-FE-10 a RNF-FE-15, RNF-FE-20 a RNF-FE-26, RNF-FE-30, RNF-FE-34 e RNF-FE-35.

### Testes exigidos

- **Componente/integração (Vitest + RTL + MSW):** formulário (validações, estados de carregamento e de erro por tipo de resposta), `LoginPage` completa, `AuthProvider` (restauração de sessão com sucesso, `401` e falha de rede), `RequireAuth`, redirecionamento com `from`, logout.
- **Cliente HTTP (unitário):** injeção de cabeçalhos, *timeout*, conversão de `problem+json` em `ApiError`, **renovação única e repetição da requisição**, **agrupamento (single-flight) de renovações simultâneas** e encerramento de sessão quando a renovação falha. Os handlers MSW devem simular a rotação do refresh token.
- **Unitários:** *schemas* Zod e cálculo do tempo de espera a partir de `Retry-After`.
- **Acessibilidade:** `vitest-axe` na página de login e na página inicial.
- **Opcional recomendado:** um teste de ponta a ponta com Playwright (login real contra o backend em ambiente local, incluindo recarregar a página).

### Pontos de atenção ao revisar código gerado por IA

- Token de acesso ou de refresh salvo em `localStorage`/`sessionStorage` ou em cookie acessível por JavaScript (a IA sugere isso com muita frequência). Aqui o refresh é exclusivamente cookie `HttpOnly`, controlado pelo servidor.
- Esquecer `credentials: "include"` nas chamadas de autenticação (a sessão "não persiste" e a IA tenta "consertar" guardando token no navegador).
- **Laço infinito de renovação:** a chamada a `/auth/refresh` receber `401` e disparar outra renovação. A rota de refresh nunca deve entrar no fluxo de "renovar e repetir".
- Renovações paralelas sem *single-flight*, o que derruba a sessão por reuso do token rotativo; renovação disparada duas vezes pelo modo estrito do React em desenvolvimento (`StrictMode`) sem controle adequado.
- Tratar falha de rede na restauração de sessão como "sessão inválida" e enviar o usuário ao login.
- Renderizar telas protegidas ou o formulário de login antes do fim da restauração de sessão ("flash" de conteúdo).
- Mensagens distintas para "usuário não existe" e "senha incorreta", ou exibição do `detail` bruto do servidor.
- Validação feita só com `required` do HTML, ou apenas no cliente confiando nela como segurança.
- Chamada `fetch` solta dentro do componente, sem passar pelo cliente HTTP, e `useEffect` usado para buscar dados.
- Cores e fontes escritas direto no CSS dos componentes, ou cor de texto que não atende ao contraste (ver seção 3.2), `outline: none` sem substituto.
- Rótulos substituídos por *placeholder*, falta de `aria-live`/`role="alert"`, foco perdido após erro.
- Redirecionamento aberto: `from` aceitando URLs externas. Só caminhos internos começando com `/` (e não `//`) são permitidos.
- Pacotes inexistentes, desatualizados ou com API antiga (ex.: versões anteriores do React Router) sugeridos pela IA.
- Testes que usam `data-testid` para tudo, *mocks* manuais de `fetch` e `waitFor` arbitrários sem asserção significativa.

### Extensões opcionais (desafios)

1. **Sincronização entre abas:** ao sair ou expirar em uma aba, as demais também encerram a sessão (`BroadcastChannel`), e a renovação é coordenada entre abas para não gerar reuso de token.
2. Botão "mostrar/ocultar senha" acessível (com `aria-pressed` e rótulo dinâmico).
3. Alternância de tema claro/escuro derivada dos tokens, mantendo AA.
4. Teste de ponta a ponta com Playwright e relatório de acessibilidade automatizado no CI.
