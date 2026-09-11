/**
 * @file trailService.js
 * @description Serviços de trilhas, perguntas e respostas.
 *
 * Endpoints utilizados:
 *  - GET  /trails                         → lista todas as trilhas
 *  - GET  /questions/trail/{trailId}      → perguntas de uma trilha
 *  - GET  /answers?alunoId=&trailId=      → respostas do aluno na trilha
 *  - POST /answers                        → salva/substitui uma resposta
 */

import api from './api'

// ─── Trilhas ──────────────────────────────────────────────────

/**
 * Busca todas as trilhas disponíveis.
 * @returns {Promise<Array>}
 */
export const getTrails = async () => {
  const response = await api.get('/trails')
  return response.data
}

/**
 * Busca uma trilha pelo id.
 * @param {number} trailId
 * @returns {Promise<Object>}
 */
export const getTrailById = async (trailId) => {
  const response = await api.get(`/trails/${trailId}`)
  return response.data
}

// ─── Perguntas ────────────────────────────────────────────────

/**
 * Busca todas as perguntas de uma trilha.
 * @param {number} trailId
 * @returns {Promise<Array>}
 */
export const getQuestionsByTrail = async (trailId) => {
  const response = await api.get(`/questions/trail/${trailId}`)
  return response.data
}

// ─── Respostas ────────────────────────────────────────────────

/**
 * Busca as respostas de um aluno em uma trilha.
 * @param {number} studentId
 * @param {number} trailId
 * @returns {Promise<Array>}
 */
export const getAnswers = async (studentId, trailId) => {
  const response = await api.get('/answers', {
    params: { studentId, trailId },
  })
  return response.data
}

/**
 * Salva (ou substitui) uma resposta do aluno.
 * O backend substitui automaticamente respostas anteriores da mesma questão.
 *
 * @param {Object} payload
 * @param {string} payload.content          - texto livre (pode ser vazio para CHECKBOX)
 * @param {number} payload.studentId        - id do aluno
 * @param {number} payload.questionOptionId - id da opção selecionada
 * @returns {Promise<Object>}
 */
export const postAnswer = async ({ content = '', studentId, questionOptionId }) => {
  const response = await api.post('/answers', {
    content,
    studentId,
    questionOptionId,
  })
  return response.data
}
