/**
 * @file AlunoLayout.jsx
 * @description Shell da área do aluno (RF-10..RF-16). Cabeçalho + navegação +
 * rotas aninhadas. Identidade visual reaproveitada (variáveis --sc-*).
 */

import { Routes, Route, Navigate, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import JornadaPage from './JornadaPage.jsx'
import TrilhaPage from './TrilhaPage.jsx'
import EtapaFinalPage from './EtapaFinalPage.jsx'
import PanoramaPage from './PanoramaPage.jsx'
import PerfilPage from './PerfilPage.jsx'
import HistoricoPage from './HistoricoPage.jsx'
import AgendaPage from './AgendaPage.jsx'
import './Aluno.css'

const NAV = [
  { to: 'jornada', label: 'Jornada' },
  { to: 'panorama', label: 'Panorama' },
  { to: 'agenda', label: 'Agenda' },
  { to: 'historico', label: 'Histórico' },
  { to: 'perfil', label: 'Perfil' },
]

export default function AlunoLayout() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  return (
    <div className="al-root">
      <header className="al-header">
        <div className="al-brand">
          <span className="al-brand__mark" aria-hidden="true">S</span>
          <span className="al-brand__name">SyncCarreira</span>
        </div>
        <div className="al-header__right">
          <span className="al-user">{user?.nome ?? 'Aluno'}</span>
          <button className="al-logout" onClick={handleLogout}>Sair</button>
        </div>
      </header>

      <nav className="al-nav" aria-label="Seções do aluno">
        {NAV.map((item) => (
          <NavLink
            key={item.to}
            to={item.to}
            className={({ isActive }) => `al-nav__link${isActive ? ' al-nav__link--active' : ''}`}
          >
            {item.label}
          </NavLink>
        ))}
      </nav>

      <main className="al-main">
        <Routes>
          <Route index element={<Navigate to="jornada" replace />} />
          <Route path="jornada" element={<JornadaPage />} />
          <Route path="jornada/:journeyId/trilha/:trailId" element={<TrilhaPage />} />
          <Route path="jornada/:journeyId/final" element={<EtapaFinalPage />} />
          <Route path="panorama" element={<PanoramaPage />} />
          <Route path="panorama/:journeyId" element={<PanoramaPage />} />
          <Route path="agenda" element={<AgendaPage />} />
          <Route path="historico" element={<HistoricoPage />} />
          <Route path="perfil" element={<PerfilPage />} />
          <Route path="*" element={<Navigate to="jornada" replace />} />
        </Routes>
      </main>
    </div>
  )
}
