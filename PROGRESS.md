# Progresso — Notura Mobile KMP

## Status
Passo 1 (Auth) em andamento: **dados, rede e estado concluídos e testados**; **UI bloqueada** pelo ambiente de build (ver Bloqueios).

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
| 1 | Auth (intro, login, cadastro, Google) | ✅ | ✅ | ⛔ bloqueada | 49 passando (JVM) |
| 2 | Gravar + upload de reunião | — | — | — | — |
| 3 | Home / lista de reuniões + detalhe/ata | — | — | — | — |
| 4 | Chat RAG da reunião | — | — | — | — |
| 5 | Kanban de tasks | — | — | — | sem Figma |
| 6 | Billing / configurações | — | — | — | sem Figma |

### Passo 1 — o que existe
- `network/NoturaApiClient`: Bearer, refresh único em 401, `ApiError` por status documentado
  (403 com `code`/`quotaLimit`, 429 com `Retry-After`, 400/413/415/422, 5xx, rede, corpo fora do contrato).
- `data/user`: `GET /api/user/me` → `CurrentUser` (enums validados contra o contrato).
- `data/auth/SupabaseAuthRepository`: login e-mail/senha, cadastro com `full_name`, Google por ID token,
  refresh, logout (limpa a sessão local mesmo offline).
- `presentation/auth`: `SignInStateHolder`, `SignUpStateHolder` (validação, loading, erros, confirmação por e-mail).

### Passo 1 — o que falta
- UI Compose: tema (`ui/theme`), componentes (input, botão primário, botão Google), telas intro/login/cadastro,
  testes de UI. Requer Google Maven.
- Google Sign-In nativo (Credential Manager no Android, GoogleSignIn no iOS) para obter o ID token.
- Configuração por ambiente: URL da API, URL e anon key do Supabase (BuildConfig / xcconfig).

### Divergências e lacunas do Figma (Auth)
- "Esqueceu a senha?" existe no login, mas não há tela de recuperação no Figma.
- Não há estados desenhados para: erro de login, campo inválido, loading do botão, e-mail de confirmação enviado.
- Textos de erro não estão no Figma; o web mostra a mensagem crua do Supabase (em inglês).
- "Termos de Uso" / "Privacidade": links sem destino definido.

## Endpoints consumidos
- `GET /api/user/me`
- Supabase Auth: `POST /auth/v1/token` (password, id_token, refresh_token), `POST /auth/v1/signup`, `POST /auth/v1/logout`

## Endpoints ainda não consumidos
Todos os demais da seção 4 do contrato.

## Bloqueios em aberto
- Rede do ambiente cloud: `dl.google.com` / `maven.google.com` bloqueados (403 no proxy). Necessários para AGP,
  Compose Multiplatform e androidx. Sem isso não há como compilar nem testar a UI aqui.
