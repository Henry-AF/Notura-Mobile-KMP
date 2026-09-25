# Passo 0 — Plano de construção (aguardando aprovação)

Fontes analisadas: `Notura-App@62db909` (docs/api-codex-mobile.md, docs/meeting-rag-chat-frontend.md,
src/lib/api/auth.ts, src/lib/api/rate-limit-policies.ts, supabase/migrations/001–038, rotas em src/app/api)
e Figma `41ZjfF3q615rQZP2O9unhN` (18 frames, página "Page 1").

## 1. Bloqueios e divergências (precisam de decisão antes do Passo 1)

### B1. Login não está no contrato de API
Não existe endpoint de login no backend. O web faz `supabase.auth.signInWithPassword`,
`signUp` e `signInWithOAuth` (Google) direto no Supabase Auth; a API só valida o
`Authorization: Bearer <access_token>` (`requireAuth` em auth.ts). Para o mobile isso
significa falar com o Supabase Auth (GoTrue) diretamente — via `supabase-kt` ou REST
(`/auth/v1/token?grant_type=password`, `/auth/v1/signup`, refresh token).
**Decisão necessária:** autorizar o uso do Supabase Auth direto (e qual lib), e como
o Google Sign-In nativo deve funcionar (ID token nativo → `signInWithIdToken`, ou OAuth via browser).

### B2. Contrato desatualizado em relação ao código
| Item | Contrato (2026-04-22) | Código atual |
|---|---|---|
| Auth | "cookies, não Bearer" | Bearer aceito em `requireAuth` |
| Rate limit upload | 10/60s | 20/60s |
| Rate limit process | 20/60s | 10/60s |
| `GET /api/meetings` | sem paginação | `?limit&cursor&groupId` → `nextCursor`, `hasMore` |
| `process` | `clientName` no exemplo | `meetingDate` obrigatório (422), título default `Reunião <data>`, aceita grupo |
| Chat RAG | ausente do contrato | documentado só em meeting-rag-chat-frontend.md; `GET /api/meetings/{id}/chats` (lista) existe |
| Grupos / Modelos de Ata / `/api/billing/*` / retry & resend limits | ausentes | existem no código |
**Decisão necessária:** atualizo `docs/api-codex-mobile.md` no Notura-App para refletir o código
(PR separado) antes de consumir esses endpoints, ou vocês preferem fazer?

### B3. Ambiente de build
Neste container `dl.google.com` (Google Maven) está bloqueado pela política de rede e
não há Android SDK nem Xcode. Compose Multiplatform (inclusive desktop) depende de
`androidx.*` do Google Maven, então **nenhum teste Compose/ViewModel roda aqui**, e o
build Android/iOS também não. Só testes Kotlin puros (serialização, Ktor MockEngine).
Para cumprir "nunca concluir tela com teste pulado", preciso de: `dl.google.com` e
`maven.google.com` liberados na rede do ambiente + Android SDK (setup script), ou
rodar os testes de UI localmente/CI (GitHub Actions macOS) na sua máquina.

### B4. Repositório GitHub
A integração não tem permissão para criar repositórios (403). O esqueleto está pronto
localmente com 1 commit; crie `Notura-Mobile-KMP` (vazio) no GitHub e eu faço o push.

### Divergências Figma × API
- **Gravação → process:** Figma vai de "Parar" direto para "processing"; a API exige
  `meetingDate` (e opcionalmente cliente/grupo/WhatsApp). Não há tela de metadados — proposta:
  enviar `meetingDate = hoje` automaticamente; confirmar.
- **Upload de arquivo:** Figma só tem gravação ao vivo; não há tela de "enviar arquivo existente".
- **Chat IA:** Figma sugere conversa contínua; a API cria **um chat por pergunta** (POST 202 + polling),
  com limite 2/60s e cota diária (403 `ai_chat_daily_quota_exceeded`). Não há frame de
  pergunta/resposta, fontes, fallback, cota esgotada nem "aguarde X s".
- **Home "Ver tudo":** não há frame de lista completa de reuniões.
- **Menu "Seus Documentos Gerados":** nenhum endpoint correspondente encontrado.
- **Tabs "Chats IA" com cards sugeridos** ("O que foi dito…", "Quero fazer uma ata…"): conteúdo estático, não vem da API.
- **Kanban de tasks e Billing/Configurações: não existem no Figma.**
- **Estados não desenhados:** erro de login, loading de listas, lista vazia (home sem reuniões),
  falha de processamento (`failed` + retry), erros 403 de plano/limite no upload.
  Serão sinalizados, não inventados.

### Tokens de design no Figma
Variáveis definidas são poucas: `notura-main-color #6656E6`, `title-notura #5A4DC5`,
`title #3B3B3B`, `subtitle #474747`, `main-text #525252`, `gradient #F5F4FC`, e o
estilo `body/large/semibold` (Urbanist 16/600, lh 1.4, ls 0.2). O resto (cinzas de
borda, raio de cards/botões, espaçamentos, demais tamanhos de fonte) está solto nos
frames — vou extrair via `get_design_context` e centralizar em `ui/theme`
(`Color.kt`, `Type.kt`, `Spacing.kt`, `Shape.kt`, `Theme.kt`), listando no PROGRESS
quais tokens vieram de variável e quais foram inferidos de valores fixos.

## 2. Telas na ordem de construção

