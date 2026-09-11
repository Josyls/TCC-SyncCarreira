/**
 * @file LinksPage.jsx
 * @description RF-06 / RN-05 — curadoria de links de fontes externas confiáveis
 * exibidos na trilha de Informação.
 */

import { useEffect, useState, useCallback } from 'react'
import {
  listLinks, createLink, updateLink, deleteLink, CATEGORIAS, errMsg,
} from '../../services/psicologaService'
import api from '../../services/api'

const EMPTY = { title: '', url: '', description: '', category: 'ENEM', institutionId: '', active: true }

export default function LinksPage() {
  const [items, setItems] = useState([])
  const [institutions, setInstitutions] = useState([])
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
      setItems(await listLinks())
      try {
        const opts = (await api.get('/curated-links/institutions')).data
        setInstitutions(opts.map((o) => ({ id: o.id, legalName: o.name })))
      } catch { setInstitutions([]) }
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar os links.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function openCreate() { setEditing(null); setForm(EMPTY); setModalOpen(true) }
  function openEdit(l) {
    setEditing(l)
    setForm({
      title: l.title, url: l.url, description: l.description ?? '',
      category: l.category, institutionId: l.institutionId ?? '', active: l.active,
    })
    setModalOpen(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true); setError(''); setOk('')
    const payload = {
      title: form.title.trim(),
      url: form.url.trim(),
      description: form.description.trim() || null,
      category: form.category,
      institutionId: form.institutionId ? Number(form.institutionId) : null,
    }
    try {
      if (editing) await updateLink(editing.id, { ...payload, active: form.active })
      else await createLink(payload)
      setOk('Link salvo.')
      setModalOpen(false)
      await load()
    } catch (e2) {
      setError(errMsg(e2))
    } finally {
      setSaving(false)
    }
  }

  async function handleDelete(l) {
    if (!window.confirm(`Excluir "${l.title}"?`)) return
    try { await deleteLink(l.id); await load() } catch (e) { setError(errMsg(e)) }
  }

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Links da trilha de Informação</h1>
          <p className="adm-page__sub">Fontes confiáveis (Enem, ProUni, Sisu, cotas, cursos). RF-06, RN-05.</p>
        </div>
        <button className="adm-btn" onClick={openCreate}>Novo link</button>
      </div>

      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {loading ? (
        <div className="adm-loading">Carregando…</div>
      ) : items.length === 0 ? (
        <div className="adm-empty">Nenhum link cadastrado. Os alunos verão um aviso na trilha de Informação.</div>
      ) : (
        <div className="adm-list">
          {items.map((l) => (
            <div key={l.id} className={`adm-card${l.active ? '' : ' adm-card--inactive'}`}>
              <div className="adm-card__info">
                <div className="adm-card__title">
                  {l.title}
                  <span className="adm-badge adm-badge--soft">{l.category}</span>
                  {!l.active && <span className="adm-badge adm-badge--off">inativo</span>}
                </div>
                <div className="adm-card__meta">
                  <a href={l.url} target="_blank" rel="noreferrer" style={{ color: 'var(--sc-roxo)' }}>{l.url}</a>
                  {l.institutionName ? ` · ${l.institutionName}` : ' · todas as instituições'}
                  {l.description ? ` · ${l.description}` : ''}
                </div>
              </div>
              <div className="adm-card__actions">
                <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openEdit(l)}>Editar</button>
                <button className="adm-btn adm-btn--danger adm-btn--sm" onClick={() => handleDelete(l)}>Excluir</button>
              </div>
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <div className="adm-modal-backdrop" onMouseDown={() => setModalOpen(false)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={handleSave}>
            <h2 className="adm-modal__title">{editing ? 'Editar link' : 'Novo link'}</h2>
            <div className="adm-field">
              <label htmlFor="t">Título *</label>
              <input id="t" required value={form.title} onChange={(e) => setForm({ ...form, title: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="u">URL *</label>
              <input id="u" required type="url" value={form.url} onChange={(e) => setForm({ ...form, url: e.target.value })} placeholder="https://…" />
            </div>
            <div className="adm-field">
              <label htmlFor="d">Descrição</label>
              <input id="d" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div className="adm-field">
              <label htmlFor="c">Categoria *</label>
              <select id="c" value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
                {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </div>
            <div className="adm-field">
              <label htmlFor="i">Instituição</label>
              <select id="i" value={form.institutionId} onChange={(e) => setForm({ ...form, institutionId: e.target.value })}>
                <option value="">Todas</option>
                {institutions.map((i) => <option key={i.id} value={i.id}>{i.legalName}</option>)}
              </select>
            </div>
            {editing && (
              <div className="adm-field adm-field__check">
                <input id="a" type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} />
                <label htmlFor="a">Link ativo</label>
              </div>
            )}
            <div className="adm-modal__actions">
              <button type="button" className="adm-btn adm-btn--ghost" onClick={() => setModalOpen(false)}>Cancelar</button>
              <button type="submit" className="adm-btn" disabled={saving}>{saving ? 'Salvando…' : 'Salvar'}</button>
            </div>
          </form>
        </div>
      )}
    </section>
  )
}
