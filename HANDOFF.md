# HANDOFF — SyncCarreira

> Escrito para uma sessão do Claude Code **sem nenhum contexto anterior**.
> Toda afirmação abaixo foi **verificada no código/banco nesta sessão**
> (não é memória) — quando não deu pra verificar, está marcado como tal.
> Data desta verificação: **2026‑09‑11**.

---

## 0. REGRA INVIOLÁVEL — leia antes de tocar em qualquer arquivo

> **Nenhum arquivo pré‑existente do repositório pode ser modificado. Só
> arquivos novos.**

**Por quê:** este é um projeto de equipe (TCC, 6 pessoas, IFSP). Os arquivos
que já existiam quando esta sessão começou são de outras pessoas. Editar algo
que "parece" um bug pode:
- conflitar com o que um colega está fazendo em paralelo,
- apagar uma decisão que ele tomou por um motivo que você não vê,
- e é impossível saber, só olhando o código, se algo "errado" foi um erro ou
  uma escolha.

Toda funcionalidade nova até agora foi feita **aditivamente**: entidades,
controllers, services, páginas e arquivos `.sql` **novos**, ligados ao que já
existia sem alterá‑lo. Onde o comportamento legado tinha um bug real (ver
seção 5), a solução foi contornar por fora, nunca corrigir o arquivo original.

### Exceções já autorizadas (e só estas)

| Arquivo | O que mudou | Por quê foi autorizado |
|---|---|---|
| `Source/synccarreira-frontend/src/App.jsx` | 1 linha: `import AppRoutes from './routes/AppRoutes.jsx'` → `'./routes/AppRoutes2.jsx'` | Era a única forma de o React montar as rotas novas. O usuário autorizou explicitamente essa e só essa linha. |

Se uma tarefa futura *parecer* exigir editar outro arquivo existente, **pare e
pergunte antes** — não assuma que a regra afrouxou.

---

## 1. O projeto

**SyncCarreira** é uma plataforma web de orientação vocacional para alunos do
ensino médio: guia o aluno por uma jornada de autoconhecimento em trilhas
sequenciais, dá à psicóloga/orientadora escolar um painel de acompanhamento, e
ao administrador uma gestão de instituições e contas. É um **TCC em equipe de
6 pessoas do IFSP** (documento de requisitos: `REQUISITOS_SyncCarreira.md`,
cliente Patrícia Maria Galvão Cintra Mortara).

```
SyncCarreira-main/
├── REQUISITOS_SyncCarreira.md      # documento de requisitos aprovado (com anexo de inconsistências)
├── ENTREGA_V2.md                    # entrega dos blocos 1–6 desta sessão (setup, decisões, cobertura)
├── HANDOFF.md                       # este arquivo
├── Source/
│   ├── synccarreira-api/            # backend — Spring Boot
│   │   ├── db/                      # migrações V2..V5 (novas, desta sessão)
│   │   ├── create.sql, import.sql   # schema+seed legados (NÃO editar)
│   │   └── src/main/java/.../synccarreira_api/
│   └── synccarreira-frontend/       # frontend — React + Vite
│       ├── API_CONTRACT.md          # contrato original (legado)
│       └── API_CONTRACT_V2.md       # contrato dos endpoints novos (desta sessão)
└── Projeto/                         # documentos do TCC (Gantt, diário, template ABNT)
```

### Como rodar o backend
```powershell
cd Source/synccarreira-api
.\gradlew.bat bootRun          # perfil dev por padrão — sobe em http://localhost:8080
```
Pré‑requisito: MySQL rodando e **já populado manualmente** (ver seção 3 —
`ddl-auto=none`, nada é criado sozinho). Swagger em
`http://localhost:8080/swagger-ui/index.html`.

### Como rodar o frontend
```powershell
cd Source/synccarreira-frontend
npm ci
npm run dev                     # http://localhost:5173
```

### Credenciais de demonstração (usuários já no banco, senha `12345678` para todos)
| E‑mail | Perfil |
|---|---|
| `admin@synccarreira.com` | Administrador (criado pela migração `V2`) |
| `joao@gmail.com` | Aluno (seed original do `import.sql`) |
| `fernanda@gmail.com` | Administrador (seed original — `import.sql` já mapeia para `ROLE_ADMIN`) |
| `ana@escola.com` | Psicóloga (criada nesta sessão, vinculada ao Colégio Santa Rita) |
| `maria@escola.com` | Aluna (mesma instituição da Ana, tem 1 jornada concluída "em dúvida" + 1 jornada em andamento) |
| `pedro@escola.com` | Aluno (mesma instituição, jornada com 1 trilha concluída) |

---

## 2. O que EU verifiquei agora, ao vivo (não é suposição)

Rodei os serviços e consultei o MySQL nesta sessão antes de escrever este
arquivo. Estado real do banco no momento desta verificação:

```
tb_role:  1=ROLE_USER  2=ROLE_ADMIN  3=ROLE_PSICOLOGA
tb_trilha (ordenado): AUTOCONHECIMENTO(1) INFLUENCIAS(2) INFORMACAO(3) PLANO_DE_FUTURO(4)
tb_usuario: joao(ROLE_USER) fernanda(ROLE_ADMIN) admin(ROLE_ADMIN)
            maria(ROLE_USER) ana(ROLE_PSICOLOGA) pedro(ROLE_USER)
Tabelas novas populadas: 1 instituição, 1 turma, 6 contas, 4 jornadas,
  20 itens de catálogo de carreira, 20 interesses, 1 link curado,
  1 sessão, 3 feedbacks, 1 alerta de orientação.
Tamanho total do banco: 1.00 MB.
tb_respostas_aluno (legado): 0 linhas.  tb_resposta_jornada (novo): 50 linhas.
Um registro de tb_sintese_trilha começa com "enc:v1:..." → confirma que a
  criptografia está de fato sendo aplicada, não é só código morto.
```

