/**
 * @file alunoService.js
 * @description Chamadas da jornada do aluno (RF-10..RF-16). Usa a instância `api`.
 */

import api from './api'

// ─── Jornada (RF-10, RF-11) ───────────────────────────────────
export const getCurrentJourney = async () => (await api.get('/journeys/current')).data
export const getJourneyView = async (id) => (await api.get(`/journeys/${id}`)).data
export const getTrailDetail = async (journeyId, trailId) =>
  (await api.get(`/journeys/${journeyId}/trails/${trailId}`)).data
export const saveAnswer = async (journeyId, payload) =>
  (await api.put(`/journeys/${journeyId}/answers`, payload)).data
export const saveSynthesis = async (journeyId, trailId, text) =>
  (await api.put(`/journeys/${journeyId}/trails/${trailId}/synthesis`, { text })).data

// ─── Etapa final / leque (RF-12) ──────────────────────────────
export const getFinalPhase = async (journeyId) => (await api.get(`/journeys/${journeyId}/final`)).data
export const completeJourney = async (journeyId, finalSynthesis, inDoubt) =>
  (await api.post(`/journeys/${journeyId}/complete`, { finalSynthesis, inDoubt })).data

// ─── Panorama / histórico (RF-13, RF-15) ──────────────────────
export const getPanorama = async (journeyId) => (await api.get(`/journeys/${journeyId}/panorama`)).data
export const getHistory = async () => (await api.get('/journeys/history')).data

// ─── Perfil (RF-14) ───────────────────────────────────────────
export const getProfile = async () => (await api.get('/student-profile')).data
export const updateProfile = async (payload) => (await api.put('/student-profile', payload)).data

// ─── Curadoria de links da trilha Informação (RF-06, RN-05) ────
export const getCuratedLinks = async () => {
  try {
    return (await api.get('/curated-links/for-me')).data
  } catch {
    return []
  }
}

// ─── Agendamentos e feedbacks do aluno (RF-16) ───────────────
export const getMyAppointments = async () => (await api.get('/appointments/mine')).data
export const getMyFeedbacks = async () => (await api.get('/feedbacks/mine')).data

export const errMsg = (e, fallback = 'Ocorreu um erro.') =>
  e?.response?.data?.error ||
  e?.response?.data?.errors?.map((x) => x.message).join(' ') ||
  e?.message ||
  fallback

export const TRAIL_LABEL = {
  AUTOCONHECIMENTO: 'Autoconhecimento',
  INFLUENCIAS: 'Influências',
  INFORMACAO: 'Informação',
  PLANO_DE_FUTURO: 'Projeto de Futuro',
}

export const AREA_LABEL = {
  HUMANAS: 'Humanas',
  EXATAS: 'Exatas',
  BIOLOGICAS: 'Biológicas',
  ARTES: 'Artes',
}
