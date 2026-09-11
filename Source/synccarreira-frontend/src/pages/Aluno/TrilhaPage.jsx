/**
 * @file TrilhaPage.jsx  (área do aluno)
 * @description RF-10/RF-11 — responder as perguntas de uma trilha e registrar a
 * síntese textual obrigatória (RN-02). A trilha só é acessível se liberada (RN-01).
 */

import { useEffect, useState, useCallback } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  getTrailDetail, saveAnswer, saveSynthesis, getCuratedLinks,
  errMsg, TRAIL_LABEL,
} from '../../services/alunoService'

export default function TrilhaPage() {
  const { journeyId, trailId } = useParams()
  const navigate = useNavigate()

  const [detail, setDetail] = useState(null)
  const [answers, setAnswers] = useState({})
  const [synthesis, setSynthesis] = useState('')
  const [links, setLinks] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [saveState, setSaveState] = useState({})
  const [savingSynthesis, setSavingSynthesis] = useState(false)
  const [okMsg, setOkMsg] = useState('')

  const load = useCallback(async () => {
    setLoading(true)
    setError('')
    try {
      const d = await getTrailDetail(journeyId, trailId)
      setDetail(d)
      setAnswers(d.answers || {})
      setSynthesis(d.synthesisText || '')
      if (d.name === 'INFORMACAO') setLinks(await getCuratedLinks())
    } catch (e) {
      setError(errMsg(e, 'Não foi possível abrir a trilha.'))
    } finally {
      setLoading(false)
    }
  }, [journeyId, trailId])

  useEffect(() => { load() }, [load])

  async function persist(questionId, optionId, content) {
    setSaveState((s) => ({ ...s, [questionId]: 'saving' }))
    try {
      await saveAnswer(journeyId, { questionId, optionId: optionId ?? null, content: content ?? null })
      setAnswers((a) => ({ ...a, [questionId]: { optionId: optionId ?? null, content: content ?? null } }))
      setSaveState((s) => ({ ...s, [questionId]: 'saved' }))
      setTimeout(() => setSaveState((s) => ({ ...s, [questionId]: undefined })), 2000)
    } catch (e) {
      setSaveState((s) => ({ ...s, [questionId]: 'error' }))
      setError(errMsg(e))
    }
  }

  function pickOption(q, optionId) {
    persist(q.id, optionId, null)
  }
  function typeText(q, text) {
    setAnswers((a) => ({ ...a, [q.id]: { optionId: null, content: text } }))
  }
  function blurText(q) {
    const content = answers[q.id]?.content?.trim()
    if (content) persist(q.id, null, content)
  }

  async function handleConclude() {
    if (!synthesis.trim()) {
      setError('Escreva a síntese antes de concluir a trilha (RN-02).')
      return
    }
    setSavingSynthesis(true)
    setError('')
    setOkMsg('')
    try {
      await saveSynthesis(journeyId, trailId, synthesis.trim())
      setOkMsg('Trilha concluída! A próxima etapa foi liberada.')
      setTimeout(() => navigate('/aluno/jornada'), 1200)
    } catch (e) {
      setError(errMsg(e, 'Não foi possível concluir a trilha.'))
    } finally {
      setSavingSynthesis(false)
    }
  }

  if (loading) return <div className="al-loading">Carregando trilha…</div>
  if (error && !detail) return (
    <div>
      <button className="al-back" onClick={() => navigate('/aluno/jornada')}>← Voltar</button>
      <div className="al-feedback al-feedback--error">{error}</div>
    </div>
  )
  if (!detail) return null

  const answeredCount = detail.questions.filter((q) => answers[q.id]).length
  const total = detail.questions.length
  const canConclude = total === 0 || answeredCount >= total
  const isInfo = detail.name === 'INFORMACAO'

  return (
    <section>
      <button className="al-back" onClick={() => navigate('/aluno/jornada')}>← Voltar para a jornada</button>
      <h1 className="al-page__title">{TRAIL_LABEL[detail.name] ?? detail.name}</h1>
      <p className="al-page__sub">
        {isInfo
          ? 'Consulte as fontes confiáveis abaixo e registre sua síntese.'
          : `Etapa ${detail.order} · ${answeredCount}/${total} respondidas`}
      </p>

      {error && <div className="al-feedback al-feedback--error">{error}</div>}
      {okMsg && <div className="al-feedback al-feedback--ok">{okMsg}</div>}

      {isInfo && (
        <div className="al-links">
          {links.length === 0 ? (
            <div className="al-feedback al-feedback--info">
              A psicóloga ainda não cadastrou links para esta trilha. Você já pode registrar sua síntese.
            </div>
          ) : links.map((l) => (
            <div key={l.id} className="al-link-card">
              <span className="al-link-card__tag">{l.category}</span>
              <div><a href={l.url} target="_blank" rel="noreferrer">{l.title}</a></div>
              {l.description && <p>{l.description}</p>}
            </div>
          ))}
        </div>
      )}

      {detail.questions.map((q, i) => {
        const ans = answers[q.id]
        const isLikert = q.type === 'LIKERT'
        const isOpen = q.type === 'ABERTA'
        return (
          <div key={q.id} className={`al-question${ans ? ' al-question--answered' : ''}`}>
            <div className="al-question__num">Pergunta {i + 1}{ans ? ' · respondida' : ''}</div>
            <div className="al-question__text">{q.content}</div>

            {isOpen ? (
              <textarea
                className="al-textarea"
                value={ans?.content ?? ''}
                onChange={(e) => typeText(q, e.target.value)}
                onBlur={() => blurText(q)}
                placeholder="Escreva sua resposta…"
              />
            ) : isLikert ? (
              <>
                <div className="al-likert">
                  {q.options.map((opt, n) => {
                    const selected = ans?.optionId === opt.id
                    return (
                      <button
                        key={opt.id}
                        className={`al-likert__btn${selected ? ' al-likert__btn--selected' : ''}`}
                        onClick={() => pickOption(q, opt.id)}
                        title={opt.text}
                      >{n + 1}</button>
                    )
                  })}
                </div>
                <div className="al-likert__labels">
                  <span>{q.options[0]?.text ?? 'Discordo'}</span>
                  <span>{q.options[q.options.length - 1]?.text ?? 'Concordo'}</span>
                </div>
              </>
            ) : (
              <div className="al-options">
                {q.options.map((o) => (
                  <button
                    key={o.id}
                    className={`al-option${ans?.optionId === o.id ? ' al-option--selected' : ''}`}
                    onClick={() => pickOption(q, o.id)}
                  >
                    <span className="al-option__dot" />
                    <span>{o.text}</span>
                  </button>
                ))}
              </div>
            )}

            {saveState[q.id] === 'saving' && <div className="al-save-hint">Salvando…</div>}
            {saveState[q.id] === 'saved' && <div className="al-save-hint">✓ resposta salva</div>}
          </div>
        )
      })}

      <div className="al-synthesis">
        <h3>Síntese da trilha</h3>
        <p>Registre livremente o que você percebeu nesta etapa. É obrigatório para concluir (RN-02).</p>
        <textarea
          className="al-textarea"
          value={synthesis}
          onChange={(e) => setSynthesis(e.target.value)}
          placeholder="O que essa trilha te fez pensar?"
        />
        <button
          className="al-btn al-btn--block"
          style={{ marginTop: 12 }}
          disabled={savingSynthesis || !canConclude || !synthesis.trim()}
          onClick={handleConclude}
        >
          {savingSynthesis ? 'Salvando…'
            : detail.status === 'CONCLUIDA' ? 'Atualizar síntese'
            : !canConclude ? `Responda todas as perguntas (${answeredCount}/${total})`
            : 'Concluir trilha'}
        </button>
      </div>
    </section>
  )
}
