/**
 * @file PainelPage.jsx
 * @description RF-03 (status por aluno, sem respostas — RN-03), RF-04 (alertas),
 * RF-05 (filtrar "em dúvida"), RF-09 (enviar feedback).
 */

import { useEffect, useState, useCallback } from 'react'
import {
  getPanel, resolveAlert, sendFeedback, listStudentFeedbacks, downloadStudentReport, errMsg,
} from '../../services/psicologaService'

const JS_LABEL = { SEM_JORNADA: 'Sem jornada', EM_ANDAMENTO: 'Em andamento', CONCLUIDA: 'Concluída' }

export default function PainelPage() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')
  const [onlyInDoubt, setOnlyInDoubt] = useState(false)

  const [fbFor, setFbFor] = useState(null)   // student status object
  const [fbText, setFbText] = useState('')
  const [fbHistory, setFbHistory] = useState([])
  const [sending, setSending] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setData(await getPanel(onlyInDoubt))
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar o painel.'))
    } finally {
      setLoading(false)
    }
  }, [onlyInDoubt])

  useEffect(() => { load() }, [load])

  async function openFeedback(student) {
    setFbFor(student)
    setFbText('')
    setFbHistory([])
    try {
      setFbHistory(await listStudentFeedbacks(student.studentId))
    } catch { /* ignore */ }
  }

  async function submitFeedback(e) {
    e.preventDefault()
    if (!fbText.trim()) return
    setSending(true)
    setError('')
    try {
      await sendFeedback({ studentId: fbFor.studentId, text: fbText.trim() })
      setOk(`Feedback enviado para ${fbFor.studentName}.`)
      setFbFor(null)
      await load()
    } catch (e2) {
      setError(errMsg(e2, 'Não foi possível enviar o feedback.'))
    } finally {
      setSending(false)
    }
  }

  async function handleResolve(alertId) {
    setError('')
    try {
      await resolveAlert(alertId)
      await load()
    } catch (e) {
      setError(errMsg(e))
    }
  }

  if (loading) return <div className="adm-loading">Carregando painel…</div>

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Painel de acompanhamento</h1>
          <p className="adm-page__sub">Progresso das turmas nas trilhas. RF-03.</p>
        </div>
      </div>

      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {data && (
        <>
          <div className="ps-summary">
            <div className="ps-stat"><div className="ps-stat__n">{data.totalStudents}</div><div className="ps-stat__l">alunos</div></div>
            <div className="ps-stat"><div className="ps-stat__n">{data.completedJourneys}</div><div className="ps-stat__l">jornadas concluídas</div></div>
            <div className="ps-stat"><div className="ps-stat__n">{data.inDoubtStudents}</div><div className="ps-stat__l">em dúvida</div></div>
            <div className="ps-stat"><div className="ps-stat__n">{data.openAlerts}</div><div className="ps-stat__l">alertas abertos</div></div>
          </div>

          {data.alerts.length > 0 && (
            <>
              <h2 className="adm-page__title" style={{ fontSize: 15, margin: '6px 0 8px' }}>
                Alertas de necessidade de orientação (RF-04)
              </h2>
              {data.alerts.map((a) => (
                <div key={a.alertId} className="ps-alert">
                  <div>
                    <div className="ps-alert__info">
                      {a.studentName} — {a.reason === 'EM_DUVIDA' ? 'concluiu em dúvida' : a.reason}
                    </div>
                    <div className="ps-alert__meta">
                      {a.className ? `${a.className} · ` : ''}
                      prioridade: sessão em {a.priority === 'GRUPO' ? 'grupo' : 'individual'} (RN-04)
                    </div>
                  </div>
                  <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => handleResolve(a.alertId)}>
                    Marcar como resolvido
                  </button>
                </div>
              ))}
            </>
          )}

          <div className="ps-note">
            Este painel mostra apenas o progresso e as sinalizações — não exibe o conteúdo das respostas dos alunos (RN-03).
          </div>

          <div className="ps-filter-row">
            <label className="ps-chk">
              <input type="checkbox" checked={onlyInDoubt} onChange={(e) => setOnlyInDoubt(e.target.checked)} />
              Mostrar só alunos em dúvida (RF-05)
            </label>
          </div>

          <div className="adm-list">
            {data.students.length === 0 && <div className="adm-empty">Nenhum aluno neste filtro.</div>}
            {data.students.map((s) => {
              const pct = s.totalTrails > 0 ? Math.round((s.concludedTrails / s.totalTrails) * 100) : 0
              return (
                <div key={s.studentId} className={`adm-card${s.needsGuidance ? '' : ''}`}>
                  <div className="adm-card__info">
                    <div className="adm-card__title">
                      {s.studentName}
                      {s.needsGuidance && <span className="adm-badge adm-badge--off">precisa de orientação</span>}
                      {s.inDoubt === true && <span className="adm-badge adm-badge--soft">em dúvida</span>}
                    </div>
                    <div className="adm-card__meta">
                      {s.className ? `${s.className} · ` : ''}
                      {JS_LABEL[s.journeyStatus] ?? s.journeyStatus}
                      {s.cycle ? ` · ciclo ${s.cycle}` : ''}
                      {' · '}{s.concludedTrails}/{s.totalTrails} trilhas
                      <div className="ps-progress"><div className="ps-progress__fill" style={{ width: `${pct}%` }} /></div>
                    </div>
                  </div>
                  <div className="adm-card__actions">
                    <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openFeedback(s)}>Feedback</button>
                    <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => downloadStudentReport(s.studentId)}>
                      Relatório
                    </button>
                  </div>
                </div>
              )
            })}
          </div>
        </>
      )}

      {fbFor && (
        <div className="adm-modal-backdrop" onMouseDown={() => setFbFor(null)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={submitFeedback}>
            <h2 className="adm-modal__title">Feedback para {fbFor.studentName}</h2>
            <textarea
              className="ps-textarea"
              value={fbText}
              onChange={(e) => setFbText(e.target.value)}
              placeholder="Escreva um feedback textual para o aluno (RF-09)…"
            />
            {fbHistory.length > 0 && (
              <div className="ps-fb-list">
                <div style={{ fontSize: 12, color: 'var(--sc-muted)', marginTop: 6 }}>Feedbacks anteriores:</div>
                {fbHistory.map((f) => (
                  <div key={f.id} className="ps-fb">
                    {f.text}
                    <div className="ps-fb__meta">{new Date(f.createdAt).toLocaleDateString('pt-BR')}</div>
                  </div>
                ))}
              </div>
            )}
            <div className="adm-modal__actions">
              <button type="button" className="adm-btn adm-btn--ghost" onClick={() => setFbFor(null)}>Cancelar</button>
              <button type="submit" className="adm-btn" disabled={sending || !fbText.trim()}>
                {sending ? 'Enviando…' : 'Enviar feedback'}
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  )
}