⚠️ **No momento em que este arquivo foi escrito, backend, frontend e MySQL
estão parados** (a sessão anterior foi reiniciada duas vezes e eu não
religuei tudo de propósito, pra não deixar processos zumbis). Pra retomar:
```powershell
# MySQL (Laragon) — se não estiver rodando:
Start-Process "C:\laragon\bin\mysql\mysql-8.4.3-winx64\bin\mysqld.exe" -ArgumentList '--defaults-file=C:\laragon\bin\mysql\mysql-8.4.3-winx64\my.ini' -WindowStyle Hidden
# depois: gradlew bootRun (api) e npm run dev (front), como na seção 1.
```
`root` do MySQL local tem **senha vazia** (Laragon padrão) — não peça senha
ao usuário para isso.

---

## 3. Stack e ambiente

| Item | Valor confirmado | Onde |
|---|---|---|
| Java | **21** (toolchain obrigatório) | `Source/synccarreira-api/build.gradle` |
| Spring Boot | **4.0.5** | idem |
| Gradle | wrapper **9.4.1** | `gradle/wrapper/gradle-wrapper.properties` |
| springdoc (Swagger) | 3.0.2 | `build.gradle` |
| Node | 20+ (usei v24.11.1 nesta máquina) | — |
| MySQL | 8.4.3 (Laragon local) | — |
| Banco de dev | `jdbc:mysql://localhost:3306/synccarreiradb`, user `synccarreira` / senha `@@jdbc2026sync-connection` | `application-dev.properties` |

**`spring.jpa.hibernate.ddl-auto=none`** nos perfis `dev` e `prod` — o
Hibernate **não cria nenhuma tabela** e o `import.sql` **não roda sozinho**
(Hibernate só executa `import.sql` automaticamente quando `ddl-auto` é
`create`/`create-drop`, e aqui não é). Isso significa: **todo `.sql` do
projeto tem que ser rodado manualmente**, sempre, inclusive num ambiente
novo. Ordem exata (idempotente, pode rodar de novo sem duplicar):

```powershell
# 1) schema legado — SÓ as 22 primeiras linhas do create.sql (ele tem o
#    bloco inteiro de CREATE/ALTER + seed repetido duas vezes; as 22
#    primeiras linhas são só o DDL: 10 CREATE TABLE + 12 ALTER)
(Get-Content Source/synccarreira-api/create.sql -TotalCount 22) | mysql -u root synccarreiradb

# 2) seed legado — com utf8mb4 explícito, senão os acentos corrompem
mysql -u root --default-character-set=utf8mb4 synccarreiradb -e "SOURCE Source/synccarreira-api/src/main/resources/import.sql"

# 3) migrações desta sessão, NESTA ordem
foreach ($v in 'V2__fundacao','V3__jornada','V4__resultados_e_alertas','V5__agendamento_links_feedback') {
  mysql -u root --default-character-set=utf8mb4 synccarreiradb -e "SOURCE Source/synccarreira-api/db/$v.sql"
}
```
Se pular esses passos, a aplicação sobe mas toda query dá erro de "tabela não existe".

**Perfil `test`** (`application-test.properties`) usa H2 em memória e **não**
define `ddl-auto` explicitamente → o Hibernate usa `create-drop` por padrão
com H2 → aí sim as entidades geram o schema sozinhas e o `import.sql`
roda automaticamente (`spring.jpa.defer-datasource-initialization=true`
está ligado). É por isso que os testes automatizados funcionam sem rodar
`.sql` na mão.

**Gerenciador de pacotes do front: npm**, não Yarn — embora o projeto tenha
`.pnp.cjs` + `.yarn/` (Yarn PnP) **e** `package-lock.json` ao mesmo tempo.
Motivo: `.github/workflows/build-code.yml` (CI) roda `npm ci`, o
`package-lock.json` está sincronizado (`npm ci` funciona sem erro), e os
artefatos do Yarn só carregam se alguém rodar `yarn` — não interferem em nada
rodando `npm`. Não rode `yarn` neste projeto.

