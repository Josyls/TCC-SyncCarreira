/**
 * @file AdminLayout.jsx
 * @description Shell do painel do administrador (RF-01, RF-02).
 * Cabeçalho + navegação + área de conteúdo com rotas aninhadas.
 * Reaproveita a identidade visual (variáveis --sc-*) de HomePage/CadastroPage.
 */

import { Routes, Route, Navigate, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import InstitutionsPage from './InstitutionsPage.jsx'
import ClassesPage from './ClassesPage.jsx'
import UsersPage from './UsersPage.jsx'
import './AdminLayout.css'

const NAV = [
  { to: 'instituicoes', label: 'Instituições' },
  { to: 'turmas', label: 'Turmas' },
  { to: 'usuarios', label: 'Usuários' },
]

export default function AdminLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="adm-root">
      <header className="adm-header">
        <div className="adm-brand">
          <span className="adm-brand__mark" aria-hidden="true">S</span>
          <span className="adm-brand__name">SyncCarreira</span>
          <span className="adm-brand__tag">Administração</span>
        </div>
        <div className="adm-header__right">
          <span className="adm-user">{user?.nome ?? 'Administrador'}</span>
          <button className="adm-logout" onClick={handleLogout}>Sair</button>
        </div>
      </header>

      <nav className="adm-nav" aria-label="Seções da administração">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `adm-nav__link${isActive ? ' adm-nav__link--active' : ''}`}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <main className="adm-main">
        <Routes>
          <Route index element={<Navigate to="instituicoes" replace />} />
          <Route path="instituicoes" element={<InstitutionsPage />} />
          <Route path="turmas" element={<ClassesPage />} />
          <Route path="usuarios" element={<UsersPage />} />
          <Route path="*" element={<Navigate to="instituicoes" replace />} />
        </Routes>
      </main>
    </div>
  )
}
