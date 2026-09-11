/**
 * @file adminService.js
 * @description Chamadas do painel do administrador (RF-01, RF-02).
 * Usa a instância `api` (axios) já configurada com base URL + Bearer token.
 */

import api from './api'

// ─── Instituições (RF-01) ─────────────────────────────────────

export const listInstitutions = async () => (await api.get('/institutions')).data

export const getInstitution = async (id) => (await api.get(`/institutions/${id}`)).data

export const createInstitution = async (payload) =>
  (await api.post('/institutions', payload)).data

export const updateInstitution = async (id, payload) =>
  (await api.put(`/institutions/${id}`, payload)).data

export const deleteInstitution = async (id) => {
  await api.delete(`/institutions/${id}`)
}

// ─── Turmas (RF-01) ───────────────────────────────────────────

export const listClasses = async (institutionId) =>
  (await api.get('/classes', { params: institutionId ? { institutionId } : {} })).data

export const createClass = async (payload) => (await api.post('/classes', payload)).data

export const updateClass = async (id, payload) =>
  (await api.put(`/classes/${id}`, payload)).data

export const deleteClass = async (id) => {
  await api.delete(`/classes/${id}`)
}

// ─── Usuários / contas (RF-02) — preenchido no Bloco 3 ────────

export const listManagedUsers = async (params) =>
  (await api.get('/admin/users', { params })).data

export const createManagedUser = async (payload) =>
  (await api.post('/admin/users', payload)).data

export const updateManagedUser = async (id, payload) =>
  (await api.put(`/admin/users/${id}`, payload)).data

export const setUserActive = async (id, active, reason) =>
  (await api.patch(`/admin/users/${id}/status`, { active, reason })).data

// ─── Helper de mensagem de erro padronizada ──────────────────

export const errorMessage = (err, fallback = 'Ocorreu um erro.') =>
  err?.response?.data?.error ||
  err?.response?.data?.errors?.map((e) => e.message).join(' ') ||
  err?.message ||
  fallback
