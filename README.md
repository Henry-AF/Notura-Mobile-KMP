# Notura Mobile (KMP)

App mobile do Notura para **iOS e Android**, escrito em **Kotlin Multiplatform** com
**Compose Multiplatform** para a UI compartilhada. Consome a API do backend Notura
(Next.js + Supabase) — repositório [`Henry-AF/Notura-App`](https://github.com/Henry-AF/Notura-App).

> O app antigo em React Native/Expo (`Notura-App/mobile`) foi abandonado e **não** é
> referência de padrão para este projeto.

## Contrato de API

Fonte de verdade dos endpoints, auth, rate limits e formatos de erro:

- [`docs/api-codex-mobile.md`](https://github.com/Henry-AF/Notura-App/blob/main/docs/api-codex-mobile.md) — contrato geral da API
- [`docs/meeting-rag-chat-frontend.md`](https://github.com/Henry-AF/Notura-App/blob/main/docs/meeting-rag-chat-frontend.md) — chat RAG por reunião

Autenticação: `Authorization: Bearer <supabase access_token>` (ver `src/lib/api/auth.ts` no backend).

## Estrutura

```
shared/                domain, data, network e presentation (Kotlin puro: JVM + iOS)
composeApp/            UI Compose (commonMain) e entry points Android/iOS
  src/commonMain/kotlin/com/notura/mobile/
    ui/                telas, componentes reutilizáveis e tema (ui/theme = tokens do Figma)
    domain/            modelos de domínio e regras (sem dependência de Ktor/Compose)
    data/              repositórios, DTOs serializáveis, mapeamento DTO → domínio
    network/           cliente Ktor, auth Bearer, tratamento de erros HTTP do contrato
  src/commonTest/      testes de serialização, rede (MockEngine) e state holders
iosApp/                shell mínimo SwiftUI que hospeda o Compose
```

## Acompanhamento

Ver [`PROGRESS.md`](./PROGRESS.md).
