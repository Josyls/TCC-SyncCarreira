/**
 * @file ClassesPage.jsx
 * @description RF-01 — criar e gerenciar turmas vinculadas a instituições.
 * Opcionalmente com uma psicóloga responsável.
 */

import { useEffect, useState, useCallback } from 'react'
import api from '../../services/api'
import {
  listClasses,
  listInstitutions,
  createClass,
  updateClass,
  deleteClass,
  errorMessage,
} from '../../services/adminService'

const currentYear = new Date().getFullYear()
const EMPTY = { name: '', schoolYear: currentYear, institutionId: '', psychologistId: '', active: true }

export default function ClassesPage() {
  const [items, setItems] = useState([])
  const [institutions, setInstitutions] = useState([])
  const [psychologists, setPsychologists] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [classes, insts] = await Promise.all([listClasses(), listInstitutions()])
      setItems(classes)
      setInstitutions(insts)
      try {
        setPsychologists((await api.get('/psychologists')).data)
      } catch {
        setPsychologists([])
      }
    } catch (e) {
      setError(errorMessage(e, 'Não foi possível carregar as turmas.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function openCreate() {
    setEditing(null)
    setForm({ ...EMPTY, institutionId: institutions[0]?.id ?? '' })
    setModalOpen(true)
  }

  function openEdit(t) {
    setEditing(t)
    setForm({
      name: t.name,
      schoolYear: t.schoolYear,
      institutionId: t.institutionId ?? '',
      psychologistId: t.psychologistId ?? '',
      active: t.active,
    })
    setModalOpen(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    setOk('')
    try {
      const psy = form.psychologistId ? Number(form.psychologistId) : null
      if (editing) {
        await updateClass(editing.id, {
          name: form.name.trim(),
          schoolYear: Number(form.schoolYear),
          psychologistId: psy,
          active: form.active,
        })
        setOk('Turma atualizada.')
      } else {
        await createClass({
          name: form.name.trim(),
          schoolYear: Number(form.schoolYear),
          institutionId: Number(form.institutionId),
          psychologistId: psy,
        })
        setOk('Turma criada.')
      }
      setModalOpen(false)
      await load()
    } catch (e2) {
      setError(errorMessage(e2, 'Não foi possível salvar.'))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(t) {
    if (!window.confirm(`Excluir a turma "${t.name}"?`)) return
    setError('')
    try {
      await deleteClass(t.id)
      await load()
    } catch (e) {
      setError(errorMessage(e, 'Não foi possível excluir.'))
    }
  }

  const canCreate = institutions.some((i) => i.active)

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Turmas</h1>
          <p className="adm-page__sub">Turmas operacionais por instituição. RF-01.</p>
        </div>
        <button className="adm-btn" onClick={openCreate} disabled={!canCreate}>Nova turma</button>
      </div>

      {!canCreate && !loading && (
        <div className="adm-feedback adm-feedback--error">
          Cadastre uma instituição ativa antes de criar turmas.
        </div>
      )}
      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {loading ? (
        <div className="adm-loading">Carregando…</div>
      ) : items.length === 0 ? (
        <div className="adm-empty">Nenhuma turma cadastrada.</div>
      ) : (
        <div className="adm-list">
          {items.map((t) => (
            <div key={t.id} className={`adm-card${t.active ? '' : ' adm-card--inactive'}`}>
              <div className="adm-card__info">
                <div className="adm-card__title">
                  {t.name}
                  <span className={`adm-badge ${t.active ? 'adm-badge--on' : 'adm-badge--off'}`}>
                    {t.active ? 'Ativa' : 'Inativa'}
                  </span>
                </div>
                <div className="adm-card__meta">
                  {t.institutionName} · {t.schoolYear} ·{' '}
                  {t.psychologistName ? `Psicóloga: ${t.psychologistName}` : 'Sem psicóloga'}
                </div>
              </div>
              <div className="adm-card__actions">
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openEdit(t)}>Editar</button>
                <button className="adm-btn adm-btn--danger adm-btn--sm" onClick={() => handleDelete(t)}>Excluir</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <div className="adm-modal-backdrop" onMouseDown={() => setModalOpen(false)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={handleSave}>
            <h2 className="adm-modal__title">{editing ? 'Editar turma' : 'Nova turma'}</h2>

            <div className="adm-field">
              <label htmlFor="name">Nome da turma *</label>
              <input
                id="name" required value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })}
                placeholder="Ex: 3º Ano A"
              />
            </div>

            <div className="adm-field">
              <label htmlFor="schoolYear">Ano letivo *</label>
              <input
                id="schoolYear" type="number" required min="2000" max="2100"
                value={form.schoolYear}
                onChange={(e) => setForm({ ...form, schoolYear: e.target.value })}
              />
            </div>

            {!editing && (
              <div className="adm-field">
                <label htmlFor="institutionId">Instituição *</label>
                <select
                  id="institutionId" required value={form.institutionId}
                  onChange={(e) => setForm({ ...form, institutionId: e.target.value })}
                >
                  <option value="">Selecione…</option>
                  {institutions.filter((i) => i.active).map((i) => (
                    <option key={i.id} value={i.id}>{i.legalName}</option>
                  ))}
                </select>
              </div>
            )}

            <div className="adm-field">
              <label htmlFor="psychologistId">Psicóloga responsável</label>
              <select
                id="psychologistId" value={form.psychologistId}
                onChange={(e) => setForm({ ...form, psychologistId: e.target.value })}
              >
                <option value="">Sem psicóloga</option>
                {psychologists.map((p) => (
                  <option key={p.id} value={p.id}>{p.name}</option>
                ))}
              </select>
            </div>

            {editing && (
              <div className="adm-field adm-field__check">
                <input
                  id="classActive" type="checkbox" checked={form.active}
                  onChange={(e) => setForm({ ...form, active: e.target.checked })}
                />
                <label htmlFor="classActive">Turma ativa</label>
              </div>
            )}

            <div className="adm-modal__actions">
              <button type="button" className="adm-btn adm-btn--ghost" onClick={() => setModalOpen(false)}>
                Cancelar
              </button>
              <button type="submit" className="adm-btn" disabled={saving}>
                {saving ? 'Salvando…' : 'Salvar'}
              </button>
            </div>
          </form>
        </div>
      )}
    </section>
  )
}
