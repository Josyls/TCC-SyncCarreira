# Contrato de API — SyncCarreira
> Documento atualizado após integração real com o backend.
> Reflete o comportamento confirmado em produção local.

---

## Configuração

| Item | Valor |
|---|---|
| Base URL (dev) | `http://localhost:8080` |
| Autenticação | OAuth2 Password Grant + JWT |
| Header (rotas protegidas) | `Authorization: Bearer <access_token>` |
| Content-Type (login) | `application/x-www-form-urlencoded` |
| Content-Type (demais) | `application/json` |

---

## 1. Login

**`POST /oauth2/token`**

> Autenticação OAuth2 com grant type customizado `password`.
> Requer `Authorization: Basic` com `client_id:client_secret` em Base64.

### Credenciais do cliente (Basic Auth)
| Item | Valor |
|---|---|
| `client_id` | `synccarreira-front-id` |
| `client_secret` | `synccarreira-project-2026` |
| Header gerado | `Authorization: Basic c3luY2NhcnJlaXJhLWZyb250LWlkOnN5bmNjYXJyZWlyYS1wcm9qZWN0LTIwMjY=` |

### Request Body (form-urlencoded)
```
grant_type=password&username=ana@email.com&password=minhasenha123
```

### Response `200 OK`
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiJ9...",
  "token_type": "Bearer",
  "expires_in": 18000
}
```

### Erros esperados
| Status | Quando |
|---|---|
| `401` | Credenciais do cliente inválidas |
| `400` | `invalid_request` — grant_type ausente ou inválido |
| `400` | `invalid_grant` — usuário/senha incorretos |

---

## 2. Cadastro

**`POST /users`**

> Rota pública — não requer autenticação.
> Senha deve ter no mínimo **8 caracteres**.
> Após cadastro, redirecionar para `/login` (backend não retorna token).

### Request Body — Perfil Aluno
```json
{
  "name": "João Silva",
  "email": "joao@email.com",
  "password": "minhasenha123",
  "roleId": 1
}
```

### Request Body — Perfil Psicóloga
```json
{
  "name": "Dra. Maria",
  "email": "maria@escola.com",
  "password": "minhasenha123",
  "roleId": 2
}
```

### Response `201 Created`
```json
{
  "id": 6,
  "name": "João Silva",
  "email": "joao@email.com",
  "roles": [
    { "id": 1, "authority": "ROLE_USER" }
  ]
}
```

### Erros esperados
| Status | Quando |
|---|---|
| `422` | Dados inválidos (ex: senha com menos de 8 caracteres) |
| `409` | E-mail já cadastrado |

### Formato de erro de validação (422)
```json
{
  "timestamp": "2026-05-11T17:55:37Z",
  "status": 422,
  "error": "Dados inválidos",
  "path": "/users",
  "errors": [
    { "fieldName": "password", "message": "Deve ter no mínimo 8 caracteres" }
  ]
}
```

---

## 3. Usuário autenticado

**`GET /users/me`**
> Requer: `Authorization: Bearer <access_token>`

### Response `200 OK`
```json
{
  "id": 6,
  "name": "João Silva",
  "email": "joao@email.com",
  "roles": [
    { "id": 1, "authority": "ROLE_USER" }
  ]
}
```

### Erros esperados
| Status | Quando |
|---|---|
| `401` | Token ausente, inválido ou expirado |

---

## 4. Buscar usuário por ID

**`GET /users/{id}`**
> Requer: `Authorization: Bearer <access_token>`

### Response `200 OK`
```json
{
  "id": 6,
  "name": "João Silva",
  "email": "joao@email.com",
  "roles": [
    { "id": 1, "authority": "ROLE_USER" }
  ]
}
```

### Erros esperados
| Status | Quando |
|---|---|
| `404` | Usuário não encontrado |
| `401` | Token ausente ou inválido |

---

## 5. Logout

> O backend **não possui endpoint de logout**.
> O logout é feito apenas localmente, removendo o `access_token` do `localStorage`.
> O token continua válido no servidor até expirar (duração padrão: 18000 segundos / 5 horas).

---

## Padrão de erros

Erros gerais seguem o formato:
```json
{
  "timestamp": "2026-05-11T23:58:27.462Z",
  "status": 404,
  "error": "Descrição do erro",
  "path": "/users/99"
}
```

Erros de validação incluem o campo `errors`:
```json
{
  "timestamp": "2026-05-11T23:59:54.112Z",
  "status": 422,
  "error": "Dados inválidos",
  "path": "/users",
  "errors": [
    { "fieldName": "password", "message": "Deve ter no mínimo 8 caracteres" }
  ]
}
```

---

## Mapeamento de roles

| `roleId` (cadastro) | `authority` (resposta) | Descrição |
|---|---|---|
| `1` | `ROLE_USER` | Aluno |
| `2` | _(a confirmar)_ | Psicóloga |

---

## Normalização frontend

O backend usa campos em inglês. O frontend normaliza internamente no `authService.js`:

| Backend | Frontend |
|---|---|
| `name` | `nome` |
| `access_token` | `token` (localStorage) |
| `roles[0].authority` | `perfil` |