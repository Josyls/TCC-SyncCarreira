/**
 * @file PanoramaPage.jsx
 * @description RF-13 / RN-08 — panorama completo: respostas e sínteses de todas
 * as trilhas concluídas da jornada.
 */

import { useEffect, useState, useCallback } from 'react'
import { useParams } from 'react-router-dom'
import { getCurrentJourney, getPanorama, errMsg, TRAIL_LABEL } from '../../services/alunoService'

export default function PanoramaPage() {
  const { journeyId: paramId } = useParams()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      let id = paramId
      if (!id) {
        const cur = await getCurrentJourney()
        id = cur.journeyId
      }
      setData(await getPanorama(id))
    } catch (e) {
      setError(errMsg(e, 'Não foi possível carregar o panorama.'))
    } finally {
      setLoading(false)
    }
  }, [paramId])

  useEffect(() => { load() }, [load])

  if (loading) return <div className="al-loading">Carregando panorama…</div>
  if (error) return <div className="al-feedback al-feedback--error">{error}</div>
  if (!data) return null

  const hasContent = data.trails.some((t) => t.synthesisText || t.items.some((i) => i.chosenOption || i.content))

  return (
    <section>
      <h1 className="al-page__title">Panorama da jornada</h1>
      <p className="al-page__sub">
        Ciclo {data.cycle} · {data.status === 'CONCLUIDA' ? 'concluída' : 'em andamento'}
        {data.inDoubt === true ? ' · sinalizou dúvida' : data.inDoubt === false ? ' · com direção definida' : ''}
      </p>

      {!hasContent && (
        <div className="al-feedback al-feedback--info">
          Você ainda não registrou respostas ou sínteses nesta jornada.
        </div>
      )}

      {data.trails.map((t) => {
        const answered = t.items.filter((i) => i.chosenOption || i.content)
        if (!t.synthesisText && answered.length === 0) return null
        return (
          <div key={t.order} className="al-pan-block">
            <h3>{TRAIL_LABEL[t.trailName] ?? t.trailName}</h3>
            {answered.map((it, i) => (
              <div key={i} className="al-pan-item">
                <div className="al-pan-item__q">{it.question}</div>
                <div className="al-pan-item__a">
                  {it.chosenOption ? `→ ${it.chosenOption}` : ''}
                  {it.content ? (it.chosenOption ? ` · ${it.content}` : `→ ${it.content}`) : ''}
                </div>
              </div>
            ))}
            {t.synthesisText && <div className="al-pan-synthesis">{t.synthesisText}</div>}
          </div>
        )
      })}

      {data.finalSynthesis && (
        <div className="al-pan-block">
          <h3>Síntese final</h3>
          <div className="al-pan-synthesis">{data.finalSynthesis}</div>
        </div>
      )}
    </section>
  )
}
