# SyncCarreira — Entrega V2 (requisitos que faltavam)

Implementação **aditiva** dos requisitos ainda não atendidos. **Nenhum arquivo
existente foi modificado**, com uma única exceção autorizada: a linha de `import`
do roteador em `Source/synccarreira-frontend/src/App.jsx`
(`AppRoutes.jsx` → `AppRoutes2.jsx`).

- Contrato dos endpoints novos: `Source/synccarreira-frontend/API_CONTRACT_V2.md`
- Swagger (documentação automática — RNF‑07): `http://localhost:8080/swagger-ui/index.html`

---

## 1. Como subir

### Pré‑requisitos (já presentes na máquina de desenvolvimento)
- JDK 21 (o `build.gradle` exige toolchain 21; o CI usa 17 e só faz `compileJava`)
- Node 20+ e npm
- MySQL 8 (Laragon) — cliente `mysql` no PATH

### Banco
```powershell
# 1. Criar banco + usuário (credenciais de application-dev.properties)
mysql -u root -e "CREATE DATABASE IF NOT EXISTS synccarreiradb CHARACTER SET utf8mb4;
  CREATE USER IF NOT EXISTS 'synccarreira'@'localhost' IDENTIFIED BY '@@jdbc2026sync-connection';
  GRANT ALL PRIVILEGES ON synccarreiradb.* TO 'synccarreira'@'localhost'; FLUSH PRIVILEGES;"

# 2. Schema legado — SÓ a parte DDL do create.sql (as 22 primeiras linhas:
#    10 CREATE TABLE + 12 ALTER). O bloco inteiro é repetido no arquivo e os
#    INSERTs dele são idênticos aos do import.sql — por isso rodamos só o DDL.
powershell -Command "(Get-Content Source/synccarreira-api/create.sql -TotalCount 22) | mysql -u root synccarreiradb"

# 3. Seed legado
mysql -u root --default-character-set=utf8mb4 synccarreiradb -e "SOURCE Source/synccarreira-api/src/main/resources/import.sql"

# 4. Migrações V2..V5 (nesta ordem). ddl-auto=none, então rodam à mão.
foreach ($v in 'V2__fundacao','V3__jornada','V4__resultados_e_alertas','V5__agendamento_links_feedback') {
  mysql -u root --default-character-set=utf8mb4 synccarreiradb -e "SOURCE Source/synccarreira-api/db/$v.sql"
}
```
> As migrações são **idempotentes** (podem ser rodadas de novo sem duplicar).
> `import.sql` foi carregado com `--default-character-set=utf8mb4` para não
> corromper os acentos.

### Backend
```powershell
cd Source/synccarreira-api
.\gradlew.bat bootRun            # sobe em http://localhost:8080 (profile dev)
```
Chave de criptografia (RNF‑02): use a env `SC_CRYPTO_KEY` (Base64). Sem ela,
usa uma chave de desenvolvimento fixa — **trocar em produção**.

### Frontend
```powershell
cd Source/synccarreira-frontend
npm ci        # escolhido em vez de Yarn: o CI usa `npm ci`, o package-lock está
              # sincronizado e os artefatos .pnp.cjs/.yarn são resíduos inertes
npm run dev   # http://localhost:5173
```

---

## 2. Dados de demonstração

| Conta | Senha | Perfil |
|---|---|---|
| `admin@synccarreira.com` | `12345678` | Administrador (criado pela V2) |
| `joao@gmail.com` | `12345678` | Aluno (seed) |
| `fernanda@gmail.com` | `12345678` | Administrador (seed — `import.sql` mapeia para `ROLE_ADMIN`) |

Criados durante os testes (podem ser recriados pelo painel Admin):
Colégio Santa Rita + turma "3A - Ensino Medio"; aluna **Maria** (`maria@escola.com`),
aluno **Pedro** (`pedro@escola.com`), psicóloga **Dra Ana** (`ana@escola.com`) — todos `12345678`.

---

## 3. Decisões de projeto (alinhadas antes de codar)

