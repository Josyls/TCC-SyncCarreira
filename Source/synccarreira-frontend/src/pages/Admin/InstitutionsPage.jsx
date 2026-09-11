/**
 * @file InstitutionsPage.jsx
 * @description RF-01 — cadastrar e gerenciar instituições (escolas/ONGs).
 * CNPJ obrigatório e único (validado no backend).
 */

import { useEffect, useState, useCallback } from 'react'
import {
  listInstitutions,
  createInstitution,
  updateInstitution,
  deleteInstitution,
  errorMessage,
} from '../../services/adminService'

const TIPOS = [
  { value: 'ESCOLA', label: 'Escola' },
  { value: 'ONG', label: 'ONG' },
]

const EMPTY = { legalName: '', tradeName: '', cnpj: '', type: 'ESCOLA', active: true }

function maskCnpj(v) {
  const d = (v || '').replace(/\D/g, '').slice(0, 14)
  return d
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/\.(\d{3})(\d)/, '.$1/$2')
    .replace(/(\d{4})(\d)/, '$1-$2')
}

export default function InstitutionsPage() {
  const [items, setItems] = useState([])
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
      setItems(await listInstitutions())
    } catch (e) {
      setError(errorMessage(e, 'Não foi possível carregar as instituições.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function openCreate() {
    setEditing(null)
    setForm(EMPTY)
    setModalOpen(true)
  }

  function openEdit(inst) {
    setEditing(inst)
    setForm({
      legalName: inst.legalName,
      tradeName: inst.tradeName ?? '',
      cnpj: maskCnpj(inst.cnpj),
      type: inst.type,
      active: inst.active,
    })
    setModalOpen(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    setOk('')
    try {
      const payload = {
        legalName: form.legalName.trim(),
        tradeName: form.tradeName.trim() || null,
        cnpj: form.cnpj.replace(/\D/g, ''),
        type: form.type,
      }
      if (editing) {
        await updateInstitution(editing.id, { ...payload, active: form.active })
        setOk('Instituição atualizada.')
      } else {
        await createInstitution(payload)
        setOk('Instituição cadastrada.')
      }
      setModalOpen(false)
      await load()
    } catch (e2) {
      setError(errorMessage(e2, 'Não foi possível salvar.'))
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(inst) {
    setError('')
    try {
      await updateInstitution(inst.id, {
        legalName: inst.legalName,
        tradeName: inst.tradeName ?? null,
        cnpj: inst.cnpj,
        type: inst.type,
        active: !inst.active,
      })
      await load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  async function handleDelete(inst) {
    if (!window.confirm(`Excluir a instituição "${inst.legalName}"?`)) return
    setError('')
    try {
      await deleteInstitution(inst.id)
      await load()
    } catch (e) {
      setError(errorMessage(e, 'Não foi possível excluir.'))
    }
  }

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Instituições</h1>
          <p className="adm-page__sub">Escolas e ONGs parceiras. RF-01.</p>
        </div>
        <button className="adm-btn" onClick={openCreate}>Nova instituição</button>
      </div>

      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {loading ? (
        <div className="adm-loading">Carregando…</div>
      ) : items.length === 0 ? (
        <div className="adm-empty">Nenhuma instituição cadastrada.</div>
      ) : (
        <div className="adm-list">
          {items.map((inst) => (
            <div key={inst.id} className={`adm-card${inst.active ? '' : ' adm-card--inactive'}`}>
              <div className="adm-card__info">
                <div className="adm-card__title">
                  {inst.legalName}
                  <span className={`adm-badge ${inst.active ? 'adm-badge--on' : 'adm-badge--off'}`}>
                    {inst.active ? 'Ativa' : 'Inativa'}
                  </span>
                  <span className="adm-badge adm-badge--soft">
                    {inst.type === 'ONG' ? 'ONG' : 'Escola'}
                  </span>
                </div>
                <div className="adm-card__meta">
                  {inst.tradeName && <>{inst.tradeName} · </>}
                  CNPJ {maskCnpj(inst.cnpj)} · {inst.classCount} turma(s)
                </div>
              </div>
              <div className="adm-card__actions">
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openEdit(inst)}>Editar</button>
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => toggleActive(inst)}>
                  {inst.active ? 'Desativar' : 'Reativar'}
                </button>
                <button className="adm-btn adm-btn--danger adm-btn--sm" onClick={() => handleDelete(inst)}>Excluir</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <div className="adm-modal-backdrop" onMouseDown={() => setModalOpen(false)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={handleSave}>
            <h2 className="adm-modal__title">
              {editing ? 'Editar instituição' : 'Nova instituição'}
            </h2>

            <div className="adm-field">
              <label htmlFor="legalName">Razão social *</label>
              <input
                id="legalName" required value={form.legalName}
                onChange={(e) => setForm({ ...form, legalName: e.target.value })}
              />
            </div>

            <div className="adm-field">
              <label htmlFor="tradeName">Nome fantasia</label>
              <input
                id="tradeName" value={form.tradeName}
                onChange={(e) => setForm({ ...form, tradeName: e.target.value })}
              />
            </div>

            <div className="adm-field">
              <label htmlFor="cnpj">CNPJ *</label>
              <input
                id="cnpj" required inputMode="numeric" placeholder="00.000.000/0000-00"
                value={form.cnpj}
                onChange={(e) => setForm({ ...form, cnpj: maskCnpj(e.target.value) })}
              />
            </div>

            <div className="adm-field">
              <label htmlFor="type">Tipo *</label>
              <select
                id="type" value={form.type}
                onChange={(e) => setForm({ ...form, type: e.target.value })}
              >
                {TIPOS.map((t) => <option key={t.value} value={t.value}>{t.label}</option>)}
              </select>
            </div>

            {editing && (
              <div className="adm-field adm-field__check">
                <input
                  id="active" type="checkbox" checked={form.active}
                  onChange={(e) => setForm({ ...form, active: e.target.checked })}
                />
                <label htmlFor="active">Instituição ativa</label>
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
