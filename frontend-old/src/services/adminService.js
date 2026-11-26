// src/services/adminService.js
import api from '../api'

// NGO Management
export const getPendingNgos = async () => {
  const res = await api.get('/users/me') // workaround - admin gets all users via userService
  return res.data
}

export const approveNgo = async (ngoId) => {
  const res = await api.patch(`/admin/ngos/${ngoId}/approve`)
  return res.data
}

// User management (admin reads through user controller for now)
export const getMyProfile = async () => {
  const res = await api.get('/users/me')
  return res.data
}

// Platform Stats - uses donations stats endpoint
export const getPlatformStats = async () => {
  const res = await api.get('/donations/stats')
  return res.data
}
