/**
 * @file authServiceV2.js
 * @description Serviço de autenticação/cadastro corrigido (Bloco 1).
 *
 * Substitui o authService.js legado, que não pode ser editado e tem dois problemas:
 *  - ROLE_MAP mapeia `psicologa: 2`, mas o id 2 é ROLE_ADMIN — toda psicóloga
 *    cadastrada virava admin.
 *  - Não conhece o perfil de administrador.
 *
 * Mapa correto (após a migração V2__fundacao.sql):
 *  | roleId | authority        | perfil    |
 *  |--------|------------------|-----------|
 *  | 1      | ROLE_USER        | aluno     |
 *  | 2      | ROLE_ADMIN       | admin     |
 *  | 3      | ROLE_PSICOLOGA   | psicologa |
 */

import axios from 'axios'
import api from './api'

const CLIENT_ID = 'synccarreira-front-id'
const CLIENT_SECRET = 'synccarreira-project-2026'
const BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const ROLE_MAP = {
  aluno: 1,
  admin: 2,
  psicologa: 3,
}

export const AUTHORITY_TO_PROFILE = {
  ROLE_USER: 'aluno',
  ROLE_ADMIN: 'admin',
  ROLE_PSICOLOGA: 'psicologa',
}

function normalizeUser(data) {
  const authority = data.roles?.[0]?.authority
  return {
    id: data.id,
    nome: data.name,
    name: data.name,
    email: data.email,
    perfil: AUTHORITY_TO_PROFILE[authority] ?? 'aluno',
    authority: authority ?? null,
    roles: data.roles ?? [],
  }
}

// ─── Login (OAuth2 password grant) ────────────────────────────
export const login = async (email, senha) => {
  try {
    const credentials = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)

    const body = new URLSearchParams()
    body.append('grant_type', 'password')
    body.append('username', email)
    body.append('password', senha)

    const response = await axios.post(`${BASE_URL}/oauth2/token`, body, {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        Authorization: `Basic ${credentials}`,
      },
    })

    const accessToken = response.data.access_token
    if (accessToken) {
      localStorage.setItem('token', accessToken)
    }

    const me = await api.get('/users/me')
    return { token: accessToken, usuario: normalizeUser(me.data) }
  } catch (error) {
    const mensagem =
      error.response?.data?.error_description ||
      error.response?.data?.error ||
      'E-mail ou senha incorretos.'
    throw new Error(mensagem)
  }
}

// ─── Cadastro (auto-serviço: aluno ou psicóloga) ──────────────
export const register = async (dados) => {
  try {
    let response

    if (dados.perfil === 'psicologa') {
      response = await api.post('/psychologists', {
        name: dados.nome,
        email: dados.email,
        password: dados.password,
        roleId: ROLE_MAP.psicologa,
        crp: dados.crp,
        contractExpirationDate: dados.contractExpirationDate,
      })
    } else {
      response = await api.post('/students', {
        name: dados.nome,
        email: dados.email,
        password: dados.password,
        roleId: ROLE_MAP.aluno,
        schollarYear: dados.schollarYear,
        schoolType: dados.schoolType,
      })
    }

    return response.data
  } catch (error) {
    const validationErrors = error.response?.data?.errors
    if (validationErrors?.length) {
      throw new Error(validationErrors.map((e) => e.message).join(' '))
    }
    const mensagem =
      error.response?.data?.error ||
      error.response?.data?.message ||
      'Erro ao criar a conta. Tente novamente.'
    throw new Error(mensagem)
  }
}

// ─── Usuário logado ───────────────────────────────────────────
export const me = async () => {
  const response = await api.get('/users/me')
  return normalizeUser(response.data)
}

// ─── Logout (somente local — backend não tem endpoint) ────────
export const logout = async () => {
  localStorage.removeItem('token')
}
