import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext.jsx'
import { getTrails, getQuestionsByTrail, getAnswers } from '../../services/trailService'
import './HomePage.css'

// ─── Card de trilha com progresso ─────────────────────────────

function TrailCard({ trail, studentId, onEnter, refreshKey }) {
  const [progress, setProgress] = useState(null)

  // refreshKey muda quando o usuário volta para a Home — força recarregar o progresso
  useEffect(() => {
    if (!trail?.id || !studentId) return

    setProgress(null) // reseta para mostrar "Carregando..."

    async function loadProgress() {
      try {
        const questions = await getQuestionsByTrail(trail.id)
        const total = questions.length
        if (total === 0) { setProgress({ pct: 0, answered: 0, total: 0 }); return }

        let answers = []
        try {
          answers = await getAnswers(studentId, trail.id)
        } catch {
          // aluno sem respostas ainda — progresso 0
        }

        const answeredSet = new Set()
        for (const ans of answers) {
          const optId = ans.questionOptionDTO?.id
          if (!optId) continue
          const q = questions.find(q => q.options?.some(o => o.id === optId))
          if (q) answeredSet.add(q.id)
        }

        const answered = answeredSet.size
        setProgress({ pct: Math.round((answered / total) * 100), answered, total })

      } catch {
        setProgress({ pct: 0, answered: 0, total: '?' })
      }
    }

    loadProgress()
  }, [trail?.id, studentId, refreshKey]) // refreshKey garante reload ao voltar

  const isComplete = progress?.pct === 100
  const isStarted  = progress?.answered > 0

  return (
    <div className={`hp-trail-card${isComplete ? ' hp-trail-card--done' : ''}`}>
      <div className="hp-trail-card__icon" aria-hidden="true">
        {isComplete
          ? <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.2" width="22" height="22"><path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" strokeLinecap="round"/><polyline points="22 4 12 14.01 9 11.01" strokeLinecap="round" strokeLinejoin="round"/></svg>
          : <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" width="22" height="22"><path d="M12 2a10 10 0 1 0 10 10A10 10 0 0 0 12 2z" strokeLinecap="round"/><path d="M12 8v4l3 3" strokeLinecap="round" strokeLinejoin="round"/></svg>
        }
      </div>

      <div className="hp-trail-card__info">
        <span className="hp-trail-card__name">{trail.name}</span>

        {progress === null ? (
          <span className="hp-trail-card__loading">Carregando progresso…</span>
        ) : (
          <>
            <div className="hp-trail-card__bar">
              <div
                className="hp-trail-card__bar-fill"
                style={{ width: `${progress.pct}%` }}
              />
            </div>
            <span className="hp-trail-card__pct">
              {progress.answered} / {progress.total} perguntas · {progress.pct}%
            </span>
          </>
        )}
      </div>

      <button
        className={`hp-trail-card__btn${isComplete ? ' hp-trail-card__btn--done' : ''}`}
        onClick={() => onEnter(trail.id)}
      >
        {isComplete ? 'Revisar' : isStarted ? 'Continuar' : 'Iniciar'}
      </button>
    </div>
  )
}

// ─── Componente principal ─────────────────────────────────────

export default function HomePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const [trails, setTrails]         = useState([])
  const [loadingTrails, setLoading] = useState(true)
  const [trailsError, setError]     = useState('')
  // refreshKey muda ao ganhar foco — força TrailCards a recarregar progresso
  const [refreshKey, setRefreshKey] = useState(() => Date.now())

  // Recarrega progresso quando o usuário volta para a aba ou janela
  useEffect(() => {
    function handleFocus() { setRefreshKey(Date.now()) }
    window.addEventListener('focus', handleFocus)
    return () => window.removeEventListener('focus', handleFocus)
  }, [])

  // Carrega lista de trilhas uma vez
  useEffect(() => {
    async function load() {
      try {
        const data = await getTrails()
        setTrails(data)
      } catch {
        setError('Não foi possível carregar as trilhas.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [])

  async function handleLogout() {
    await logout()
    navigate('/login')
  }

  // Ao entrar numa trilha, atualiza o refreshKey ao voltar via navigate
  function handleEnterTrail(id) {
    navigate(`/trail/${id}`)
  }

  return (
    <div className="hp-root">

      {/* ── Cabeçalho ── */}
      <header className="hp-header">
        <div className="hp-brand">
          <span className="hp-brand__mark" aria-hidden="true">S</span>
          <span className="hp-brand__name">SyncCarreira</span>
        </div>
        <button className="hp-logout" onClick={handleLogout}>
          Sair
        </button>
      </header>

      {/* ── Conteúdo ── */}
      <main className="hp-main">
        <div className="hp-content">

          {/* Card de boas-vindas */}
          <div className="hp-card">
            <div className="hp-avatar" aria-hidden="true">
              {user?.nome?.charAt(0).toUpperCase() ?? '?'}
            </div>
            <h1 className="hp-title">
              Bem-vindo, <span>{user?.nome ?? 'Usuário'}</span>!
            </h1>
            <p className="hp-sub">
              Continue sua jornada de autoconhecimento.
            </p>
            <button
              className="hp-btn-cadastro"
              onClick={() => navigate('/cadastro')}
            >
              Cadastrar novo usuário
            </button>
          </div>

          {/* Seção de trilhas */}
          <section className="hp-trails">
            <h2 className="hp-trails__title">Suas trilhas</h2>

            {loadingTrails && (
              <div className="hp-trails__loading">
                <div className="hp-trails__spinner" />
                Carregando trilhas…
              </div>
            )}

            {!loadingTrails && trailsError && (
              <div className="hp-trails__error">{trailsError}</div>
            )}

            {!loadingTrails && !trailsError && trails.length === 0 && (
              <div className="hp-trails__empty">
                Nenhuma trilha disponível no momento.
              </div>
            )}

            {!loadingTrails && trails.map(trail => (
              <TrailCard
                key={`${trail.id}-${refreshKey}`}
                trail={trail}
                studentId={user?.id}
                onEnter={handleEnterTrail}
                refreshKey={refreshKey}
              />
            ))}
          </section>

        </div>
      </main>
    </div>
  )
}
