# Contrato de API — SyncCarreira V2

> Complementa o `API_CONTRACT.md`. Documenta **apenas os endpoints novos**
> (Blocos 1–6). Nada do contrato original foi alterado; os endpoints legados
> continuam funcionando, mas agora **exigem autenticação** (ver seção 0).
>
> Toda a API nova também está documentada automaticamente no Swagger:
> `http://localhost:8080/swagger-ui/index.html` (RNF-07).

---

## 0. Mudanças na segurança (Bloco 1)

Uma nova cadeia de segurança (`SecurityConfigV2`, `@Order(HIGHEST_PRECEDENCE)`)
cobre os prefixos novos **e** os endpoints legados sensíveis
(`/users`, `/students`, `/psychologists`, `/trails`, `/questions`, `/answers`).

| Regra | Efeito |
|---|---|
| `POST /users`, `POST /students`, `POST /psychologists` | continuam **públicos** (auto‑cadastro) |
| `GET /users/me`, `GET /trails`, `GET /questions/**`, `GET/POST /answers` | agora exigem **token** |
| `POST/PUT/DELETE /trails`, `/questions` | exigem papel **ADMIN** ou **PSICOLOGA** (RN‑06) |
| `PUT/DELETE /users`, `/students`, `/psychologists` | exigem papel **ADMIN** |
| Conta desativada | qualquer requisição autenticada → `403 {"error":"Conta desativada..."}` |

**Papéis** (após migração `V2__fundacao.sql`):

| roleId | authority | perfil |
|---|---|---|
| 1 | `ROLE_USER` | aluno |
| 2 | `ROLE_ADMIN` | administrador |
| 3 | `ROLE_PSICOLOGA` | psicóloga |

Formato de erro (igual ao legado): `{ "timestamp", "status", "error", "path" }`.
Status usados: `400`, `403` (papel/instituição), `404`, `409` (unicidade), `422` (regra de negócio / validação).

---

## 1. Administrador

### 1.1 Instituições — `/institutions` (RF‑01) · papel **ADMIN**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/institutions` | lista |
| `GET` | `/institutions/{id}` | detalhe |
| `POST` | `/institutions` | cria — CNPJ validado (dígito verificador) e único |
| `PUT` | `/institutions/{id}` | edita / ativa‑desativa |
| `DELETE` | `/institutions/{id}` | exclui (só sem turmas → senão `409`) |

`POST/PUT` body:
```json
{ "legalName": "Colégio Santa Rita", "tradeName": "CSR",
  "cnpj": "34.238.864/0001-68", "type": "ESCOLA", "active": true }
```
`type`: `ESCOLA` | `ONG`. `active` só no `PUT`. CNPJ aceita com ou sem máscara.

Resposta:
```json
{ "id": 2, "legalName": "...", "tradeName": "...", "cnpj": "34238864000168",
  "type": "ESCOLA", "active": true, "classCount": 1, "createdAt": "..." }
```

### 1.2 Turmas — `/classes` (RF‑01) · papel **ADMIN**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/classes?institutionId=` | lista (filtro opcional) |
| `GET` | `/classes/{id}` | detalhe |
| `POST` | `/classes` | cria vinculada a uma instituição |
| `PUT` | `/classes/{id}` | edita / troca psicóloga / ativa‑desativa |
| `DELETE` | `/classes/{id}` | exclui |

`POST` body: `{ "name":"3º Ano A", "schoolYear":2026, "institutionId":2, "psychologistId": 5 }`
`psychologistId` opcional; se informado, precisa ser um usuário `ROLE_PSICOLOGA`.

### 1.3 Gestão de usuários — `/admin/users` (RF‑02) · papel **ADMIN**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/admin/users?perfil=&institutionId=&active=` | lista com filtros |
| `GET` | `/admin/users/{id}` | detalhe |
| `POST` | `/admin/users` | cria aluno ou psicóloga — CPF validado e único |
| `PUT` | `/admin/users/{id}` | edita (sem trocar o perfil) |
| `PATCH` | `/admin/users/{id}/status` | ativa / **desativa** conta |

`POST` body:
```json
{ "name":"Maria Aluna", "email":"maria@escola.com", "password":"12345678",
  "cpf":"390.533.447-05", "perfil":"ALUNO",
  "institutionId":2, "schoolClassId":2,
  "schoolYear":"3º Ano - Ensino Médio", "schoolType":"Pública",
  "crp": null, "contractExpirationDate": null }
```
`perfil`: `ALUNO` | `PSICOLOGA`. `crp` + `contractExpirationDate` obrigatórios para psicóloga.
`PATCH .../status` body: `{ "active": false, "reason": "Transferência de escola" }`.
Não é possível desativar administrador (`422`).

