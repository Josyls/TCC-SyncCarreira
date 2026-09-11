# HANDOFF V2 — SyncCarreira (atualizações + deploy)

> Este arquivo é um **complemento** do `HANDOFF.md` original (escrito em
> 2026‑09‑11, antes do deploy existir). Ele não repete o que já está lá —
> arquitetura, decisões técnicas 5.1–5.8, status de cada requisito,
> criptografia — **leia o `HANDOFF.md` primeiro** se for a sua primeira vez
> neste projeto. Este arquivo cobre só o que aconteceu **depois** dele: o
> projeto foi de "roda só localmente" pra **deployado e funcionando de
> verdade na nuvem**, e existe agora um runbook pra você (ou uma sessão
> futura) atualizar páginas/classes sem precisar redescobrir tudo isso.
>
> Toda informação abaixo foi **verificada ao vivo** contra o Render, a
> Vercel e o GitHub nesta sessão (não é memória). Data: **2026‑09‑11**.

---

## 1. O que mudou desde o HANDOFF.md original

Continua valendo 100% a regra inviolável da seção 0 do `HANDOFF.md`:
nenhum arquivo pré-existente foi editado, só a 1 linha já autorizada em
`App.jsx`. Tudo abaixo é **arquivo novo**, adicionado depois do commit
inicial (`09fc165`):

| Arquivo novo | Por quê existe |
|---|---|
| `Source/synccarreira-api/Dockerfile` | Render não tem runtime nativo pra Java — só roda apps em Docker. Empacota exatamente o `./gradlew build -x test` + `java -jar` que já funcionavam localmente, sem tocar em nenhum `.java`/`.gradle`. |
| `Source/synccarreira-api/.dockerignore` | evita mandar `build/`, `.gradle/`, `.idea/` etc. pro contexto de build do Docker |
| `Source/synccarreira-frontend/vercel.json` | diz à Vercel pra tratar rotas desconhecidas como `index.html` (senão o React Router quebra com F5 numa rota tipo `/aluno/jornada`, dando 404 da Vercel em vez de deixar o React decidir) |
| `.gitignore` (raiz) | cobre o que sobra na raiz do monorepo (inclui agora `.vercel/`, ver item 4 abaixo) |
| `HANDOFF.md`, `ENTREGA_V2.md` | (já existiam antes deste V2, não são novidade) |
| `RESUMO_PARA_VICTOR.txt` | resumo auditável pro colega que fez o legado — o que mudou, pontos de atenção no código dele, como reverter |
| `HANDOFF_V2.md` | este arquivo |

Nada do backend/frontend em si mudou nesta etapa — **só infraestrutura de
deploy**. O código das seções 4–8 do `HANDOFF.md` está exatamente como
estava.

---

## 2. Estado atual — o que está no ar agora

| Camada | URL | Plataforma | Verificado como |
|---|---|---|---|
| Frontend | **https://synccarreira-frontend.vercel.app** | Vercel (free/hobby) | HTTP 200, bundle carrega, login real funciona ponta a ponta |
| Backend | **https://tcc-synccarreira.onrender.com** | Render (free) | login OAuth2 real retorna JWT válido, `/users/me` retorna dado real |
| Banco | Aiven MySQL 8.4.8 (host `mysql-31255459-synccarreira-db.g.aivencloud.com`) | Aiven (free tier) | schema completo aplicado (create.sql 22 linhas + import.sql + V2..V5), consultado ao vivo |
| Repositório | https://github.com/Josyls/TCC-SyncCarreira | GitHub (público) | branch `master`, 4 commits |

Teste de login real que rodei pra confirmar (credencial semeada no
`import.sql` legado, senha de demonstração `12345678`):
```
POST https://tcc-synccarreira.onrender.com/oauth2/token
  grant_type=password&username=joao@gmail.com&password=12345678
  Authorization: Basic base64(synccarreira-front-id:synccarreira-project-2026)
→ 200, access_token real (JWT RS256)

GET https://tcc-synccarreira.onrender.com/users/me
  Authorization: Bearer <token>
→ 200 {"email":"joao@gmail.com","id":1,"name":"João Silva","roles":[{"authority":"ROLE_USER"...}]}
```
Isso simula exatamente o fluxo que `authService.js` (frontend) faz — não é
só "o servidor responde", é "o login de verdade funciona".

---

