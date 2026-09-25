# Progresso — Notura Mobile KMP

## Status
Passo 1 (Auth): **dados, rede, estado e textos concluídos e testados** (56 testes JVM no `:shared`).
**UI Compose escrita, mas NÃO compilada nem testada**: o `:composeApp` não configura sem o Google Maven
(plugin Android `com.android.application` e artefatos `androidx.*`, dos quais até o Compose desktop depende).
A etapa só estará concluída depois de `./gradlew :composeApp:desktopTest` passar num ambiente com acesso.

## Decisões
- Auth: `supabase-kt` 3.8.0 (`auth-kt`, somente o plugin Auth) para login/cadastro/Google; Bearer nas chamadas às rotas `/api`.
- Contrato de API atualizado no backend: Henry-AF/Notura-App#92.
- Gravação: `meetingDate` = data de hoje, sem tela de metadados; sem upload de arquivo existente.
- Estados sem design no Figma: sinalizados, não inventados.
- Módulos: `:shared` (domain/data/network/presentation, Kotlin puro, JVM + iOS) e `:composeApp` (UI).
  A separação permite compilar e testar tudo que não é UI sem o Google Maven.

## Como compilar e testar
```
./gradlew :shared:jvmTest                            # ambiente completo
./gradlew -Pnotura.includeApp=false :shared:jvmTest  # sem acesso ao Google Maven
```
Alvos iOS só compilam em macOS.

## Telas
| # | Tela | Dados/rede | Estado | UI | Testes |
|---|------|-----------|--------|----|--------|
| 1 | Auth (intro, login, cadastro, Google) | ✅ | ✅ | ⚠️ escrita, não compilada | 56 JVM passando; 9 de UI escritos, não executados |
| 2 | Gravar + upload de reunião | — | — | — | — |
| 3 | Home / lista de reuniões + detalhe/ata | — | — | — | — |
| 4 | Chat RAG da reunião | — | — | — | — |
| 5 | Kanban de tasks | — | — | — | sem Figma |
| 6 | Billing / configurações | — | — | — | sem Figma |

### Passo 1 — o que existe
- `:shared` (compilado e testado):
  - `network/NoturaApiClient`: Bearer, refresh único em 401, `ApiError` por status documentado.
  - `data/user`: `GET /api/user/me` → `CurrentUser`.
  - `data/auth/SupabaseAuthRepository`: login, cadastro (`full_name`), Google por ID token, refresh, logout
    (limpa a sessão local mesmo offline). `AuthFailure` carrega a mensagem do Supabase.
  - `presentation/auth`: `SignInStateHolder`, `SignUpStateHolder`, `AuthCopy` (todos os textos, com origem).
- `:composeApp` (escrito, **não compilado**):
  - `ui/theme`: `NoturaTheme` com cores, tipografia (Plus Jakarta Sans + Poppins), formas e espaçamentos do Figma.
  - `ui/components`: campo de texto e senha, botão principal, botão Google, divisor "ou", logo, voltar,
    checkbox de termos, linha de erro.
  - `ui/auth`: `IntroScreen`, `SignInScreen`, `SignUpScreen` (inclui estado de confirmação de e-mail),
    `AuthFlow` (liga telas aos state holders), `GoogleSignInLauncher` (interface por plataforma).
  - `App.kt` / `AppGraph.kt` e entradas Android (`MainActivity`) e iOS (`MainViewController`).
  - Testes de UI (`commonTest`, executados no alvo `desktop`): navegação, loading → erro, validação,
    Google com/sem launcher, termos, loading → confirmação, raiz reagindo à sessão.

### Passo 1 — o que falta
- Compilar `:composeApp` e rodar `:composeApp:desktopTest` com acesso ao Google Maven; corrigir o que aparecer.
- Google Sign-In nativo (Credential Manager / GoogleSignIn iOS) implementando `GoogleSignInLauncher`.
- Projeto Xcode do `iosApp` (hoje só há os arquivos Swift) e `Info.plist` com `NoturaApiBaseUrl`,
  `SupabaseUrl`, `SupabaseAnonKey`.
- Android: `notura.apiBaseUrl`, `notura.supabaseUrl`, `notura.supabaseAnonKey` em `local.properties`.
- Botão voltar do sistema (Android) no fluxo de Auth.

### Lacunas de texto (não inventadas — precisam de decisão de produto)
- Mensagens de validação por campo: nome vazio, e-mail vazio/inválido, senha vazia, senha < 8.
  O web depende da validação nativa do navegador. Hoje o campo só fica com a borda de erro, sem texto.
- Erros do Supabase aparecem em inglês, como no web (ex.: "Invalid login credentials").
  Falta copy em português para: credenciais inválidas, e-mail não confirmado, e-mail já cadastrado,
  senha fraca, limite de tentativas.
- Tela de "e-mail de confirmação enviado": sem título nem texto no Figma/web. Hoje mostra logo, o e-mail e "Sign In".
- Corpo da intro: o Figma tem Lorem ipsum; usei o texto do web sob o mesmo título
  ("Notura organiza tudo para você com IA...").
- Rótulo de acessibilidade do botão voltar ("Voltar") não vem de nenhuma fonte.

### Lacunas de design (Figma)
- Intro: só a captura geral foi lida (limite de chamadas do Figma MCP no plano Starter). Layout, tamanhos
  e ilustração precisam ser conferidos com `get_design_context` no nó 2270:148.
- Ativos não exportados (`figma.com` bloqueado no proxy): ícone do logo, ícone do Google, olho da senha,
  seta de voltar, ilustração da intro. Hoje: só o nome "Notura", botão Google só com texto, ícones Material
  no olho e na seta.
- Sem estados desenhados: erro de campo, erro de login, botão em loading, checkbox marcado, confirmação de e-mail.
  Cor e tamanho do erro vêm do web (`destructive` #EF4444, 14px); checkbox marcado usa a cor primária.
- Sombra do botão principal: o Figma tem 5 camadas; implementada uma sombra única equivalente à principal.
- Cor das linhas do divisor "ou" não foi lida (asset SVG); usada a cor da borda dos campos.
- "Esqueceu a senha?", "Termos de Uso" e "Privacidade" sem destino (sem tela no Figma), renderizados como texto.

## Endpoints consumidos
- `GET /api/user/me`
- Supabase Auth: `POST /auth/v1/token` (password, id_token, refresh_token), `POST /auth/v1/signup`, `POST /auth/v1/logout`

## Endpoints ainda não consumidos
Todos os demais da seção 4 do contrato.

## Bloqueios em aberto
- Rede do ambiente cloud: `dl.google.com` / `maven.google.com` bloqueados (403 no proxy). Necessários para AGP,
  Compose Multiplatform e androidx. Sem isso não há como compilar nem testar a UI aqui.