### 1. Auth — frames `intro` 2270:148, `sign in` 2270:160, `sign in - google` 2270:208, `sign up` 2270:286
- Endpoints: Supabase Auth (ver B1); `GET /api/user/me` (validar sessão/carregar perfil); `POST /api/auth/logout`.
- Figma: cores main/title/subtitle, Urbanist, input, botão primário, botão Google, link.
- Estados faltando no Figma: erro de credencial, loading do botão, e-mail não confirmado.
- Subtarefas (~8): tema base; componentes `PrimaryButton`/`TextField`/`GoogleButton`; token store seguro
  (Keychain/EncryptedSharedPreferences via expect/actual); cliente Ktor com Bearer + refresh em 401;
  `UserMe` DTO + testes; AuthViewModel; telas intro/login/cadastro; testes.

### 2. Gravar + upload — frames `recording` 2270:620/713/972, `processing` 2270:806
- Endpoints: `POST /api/meetings/upload` (20/60s) → `PUT uploadUrl` (R2, sem Bearer) →
  `POST /api/meetings/process` (10/60s) → polling `GET /api/meetings/{id}/status` → `POST /api/meetings/{id}/retry` se `failed`.
- Erros específicos: 403 limite do plano, 413 >500MB, 415 tipo, 422 validação, 409, 429 + Retry-After, 503.
- Subtarefas (~9): gravador de áudio expect/actual (AVAudioRecorder / MediaRecorder) com pausar/parar;
  timer + waveform; permissão de microfone; upload com progresso; DTOs + testes; UploadRepository;
  polling com backoff; RecordingViewModel; telas gravando/pausado/processando.

### 3. Home + lista + detalhe/ata — frames `home` 2270:337/514 (drawer), `processing` 2270:833/877/904 (detalhe: Resumo, Transcrição, Chats IA), `more options` 2270:1066, `Seus grupos` 2270:464
- Endpoints: `GET /api/dashboard/overview`, `GET /api/meetings?limit&cursor` (Ver tudo), `GET /api/meetings/{id}`,
  `PATCH`/`DELETE /api/meetings/{id}`, `GET /api/meeting-groups` + `PATCH /api/meetings/{id}/group` (Mover para um grupo).
- `summary_json` precisa ser modelado a partir de `lib` do backend (estrutura da ata) antes de codar.
- Subtarefas (~9): DTOs (overview, meeting list, meeting detail, summary_json) + testes; repos;
  Home VM; lista paginada; detalhe com tabs; bottom sheet de opções; drawer; testes.
- Nota: "Modelos de Ata" 2270:420 (gate Pro) fica junto de Billing (6), pois depende de plano.

### 4. Chat RAG — frames `chat ai` 2270:942, tab Chats IA 2270:877/904
- Endpoints: `GET /api/meetings/{id}/chats` (histórico), `POST /api/meetings/{id}/chats` (202),
  `GET /api/meetings/{id}/chats/{chatId}` (polling), `DELETE /api/meeting-chats/{chatId}`.
- Rate limit 2/60s aplicado **no cliente** (botão enviar desabilitado + contagem regressiva) e 429/Retry-After;
  cota diária 403; 409 `meeting_not_ready`; 422 `no_transcript`; 400 `question_too_long`; fallbacks por `fallbackReason`.
- Subtarefas (~7): DTOs + testes; limitador client-side testável (relógio injetado); repo; VM com polling; UI; testes.

### 5. Kanban de tasks — **sem frame no Figma**
- Endpoints: `GET/POST /api/tasks`, `PATCH/DELETE /api/tasks/{id}`.
- Bloqueado até existir design (ou autorização explícita para usar só os componentes já construídos).

### 6. Billing / configurações — **sem frame no Figma** (exceto botão "Upgrade" e card Pro de Modelos de Ata)
- Endpoints: `GET/PATCH /api/user/me`, `DELETE /api/user/account`, checkout (`/api/billing/checkout` vs
  `/api/abacatepay/*` vs `/api/stripe/*` — precisa definir qual é o atual), `GET /api/meeting-templates`.
- Observação: cobrança dentro de app iOS/Android tem regras das lojas (link externo para checkout web) — decisão de produto.

## 3. Estrutura do `composeApp`

Módulo único `composeApp` (sem `/shared` separado por enquanto: um único cliente consome
esta camada; separar depois é barato porque `data/domain/network` não dependem de Compose).

```
com.notura.mobile/
  App.kt                      raiz Compose + navegação
  ui/
    theme/                    Color.kt, Type.kt, Spacing.kt, Shape.kt, Theme.kt
    components/               PrimaryButton, NoturaTextField, MeetingCard, BottomNav, TabBar, ...
    auth/  recording/  home/  meeting/  chat/  tasks/  settings/   (Screen + ViewModel + UiState por feature)
  domain/                     modelos (Meeting, MeetingStatus, Task, TaskPriority, ChatAnswer...), sem Ktor/Compose
  data/                       *Dto (@Serializable), mappers, repositórios
  network/                    NoturaHttpClient, AuthTokenProvider, ApiError (Unauthorized, Forbidden,
                              RateLimited(retryAfter), PlanLimit, Validation...), RateLimiter client-side
```
Enums do schema: `MeetingStatus` pending|processing|completed|failed; `TaskStatus` todo|in_progress|completed;
prioridade `alta|média|baixa` (API aceita `media` e devolve `media`); plano free|pro|team;
chat processing|completed|failed.