## 3. Como cada peça está conectada (importante pra saber o que vai
   acontecer quando você mudar algo)

### Backend (Render) — **auto-deploy LIGADO e funcionando**
```
autoDeploy: "yes"   autoDeployTrigger: "commit"   branch: "master"
dockerfilePath: "./Source/synccarreira-api/Dockerfile"
dockerContext:  "./Source/synccarreira-api"
```
**Isso significa: todo `git push` pra `master` que toque em algo dentro de
`Source/synccarreira-api/` dispara sozinho um rebuild + redeploy no
Render.** Não precisa mexer em nada manualmente — é só dar push. Confirmei
isso consultando a API do Render diretamente (`GET /v1/services`), não é
suposição.

### Frontend (Vercel) — **auto-deploy do GitHub NÃO está confiável no
   monorepo; use o deploy manual via CLI**
O projeto na Vercel está conectado ao mesmo repositório GitHub
(`link.type: "github"`, `productionBranch: "master"`), e a Vercel TENTA
fazer deploy automático a cada push — **mas eu testei isso ao vivo e o
build automático falha** (`npm run build exited with 254`, `ENOENT`),
porque o repositório é um monorepo (o `package.json` do front está em
`Source/synccarreira-frontend/`, não na raiz) e configurar o "Root
Directory" do projeto pra apontar pra lá **quebra o outro caminho de
deploy** (o deploy manual via CLI, que já roda de dentro da própria pasta
do front — ver por quê nas Armadilhas, seção 6.2). Tive que escolher um
dos dois caminhos, e escolhi manter o que já está **comprovadamente
funcionando**: deploy manual via CLI.

**Conclusão prática: um `git push` sozinho NÃO atualiza o frontend
publicado.** Pra publicar uma mudança de página, rode o comando da seção 4.2.

---

## 4. Runbook — "eu mudei algo, como isso vai pro ar?"

### 4.1 Mudei uma classe Java / controller / migração SQL nova
1. Edite/crie o arquivo (lembrando: só arquivo novo, ou os que eu já criei
   nesta sessão — nunca um `.java` legado).
2. Se criou uma tabela nova, crie uma migração `.sql` nova em
   `Source/synccarreira-api/db/` (siga o padrão `V6__nome.sql`) — **ela não
   roda sozinha**, você vai ter que rodá-la manualmente contra o banco da
   Aiven, do mesmo jeito que a seção 3 do `HANDOFF.md` explica pro banco
   local (só trocando host/porta/usuário — pegue os valores reais no
   dashboard da Render, aba Environment do serviço `TCC-SyncCarreira`, ou
   no dashboard da Aiven; **não estão escritos aqui de propósito**, ver
   seção 5).
3. `git add`, `git commit`, `git push origin master`.
4. Pronto — o Render detecta o commit sozinho e faz rebuild automático
   (leva ~2–4 min: build da imagem Docker + start da JVM). Acompanhe em
   https://dashboard.render.com/web/srv-dai61e0jo6nc73fi34a0 (aba "Logs" ou
   "Events").
5. Depois que o deploy aparecer como "Live", teste:
   ```
   curl -s -o /dev/null -w "%{http_code}" https://tcc-synccarreira.onrender.com/actuator/health
   ```
   (ou qualquer endpoint que exista) — se dormiu por inatividade, a
   primeira chamada demora 30–60s (ver seção 6.4 e o aviso já existente na
   seção 9 do `HANDOFF.md` sobre `api.js timeout: 10000`).

**Nunca use "Manual Deploy → Restart" no dashboard do Render depois de
mudar variável de ambiente — só Restart não aplica env var nova.** Se
precisar mudar uma env var, depois de salvá‑la clique em "Deploy latest
commit" (ou, via API, `POST /v1/services/{id}/deploys` com
`{"clearCache":"clear"}`) — foi um bug real que bati de cara nesta sessão:
atualizei a env var, só dei restart, e a aplicação continuou usando o
banco antigo.

### 4.2 Mudei uma página/componente React
1. Edite/crie o arquivo em `Source/synccarreira-frontend/src/...` (lembrando
   da regra: só `AppRoutes2.jsx` e os arquivos novos do Bloco 1–6 podem ser
   editados livremente; páginas legadas continuam proibidas).