**Path com acento no OneDrive** — a pasta do projeto é
`C:\Users\...\OneDrive\Área de Trabalho\TCC\...` (tem "Á" e "ç"). Isso quebra
o **test worker do Gradle**: `gradlew test` falha com erro de classe não
encontrada, mesmo com os testes corretos. `compileJava` e `bootRun` funcionam
normalmente — só o `test` runner tem problema com esse path específico.
Contorno usado nesta sessão: copiar `src/` para um path sem acento
(`C:\Users\drehs\dev\sc-apitest\`, com os mesmos `build.gradle` e
`gradle/wrapper/`) e rodar `gradlew clean test` lá. Da última vez: **5/5
testes passando** (4 de `EncryptedStringConverterTest` + o `contextLoads`
padrão, que carrega o Spring context inteiro — todos os beans dos blocos 1–6
— contra o MySQL real). O CI do GitHub Actions roda em Linux (sem esse
problema de path), mas só faz `compileJava`, não `test`.

---

## 4. O que foi criado nesta sessão

Tudo abaixo é **arquivo novo**. Nenhum dos arquivos listados aqui existia
antes desta sessão.

### Bloco 1 — Fundação (segurança, papéis, criptografia)

**Backend**
- `crypto/EncryptedStringConverter.java` — cifra/decifra campos sensíveis (ver seção 6)
- `config/SecurityConfigV2.java` — segunda `SecurityFilterChain`, com precedência sobre a legada, cobrindo rotas novas + rotas legadas sensíveis
- `security/CurrentUser.java` — resolve o usuário autenticado a partir do JWT
- `security/AccountStatusFilter.java` — bloqueia (403) requisição de conta desativada
- `entities/Account.java`, `entities/Institution.java`, `entities/SchoolClass.java`, `entities/enums/InstitutionType.java` — modelo base (também usado pelo Bloco 2)
- `repositories/AccountRepository.java`, `InstitutionRepository.java`, `SchoolClassRepository.java`
- `controllers/handlers/ControllerExceptionHandlerV2.java` — tratamento de erro dos tipos novos (409/422/403), mesmo formato de erro do legado
- `services/exceptions/ConflictException.java`, `BusinessException.java`, `ForbiddenOperationException.java`
- `db/V2__fundacao.sql` — cria `ROLE_PSICOLOGA`, corrige a ordem das trilhas (ver seção 5), cria `tb_instituicao`/`tb_turma`/`tb_conta`, cria o usuário admin
- `src/test/.../crypto/EncryptedStringConverterTest.java` — 4 testes de round‑trip da criptografia

**Frontend**
- `services/authServiceV2.js` — login/cadastro com o `ROLE_MAP` corrigido
- `routes/AppRoutes2.jsx` — roteador novo (ver seção 5)

### Bloco 2 — RF‑01 (Instituições e turmas)

**Backend:** `dto/InstitutionDTO.java`, `InstitutionInsertDTO.java`, `InstitutionUpdateDTO.java`, `SchoolClassDTO.java`, `SchoolClassInsertDTO.java`, `SchoolClassUpdateDTO.java` · `services/InstitutionService.java`, `SchoolClassService.java` · `services/validation/DocumentoValidator.java` (CPF/CNPJ com dígito verificador) · `controllers/InstitutionController.java`, `SchoolClassController.java`

**Frontend:** `pages/Admin/AdminLayout.jsx` + `.css`, `InstitutionsPage.jsx`, `ClassesPage.jsx` · `services/adminService.js`

### Bloco 3 — RF‑02 (Gestão de usuários/perfis)

**Backend:** `dto/ManagedUserDTO.java`, `ManagedUserInsertDTO.java`, `ManagedUserUpdateDTO.java`, `AccountStatusUpdateDTO.java` · `services/AdminUserService.java` · `controllers/AdminUserController.java`

**Frontend:** `pages/Admin/UsersPage.jsx`

### Bloco 4 — Jornada do aluno (RF‑10, 11, 12, 13, 14, 15)

**Backend**
- `entities/StudentProfile.java`, `Interest.java`, `Journey.java`, `JourneyTrail.java`, `JourneyAnswer.java`, `TrailSynthesis.java`, `CareerArea.java`, `OrientationAlert.java` + `enums/JourneyStatus.java`, `JourneyTrailStatus.java`
- `repositories/StudentProfileRepository.java`, `InterestRepository.java`, `JourneyRepository.java`, `JourneyTrailRepository.java`, `JourneyAnswerRepository.java`, `TrailSynthesisRepository.java`, `CareerAreaRepository.java`, `OrientationAlertRepository.java`
- `services/StudentProfileService.java`, `JourneyService.java` (o maior — gating das trilhas), `CareerSuggestionService.java` (monta o leque), `OrientationAlertService.java` (cria o alerta quando o aluno conclui "em dúvida")
- `dto/StudentProfileDTO.java`, `StudentProfileUpdateDTO.java` · `dto/journey/JourneyViewDTO.java`, `TrailDetailDTO.java`, `JourneyRequests.java`, `FinalPhaseDTO.java`, `PanoramaDTO.java`, `JourneyHistoryItemDTO.java`
- `controllers/StudentProfileController.java`, `JourneyController.java`
- `db/V3__jornada.sql` (perfil, interesses, jornada, gating, respostas e sínteses cifradas) e `db/V4__resultados_e_alertas.sql` (catálogo de carreiras + tabela de alerta)

**Frontend:** `pages/Aluno/AlunoLayout.jsx` + `Aluno.css`, `JornadaPage.jsx`, `TrilhaPage.jsx`, `EtapaFinalPage.jsx`, `PanoramaPage.jsx`, `PerfilPage.jsx`, `HistoricoPage.jsx` · `services/alunoService.js`

### Bloco 6 — Painel da psicóloga (RF‑03, 04, 05, 06, 07, 08, 09, 16)
*(o "Bloco 5" — leque de carreiras/alertas — acabou fundido no Bloco 4 porque `JourneyService` dependia deles; não existe código rotulado "bloco 5" separado.)*

**Backend**
- `entities/CuratedLink.java`, `OrientationSession.java`, `SessionParticipant.java`, `Feedback.java` + `enums/SessionType.java`, `SessionStatus.java`
- `repositories/CuratedLinkRepository.java`, `OrientationSessionRepository.java`, `FeedbackRepository.java`
- `security/PsychologistScope.java` — restringe a psicóloga aos alunos da própria instituição
- `services/CuratedLinkService.java`, `PsychologistPanelService.java`, `SessionService.java`, `NotificationService.java` (gera `.ics` + loga notificação mock), `FeedbackService.java`, `ReportService.java` (CSV)
- `dto/psicologa/CuratedLinkDTOs.java`, `PanelDTOs.java`, `SessionDTOs.java`, `FeedbackDTOs.java`
- `controllers/CuratedLinkController.java`, `PsychologistPanelController.java`, `SessionController.java`, `FeedbackController.java`, `ReportController.java`
- `db/V5__agendamento_links_feedback.sql`

**Frontend:** `pages/Psicologa/PsicologaLayout.jsx` + `Psicologa.css`, `PainelPage.jsx`, `LinksPage.jsx`, `AgendaPage.jsx`, `RelatoriosPage.jsx` · `pages/Aluno/AgendaPage.jsx` (RF‑16, área do aluno) · `services/psicologaService.js`

### Documentação
- `Source/synccarreira-frontend/API_CONTRACT_V2.md` — contrato de todos os endpoints novos (payloads, papéis exigidos)
- `ENTREGA_V2.md` — setup passo a passo, decisões C1–C8, matriz de cobertura de requisitos, arquivos novos, limitações
- `HANDOFF.md` — este arquivo

---

## 5. Decisões técnicas e trade‑offs

### 5.1 Conflito de papéis: `ROLE_ADMIN` id 2 × `psicologa: 2` no front

**O bug original:** `import.sql` (legado) cria `ROLE_USER`=1 e `ROLE_ADMIN`=2.
O `authService.js` (legado, `src/services/authService.js`) tem
`const ROLE_MAP = { aluno: 1, psicologa: 2 }` — ou seja, toda psicóloga
cadastrada pelo cadastro público virava **administrador**, porque o id 2 é
admin, não psicóloga. Nenhum dos dois arquivos podia ser editado.

**Solução:** `db/V2__fundacao.sql` insere um **terceiro papel**,
`ROLE_PSICOLOGA` (fica com id **3**, não reaproveita o 2). O
`authServiceV2.js` novo usa `{ aluno: 1, admin: 2, psicologa: 3 }` —
correto. O `authService.js` legado com o bug **continua existindo e
continua errado**, mas só é chamado pela `CadastroPage.jsx` legada (ver 5.6)
— se alguém cadastrar uma psicóloga por aquele formulário específico, ela
ainda vira admin. O cadastro de psicóloga funcional está em qualquer fluxo
que use `authServiceV2.register` ou o endpoint `/admin/users` (que o admin
usa para criar contas — esse sempre usou o `roleId` certo, porque o
`AdminUserService` que criei mapeia `PSICOLOGA` → busca o papel pelo nome
(`roleRepository.findByAuthority("ROLE_PSICOLOGA")`), não por id fixo).

### 5.2 Trilha "Síntese" sem editar o enum `TrailName`

`RF-10` pede 5 etapas (4 trilhas + Síntese). O enum
`entities/enums/TrailName.java` (legado, não editável) só tem 4 valores
(`AUTOCONHECIMENTO, INFLUENCIAS, INFORMACAO, PLANO_DE_FUTURO`). Cheguei a
cogitar um `ALTER TABLE` pra adicionar um 5º valor `SINTESE` na coluna
`nome_trilha`, mas isso **quebra o `GET /trails` legado**: o Hibernate, ao
ler a linha nova, tenta desserializar `"SINTESE"` num enum Java que não tem
esse valor e explode.

**Decisão final:** a Síntese **não é uma linha em `tb_trilha`**. É uma
**fase final derivada**, calculada no `JourneyService`: só fica disponível
(`finalPhaseAvailable: true` no `GET /journeys/{id}`) quando as 4
`JourneyTrail` da jornada estão todas `CONCLUIDA`. O texto da síntese final
fica direto em `tb_jornada.sintese_final` (não em `tb_sintese_trilha`, que é
só para as sínteses das 4 trilhas reais). **Se uma sessão futura for
tentada a "adicionar a 5ª trilha de verdade" para ficar "mais literal" com o
RF‑10 — não faça isso sem reler esta seção**, porque quebra o `/trails`
legado.

### 5.3 RF‑08 (Google Calendar) virou `.ics`

Integrar a Google Calendar API de verdade exige credenciais OAuth de um
projeto no Google Cloud (client id/secret, tela de consentimento) que
ninguém tinha configurado. A alternativa aditiva: cada sessão de orientação
gera um arquivo `.ics` (padrão iCalendar — `VCALENDAR`/`VEVENT`), que abre
nativamente no Google Calendar, Outlook, Apple Calendar etc., disponível em
`GET /appointments/{id}/ics`. A "notificação ao aluno" é um mock: só grava
um log estruturado (`NotificationService.notifySessionScheduled`), não
manda e‑mail de verdade. Isso está isolado numa interface
(`NotificationService`) — trocar por Google Calendar de verdade depois é
implementar essa classe de novo, sem mexer no resto (`SessionService` não
sabe como a notificação é entregue).

### 5.4 Ordem das trilhas: documento × `import.sql`

O anexo de inconsistências do `REQUISITOS_SyncCarreira.md` (item 3) apontava
que o documento define **Informação como 3ª** e **Projeto de Futuro como
4ª**, mas o `import.sql` cadastrava o contrário (Plano de Futuro=3,
Informação=4). **Prevaleceu o documento** — combinado com o usuário antes de
implementar (decisão "C2"). A correção é um `UPDATE tb_trilha` dentro de
`V2__fundacao.sql` (não mexe no `import.sql`). Confirmado no banco agora:
`AUTOCONHECIMENTO(1) INFLUENCIAS(2) INFORMACAO(3) PLANO_DE_FUTURO(4)`.

### 5.5 Duas cadeias de segurança coexistindo

O `ResourceServerConfig` legado (não editável) deixa **todos** os endpoints
antigos como `permitAll` e nega (403) qualquer rota que não esteja na lista
dele — descobri isso testando: uma rota totalmente nova, sem nenhuma regra,
dava 403 mesmo autenticado. Por isso `SecurityConfigV2` existe como uma
**segunda** `SecurityFilterChain`, com `@Order(Ordered.HIGHEST_PRECEDENCE)`
(maior prioridade), e um `securityMatcher` que lista tanto os prefixos novos
quanto os legados sensíveis (`/users`, `/students`, `/psychologists`,
`/trails`, `/questions`, `/answers`). Para essas rotas, a cadeia nova decide
sozinha; o `ResourceServerConfig` legado só continua valendo para
`/oauth2/**`, `/swagger-ui/**` e qualquer rota que eu não tenha listado.
**Consequência que uma sessão futura precisa saber:** se você criar um
controller novo com um prefixo que não está em
`SecurityConfigV2.MATCHED_PATHS`, ele cai na cadeia legada e será negado
(403) por padrão — tem que adicionar o prefixo no array.

### 5.6 Páginas legadas ficaram órfãs

`AppRoutes2.jsx` **não** referencia mais `pages/Home/HomePage.jsx` nem
`pages/Trilha/TrailPage.jsx` (confirmei com grep — zero ocorrências). Elas
continuam no repo (não posso apagar arquivo existente) mas nada as importa
mais. `pages/Login/LoginPage.jsx` e `pages/Cadastro/CadastroPage.jsx`
**continuam em uso** (via `AppRoutes2`) — o login funciona porque não tem o
bug de roles; o cadastro **usa o `authService.js` legado com o `ROLE_MAP`
errado** (ver 5.1) para cadastro de psicóloga. Isso significa: **o botão
"Criar conta" público, com perfil psicóloga, ainda cria administradores por
engano.** O cadastro de aluno pelo mesmo formulário está correto (id 1 é
`ROLE_USER` mesmo). Contas de psicóloga corretas devem ser criadas pelo
painel do Admin (`/admin/usuarios`), que usa `/admin/users` e não tem esse
bug.

### 5.7 CSV em vez de PDF para relatórios (RF‑07)

`build.gradle` não pode ganhar uma dependência nova (regra de "só arquivo
novo" também vale pra isso). Sem lib de PDF, os relatórios saem como CSV
(UTF‑8 com BOM, abre certo no Excel) + uma dica de usar Ctrl+P na página
`RelatoriosPage.jsx` pra gerar PDF do navegador.

### 5.8 Catálogo de carreiras semeado como "pendente de validação"

RN‑09 exige que o conteúdo seja "validado pela psicóloga". `V4` semeia 20
itens em `tb_area_carreira` com `validado = 0`. O campo e a relação com
`fk_validado_por` **existem no modelo**, mas **não há endpoint nem tela**
para a psicóloga efetivamente marcar um item como validado — ficou faltando
(ver seção 8, item 2).

---

## 6. Criptografia (RNF‑02) — SEÇÃO CRÍTICA

- **Variável de ambiente:** `SC_CRYPTO_KEY` (string Base64).
- **Onde é lida:** `Source/synccarreira-api/src/main/java/com/synccarreira/synccarreira_api/crypto/EncryptedStringConverter.java`, método `resolveKey()`, linha
  `String base64 = System.getenv("SC_CRYPTO_KEY");`.
- **Se a variável não estiver definida**, cai num valor fixo
  `DEV_DEFAULT_KEY` embutido no próprio `.java` (só serve pra desenvolvimento
  — está no código-fonte, portanto não é segredo nenhum).
- **Algoritmo:** AES‑256/GCM. A chave informada (de qualquer tamanho) é
  normalizada para 32 bytes via SHA‑256 antes de virar a chave AES — ou seja,
  **não precisa ser exatamente 32 bytes**, qualquer string funciona como
  entrada, mas o valor final depende 100% dela.
- **Prefixo no banco:** todo valor cifrado é salvo como `enc:v1:<base64 de IV+ciphertext>`. Um valor sem esse prefixo é tratado como texto legado não cifrado (compatibilidade).

### O que é cifrado (5 campos, todos de entidades novas)
| Entidade | Campo | Conteúdo |
|---|---|---|
| `StudentProfile` | `bio` | texto livre do perfil do aluno |
| `TrailSynthesis` | `text` | síntese de cada trilha |
| `Journey` | `finalSynthesis` | síntese final da jornada |
| `JourneyAnswer` | `content` | texto livre de resposta (perguntas `ABERTA`) |
| `Feedback` | `text` | feedback da psicóloga pro aluno |

### O que **não** é cifrado
- `tb_respostas_aluno.conteudo` — tabela **legada**, gravada pelo
  `AnswerService.java` original (não editável). Ele grava
  `entity.setContent(dto.getContent())` direto, sem passar pelo conversor.
  **Confirmei agora:** essa tabela tem **0 linhas** no banco de
  desenvolvimento, e **`AppRoutes2.jsx` não expõe mais nenhuma tela que
  chame `POST /answers`** (o fluxo de resposta do aluno hoje é todo via
  `/journeys/{id}/answers`, que grava em `tb_resposta_jornada`, cifrado). Ou
  seja: **o endpoint legado `POST /answers` continua existindo e continua
  gravando em texto claro**, mas não tem mais nenhum botão na UI que leve até
  ele — só é alcançável batendo direto na API (Swagger/curl). Não é RNF‑02
  pleno, é parcial, e é assim porque `AnswerService.java` não pode ser
  editado.
- Nome, e‑mail, CPF, CNPJ e demais campos estruturados **não** são cifrados
  (fazem sentido em claro pra buscas/unicidade).

### ⚠️ Se a chave for diferente entre ambientes

**Todo dado cifrado com uma chave só é legível com essa mesma chave.** Se
você gravar sínteses em produção com `SC_CRYPTO_KEY=X` e depois trocar para
`SC_CRYPTO_KEY=Y` (ou esquecer de definir, caindo no `DEV_DEFAULT_KEY`), toda
linha já gravada com `X` passa a dar **`IllegalStateException: Falha ao
decifrar dado sensível`** ao ser lida — **não tem como recuperar o texto
original**. Isso não é teórico: os testes automatizados (`gradlew test`
rodando fora deste ambiente, ou o CI) usam o `DEV_DEFAULT_KEY` porque não
setam a env var; se alguém setar `SC_CRYPTO_KEY` só em produção e nunca em
dev/teste, tudo bem (bancos são diferentes) — o problema é **trocar a chave
no mesmo banco**, ou usar chaves diferentes em réplicas do mesmo banco.

**Garanta o mesmo valor:** gere a chave **uma vez**
(`openssl rand -base64 32`) e salve num cofre (ex.: no gerenciador de
segredos da plataforma de deploy escolhida). Use o **mesmo valor literal**
na env var `SC_CRYPTO_KEY` em todo ambiente que lê o mesmo banco de dados.
Nunca gere uma chave nova "porque sim" com dados já gravados.

---

## 7. Status de cada requisito (verificado, não de memória)

### Funcionais
| ID | Status | Onde / por quê |
|---|---|---|
| RF‑01 | ✅ pronto | `InstitutionController`/`SchoolClassController` + `pages/Admin` |
| RF‑02 | ✅ pronto | `AdminUserController` + `pages/Admin/UsersPage.jsx`; CPF único e validado |
| RF‑03 | ✅ pronto | `PsychologistPanelController`; DTO só tem contadores/flags (RN‑03) |
| RF‑04 | ✅ pronto | `OrientationAlertService.onJourneyCompleted`, dispara quando `inDoubt=true` |
| RF‑05 | ✅ pronto | filtro `onlyInDoubt` no painel + CTA de agendar sessão em grupo |
| RF‑06 | ✅ pronto | `CuratedLinkController` + `/curated-links/for-me` filtrado por instituição |
| RF‑07 | ✅ pronto (CSV, não PDF — ver 5.7) | `ReportController` |
| RF‑08 | ⚠️ **parcial** | `.ics` + notificação mock, **sem** integração real com Google Calendar (ver 5.3) |
| RF‑09 | ✅ pronto | `FeedbackController`, texto cifrado |
| RF‑10 | ✅ pronto (modelagem própria — ver 5.2) | `JourneyController`/`JourneyService`; Síntese é fase derivada, não linha em `tb_trilha` |
| RF‑11 | ✅ pronto | `saveTrailSynthesis` recusa (422) trilha com perguntas incompletas ou texto vazio |
| RF‑12 | ✅ pronto | `CareerSuggestionService`, sempre ≥ 1 área, disclaimer explícito |
| RF‑13 | ✅ pronto | `GET /journeys/{id}/panorama` |
| RF‑14 | ✅ pronto | `StudentProfileController`, bio cifrada |
| RF‑15 | ✅ pronto | `GET /journeys/history`; confirmei no banco que "maria" já está no ciclo 2 |
| RF‑16 | ✅ pronto | `/appointments/mine`, `/feedbacks/mine`, `pages/Aluno/AgendaPage.jsx` |

### Regras de negócio
| ID | Status | Nota |
|---|---|---|
| RN‑01 | ✅ pronto | gating server‑side em `JourneyService` (testei: trilha bloqueada → 403) |
| RN‑02 | ✅ pronto | síntese obrigatória, validada no servidor |
| RN‑03 | ✅ pronto | painel e relatório de turma não têm nenhum campo de resposta |
| RN‑04 | ✅ pronto | alerta sempre criado com `priority = "GRUPO"` |
| RN‑05 | ✅ pronto | `/curated-links/for-me` |
| RN‑06 | ✅ pronto (por design) | escrita de trilhas/perguntas restrita a ADMIN/PSICOLOGA em `SecurityConfigV2` |
| RN‑07 | ✅ pronto | leque sempre com múltiplas áreas + texto de disclaimer no DTO |
| RN‑08 | ✅ pronto | `PanoramaDTO` traz respostas + sínteses + síntese final |
| RN‑09 | ⚠️ **parcial** | perguntas do seed são fixas (ok); catálogo de carreiras tem campo "validado" mas **sem tela pra psicóloga validar** (ver seção 8) |

### Não funcionais
| ID | Status | Nota |
|---|---|---|
| RNF‑01 | ✅ pronto | `@PreAuthorize` confirmado nos 10 controllers novos (grep) |
| RNF‑02 | ⚠️ **parcial** | ver seção 6 — 5 campos novos cifrados; `tb_respostas_aluno` legado em claro (mas hoje inatingível pela UI e com 0 linhas) |
| RNF‑03 | ➖ **não verificado** | sem teste de carga; `PsychologistPanelService.panel()` e `JourneyService.view()` fazem N+1 (uma query por aluno/trilha) — em localhost é instantâneo, com banco remoto e turma grande pode passar de 2s |
| RNF‑04 | ✅ pronto | CSS puro, `@media` nas 3 áreas novas, só variáveis `--sc-*` |
| RNF‑05 | ➖ fora do escopo de código | depende da infra de deploy |
| RNF‑06 | ✅ pronto | camadas controller/service/repository/dto seguidas em todo código novo |
| RNF‑07 | ✅ pronto | confirmei: **33 endpoints novos** aparecem em `/v3/api-docs` com `@Operation` em português + `API_CONTRACT_V2.md` |
| RNF‑08 | ⚠️ **parcial** | isolamento por instituição pronto nas features novas (`PsychologistScope`); os endpoints **legados** (`GET /students`, `GET /users`) agora exigem token mas não filtram por instituição — não dá pra editar esses controllers |

### Validações de campo (seção 7 do documento de requisitos)
CPF obrigatório/único ✅ · CNPJ obrigatório/único ✅ (ambos com dígito
verificador, `DocumentoValidator.java`) · E‑mail formato ✅ (`@Email` nos
DTOs) · Telefone opcional ✅ · Data sem permitir inválida ✅
(`datetime-local` + validação de intervalo em `SessionService`).

---

## 8. O que falta (em ordem de prioridade)

1. **Deploy** — nada está publicado ainda. Ver seção 9. Estimativa: depende
   muito da plataforma escolhida (ver `DEPLOY.md`, se existir — checar antes
   de assumir que não existe).
2. **Tela/endpoint da psicóloga para validar itens do catálogo de carreiras**
   (RN‑09) — o modelo já tem `CareerArea.validated`/`validatedBy`; falta
   `PATCH /career-catalog/{id}/validate` + uma aba na `PsicologaLayout`.
   Estimativa: 3–4 h.
3. **Exclusão total de dados do aluno mediante solicitação** (RNF‑02, LGPD)
   — hoje não existe endpoint que apague jornadas/respostas/sínteses/perfil
   de um aluno. Precisa cuidado com a ordem de exclusão por causa das FKs
   (`tb_resposta_jornada` → `tb_jornada`, `tb_sintese_trilha` → `tb_jornada`,
   etc.). Estimativa: 4–6 h.
4. **Resolver o N+1 do painel e do `JourneyService.view()`** (RNF‑03) — só
   importa em produção com banco remoto e turmas grandes. Estimativa: 3–4 h.
5. **Cobertura de testes dos services novos** — hoje só existem os 4 testes
   de criptografia + o `contextLoads` padrão. `JourneyService` (gating),
   `AdminUserService` (CPF/e‑mail duplicado) e `PsychologistScope`
   (isolamento) são os candidatos mais importantes. Estimativa: 1–2 dias
   pra cobertura decente.
6. **Corrigir o cadastro público de psicóloga** (item 5.6/5.1) — criar uma
   `CadastroPageV2.jsx` que usa `authServiceV2.register` em vez da
   `CadastroPage.jsx` legada, e trocar a rota `/cadastro` em `AppRoutes2.jsx`
   pra apontar pra ela (isso é editar um arquivo **meu**, `AppRoutes2.jsx`,
   não um legado — permitido). Sem isso, o único jeito confiável de criar
   psicóloga é pelo painel do Admin. Estimativa: 1–2 h.
7. **RNF‑08 nos endpoints legados** — filtrar `/students`, `/users` por
   instituição não é possível sem editar os controllers legados; se isso
   virar requisito obrigatório, precisa de uma decisão explícita do usuário
   sobre relaxar a regra de "não editar" só pra esse ponto, ou aceitar como
   limitação permanente.

---

## 9. Próximo passo: DEPLOY

**Verifiquei: não existe `DEPLOY.md` neste repositório.** Se uma sessão
futura encontrar um, ele foi criado depois deste handoff — leia‑o em vez
desta seção.

O plano discutido com o usuário (fora desta sessão de código, numa conversa
de análise de viabilidade) foi: **Vercel** (front) + **Render** via
Dockerfile (API) + **Aiven** (MySQL) + **UptimeRobot** (ping anti‑cold‑start),
tudo free tier, sem cartão. **Nenhum desses três serviços foi configurado
ainda — nem existe Dockerfile, nem `vercel.json`, nem conta criada por
mim.** Duas ressalvas importantes que já foram levantadas nessa análise e
que a próxima sessão não deve redescobrir do zero:

- **Render hoje exige cartão de crédito no cadastro** mesmo para o free
  tier (mudança de política da plataforma) — isso pode invalidar a premissa
  de "sem cartão". Confirmar o estado atual da política antes de prosseguir.
- **Alternativas discutidas e não implementadas:** AWS free tier (12 meses,
  via Terraform) e Oracle Cloud "Always Free" (sem limite de tempo, mas
  exige cartão de verificação). Nenhuma delas foi iniciada.

### Variáveis de ambiente necessárias

O `application.properties` (legado, comum aos perfis) já tem overrides por
env var pra estas: `APP_PROFILE`, `CLIENT_ID`, `CLIENT_SECRET`,
`JWT_DURATION`, `CORS_ORIGINS`. O `application-prod.properties` (legado) tem
o datasource **hardcoded** para uma RDS que não existe mais — variáveis de
ambiente do Spring Boot têm precedência sobre o valor do arquivo de
properties, então basta definir as variáveis abaixo; não precisa (nem pode)
editar o arquivo.

**Backend (Render ou equivalente):**
| Variável | Exemplo | Serve para |
|---|---|---|
| `APP_PROFILE` | `prod` | ativa `application-prod.properties` |
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<host-aiven>:<porta>/synccarreiradb?sslMode=REQUIRED` | sobrescreve o endpoint morto do arquivo legado |
| `SPRING_DATASOURCE_USERNAME` | `<usuário Aiven>` | idem |
| `SPRING_DATASOURCE_PASSWORD` | `<senha Aiven>` | idem |
| `SC_CRYPTO_KEY` | saída de `openssl rand -base64 32` | **criptografia RNF‑02 — ver seção 6. Gerar uma vez, nunca trocar depois de gravar dados.** |
| `CORS_ORIGINS` | `https://<seu-app>.vercel.app` | libera o front hospedado |
| `SERVER_PORT` | depende da plataforma | a app escuta 8080 fixo hoje; o Render injeta a porta via `$PORT` — **o Dockerfile (a ser criado) precisa mapear isso**, ex. `ENTRYPOINT ["sh","-c","java -jar app.jar --server.port=${PORT:-8080}"]` |
| `JAVA_TOOL_OPTIONS` | `-XX:MaxRAMPercentage=55 -XX:+UseSerialGC -Xss512k` | Render free = 512 MB; a JVM sem isso pode passar do limite (a API mediu ~387 MB ociosa nesta sessão, sem essas flags) |

**Frontend (Vercel):**
| Variável | Exemplo | Serve para |
|---|---|---|
| `VITE_API_URL` | `https://<sua-api>.onrender.com` | base URL do axios. **Precisa ser setada no dashboard da Vercel** — o `.env.production` (legado, não editável) hoje aponta pra `https://synccarreira.duckdns.org`, que não existe mais; uma env var real do ambiente de build tem prioridade sobre esse arquivo no Vite, então funciona sem editar nada. |

### ⚠️ `api.js` — `timeout: 10000` (não editável)

`Source/synccarreira-frontend/src/services/api.js` tem `timeout: 10000` (10
segundos) fixo na instância do axios usada por **quase todas** as chamadas
autenticadas (login usa `axios` cru sem timeout pro `/oauth2/token`, mas o
`GET /users/me` seguinte já usa a instância `api` com os 10s). Confirmei: é
o **único** timeout explícito no frontend inteiro.

**Por que isso importa pro deploy:** Render free **dorme depois de ~15 min
sem tráfego** e o primeiro request após dormir leva **30–60 s** pra
responder (cold start da JVM). Isso estoura os 10 s do axios. O
`api.js` tem um interceptor de resposta que, em **qualquer** erro 401, faz
`localStorage.removeItem('token')` + `window.location.href = '/login'` — um
timeout na restauração de sessão (`AuthContext.jsx` legado chama
`authService.me()` no mount se houver token salvo) não é tecnicamente um
401, mas o `catch` do `AuthContext` já remove o token sozinho, e o usuário
cai na tela de login sem explicação. **O UptimeRobot mitiga isso mantendo a
API acordada, mas não elimina o risco** — se o ping falhar uma vez ou o
intervalo (mínimo 5 min no free) coincidir mal com o sono de 15 min, alguém
vai ver essa falha. Isso **não pode ser corrigido no código** sem editar
`api.js` (proibido); é uma limitação a documentar para quem for demonstrar o
sistema.

### Migração do banco pro Aiven

Mesmos comandos da seção 3, só trocando o host:
```powershell
mysql -h <host-aiven> -P <porta> -u <usuario> -p --default-character-set=utf8mb4 <banco> -e "SOURCE .../import.sql"
# e o mesmo pra V2..V5. Aiven MySQL geralmente exige SSL — some --ssl-mode=REQUIRED
# se o cliente mysql reclamar.
```

---

## 10. Armadilhas conhecidas (economize horas — leia antes de mexer)

1. **`gradlew test` falha no path do OneDrive** (acento) — não é bug no
   código, é o test worker do Gradle. `compileJava`/`bootRun` funcionam
   normal. Rode `test` num espelho sem acento (seção 3).
2. **A API gera um par de chaves RSA novo a cada `bootRun`**
   (`AuthorizationServerConfig.generateRsaKey()`, arquivo legado) — todo
   restart invalida **todos** os tokens JWT emitidos antes. Depois de
   reiniciar o backend em dev, sempre `localStorage.clear()` no navegador
   antes de logar de novo, senão a sessão "restaura" com token inválido e
   você cai num loop de redirect pro login (ver item 9, mesmo mecanismo).
3. **`import.sql` corrompe acentos se carregado via pipe do PowerShell**
   (`Get-Content -Raw | mysql`) — o PowerShell lê como ANSI, "João" vira
   "JoÃ£o" no banco. Sempre usar
   `mysql --default-character-set=utf8mb4 <banco> -e "SOURCE <path>"`, **e**
   o `SOURCE` do MySQL não lê path com acento — copie o `.sql` pra um path
   ASCII antes (ex. uma pasta temp) se o caminho do projeto tiver acento.
4. **`create.sql` tem o schema + seed duplicados de ponta a ponta** (o
   arquivo inteiro se repete uma vez). Rodar o arquivo inteiro duplica o
   seed quando somado ao `import.sql`. A solução usada: rodar só as **22
   primeiras linhas** (puro DDL) e depois o `import.sql` separado (seção 3).
5. **Rota nova sem entrada em `SecurityConfigV2.MATCHED_PATHS`** cai na
   cadeia de segurança legada, que nega (403) qualquer coisa fora da lista
   dela — mesmo autenticado. Se um controller novo "não funcionar" com 403
   estranho, primeiro confira esse array antes de desconfiar de
   `@PreAuthorize`.
6. **Endpoint `/error` do Spring precisou ser adicionado ao matcher da
   `SecurityConfigV2`** com `permitAll` — sem isso, um 404/500 genuíno de
   rota nova virava um 403 mascarado (o dispatch de erro reentra no filtro
   de segurança, caía na cadeia legada, que nega tudo). Já está corrigido no
   arquivo; só documentando o porquê, caso pareça estranho ter `/error` na
   lista.
7. **Nunca insira uma linha `SINTESE` em `tb_trilha`** — quebra a
   desserialização do enum `TrailName` legado no `GET /trails`. Ver seção 5.2.
8. **Playwright MCP instável nesta sessão** — caiu e reconectou várias
   vezes; quando conectado, os snapshots de acessibilidade às vezes vinham
   vazios/desatualizados logo após uma navegação, dando a falsa impressão de
   que cliques não funcionavam. Contorno que funcionou 100%: instalar
   `playwright-core` (mesma major do Chromium já baixado em
   `%LOCALAPPDATA%\ms-playwright\`) num diretório de scratch e dirigir um
   Chromium headless via script Node próprio, sem depender do MCP.
9. **`node_modules` do frontend tem ~57 MB** — se for versionar o projeto,
   um `.gitignore` (o das subpastas já cobre isso; se criar um na raiz,
   replicar `node_modules`, `dist`, `build/`, `.gradle`, `.idea`).
10. **Duas `@ControllerAdvice` coexistindo** —
    `ControllerExceptionHandlerV2` (`@Order(0)`, meu) e o legado (sem
    `@Order`, portanto menor prioridade). Tratam tipos de exceção
    diferentes, não competem, mas se criar uma exceção nova pense em qual
    handler ela deveria cair.
