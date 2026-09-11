/**
 * @file PsicologaLayout.jsx
 * @description Shell do painel da psicóloga (RF-03..RF-09).
 * Reaproveita a identidade visual do admin (classes adm-*).
 */

import { Routes, Route, Navigate, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import PainelPage from './PainelPage.jsx'
import LinksPage from './LinksPage.jsx'
import AgendaPage from './AgendaPage.jsx'
import RelatoriosPage from './RelatoriosPage.jsx'
import '../Admin/AdminLayout.css'
import './Psicologa.css'

const NAV = [
  { to: 'painel', label: 'Painel' },
  { to: 'links', label: 'Links' },
  { to: 'agenda', label: 'Agenda' },
  { to: 'relatorios', label: 'Relatórios' },
]

export default function PsicologaLayout() {
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
          <span className="adm-brand__tag">Orientação</span>
        </div>
        <div className="adm-header__right">
          <span className="adm-user">{user?.nome ?? 'Psicóloga'}</span>
          <button className="adm-logout" onClick={handleLogout}>Sair</button>
        </div>
      </header>

      <nav className="adm-nav" aria-label="Seções do painel">
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
          <Route index element={<Navigate to="painel" replace />} />
          <Route path="painel" element={<PainelPage />} />
          <Route path="links" element={<LinksPage />} />
          <Route path="agenda" element={<AgendaPage />} />
          <Route path="relatorios" element={<RelatoriosPage />} />
          <Route path="*" element={<Navigate to="painel" replace />} />
        </Routes>
      </main>
    </div>
  )
}
