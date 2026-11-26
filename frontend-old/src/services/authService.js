// src/services/authService.js
import api from '../api'

export const login = async (email, password) => {
  const res = await api.post('/auth/login', { email, password })
  if (res.data.token) localStorage.setItem('jwtToken', res.data.token)
  return res.data
}

export const register = async (dto) => {
  const res = await api.post('/auth/register', dto)
  if (res.data.token) localStorage.setItem('jwtToken', res.data.token)
  return res.data
}

export const logout = () => {
  localStorage.removeItem('jwtToken')
  localStorage.removeItem('reliefbridge_user')
}

export const getProfile = async () => {
  const res = await api.get('/auth/me')
  return res.data
}

export const updateProfile = async (dto) => {
  const res = await api.put('/auth/me', dto)
  return res.data
}
