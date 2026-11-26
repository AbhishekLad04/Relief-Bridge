// src/services/campaignService.js
import api from '../api'

export const getAllCampaigns = async (params = {}) => {
  const res = await api.get('/campaigns', { params })
  return res.data
}

export const getMyCampaigns = async () => {
  const res = await api.get('/campaigns/my')
  return res.data
}

export const getCampaignById = async (id) => {
  const res = await api.get(`/campaigns/${id}`)
  return res.data
}

export const createCampaign = async (dto) => {
  const res = await api.post('/campaigns', dto)
  return res.data
}

export const updateCampaign = async (id, dto) => {
  const res = await api.put(`/campaigns/${id}`, dto)
  return res.data
}

export const updateCampaignStatus = async (id, status) => {
  const res = await api.patch(`/campaigns/${id}/status`, { status })
  return res.data
}

// Note: backend endpoint is /financials not /financial-summary
export const getFinancialSummary = async (campaignId) => {
  const res = await api.get(`/campaigns/${campaignId}/financials`)
  return res.data
}
