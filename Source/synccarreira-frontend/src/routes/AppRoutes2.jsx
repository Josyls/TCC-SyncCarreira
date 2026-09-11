/**
 * @file AppRoutes2.jsx
 * @description Roteador ativo da aplicação (substitui AppRoutes.jsx via App.jsx).
 *
 * Criado porque nem AppRoutes.jsx nem as páginas legadas podem ser editados.
 *
 * Estado (Blocos 1–4):
 *  - Login/Cadastro: páginas legadas reaproveitadas.
 *  - `/home` redireciona por perfil: admin → /admin, psicóloga → /psicologa, aluno → /aluno.
 *  - `/admin/*`  : painel do administrador (RF-01, RF-02).
 *  - `/aluno/*`  : jornada do aluno (RF-10..RF-16).
 *  - `/psicologa/*` : placeholder até o Bloco 6.
 */

import { Routes, Route, Navigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'
import LoginPage from '../pages/Login/LoginPage.jsx'
import CadastroPage from '../pages/Cadastro/CadastroPage.jsx'
import AdminLayout from '../pages/Admin/AdminLayout.jsx'
import AlunoLayout from '../pages/Aluno/AlunoLayout.jsx'
import PsicologaLayout from '../pages/Psicologa/PsicologaLayout.jsx'

/** Deriva o perfil ('admin' | 'psicologa' | 'aluno') a partir do usuário logado. */
export function perfilOf(user) {
  const authority = user?.roles?.[0]?.authority || user?.perfil
  if (authority === 'ROLE_ADMIN') return 'admin'
  if (authority === 'ROLE_PSICOLOGA') return 'psicologa'
  return 'aluno'
}

function RoleRoute({ role, children }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  return perfilOf(user) === role ? children : <HomeByRole />
}

/** Landing pós-login: cada perfil vai para a sua área. */
function HomeByRole() {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  const perfil = perfilOf(user)
  if (perfil === 'admin') return <Navigate to="/admin" replace />
  if (perfil === 'psicologa') return <Navigate to="/psicologa" replace />
  return <Navigate to="/aluno" replace />
}

export default function AppRoutes2() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/cadastro" element={<CadastroPage />} />

      <Route path="/home" element={<HomeByRole />} />

      <Route path="/admin/*" element={<RoleRoute role="admin"><AdminLayout /></RoleRoute>} />
      <Route path="/aluno/*" element={<RoleRoute role="aluno"><AlunoLayout /></RoleRoute>} />
      <Route path="/psicologa/*" element={<RoleRoute role="psicologa"><PsicologaLayout /></RoleRoute>} />

      <Route path="*" element={<Navigate to="/login" replace />} />
    </Routes>
  )
}
