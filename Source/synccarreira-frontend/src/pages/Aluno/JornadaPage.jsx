/**
 * @file JornadaPage.jsx
 * @description RF-10 — as 5 etapas em ordem (4 trilhas + Síntese final).
 * Cada trilha só libera a próxima quando concluída (RN-01).
 */

import { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import { getCurrentJourney, errMsg, TRAIL_LABEL } from '../../services/alunoService'

export default function JornadaPage() {
  const navigate = useNavigate()
  const [journey, setJourney] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setJourney(await getCurrentJourney())
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar sua jornada.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  if (loading) return <div className="al-loading">Carregando sua jornada…</div>
  if (error) return <div className="al-feedback al-feedback--error">{error}</div>
  if (!journey) return null

  const trails = [...journey.trails].sort((a, b) => a.order - b.order)
  const completed = journey.status === 'CONCLUIDA'

  return (
    <section>
      <div className="al-journey-head">
        <h1>
          Sua jornada
          <span className="al-cycle-tag">Ciclo {journey.cycle}</span>
        </h1>
        <p>
          {completed
            ? 'Você concluiu esta jornada. Consulte o Panorama e o Histórico.'
            : 'Percorra as 4 trilhas em ordem. Ao final, a etapa de Síntese reúne tudo.'}
        </p>
      </div>

      <div className="al-trail-list">
        {trails.map((t, i) => {
          const pct = t.total > 0 ? Math.round((t.answered / t.total) * 100) : (t.hasSynthesis ? 100 : 0)
          const locked = t.status === 'BLOQUEADA'
          const done = t.status === 'CONCLUIDA'
          return (
            <div key={t.trailId} className={`al-trail${locked ? ' al-trail--locked' : ''}${done ? ' al-trail--done' : ''}`}>
              <div className="al-trail__step">{done ? '✓' : i + 1}</div>
              <div className="al-trail__info">
                <div className="al-trail__name">{TRAIL_LABEL[t.name] ?? t.name}</div>
                <div className="al-trail__meta">
                  {t.total > 0 ? `${t.answered}/${t.total} perguntas` : 'Trilha de leitura'}
                  {t.hasSynthesis ? ' · síntese registrada' : ''}
                  {locked ? ' · bloqueada' : ''}
                </div>
                <div className="al-bar"><div className="al-bar__fill" style={{ width: `${pct}%` }} /></div>
              </div>
              <button
                className={`al-trail__btn${done ? ' al-trail__btn--done' : ''}`}
                disabled={locked}
                onClick={() => navigate(`/aluno/jornada/${journey.journeyId}/trilha/${t.trailId}`)}
              >
                {done ? 'Revisar' : t.answered > 0 || t.hasSynthesis ? 'Continuar' : 'Iniciar'}
              </button>
            </div>
          )
        })}
      </div>

      {journey.finalPhaseAvailable && (
        <div className="al-final-cta">
          <h2>Etapa de Síntese</h2>
          <p>Você concluiu as 4 trilhas. Reúna suas reflexões e veja o leque de carreiras.</p>
          <button className="al-btn" onClick={() => navigate(`/aluno/jornada/${journey.journeyId}/final`)}>
            {completed ? 'Ver resultado' : 'Ir para a Síntese'}
          </button>
        </div>
      )}
    </section>
  )
}
