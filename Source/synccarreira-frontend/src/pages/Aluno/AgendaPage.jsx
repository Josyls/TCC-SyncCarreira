/**
 * @file AgendaPage.jsx  (área do aluno)
 * @description RF-16 — o aluno vê seus agendamentos de sessão e os feedbacks
 * enviados pela psicóloga.
 */

import { useEffect, useState } from 'react'
import { getMyAppointments, getMyFeedbacks, errMsg } from '../../services/alunoService'
import api from '../../services/api'

const ST = { AGENDADA: 'Agendada', REALIZADA: 'Realizada', CANCELADA: 'Cancelada' }

async function baixarIcs(sessionId) {
  const res = await api.get(`/appointments/${sessionId}/ics`, { responseType: 'blob' })
  const url = URL.createObjectURL(new Blob([res.data], { type: 'text/calendar' }))
  const a = document.createElement('a')
  a.href = url
  a.download = `sessao-${sessionId}.ics`
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(url), 1000)
}

export default function AgendaPage() {
  const [sessions, setSessions] = useState([])
  const [feedbacks, setFeedbacks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    (async () => {
      try {
        const [s, f] = await Promise.all([getMyAppointments(), getMyFeedbacks()])
        setSessions(s)
        setFeedbacks(f)
      } catch (e) {
        setError(errMsg(e, 'Não foi possível carregar sua agenda.'))
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  if (loading) return <div className="al-loading">Carregando…</div>

  return (
    <section>
      <h1 className="al-page__title">Agenda e feedbacks</h1>
      <p className="al-page__sub">Suas sessões de orientação e os feedbacks da psicóloga. RF-16.</p>

      {error && <div className="al-feedback al-feedback--error">{error}</div>}

      <h2 style={{ fontSize: 15, color: 'var(--sc-text)', margin: '16px 0 8px' }}>Sessões</h2>
      {sessions.length === 0 ? (
        <div className="al-feedback al-feedback--info">Você não tem sessões agendadas.</div>
      ) : (
        <div className="al-trail-list">
          {sessions.map((s) => (
            <div key={s.id} className="al-hist">
              <div>
                <div className="al-hist__info">
                  {s.title} · {s.type === 'GRUPO' ? 'em grupo' : 'individual'} · {ST[s.status] ?? s.status}
                </div>
                <div className="al-hist__meta">
                  {new Date(s.start).toLocaleString('pt-BR')}
                  {s.location ? ` · ${s.location}` : ''}
                  {s.psychologistName ? ` · ${s.psychologistName}` : ''}
                </div>
              </div>
              {s.status !== 'CANCELADA' && (
                <button className="al-btn al-btn--ghost" onClick={() => baixarIcs(s.id)}>Adicionar ao calendário</button>
              )}
            </div>
          ))}
        </div>
      )}

      <h2 style={{ fontSize: 15, color: 'var(--sc-text)', margin: '20px 0 8px' }}>Feedbacks da psicóloga</h2>
      {feedbacks.length === 0 ? (
        <div className="al-feedback al-feedback--info">Nenhum feedback recebido ainda.</div>
      ) : (
        feedbacks.map((f) => (
          <div key={f.id} className="al-pan-block">
            <h3>{f.psychologistName ?? 'Psicóloga'}{f.cycle ? ` · ciclo ${f.cycle}` : ''}</h3>
            <div className="al-pan-synthesis">{f.text}</div>
            <div className="al-hist__meta" style={{ marginTop: 6 }}>
              {new Date(f.createdAt).toLocaleDateString('pt-BR')}
            </div>
          </div>
        ))
      )}
    </section>
  )
}