| # | Assunto | Decisão |
|---|---|---|
| **C1** | Endpoints legados são todos `permitAll` e `ResourceServerConfig` não pode ser editado | `SecurityConfigV2` — cadeia nova com precedência que cobre rotas novas **e** legadas sensíveis, exigindo autenticação e papel. Filtro `AccountStatusFilter` bloqueia conta desativada. |
| **C2** | Ordem das trilhas: documento × `import.sql` divergem | Prevalece o **Documento de Requisitos**: Informação = 3ª, Projeto de Futuro = 4ª. A `V2` faz `UPDATE tb_trilha`. |
| **C3** | 5ª etapa "Síntese" não existe no enum `TrailName` (não editável) | **C3‑B**: Síntese é uma **fase final derivada** — abre só com as 4 trilhas concluídas, consolida as sínteses e mostra o leque. Sem 5ª linha em `tb_trilha`; nada quebra. |
| **C4** | RNF‑02 (criptografia) | Cifra os campos sensíveis **novos** (`AttributeConverter` AES‑GCM). O `tb_respostas_aluno.conteudo` legado permanece em claro. |
| **C5** | RF‑08 pede Google Calendar (exige credenciais/chamadas externas) | Geração de convite **`.ics`** (abre no Google Calendar/Outlook) + notificação em log (mock de e‑mail). Interface isolada em `NotificationService` para plugar um provedor real depois. |
| **C6** | Bug do `ROLE_MAP` no `authService.js` (não editável): psicóloga virava admin | `authServiceV2.js` + páginas novas + `AppRoutes2.jsx` + a única linha de `App.jsx`. As páginas legadas (Login/Cadastro/Home/Trilha) ficam órfãs. |
| **C7** | RF‑07 pede baixar relatório e `build.gradle` não pode ganhar lib de PDF | **CSV** (UTF‑8 com BOM, abre no Excel) + página imprimível (Ctrl+P → PDF). |
| **C8** | RF‑12 / RN‑09 — catálogo de carreiras | `V4` semeia `tb_area_carreira` (20 itens) marcados `validado = 0`; a psicóloga valida. |

---

## 4. Cobertura dos requisitos

### Funcionais
| ID | Status | Onde |
|---|---|---|
| RF‑01 Instituições + turmas | ✅ | `/institutions`, `/classes` · painel Admin |
| RF‑02 Usuários/perfis, desativar, CPF | ✅ | `/admin/users` · painel Admin |
| RF‑03 Painel de status por aluno | ✅ | `/psychologist-panel` (RN‑03: sem respostas) |
| RF‑04 Alerta automático de orientação | ✅ | gerado ao concluir jornada em dúvida; exibido no painel |
| RF‑05 Priorização "em dúvida" + agendamento | ✅ | filtro no painel + CTA "sessão em grupo" |
| RF‑06 Curadoria de links | ✅ | `/curated-links` · aba Links |
| RF‑07 Relatórios turma + individuais | ✅ | `/reports/*` (CSV) |
| RF‑08 Agendamento + Google Calendar | ✅ | `/appointments` + `.ics` (C5) |
| RF‑09 Feedback personalizado | ✅ | `/feedbacks` |
| RF‑10 Jornada de 5 etapas em ordem | ✅ | `/journeys` — 4 trilhas + Síntese; gating server‑side (RN‑01) |
| RF‑11 Síntese ao final de cada trilha | ✅ | obrigatória para concluir (RN‑02) |
| RF‑12 Leque de carreiras | ✅ | `/journeys/{id}/final` — multi‑área + disclaimer (RN‑07) |
| RF‑13 Panorama da jornada | ✅ | `/journeys/{id}/panorama` |
| RF‑14 Personalização de perfil | ✅ | `/student-profile` |
| RF‑15 Histórico de jornadas anteriores | ✅ | `/journeys/history` — ciclos incrementais |
| RF‑16 Agendamentos e feedbacks do aluno | ✅ | `/appointments/mine`, `/feedbacks/mine` |

### Regras de negócio
| ID | Status | Nota |
|---|---|---|
| RN‑01 5 trilhas sequenciais | ✅ | `JourneyTrail.status` BLOQUEADA→LIBERADA→CONCLUIDA, validado no serviço |
| RN‑02 Síntese obrigatória | ✅ | `422` se perguntas incompletas ou texto vazio |
| RN‑03 Painel sem respostas individuais | ✅ | DTO do painel só tem progresso/flags |
| RN‑04 Em dúvida → grupo automático | ✅ | alerta com `priority = GRUPO` |
| RN‑05 Links curados na trilha Informação | ✅ | `/curated-links/for-me` filtrado por instituição |
| RN‑06 Sistema não valida conteúdo psicológico | ✅ | não geramos conteúdo; escrita de trilhas/perguntas restrita a ADMIN/PSICOLOGA |
| RN‑07 Leque, sem diagnóstico fechado | ✅ | sempre ≥ 2 áreas + disclaimer explícito |
| RN‑08 Panorama completo | ✅ | respostas + sínteses + síntese final |
| RN‑09 Banco fixo validado | ✅ | seed de perguntas + catálogo de carreiras `validado` pela psicóloga |

### Não funcionais
| ID | Status | Nota |
|---|---|---|
| RNF‑01 Autenticação/autorização | ✅ | `SecurityConfigV2` + `@PreAuthorize` + `CurrentUser`/`PsychologistScope` |
| RNF‑02 Privacidade / LGPD / criptografia | ⚠️ parcial | campos sensíveis **novos** cifrados (AES‑GCM); `tb_respostas_aluno` legado em claro |
| RNF‑03 Desempenho (< 2 s) | ➖ | endpoints simples; não há medição formal de carga |
| RNF‑04 Usabilidade mobile‑first | ✅ | CSS puro responsivo, `@media` em todas as áreas novas, só variáveis `--sc-*` |
| RNF‑05 Disponibilidade 99% | ➖ | responsabilidade de infraestrutura |
| RNF‑06 Manutenibilidade / camadas | ✅ | controllers / services / repositories / dto separados, seguindo os padrões existentes |
| RNF‑07 Documentação de API | ✅ | Swagger + `@Operation` PT‑BR + `API_CONTRACT_V2.md` |
| RNF‑08 Isolamento por instituição | ✅ | `tb_conta.fk_instituicao` + `PsychologistScope` + checagens de escopo |

