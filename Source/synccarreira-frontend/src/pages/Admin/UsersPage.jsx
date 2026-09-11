/**
 * @file UsersPage.jsx
 * @description RF-02 — o administrador cria, edita e desativa contas de alunos
 * e psicólogas. CPF obrigatório e único (validado no backend).
 */

import { useEffect, useState, useCallback } from 'react'
import {
  listManagedUsers,
  createManagedUser,
  updateManagedUser,
  setUserActive,
  listInstitutions,
  listClasses,
  errorMessage,
} from '../../services/adminService'

const ANOS = ['1º Ano - Ensino Médio', '2º Ano - Ensino Médio', '3º Ano - Ensino Médio', 'Outro']
const ESCOLAS = ['Pública', 'Privada']

function maskCpf(v) {
  return (v || '').replace(/\D/g, '').slice(0, 11)
    .replace(/(\d{3})(\d)/, '$1.$2')
    .replace(/(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4')
}

const EMPTY = {
  name: '', email: '', password: '', cpf: '', perfil: 'ALUNO',
  institutionId: '', schoolClassId: '',
  schoolYear: '', schoolType: '',
  crp: '', contractExpirationDate: '',
}

const PERFIL_LABEL = { ALUNO: 'Aluno', PSICOLOGA: 'Psicóloga', ADMIN: 'Administrador' }

export default function UsersPage() {
  const [users, setUsers] = useState([])
  const [institutions, setInstitutions] = useState([])
  const [classes, setClasses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')
  const [filterPerfil, setFilterPerfil] = useState('')

  const [modalOpen, setModalOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const [u, i, c] = await Promise.all([
        listManagedUsers(filterPerfil ? { perfil: filterPerfil } : {}),
        listInstitutions(),
        listClasses(),
      ])
      setUsers(u)
      setInstitutions(i)
      setClasses(c)
    } catch (e) {
      setError(errorMessage(e, 'Não foi possível carregar os usuários.'))
    } finally {
      setLoading(false)
    }
  }, [filterPerfil])

  useEffect(() => { load() }, [load])

  function openCreate() {
    setEditing(null)
    setForm(EMPTY)
    setModalOpen(true)
  }

  function openEdit(u) {
    setEditing(u)
    setForm({
      name: u.name, email: u.email, password: '', cpf: maskCpf(u.cpf ?? ''),
      perfil: u.perfil,
      institutionId: u.institutionId ?? '',
      schoolClassId: u.schoolClassId ?? '',
      schoolYear: u.schoolYear ?? '',
      schoolType: u.schoolType ?? '',
      crp: u.crp ?? '',
      contractExpirationDate: u.contractExpirationDate ?? '',
    })
    setModalOpen(true)
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true); setError(''); setOk('')
    const base = {
      name: form.name.trim(),
      email: form.email.trim(),
      cpf: form.cpf.replace(/\D/g, ''),
      institutionId: form.institutionId ? Number(form.institutionId) : null,
      schoolClassId: form.schoolClassId ? Number(form.schoolClassId) : null,
      schoolYear: form.perfil === 'ALUNO' ? form.schoolYear || null : null,
      schoolType: form.perfil === 'ALUNO' ? form.schoolType || null : null,
      crp: form.perfil === 'PSICOLOGA' ? form.crp || null : null,
      contractExpirationDate: form.perfil === 'PSICOLOGA' ? form.contractExpirationDate || null : null,
    }
    try {
      if (editing) {
        await updateManagedUser(editing.id, base)
        setOk('Conta atualizada.')
      } else {
        await createManagedUser({ ...base, password: form.password, perfil: form.perfil })
        setOk('Conta criada.')
      }
      setModalOpen(false)
      await load()
    } catch (e2) {
      setError(errorMessage(e2, 'Não foi possível salvar.'))
    } finally {
      setSaving(false)
    }
  }

  async function toggleStatus(u) {
    setError('')
    try {
      if (u.active) {
        const reason = window.prompt(`Motivo para desativar "${u.name}":`, 'Solicitação da instituição')
        if (reason === null) return
        await setUserActive(u.id, false, reason)
      } else {
        await setUserActive(u.id, true, null)
      }
      await load()
    } catch (e) {
      setError(errorMessage(e))
    }
  }

  const classesForInstitution = form.institutionId
    ? classes.filter((c) => String(c.institutionId) === String(form.institutionId))
    : []

  return (
    <section>
      <div className="adm-page__head">
        <div>
          <h1 className="adm-page__title">Usuários</h1>
          <p className="adm-page__sub">Contas de alunos e psicólogas. RF-02.</p>
        </div>
        <button className="adm-btn" onClick={openCreate}>Nova conta</button>
      </div>

      <div className="adm-field" style={{ maxWidth: 220, marginBottom: 16 }}>
        <label htmlFor="fp">Filtrar por perfil</label>
        <select id="fp" value={filterPerfil} onChange={(e) => setFilterPerfil(e.target.value)}>
          <option value="">Todos</option>
          <option value="ALUNO">Alunos</option>
          <option value="PSICOLOGA">Psicólogas</option>
          <option value="ADMIN">Administradores</option>
        </select>
      </div>

      {error && <div className="adm-feedback adm-feedback--error">{error}</div>}
      {ok && <div className="adm-feedback adm-feedback--ok">{ok}</div>}

      {loading ? (
        <div className="adm-loading">Carregando…</div>
      ) : users.length === 0 ? (
        <div className="adm-empty">Nenhum usuário encontrado.</div>
      ) : (
        <div className="adm-list">
          {users.map((u) => (
            <div key={u.id} className={`adm-card${u.active ? '' : ' adm-card--inactive'}`}>
              <div className="adm-card__info">
                <div className="adm-card__title">
                  {u.name}
                  <span className={`adm-badge ${u.active ? 'adm-badge--on' : 'adm-badge--off'}`}>
                    {u.active ? 'Ativa' : 'Inativa'}
                  </span>
                  <span className="adm-badge adm-badge--soft">{PERFIL_LABEL[u.perfil] ?? u.perfil}</span>
                </div>
                <div className="adm-card__meta">
                  {u.email}
                  {u.cpf && <> · CPF {maskCpf(u.cpf)}</>}
                  {u.institutionName && <> · {u.institutionName}</>}
                  {u.schoolClassName && <> / {u.schoolClassName}</>}
                  {!u.active && u.deactivationReason && <> · Motivo: {u.deactivationReason}</>}
                </div>
              </div>
              {u.perfil !== 'ADMIN' && (
                <div className="adm-card__actions">
                  <button className="adm-btn adm-btn--ghost adm-btn--sm" onClick={() => openEdit(u)}>Editar</button>
                  <button
                    className={`adm-btn adm-btn--sm ${u.active ? 'adm-btn--danger' : 'adm-btn--ghost'}`}
                    onClick={() => toggleStatus(u)}
                  >
                    {u.active ? 'Desativar' : 'Reativar'}
                  </button>
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {modalOpen && (
        <div className="adm-modal-backdrop" onMouseDown={() => setModalOpen(false)}>
          <form className="adm-modal" onMouseDown={(e) => e.stopPropagation()} onSubmit={handleSave}>
            <h2 className="adm-modal__title">{editing ? 'Editar conta' : 'Nova conta'}</h2>

            {!editing && (
              <div className="adm-field">
                <label htmlFor="perfil">Perfil *</label>
                <select id="perfil" value={form.perfil}
                  onChange={(e) => setForm({ ...form, perfil: e.target.value })}>
                  <option value="ALUNO">Aluno</option>
                  <option value="PSICOLOGA">Psicóloga</option>
                </select>
              </div>
            )}

            <div className="adm-field">
              <label htmlFor="name">Nome completo *</label>
              <input id="name" required value={form.name}
                onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>

            <div className="adm-field">
              <label htmlFor="email">E-mail *</label>
              <input id="email" type="email" required value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })} />
            </div>

            <div className="adm-field">
              <label htmlFor="cpf">CPF *</label>
              <input id="cpf" required inputMode="numeric" placeholder="000.000.000-00"
                value={form.cpf}
                onChange={(e) => setForm({ ...form, cpf: maskCpf(e.target.value) })} />
            </div>

            {!editing && (
              <div className="adm-field">
                <label htmlFor="password">Senha inicial * (mín. 8)</label>
                <input id="password" type="text" required minLength={8}
                  value={form.password}
                  onChange={(e) => setForm({ ...form, password: e.target.value })} />
              </div>
            )}

            <div className="adm-field">
              <label htmlFor="inst">Instituição</label>
              <select id="inst" value={form.institutionId}
                onChange={(e) => setForm({ ...form, institutionId: e.target.value, schoolClassId: '' })}>
                <option value="">Nenhuma</option>
                {institutions.map((i) => <option key={i.id} value={i.id}>{i.legalName}</option>)}
              </select>
            </div>

            {form.institutionId && (
              <div className="adm-field">
                <label htmlFor="turma">Turma</label>
                <select id="turma" value={form.schoolClassId}
                  onChange={(e) => setForm({ ...form, schoolClassId: e.target.value })}>
                  <option value="">Nenhuma</option>
                  {classesForInstitution.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
            )}

            {form.perfil === 'ALUNO' && (
              <>
                <div className="adm-field">
                  <label htmlFor="sy">Ano escolar</label>
                  <select id="sy" value={form.schoolYear}
                    onChange={(e) => setForm({ ...form, schoolYear: e.target.value })}>
                    <option value="">Selecione…</option>
                    {ANOS.map((a) => <option key={a} value={a}>{a}</option>)}
                  </select>
                </div>
                <div className="adm-field">
                  <label htmlFor="st">Tipo de escola</label>
                  <select id="st" value={form.schoolType}
                    onChange={(e) => setForm({ ...form, schoolType: e.target.value })}>
                    <option value="">Selecione…</option>
                    {ESCOLAS.map((s) => <option key={s} value={s}>{s}</option>)}
                  </select>
                </div>
              </>
            )}

            {form.perfil === 'PSICOLOGA' && (
              <>
                <div className="adm-field">
                  <label htmlFor="crp">Registro CRP *</label>
                  <input id="crp" required={form.perfil === 'PSICOLOGA'} value={form.crp}
                    onChange={(e) => setForm({ ...form, crp: e.target.value })}
                    placeholder="CRP 06/12345" />
                </div>
                <div className="adm-field">
                  <label htmlFor="ced">Vencimento do contrato *</label>
                  <input id="ced" type="date" required={form.perfil === 'PSICOLOGA'}
                    value={form.contractExpirationDate}
                    onChange={(e) => setForm({ ...form, contractExpirationDate: e.target.value })} />
                </div>
              </>
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