2. Rode local pra conferir (`npm run dev`) se quiser.
3. Publique com o comando abaixo — rode **de dentro da pasta do frontend**:
   ```powershell
   cd Source/synccarreira-frontend
   npx vercel@latest deploy --prod --yes --scope josyls-projects
   ```
   Vai pedir login na primeira vez (`npx vercel@latest login`) se você não
   tiver um token configurado. Se tiver um token da Vercel, pode passar
   `--token <seu-token>` direto no comando, sem precisar logar
   interativamente.
4. O comando já baixa e builda com a variável `VITE_API_URL` certa
   automaticamente — **ela está salva no projeto da Vercel** (Production +
   Preview), não precisa passar `-b VITE_API_URL=...` manualmente toda vez
   (eu configurei isso nesta sessão; antes disso, cada deploy exigia passar
   a flag na mão, o que é fácil de esquecer e faria o site voltar a apontar
   pro `localhost:8080` do fallback).
5. Ao final ele imprime uma URL "Production" tipo
   `https://synccarreira-frontend-xxxxx-josyls-projects.vercel.app` — o
   domínio fixo **https://synccarreira-frontend.vercel.app** é
   automaticamente re-apontado (alias) pra essa nova versão, não precisa
   fazer nada extra.
6. Um `git push` **também** é recomendado (pra manter o histórico/backup no
   GitHub e pro Victor poder auditar), mas ele sozinho **não** publica a
   mudança — sempre rode o passo 3.

### 4.3 Mudei os dois (full-stack)
Faça 4.1 e 4.2 nessa ordem (backend primeiro) se a mudança no front depende
de um endpoint novo — senão o front publicado vai chamar algo que ainda não
existe no ar por alguns minutos.

---

## 5. Variáveis de ambiente — nomes e onde ficam (SEM valores aqui)

Os valores reais (senha do banco, chave de criptografia, etc.) **não estão
escritos neste arquivo de propósito**, porque este repositório é
**público** no GitHub — colocar um segredo real de produção aqui seria
publicá-lo pro mundo inteiro. Os valores estão só nos dashboards das
próprias plataformas:

**Render** (`dashboard.render.com` → serviço `TCC-SyncCarreira` → aba
"Environment"):
| Variável | Pra quê serve |
|---|---|
| `APP_PROFILE` | ativa `application-prod.properties` |
| `SPRING_DATASOURCE_URL` | endpoint do MySQL na Aiven (sobrescreve o RDS morto do arquivo legado) |
| `SPRING_DATASOURCE_USERNAME` | usuário do MySQL na Aiven |
| `SPRING_DATASOURCE_PASSWORD` | senha do MySQL na Aiven |
| `SC_CRYPTO_KEY` | **chave de criptografia — ver aviso crítico abaixo** |
| `CORS_ORIGINS` | hoje está como `*` (libera qualquer origem — aceitável pra fase de teste com os amigos, mas o ideal depois é trocar pra `https://synccarreira-frontend.vercel.app` exato) |

**Vercel** (`vercel.com` → projeto `synccarreira-frontend` → Settings →
Environment Variables):
| Variável | Pra quê serve |
|---|---|
| `VITE_API_URL` | `https://tcc-synccarreira.onrender.com` — configurada como Production **e** Preview, então tanto `vercel deploy --prod` quanto um deploy de preview já saem com a URL certa |

### ⚠️ Sobre `SC_CRYPTO_KEY` (recapitulando a seção 6 do `HANDOFF.md`,
   porque é fácil esquecer no meio do deploy)
Essa chave já está definida no Render **e já tem dado real gravado com
ela** (bio de aluno, sínteses, respostas abertas, feedback — todos
cifrados AES‑256/GCM). **Nunca troque o valor dessa env var no Render sem
migrar os dados primeiro** — trocar a chave torna tudo que já foi
gravado permanentemente ilegível (`IllegalStateException: Falha ao
decifrar dado sensível`). Se precisar rotacionar por segurança (ela foi
digitada nesta conversa, então tecnicamente ficou exposta), o processo
correto é: decifrar tudo com a chave antiga, recifrar com a nova, só
depois trocar a env var — não é um simples "gerar de novo".

---

## 6. Armadilhas de deploy aprendidas nesta sessão (não redescubra do zero)

### 6.1 Render: `restart` não aplica env var nova, só um `deploy` novo aplica
Já mencionado na seção 4.1. Bati nisso de verdade: atualizei
`SPRING_DATASOURCE_*` via API, dei restart, e o app continuou tentando
conectar no host antigo. Só um "Deploy latest commit" (ou
`POST /v1/services/{id}/deploys`) resolve.