### Validações de campo (seção 7)
CPF obrigatório/único ✅ · CNPJ obrigatório/único ✅ (ambos com dígito verificador) ·
E‑mail formato ✅ · Telefone opcional ✅ · Data — `datetime-local` + validação de intervalo ✅

---

## 5. Arquivos novos (resumo)

### Backend — `Source/synccarreira-api/`
- `db/V2__fundacao.sql`, `V3__jornada.sql`, `V4__resultados_e_alertas.sql`, `V5__agendamento_links_feedback.sql`
- `crypto/EncryptedStringConverter.java`
- `config/SecurityConfigV2.java`
- `security/` — `CurrentUser`, `AccountStatusFilter`, `PsychologistScope`
- `entities/` — `Account`, `Institution`, `SchoolClass`, `StudentProfile`, `Interest`, `Journey`, `JourneyTrail`, `JourneyAnswer`, `TrailSynthesis`, `CareerArea`, `OrientationAlert`, `CuratedLink`, `OrientationSession`, `SessionParticipant`, `Feedback` (+ enums)
- `repositories/` — 15 repositórios novos
- `services/` — `InstitutionService`, `SchoolClassService`, `AdminUserService`, `StudentProfileService`, `JourneyService`, `CareerSuggestionService`, `OrientationAlertService`, `CuratedLinkService`, `PsychologistPanelService`, `SessionService`, `NotificationService`, `FeedbackService`, `ReportService`
- `services/validation/DocumentoValidator.java` (CPF/CNPJ)
- `controllers/` — `Institution`, `SchoolClass`, `AdminUser`, `Journey`, `StudentProfile`, `CuratedLink`, `PsychologistPanel`, `Session`, `Feedback`, `Report`
- `controllers/handlers/ControllerExceptionHandlerV2.java`
- `dto/` — DTOs correspondentes
- `src/test/java/.../crypto/EncryptedStringConverterTest.java`

### Frontend — `Source/synccarreira-frontend/src/`
- `routes/AppRoutes2.jsx` · `services/authServiceV2.js`, `adminService.js`, `alunoService.js`, `psicologaService.js`
- `pages/Admin/` — `AdminLayout`, `InstitutionsPage`, `ClassesPage`, `UsersPage` (+ `AdminLayout.css`)
- `pages/Aluno/` — `AlunoLayout`, `JornadaPage`, `TrilhaPage`, `EtapaFinalPage`, `PanoramaPage`, `PerfilPage`, `HistoricoPage`, `AgendaPage` (+ `Aluno.css`)
- `pages/Psicologa/` — `PsicologaLayout`, `PainelPage`, `LinksPage`, `AgendaPage`, `RelatoriosPage` (+ `Psicologa.css`)
- **Editado (1 linha):** `src/App.jsx`

### Documentação
- `Source/synccarreira-frontend/API_CONTRACT_V2.md`
- este arquivo

---

## 6. Limitações conhecidas / pendências

1. **`gradlew test` não roda no caminho do OneDrive** — o acento em
   "Área de Trabalho" quebra o worker do Gradle. É pré‑existente (o CI só faz
   `compileJava` e o teste legado `contextLoads` também falharia). Os testes
   foram executados num espelho em `C:\Users\drehs\dev\sc-apitest`
   (`clean test` → 5/5, incluindo o `contextLoads` que carrega todo o contexto).
2. **A API gera um novo par de chaves RSA a cada boot** (`AuthorizationServerConfig`,
   não editável) — tokens emitidos antes de um restart deixam de valer. Só
   incomoda em desenvolvimento ao reiniciar o backend.
3. **CI usa JDK 17** para `compileJava`, incompatível com a toolchain 21 do
   `build.gradle`. Não foi tocado (é arquivo existente). Localmente usa‑se 21.
4. **`HomePage` legada** (órfã) listava as trilhas sem ordenar por
   `sequentialOrder`. As páginas novas do aluno ordenam corretamente.
5. **RNF‑02 parcial**: respostas antigas em `tb_respostas_aluno` continuam em
   texto claro (o `AnswerService` legado que as grava não pode ser alterado).
   O fluxo novo de jornada usa `tb_resposta_jornada` com criptografia.
6. **Google Calendar**: entregue como `.ics` + mock de notificação (decisão C5).
   Para integração real, implementar `NotificationService` com a Google Calendar API.
