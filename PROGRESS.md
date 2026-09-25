# Progresso — Notura Mobile KMP

## Status
Passo 0 concluído e aprovado (2026-09-25). Nenhuma tela implementada ainda.

## Decisões
- Auth: `supabase-kt` (`auth-kt`) para login/cadastro/Google; Bearer nas chamadas `/api/*`.
- Contrato de API atualizado no backend: Henry-AF/Notura-App#92.
- Gravação: `meetingDate` = data de hoje, sem tela de metadados; sem upload de arquivo existente.
- Estados sem design no Figma: sinalizados, não inventados.
- Módulo único `composeApp`.

## Bloqueios em aberto
- Rede: `dl.google.com` / `maven.google.com` ainda bloqueados nesta sessão (necessários para Compose/AGP).
- Repositório GitHub `Notura-Mobile-KMP` ainda não acessível para push.

## Telas
| # | Tela | Status |
|---|------|--------|
| 1 | Auth (intro, login, cadastro, Google) | pendente |
| 2 | Gravar + upload de reunião | pendente |
| 3 | Home / lista de reuniões + detalhe/ata | pendente |
| 4 | Chat RAG da reunião | pendente |
| 5 | Kanban de tasks | pendente (sem Figma) |
| 6 | Billing / configurações | pendente (sem Figma) |

## Endpoints ainda não consumidos
Todos.