---

## 2. Aluno — Jornada

### 2.1 Perfil — `/student-profile` (RF‑14) · papel **USER**

| Método | Rota |
|---|---|
| `GET` | `/student-profile` — perfil + catálogo de interesses |
| `PUT` | `/student-profile` — atualiza |

`PUT` body: `{ "bio":"...", "phone":"...", "city":"...", "interestIds":[4,8,18] }`
`bio` é armazenada **cifrada** (RNF‑02). Resposta traz `allInterests: [{id,name}]`.

### 2.2 Jornada — `/journeys` (RF‑10, RF‑11, RF‑13, RF‑15) · papel **USER**

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/journeys/current` | jornada em andamento (cria uma se não houver) |
| `GET` | `/journeys/{id}` | panorama de progresso |
| `GET` | `/journeys/{id}/trails/{trailId}` | perguntas + respostas + síntese da trilha (só se **liberada** — RN‑01) |
| `PUT` | `/journeys/{id}/answers` | salva/atualiza resposta de uma pergunta |
| `PUT` | `/journeys/{id}/trails/{trailId}/synthesis` | registra a síntese → conclui a trilha e libera a próxima (RN‑02) |
| `GET` | `/journeys/{id}/final` | etapa de Síntese: sínteses consolidadas + leque de carreiras (só com as 4 trilhas concluídas) |
| `POST` | `/journeys/{id}/complete` | conclui a jornada com síntese final + sinalização de dúvida |
| `GET` | `/journeys/{id}/panorama` | histórico completo de respostas e sínteses (RF‑13, RN‑08) |
| `GET` | `/journeys/history` | jornadas de ciclos anteriores (RF‑15) |

`GET /journeys/current` →
```json
{ "journeyId":1, "cycle":1, "status":"EM_ANDAMENTO", "inDoubt":null,
  "finalPhaseAvailable":false,
  "trails":[
    {"trailId":1,"name":"AUTOCONHECIMENTO","order":1,"status":"LIBERADA","answered":0,"total":10,"hasSynthesis":false},
    {"trailId":2,"name":"INFLUENCIAS","order":2,"status":"BLOQUEADA","answered":0,"total":10,"hasSynthesis":false},
    {"trailId":4,"name":"INFORMACAO","order":3,"status":"BLOQUEADA","answered":0,"total":0,"hasSynthesis":false},
    {"trailId":3,"name":"PLANO_DE_FUTURO","order":4,"status":"BLOQUEADA","answered":0,"total":10,"hasSynthesis":false}
  ] }
```
> Ordem das trilhas conforme o **Documento de Requisitos** (decisão C2):
> Autoconhecimento → Influências → Informação → Projeto de Futuro → **Síntese** (etapa final derivada, decisão C3‑B).

`PUT .../answers` body: `{ "questionId": 12, "optionId": 34, "content": null }`
(pergunta `ABERTA` → `optionId: null`, `content: "texto"`).

`PUT .../synthesis` body: `{ "text": "O que essa trilha me fez pensar..." }`
→ `422` se nem todas as perguntas foram respondidas; `403` se a trilha estiver bloqueada.

`POST .../complete` body: `{ "finalSynthesis": "...", "inDoubt": true }`
→ se `inDoubt=true`, gera automaticamente um alerta de orientação com prioridade **GRUPO** (RF‑04, RN‑04).

`GET .../final` →
```json
{ "journeyId":2, "completed":true, "finalSynthesis":"...", "inDoubt":true,
  "trailSyntheses":[{"order":1,"trailName":"AUTOCONHECIMENTO","text":"..."}, ...],
  "careerSuggestions":[{"area":"EXATAS","title":"Ciência de Dados","type":"CARREIRA","description":"..."}, ...],
  "disclaimer":"Este leque é um ponto de partida... não um diagnóstico fechado (RN-07)." }
```
O leque reúne itens das **3 áreas de maior afinidade** (nunca uma só — RN‑07).

### 2.3 Links da trilha Informação — `GET /curated-links/for-me` (RN‑05) · papel **USER**
Retorna os links ativos globais + os da instituição do aluno.

### 2.4 Agenda e feedbacks do aluno (RF‑16) · papel **USER**

| Método | Rota |
|---|---|
| `GET` | `/appointments/mine` — sessões do aluno |
| `GET` | `/appointments/{id}/ics` — baixa o convite `.ics` |
| `GET` | `/feedbacks/mine` — feedbacks recebidos |

---

## 3. Psicóloga

Papel **PSICOLOGA** (ou **ADMIN**). Todo o escopo é limitado aos alunos da
**mesma instituição** da psicóloga (RNF‑08).

### 3.1 Painel — `/psychologist-panel` (RF‑03, RF‑04, RF‑05)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/psychologist-panel?onlyInDoubt=` | status por aluno + alertas abertos |
| `POST` | `/psychologist-panel/alerts/{id}/resolve` | resolve um alerta |

