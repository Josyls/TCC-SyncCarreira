/**
 * @file AuthContext.jsx
 * @description Contexto global de autenticação do SyncCarreira.
 *
 * Ajustado para o contrato real do backend:
 *  - Salva userId no localStorage após login/cadastro (necessário para GET /users/{id})
 *  - Normaliza campos: backend usa `name`, frontend usa `nome`
 *  - authService.me() agora chama GET /users/{id}
 */

import { createContext, useContext, useState, useEffect } from 'react'
import * as authService from '../services/authService'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser]             = useState(null)
  const [loading, setLoading]       = useState(false)
  const [loadingInit, setLoadingInit] = useState(true)

  // ── Restauração de sessão ──────────────────────────────────
  useEffect(() => {
    const token = localStorage.getItem('token')

    if (!token) {
      setLoadingInit(false)
      return
    }

    authService.me()
        .then((usuario) => setUser(usuario))
        .catch(() => {
          localStorage.removeItem('token')
        })
        .finally(() => setLoadingInit(false))
  }, [])

  // ── Login ──────────────────────────────────────────────────
  async function login(email, password) {
    setLoading(true)
    try {
      const data = await authService.login(email, password)

      // Normaliza o objeto de usuário (backend usa `name`, app usa `nome`)
      const usuario = data.usuario || data.user || data
      if (usuario?.name && !usuario?.nome) {
        usuario.nome = usuario.name
      }

      setUser(usuario)
      return { success: true }

    } catch (err) {
      return { success: false, message: err.message }
    } finally {
      setLoading(false)
    }
  }

  // ── Logout ─────────────────────────────────────────────────
  async function logout() {
    await authService.logout()
    setUser(null)
  }

  if (loadingInit) return null

  return (
      <AuthContext.Provider value={{ user, loading, login, logout }}>
        {children}
      </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth deve ser usado dentro de <AuthProvider>')
  return ctx
}