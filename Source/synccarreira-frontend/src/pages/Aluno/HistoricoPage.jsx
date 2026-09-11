/**
 * @file HistoricoPage.jsx
 * @description RF-15 — jornadas de ciclos anteriores, para acompanhar a evolução.
 */

import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { getHistory, errMsg } from '../../services/alunoService'

const fmt = (iso) => (iso ? new Date(iso).toLocaleDateString('pt-BR') : '—')

export default function HistoricoPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        setItems(await getHistory())
      } catch (e) {
        setError(errMsg(e, 'Não foi possível carregar o histórico.'))
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  if (loading) return <div className="al-loading">Carregando histórico…</div>
  if (error) return <div className="al-feedback al-feedback--error">{error}</div>

  return (
    <section>
      <h1 className="al-page__title">Histórico de jornadas</h1>
      <p className="al-page__sub">Cada ciclo é uma passagem completa pelas trilhas. RF-15.</p>

      {items.length === 0 ? (
        <div className="al-feedback al-feedback--info">Você ainda não iniciou nenhuma jornada.</div>
      ) : (
        items.map((h) => (
          <div key={h.journeyId} className="al-hist">
            <div>
              <div className="al-hist__info">
                Ciclo {h.cycle} · {h.status === 'CONCLUIDA' ? 'Concluída' : 'Em andamento'}
                {h.inDoubt === true ? ' · sinalizou dúvida' : h.inDoubt === false ? ' · com direção' : ''}
              </div>
              <div className="al-hist__meta">
                {h.concludedTrails}/{h.totalTrails} trilhas · início {fmt(h.startedAt)}
                {h.finishedAt ? ` · fim ${fmt(h.finishedAt)}` : ''}
              </div>
            </div>
            <button className="al-btn al-btn--ghost" onClick={() => navigate(`/aluno/panorama/${h.journeyId}`)}>
              Ver panorama
            </button>
          </div>
        ))
      )}
    </section>
  )
}