Resposta:
```json
{ "totalStudents":2, "completedJourneys":0, "inDoubtStudents":1, "openAlerts":1,
  "students":[
    {"studentId":4,"studentName":"Maria Aluna","institutionName":"...","className":"3A",
     "cycle":2,"journeyStatus":"EM_ANDAMENTO","concludedTrails":0,"totalTrails":4,
     "inDoubt":null,"needsGuidance":true}
  ],
  "alerts":[
    {"alertId":1,"studentId":4,"studentName":"Maria Aluna","className":"3A",
     "reason":"EM_DUVIDA","priority":"GRUPO","journeyId":2,"resolved":false,"createdAt":"..."}
  ] }
```
> **RN‑03:** o `students[]` traz apenas progresso e sinalizações — nunca o
> conteúdo das respostas individuais.

### 3.2 Curadoria de links — `/curated-links` (RF‑06, RN‑05)

| Método | Rota |
|---|---|
| `GET` | `/curated-links` — links da psicóloga logada |
| `GET` | `/curated-links/institutions` — instituições que ela pode associar |
| `POST` | `/curated-links` |
| `PUT` | `/curated-links/{id}` |
| `DELETE` | `/curated-links/{id}` |

`POST` body:
```json
{ "title":"Portal do Enem", "url":"https://enem.inep.gov.br",
  "description":"Inscrições e cronograma", "category":"ENEM", "institutionId": null }
```
`category`: `ENEM` | `PROUNI` | `SISU` | `COTAS` | `CURSOS` | `OUTRO`.
`institutionId: null` = link global (todas as instituições).

### 3.3 Sessões — `/appointments` (RF‑08)

| Método | Rota |
|---|---|
| `GET` | `/appointments` — sessões criadas pela psicóloga |
| `POST` | `/appointments` |
| `PUT` | `/appointments/{id}` — editar, remarcar, cancelar |
| `DELETE` | `/appointments/{id}` |
| `GET` | `/appointments/{id}/ics` — convite iCalendar |

`POST` body:
```json
{ "type":"GRUPO", "title":"Roda de conversa - indecisos",
  "description":"...", "start":"2026-10-01T14:00:00", "end":"2026-10-01T15:00:00",
  "location":"Sala 3", "studentIds":[4,6] }
```
`type`: `INDIVIDUAL` (exatamente 1 aluno) | `GRUPO`.
`PUT` inclui `status`: `AGENDADA` | `REALIZADA` | `CANCELADA`.
Ao criar/editar, os alunos são notificados (log — mock de e‑mail, decisão C5) e o
`.ics` fica disponível em `/appointments/{id}/ics` (abre no Google Calendar/Outlook).

### 3.4 Feedback — `/feedbacks` (RF‑09)

| Método | Rota |
|---|---|
| `POST` | `/feedbacks` |
| `GET` | `/feedbacks/sent` — enviados pela psicóloga |
| `GET` | `/feedbacks/student/{id}` — de um aluno |

`POST` body: `{ "studentId":4, "journeyId":2, "sessionId":null, "text":"..." }`
(`journeyId` e `sessionId` opcionais). Texto armazenado **cifrado** (RNF‑02).

### 3.5 Relatórios — `/reports` (RF‑07)

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/reports/class?classId=` | CSV geral da turma (**sem respostas** — RN‑03) |
| `GET` | `/reports/student/{studentId}` | CSV individual com sínteses e status |

Resposta: `text/csv; charset=UTF-8` com BOM (abre direto no Excel),
`Content-Disposition: attachment`.

---

## Notas

- **RNF‑02 (criptografia):** aplicada aos campos sensíveis **novos** — sínteses
  de trilha, síntese final, bio do perfil, feedback e as respostas da nova
  jornada (`AES‑256/GCM`, prefixo `enc:v1:` no banco). O armazenamento legado
  (`tb_respostas_aluno.conteudo`) permanece em texto claro (código legado não
  editável). Chave via env `SC_CRYPTO_KEY` (Base64).
- **RNF‑08 (isolamento):** derivado de `tb_conta.fk_instituicao`. Psicóloga só
  enxerga alunos da própria instituição; aluno A nunca acessa jornada do aluno B.
- **RNF‑07 (documentação):** `@Operation` em português em todos os endpoints
  novos + este arquivo + Swagger.
