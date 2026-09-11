/**
 * @file AgendaPage.jsx  (psicóloga)
 * @description RF-08 — criar, editar e cancelar sessões individuais ou em grupo,
 * com convite .ics para o aluno.
 */

import { useEffect, useState, useCallback } from 'react'
import {
  listSessions, createSession, updateSession, deleteSession, downloadIcs, getPanel, errMsg,
} from '../../services/psicologaService'

const fmtLocal = (d) => {
  // datetime-local value: YYYY-MM-DDTHH:mm
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}T${p(d.getHours())}:${p(d.getMinutes())}`
}
const nextHour = () => { const d = new Date(); d.setHours(d.getHours() + 24, 0, 0, 0); return d }

const EMPTY = () => {
  const s = nextHour()
  const e = new Date(s.getTime() + 60 * 60 * 1000)
  return { type: 'INDIVIDUAL', title: '', description: '', start: fmtLocal(s), end: fmtLocal(e), location: '', studentIds: [], status: 'AGENDADA' }
}

const ST_LABEL = { AGENDADA: 'Agendada', REALIZADA: 'Realizada', CANCELADA: 'Cancelada' }

export default function AgendaPage() {
  const [items, setItems] = useState([])
  const [students, setStudents] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')
  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY())
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      setItems(await listSessions())
      try {
        const panel = await getPanel(false)
        setStudents(panel.students.map((s) => ({ id: s.studentId, name: s.studentName, inDoubt: s.inDoubt })))
      } catch { setStudents([]) }
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar a agenda.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function openCreate(prefillInDoubt = false) {
    const f = EMPTY()
    if (prefillInDoubt) {
      f.type = 'GRUPO'
      f.title = 'Sessão em grupo — alunos em dúvida'
      f.studentIds = students.filter((s) => s.inDoubt).map((s) => s.id)
    }
    setEditing(null)
    setForm(f)
    setModalOpen(true)
  }

  function openEdit(s) {
    setEditing(s)
    setForm({
      type: s.type, title: s.title, description: s.description ?? '',
      start: s.start?.slice(0, 16), end: s.end?.slice(0, 16),
      location: s.location ?? '', status: s.status,
      studentIds: s.participants.map((p) => p.studentId),
    })
    setModalOpen(true)
  }

  function toggleStudent(id) {
    setForm((f) => ({
      ...f,
      studentIds: f.studentIds.includes(id) ? f.studentIds.filter((x) => x !== id) : [...f.studentIds, id],
    }))
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true); setError(''); setOk('')
    try {
      const payload = {
        title: form.title.trim(),
        description: form.description.trim() || null,
        start: form.start, end: form.end,
        location: form.location.trim() || null,
        studentIds: form.studentIds,
      }
      if (editing) {
        await updateSession(editing.id, { ...payload, status: form.status })
      } else {
        await createSession({ ...payload, type: form.type })
      }
      setOk('Sessão salva. Os alunos foram notificados.')
      setModalOpen(false)
      await load()
    } catch (e2) {
      setError(errMsg(e2))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(s) {
    if (!window.confirm(`Excluir a sessão "${s.title}"?`)) return
    try { await deleteSession(s.id); await load() } catch (e) { setError(errMsg(e)) }
  }

  const anyInDoubt = students.some((s) => s.inDoubt)

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Agenda de sessões</h1>
          <p className="adm-page__sub">Sessões individuais e em grupo com convite .ics. RF-08.</p>
        </div>
        <button className="adm-btn" onClick={() => openCreate(false)}>Nova sessão</button>
      </div>

      {anyInDoubt && (
        <div className="adm-feedback adm-feedback--ok">
          Há alunos sinalizados em dúvida. {' '}
          <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openCreate(true)}>
            Agendar sessão em grupo com eles (RN-04)
          </button>
        </div>
      )}
      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {loading ? (
        <div className="adm-loading">Carregando…</div>
      ) : items.length === 0 ? (
        <div className="adm-empty">Nenhuma sessão agendada.</div>
      ) : (
        <div className="adm-list">
          {items.map((s) => (
            <div key={s.id} className={`adm-card${s.status === 'CANCELADA' ? ' adm-card--inactive' : ''}`}>
              <div className="adm-card__info">
                <div className="adm-card__title">
                  {s.title}
                  <span className="adm-badge adm-badge--soft">{s.type === 'GRUPO' ? 'Grupo' : 'Individual'}</span>
                  <span className={`adm-badge ${s.status === 'CANCELADA' ? 'adm-badge--off' : 'adm-badge--on'}`}>
                    {ST_LABEL[s.status] ?? s.status}
                  </span>
                </div>
                <div className="adm-card__meta">
                  {new Date(s.start).toLocaleString('pt-BR')} — {new Date(s.end).toLocaleTimeString('pt-BR', { hour: '2-digit', minute: '2-digit' })}
                  {s.location ? ` · ${s.location}` : ''}
                  {' · '}{s.participants.map((p) => p.studentName).join(', ')}
                </div>
              </div>
              <div className="adm-card__actions">
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => downloadIcs(s.id)}>.ics</button>
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openEdit(s)}>Editar</button>
                <button className="adm-btn adm-btn--danger adm-btn--sm" onClick={() => handleDelete(s)}>Excluir</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <div className="adm-modal-backdrop" onMouseDown={() => setModalOpen(false)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={handleSave}>
            <h2 className="adm-modal__title">{editing ? 'Editar sessão' : 'Nova sessão'}</h2>

            {!editing && (
              <div className="adm-field">
                <label htmlFor="tp">Tipo *</label>
                <select id="tp" value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                  <option value="INDIVIDUAL">Individual</option>
                  <option value="GRUPO">Grupo</option>
                </select>
              </div>
            )}

            <div className="adm-field">
              <label htmlFor="ti">Título *</label>
              <input id="ti" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="de">Descrição</label>
              <input id="de" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="in">Início *</label>
              <input id="in" type="datetime-local" required value={form.start} onChange={(e) => setForm({ ...form, start: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="fi">Término *</label>
              <input id="fi" type="datetime-local" required value={form.end} onChange={(e) => setForm({ ...form, end: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="lo">Local</label>
              <input id="lo" value={form.location} onChange={(e) => setForm({ ...form, location: e.target.value })} placeholder="Sala, link da chamada…" />
            </div>

            {editing && (
              <div className="adm-field">
                <label htmlFor="stt">Status</label>
                <select id="stt" value={form.status} onChange={(e) => setForm({ ...form, status: e.target.value })}>
                  <option value="AGENDADA">Agendada</option>
                  <option value="REALIZADA">Realizada</option>
                  <option value="CANCELADA">Cancelada</option>
                </select>
              </div>
            )}

            <div className="adm-field">
              <label>Alunos {form.type === 'INDIVIDUAL' ? '(exatamente 1)' : ''}</label>
              <div className="al-chips">
                {students.map((s) => (
                  <button
                    type="button"
                    key={s.id}
                    className={`al-chip${form.studentIds.includes(s.id) ? ' al-chip--on' : ''}`}
                    onClick={() => toggleStudent(s.id)}
                  >
                    {s.name}{s.inDoubt ? ' •' : ''}
                  </button>
                ))}
              </div>
            </div>

            <div className="adm-modal__actions">
              <button type="button" className="adm-btn adm-btn--ghost" onClick={() => setModalOpen(false)}>Cancelar</button>
              <button type="submit" className="adm-btn" disabled={saving || form.studentIds.length === 0}>
                {saving ? 'Salvando…' : 'Salvar'}
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  )
}
