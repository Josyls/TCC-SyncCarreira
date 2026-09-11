/**
 * @file EtapaFinalPage.jsx
 * @description Etapa final de Síntese (RF-10 5ª etapa): consolida as sínteses das
 * trilhas, apresenta o leque de carreiras (RF-12, RN-07) e conclui a jornada,
 * com a sinalização de dúvida (RF-04, RN-04).
 */

import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { getFinalPhase, completeJourney, errMsg, TRAIL_LABEL, AREA_LABEL } from '../../services/alunoService'

export default function EtapaFinalPage() {
  const { journeyId } = useParams()
  const navigate = useNavigate()

  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [finalText, setFinalText] = useState('')
  const [inDoubt, setInDoubt] = useState(null)
  const [saving, setSaving] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const d = await getFinalPhase(journeyId)
      setData(d)
      setFinalText(d.finalSynthesis || '')
      setInDoubt(d.inDoubt)
    } catch (e) {
      setError(errMsg(e, 'A etapa de Síntese ainda não está disponível.'))
    } finally {
      setLoading(false)
    }
  }, [journeyId])

  useEffect(() => { load() }, [load])

  async function handleComplete() {
    if (!finalText.trim()) { setError('Escreva sua síntese final (RN-02).'); return }
    if (inDoubt === null) { setError('Informe se você ainda está em dúvida.'); return }
    setSaving(true)
    setError('')
    try {
      const d = await completeJourney(journeyId, finalText.trim(), inDoubt)
      setData(d)
    } catch (e) {
      setError(errMsg(e, 'Não foi possível concluir a jornada.'))
    } finally {
      setSaving(false)
    }
  }

  if (loading) return <div className="al-loading">Carregando…</div>
  if (error && !data) return (
    <div>
      <button className="al-back" onClick={() => navigate('/aluno/jornada')}>← Voltar</button>
      <div className="al-feedback al-feedback--error">{error}</div>
    </div>
  )
  if (!data) return null

  const byArea = {}
  for (const c of data.careerSuggestions) (byArea[c.area] ||= []).push(c)

  return (
    <section>
      <button className="al-back" onClick={() => navigate('/aluno/jornada')}>← Voltar para a jornada</button>
      <h1 className="al-page__title">Síntese da jornada</h1>
      <p className="al-page__sub">Ciclo concluído nas 4 trilhas. Reúna suas reflexões.</p>

      {error && <div className="al-feedback al-feedback--error">{error}</div>}
      {data.completed && <div className="al-feedback al-feedback--ok">Jornada concluída. Você pode revisar tudo no Panorama.</div>}

      <h2 style={{ fontSize: 15, color: 'var(--sc-text)', margin: '18px 0 8px' }}>Suas sínteses por trilha</h2>
      {data.trailSyntheses.map((s) => (
        <div key={s.order} className="al-pan-block">
          <h3>{TRAIL_LABEL[s.trailName] ?? s.trailName}</h3>
          <div className="al-pan-synthesis">{s.text}</div>
        </div>
      ))}

      <h2 style={{ fontSize: 15, color: 'var(--sc-text)', margin: '18px 0 4px' }}>Leque de carreiras sugeridas</h2>
      <div className="al-disclaimer">{data.disclaimer}</div>
      {Object.entries(byArea).map(([area, items]) => (
        <div key={area}>
          <div style={{ fontSize: 12, fontWeight: 700, color: 'var(--sc-muted)', textTransform: 'uppercase', letterSpacing: '0.04em', margin: '6px 0' }}>
            {AREA_LABEL[area] ?? area}
          </div>
          <div className="al-leque">
            {items.map((c, i) => (
              <div key={i} className="al-career">
                <div className="al-career__area">{AREA_LABEL[c.area] ?? c.area}</div>
                <div className="al-career__title">{c.title}</div>
                <div className="al-career__type">{c.type === 'CURSO' ? 'Curso' : 'Carreira'}</div>
                {c.description && <div className="al-career__desc">{c.description}</div>}
              </div>
            ))}
          </div>
        </div>
      ))}

      <div className="al-synthesis" style={{ marginTop: 18 }}>
        <h3>Síntese final</h3>
        <p>Um texto livre reunindo o que você concluiu sobre a sua escolha.</p>
        <textarea
          className="al-textarea"
          value={finalText}
          onChange={(e) => setFinalText(e.target.value)}
          disabled={data.completed}
          placeholder="O que você leva desta jornada?"
        />

        <h3 style={{ marginTop: 16 }}>Como você se sente sobre a sua escolha?</h3>
        <div className="al-radio-row">
          <div
            className={`al-radio${inDoubt === false ? ' al-radio--on' : ''}`}
            onClick={() => !data.completed && setInDoubt(false)}
          >
            Já tenho uma direção
          </div>
          <div
            className={`al-radio${inDoubt === true ? ' al-radio--on' : ''}`}
            onClick={() => !data.completed && setInDoubt(true)}
          >
            Ainda estou em dúvida
          </div>
        </div>

        {!data.completed && (
          <button className="al-btn al-btn--block" disabled={saving} onClick={handleComplete}>
            {saving ? 'Concluindo…' : 'Concluir jornada'}
          </button>
        )}
      </div>
    </section>
  )
}
