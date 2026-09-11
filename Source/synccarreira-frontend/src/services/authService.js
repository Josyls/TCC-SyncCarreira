import axios from 'axios'
import api from './api'

// ─── Credenciais OAuth2 ───────────────────────────────────────
const CLIENT_ID     = 'synccarreira-front-id'
const CLIENT_SECRET = 'synccarreira-project-2026'
const BASE_URL      = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// ─── Mapeamento de roleId ─────────────────────────────────────
const ROLE_MAP = {
  aluno:     1,
  psicologa: 2,
}

// ─── Login ────────────────────────────────────────────────────
export const login = async (email, senha) => {
  try {
    const credentials = btoa(`${CLIENT_ID}:${CLIENT_SECRET}`)

    const body = new URLSearchParams()
    body.append('grant_type', 'password')
    body.append('username',   email)
    body.append('password',   senha)

    const response = await axios.post(`${BASE_URL}/oauth2/token`, body, {
      headers: {
        'Content-Type':  'application/x-www-form-urlencoded',
        'Authorization': `Basic ${credentials}`,
      },
    })

    const accessToken = response.data.access_token
    if (accessToken) {
      localStorage.setItem('token', accessToken)
    }

    const meResponse = await api.get('/users/me')
    const data = meResponse.data
    const usuario = {
      id:     data.id,
      nome:   data.name,
      email:  data.email,
      perfil: data.roles?.[0]?.authority ?? 'aluno',
    }

    return { token: accessToken, usuario }

  } catch (error) {
    const mensagem = error.response?.data?.error_description
        || error.response?.data?.error
        || 'E-mail ou senha incorretos.'
    throw new Error(mensagem)
  }
}

// ─── Cadastro ─────────────────────────────────────────────────
//
// O backend possui endpoints separados por perfil:
//   POST /students      → aluno
//   POST /psychologists → psicóloga
//
export const register = async (dados) => {
  try {
    let response

    if (dados.perfil === 'psicologa') {
      // Payload para psicóloga
      const payload = {
        name:                   dados.nome,
        email:                  dados.email,
        password:               dados.password,
        roleId:                 ROLE_MAP.psicologa,
        crp:                    dados.crp,
        contractExpirationDate: dados.contractExpirationDate,
      }
      response = await api.post('/psychologists', payload)

    } else {
      // Payload para aluno (perfil padrão)
      const payload = {
        name:         dados.nome,
        email:        dados.email,
        password:     dados.password,
        roleId:       ROLE_MAP.aluno,
        schollarYear: dados.schollarYear,
        schoolType:   dados.schoolType,
      }
      response = await api.post('/students', payload)
    }

    return response.data

  } catch (error) {
    const validationErrors = error.response?.data?.errors
    if (validationErrors?.length) {
      const msgs = validationErrors.map(e => e.message).join(' ')
      throw new Error(msgs)
    }

    const mensagem = error.response?.data?.error
        || error.response?.data?.message
        || 'Erro ao criar a conta. Tente novamente.'
    throw new Error(mensagem)
  }
}

// ─── Usuário logado ───────────────────────────────────────────
export const me = async () => {
  try {
    const response = await api.get('/users/me')
    const data = response.data

    return {
      id:     data.id,
      nome:   data.name,
      email:  data.email,
      perfil: data.roles?.[0]?.authority ?? 'aluno',
      roles:  data.roles,
    }
  } catch (error) {
    throw error
  }
}

// ─── Logout ───────────────────────────────────────────────────
export const logout = async () => {
  try {
    // await api.post('/auth/logout')
  } finally {
    localStorage.removeItem('token')
  }
}