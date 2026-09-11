/**
 * @file RelatoriosPage.jsx
 * @description RF-07 — baixar relatório geral da turma e relatórios individuais.
 * Saída em CSV (abre no Excel); a página também oferece uma visão imprimível.
 */

import { useEffect, useState } from 'react'
import { getPanel, downloadClassReport, downloadStudentReport, errMsg } from '../../services/psicologaService'

export default function RelatoriosPage() {
  const [students, setStudents] = useState([])
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    (async () => {
      try {
        const p = await getPanel(false)
        setSummary(p)
        setStudents(p.students)
      } catch (e) {
        setError(errMsg(e, 'Não foi possível carregar os dados.'))
      } finally {
        setLoading(false)
      }
    })()
  }, [])

  async function dl(fn) {
    setBusy(true); setError('')
    try { await fn() } catch (e) { setError(errMsg(e, 'Falha ao baixar o relatório.')) } finally { setBusy(false) }
  }

  if (loading) return <div className="adm-loading">Carregando…</div>

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Relatórios</h1>
          <p className="adm-page__sub">Turma e individuais. RF-07. O relatório de turma não expõe respostas (RN-03).</p>
        </div>
        <button className="adm-btn" disabled={busy} onClick={() => dl(() => downloadClassReport())}>
          Baixar relatório da turma (CSV)
        </button>
      </div>

      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}

      <div className="ps-note" style={{ marginBottom: 16 }}>
        Dica: use <strong>Ctrl+P</strong> nesta página para gerar um PDF da visão abaixo.
      </div>

      {summary && (
        <div className="ps-summary">
          <div className="ps-stat"><div className="ps-stat__n">{summary.totalStudents}</div><div className="ps-stat__l">alunos</div></div>
          <div className="ps-stat"><div className="ps-stat__n">{summary.completedJourneys}</div><div className="ps-stat__l">jornadas concluídas</div></div>
          <div className="ps-stat"><div className="ps-stat__n">{summary.inDoubtStudents}</div><div className="ps-stat__l">em dúvida</div></div>
        </div>
      )}

      <div className="adm-list">
        {students.map((s) => {
          const pct = s.totalTrails > 0 ? Math.round((s.concludedTrails / s.totalTrails) * 100) : 0
          return (
            <div key={s.studentId} className="adm-card">
              <div className="adm-card__info">
                <div className="adm-card__title">{s.studentName}</div>
                <div className="adm-card__meta">
                  {s.className ? `${s.className} · ` : ''}
                  {s.journeyStatus === 'CONCLUIDA' ? 'Concluída' : s.journeyStatus === 'EM_ANDAMENTO' ? 'Em andamento' : 'Sem jornada'}
                  {' · '}{s.concludedTrails}/{s.totalTrails} trilhas ({pct}%)
                  {s.inDoubt === true ? ' · em dúvida' : ''}
                  {s.needsGuidance ? ' · alerta aberto' : ''}
                </div>
              </div>
              <div className="adm-card__actions">
                <button className="adm-btn adm-btn--ghost adm-btn--sm" disabled={busy}
                  onClick={() => dl(() => downloadStudentReport(s.studentId))}>
                  Relatório individual (CSV)
                </button>
              </div>
            </div>
          )
        })}
      </div>
    </section>
  )
}
