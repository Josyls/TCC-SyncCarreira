/**
 * @file psicologaService.js
 * @description Painel da psicóloga (RF-03..RF-09). Usa a instância `api`.
 */

import api from './api'

// ─── Painel / alertas (RF-03, RF-04, RF-05) ───────────────────
export const getPanel = async (onlyInDoubt) =>
  (await api.get('/psychologist-panel', { params: onlyInDoubt ? { onlyInDoubt: true } : {} })).data
export const resolveAlert = async (alertId) => {
  await api.post(`/psychologist-panel/alerts/${alertId}/resolve`)
}

// ─── Links curados (RF-06) ────────────────────────────────────
export const listLinks = async () => (await api.get('/curated-links')).data
export const createLink = async (p) => (await api.post('/curated-links', p)).data
export const updateLink = async (id, p) => (await api.put(`/curated-links/${id}`, p)).data
export const deleteLink = async (id) => { await api.delete(`/curated-links/${id}`) }

// ─── Sessões (RF-08) ──────────────────────────────────────────
export const listSessions = async () => (await api.get('/appointments')).data
export const createSession = async (p) => (await api.post('/appointments', p)).data
export const updateSession = async (id, p) => (await api.put(`/appointments/${id}`, p)).data
export const deleteSession = async (id) => { await api.delete(`/appointments/${id}`) }

// ─── Feedback (RF-09) ─────────────────────────────────────────
export const sendFeedback = async (p) => (await api.post('/feedbacks', p)).data
export const listSentFeedbacks = async () => (await api.get('/feedbacks/sent')).data
export const listStudentFeedbacks = async (studentId) =>
  (await api.get(`/feedbacks/student/${studentId}`)).data

// ─── Relatórios (RF-07) — download de CSV ─────────────────────
async function downloadCsv(url, filename) {
  const res = await api.get(url, { responseType: 'blob' })
  const blobUrl = URL.createObjectURL(new Blob([res.data], { type: 'text/csv;charset=utf-8' }))
  const a = document.createElement('a')
  a.href = blobUrl
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  setTimeout(() => URL.revokeObjectURL(blobUrl), 1000)
}
export const downloadClassReport = (classId) =>
  downloadCsv('/reports/class' + (classId ? `?classId=${classId}` : ''), 'relatorio-turma.csv')
export const downloadStudentReport = (studentId) =>
  downloadCsv(`/reports/student/${studentId}`, `relatorio-aluno-${studentId}.csv`)

// ─── .ics de uma sessão ───────────────────────────────────────
export const downloadIcs = (sessionId) => downloadCsv(`/appointments/${sessionId}/ics`, `sessao-${sessionId}.ics`)

export const errMsg = (e, fallback = 'Ocorreu um erro.') =>
  e?.response?.data?.error ||
  e?.response?.data?.errors?.map((x) => x.message).join(' ') ||
  e?.message ||
  fallback

export const CATEGORIAS = ['ENEM', 'PROUNI', 'SISU', 'COTAS', 'CURSOS', 'OUTRO']
