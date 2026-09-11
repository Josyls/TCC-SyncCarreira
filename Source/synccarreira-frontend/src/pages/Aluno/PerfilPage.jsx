/**
 * @file PerfilPage.jsx
 * @description RF-14 — informações de perfil e interesses do aluno.
 */

import { useEffect, useState, useCallback } from 'react'
import { getProfile, updateProfile, errMsg } from '../../services/alunoService'

export default function PerfilPage() {
  const [catalog, setCatalog] = useState([])
  const [bio, setBio] = useState('')
  const [phone, setPhone] = useState('')
  const [city, setCity] = useState('')
  const [selected, setSelected] = useState(new Set())
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')
  const [ok, setOk] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const p = await getProfile()
      setCatalog(p.allInterests || [])
      setBio(p.bio || '')
      setPhone(p.phone || '')
      setCity(p.city || '')
      setSelected(new Set(p.interestIds || []))
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar o perfil.'))
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  function toggle(id) {
    setSelected((s) => {
      const n = new Set(s)
      n.has(id) ? n.delete(id) : n.add(id)
      return n
    })
  }

  async function handleSave(e) {
    e.preventDefault()
    setSaving(true)
    setError('')
    setOk('')
    try {
      await updateProfile({
        bio: bio.trim() || null,
        phone: phone.trim() || null,
        city: city.trim() || null,
        interestIds: [...selected],
      })
      setOk('Perfil salvo.')
    } catch (e2) {
      setError(errMsg(e2, 'Não foi possível salvar.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="al-loading">Carregando perfil…</div>

  return (
    <form onSubmit={handleSave}>
      <h1 className="al-page__title">Meu perfil</h1>
      <p className="al-page__sub">Essas informações ajudam a contextualizar sua jornada. RF-14.</p>

      {error && <div className="al-feedback al-feedback--error">{error}</div>}
      {ok && <div className="al-feedback al-feedback--ok">{ok}</div>}

      <div className="al-field">
        <label htmlFor="bio">Sobre você</label>
        <textarea id="bio" className="al-textarea" value={bio}
          onChange={(e) => setBio(e.target.value)} maxLength={2000}
          placeholder="Conte um pouco sobre você, o que te move…" />
      </div>

      <div className="al-field">
        <label htmlFor="phone">Telefone</label>
        <input id="phone" value={phone} onChange={(e) => setPhone(e.target.value)} placeholder="(opcional)" />
      </div>

      <div className="al-field">
        <label htmlFor="city">Cidade</label>
        <input id="city" value={city} onChange={(e) => setCity(e.target.value)} placeholder="(opcional)" />
      </div>

      <div className="al-field">
        <label>Áreas de interesse</label>
        <div className="al-chips">
          {catalog.map((i) => (
            <button
              type="button"
              key={i.id}
              className={`al-chip${selected.has(i.id) ? ' al-chip--on' : ''}`}
              onClick={() => toggle(i.id)}
            >
              {i.name}
            </button>
          ))}
        </div>
      </div>

      <button type="submit" className="al-btn al-btn--block" disabled={saving}>
        {saving ? 'Salvando…' : 'Salvar perfil'}
      </button>
    </form>
  )
}
