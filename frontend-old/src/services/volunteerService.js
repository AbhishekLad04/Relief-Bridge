// src/services/volunteerService.js
import api from '../api'

// --- Volunteer Profile (all roles, primarily CITIZEN) ---
export const getMyVolunteerProfile = async () => {
  const res = await api.get('/volunteer/profile')
  return res.data
}

export const createVolunteerProfile = async (dto) => {
  const res = await api.post('/volunteer/profile', dto)
  return res.data
}

export const updateVolunteerProfile = async (dto) => {
  const res = await api.put('/volunteer/profile', dto)
  return res.data
}

// --- Volunteer Requirements (NGO) ---
export const createRequirement = async (dto) => {
  const res = await api.post('/volunteer/requirements', dto)
  return res.data
}

export const getMyRequirements = async () => {
  const res = await api.get('/volunteer/requirements/my')
  return res.data
}

export const getRequirementById = async (requirementId) => {
  const res = await api.get(`/volunteer/requirements/${requirementId}`)
  return res.data
}

// --- AI Matching (NGO) ---
export const generateAiMatches = async (requirementId) => {
  const res = await api.post(`/volunteer/matches/requirements/${requirementId}/generate`)
  return res.data
}

export const getAiMatches = async (requirementId) => {
  const res = await api.get(`/volunteer/matches/requirements/${requirementId}`)
  return res.data
}

export const updateRecommendationStatus = async (matchId, status) => {
  const res = await api.patch(`/volunteer/matches/${matchId}/status`, { status })
  return res.data
}

// --- Assignments ---
export const getMyAssignments = async () => {
  const res = await api.get('/volunteer/assignments/my')
  return res.data
}

export const createAssignment = async (dto) => {
  const res = await api.post('/volunteer/assignments', dto)
  return res.data
}

export const updateAssignmentStatus = async (assignmentId, status) => {
  const res = await api.patch(`/volunteer/assignments/${assignmentId}/status`, { status })
  return res.data
}
