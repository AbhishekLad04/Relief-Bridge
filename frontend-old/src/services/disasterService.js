// src/services/disasterService.js
import api from '../api'

export const getAllDisasters = async (params = {}) => {
  const res = await api.get('/disasters', { params })
  return res.data
}

export const getDisasterById = async (id) => {
  const res = await api.get(`/disasters/${id}`)
  return res.data
}

export const createDisaster = async (dto) => {
  const res = await api.post('/disasters', dto)
  return res.data
}

export const updateDisaster = async (id, dto) => {
  const res = await api.put(`/disasters/${id}`, dto)
  return res.data
}

export const deleteDisaster = async (id) => {
  const res = await api.delete(`/disasters/${id}`)
  return res.data
}