### 6.2 Vercel: "Root Directory" do projeto conflita com deploy manual via CLI num monorepo
Tentei configurar `rootDirectory: "Source/synccarreira-frontend"` no
projeto da Vercel pra fazer o auto-deploy do GitHub funcionar. Isso
**quebrou** o deploy manual via CLI (que já roda de dentro da pasta do
front, então a Vercel tentava aplicar o "Root Directory" *de novo* em cima
de um conteúdo que já era só a pasta do front — dava erro "Root Directory
does not exist"). Reverti pra deixar vazio, porque o deploy manual é o
caminho comprovado e usado agora. Se uma sessão futura quiser mesmo
resolver o auto-deploy do GitHub pro front, o caminho certo é rodar `vercel
link`/`vercel deploy` **a partir da raiz do repositório** (não de dentro da
subpasta), pra que o "Root Directory" e o deploy manual fiquem consistentes
— não tentei isso porque exigiria re-linkar o projeto e teria custo/risco
maior que o benefício agora.

### 6.3 Vercel CLI tenta editar o `.gitignore` legado do front — não deixe
Ao rodar `vercel deploy` de dentro de `synccarreira-frontend/` pela
primeira vez, o CLI tentou acrescentar `.vercel` no `.gitignore` **daquela
pasta**, que é um arquivo legado (não posso editar). Revertido, e a mesma
entrada foi pro `.gitignore` da raiz (arquivo novo) em vez disso. Se você
rodar `vercel deploy` de novo e o git acusar mudança no `.gitignore` do
front, é isso — reverta com
`git checkout -- Source/synccarreira-frontend/.gitignore` antes de commitar.

### 6.4 Render free "dorme" depois de ~15 min sem tráfego
Primeira requisição depois disso demora 30–60s. O `api.js` do frontend
(legado, não editável) tem `timeout: 10000` (10s) fixo — pode estourar
nesse cenário e parecer erro de login quando na verdade é só o servidor
acordando. Já documentado na seção 9 do `HANDOFF.md`; repetindo aqui porque
é a causa mais provável de "parou de funcionar" que alguém vai reportar.
Não tem correção sem editar `api.js` — é limitação a avisar pra quem for
testar.

### 6.5 `gh` (GitHub CLI) precisa ser reautenticado a cada sessão nova
Por hábito de segurança, desloguei o `gh` do terminal depois de cada push
nesta sessão (`gh auth logout`). Se abrir um terminal novo e o
`git push` falhar com "could not read Username", é só isso — rode
`gh auth login --with-token` de novo com um token válido, depois
`gh auth setup-git`, dê o push, e deslogue de novo no final.

---

## 7. Segurança — lembrete (o mesmo de antes, ainda vale)

O repositório é **público** e por isso:
- **NÃO** cole segredos reais em nenhum arquivo `.md`/`.txt` deste repo —
  é por isso que a seção 5 acima só tem nomes de variável, não valores.
- O `application-prod.properties` legado já expõe uma senha de um RDS da
  AWS que **não existe mais** (verificado) — é um risco morto, mas ainda
  aparece se alguém clonar o repo.
- Os segredos reais de produção (senha da Aiven, `SC_CRYPTO_KEY`, tokens de
  API do Render/Vercel/GitHub) foram digitados nesta conversa em algum
  momento — **considere-os potencialmente expostos** e, quando o TCC for
  entregue e você não precisar mais destes ambientes de demonstração,
  revogue/rotacione tudo: token do GitHub, API key do Render, token da
  Vercel, e a senha do MySQL na Aiven (esta última exige recifrar os dados,
  ver aviso da seção 5).

---

## 8. Onde ler mais

- `HANDOFF.md` — arquitetura, decisões técnicas, status de cada requisito,
  criptografia em detalhe, "o que falta". **Continua sendo a fonte
  principal** — este V2 só adiciona a parte de deploy.
- `ENTREGA_V2.md` — resumo mais curto da entrega funcional.
- `RESUMO_PARA_VICTOR.txt` — versão pro colega que fez o legado auditar as
  mudanças e decidir se quer corrigir algo do lado dele.
- `API_CONTRACT_V2.md` (dentro de `synccarreira-frontend`) — todos os
  endpoints novos.
