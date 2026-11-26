// src/services/fundUtilizationService.js
import api from '../api'

export const submitUtilization = async (campaignId, dto) => {
  const res = await api.post(`/fund-utilizations/campaigns/${campaignId}`, dto)
  return res.data
}

export const getUtilizationsByCampaign = async (campaignId, params = {}) => {
  const res = await api.get(`/fund-utilizations/campaigns/${campaignId}`, { params })
  return res.data
}

export const getAllUtilizations = async (params = {}) => {
  const res = await api.get('/fund-utilizations', { params })
  return res.data
}

export const verifyUtilization = async (utilizationId) => {
  const res = await api.post(`/fund-utilizations/${utilizationId}/verify`)
  return res.data
}

export const rejectUtilization = async (utilizationId, reason) => {
  const res = await api.post(`/fund-utilizations/${utilizationId}/reject`, { reason })
  return res.data
}
