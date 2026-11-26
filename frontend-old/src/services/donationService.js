// src/services/donationService.js
import api from '../api'

export const createDonationOrder = async (dto) => {
  const res = await api.post('/donations/order', dto)
  return res.data  // {orderId, amount, currency, donationReference, ...}
}

export const getMockSignature = async (orderId, paymentId) => {
  const res = await api.get('/donations/mock-signature', { params: { orderId, paymentId } })
  return res.data  // {orderId, paymentId, signature}
}

export const verifyDonationPayment = async (dto) => {
  const res = await api.post('/donations/verify', dto)
  return res.data
}

export const getDonationReceipt = async (receiptOrRef) => {
  const res = await api.get(`/donations/receipt/${receiptOrRef}`)
  return res.data
}

export const getDonationByReference = async (reference) => {
  const res = await api.get(`/donations/reference/${reference}`)
  return res.data
}

export const getMyDonations = async (page = 0, size = 10) => {
  const res = await api.get('/donations/my', { params: { page, size } })
  return res.data
}

export const getCampaignDonations = async (campaignId, page = 0, size = 10) => {
  const res = await api.get(`/donations/campaign/${campaignId}`, { params: { page, size } })
  return res.data
}

export const getNgoDonations = async (page = 0, size = 10) => {
  const res = await api.get('/donations/ngo/my', { params: { page, size } })
  return res.data
}

export const getAllDonations = async (params = {}) => {
  const res = await api.get('/donations/admin/all', { params })
  return res.data
}

export const getDonationStats = async () => {
  const res = await api.get('/donations/stats')
  return res.data
}
