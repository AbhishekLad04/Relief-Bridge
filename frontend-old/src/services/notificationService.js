// src/services/notificationService.js
import api from '../api'

export const getMyNotifications = async (page = 0, size = 20) => {
  const res = await api.get('/notifications', { params: { page, size } })
  return res.data
}

export const getUnreadCount = async () => {
  const res = await api.get('/notifications/unread-count')
  return res.data  // {unreadCount: N}
}

export const getUnreadNotifications = async () => {
  const res = await api.get('/notifications/unread')
  return res.data
}

export const markAsRead = async (id) => {
  const res = await api.patch(`/notifications/${id}/read`)
  return res.data
}

export const markAllAsRead = async () => {
  const res = await api.patch('/notifications/read-all')
  return res.data
}

export const deleteNotification = async (id) => {
  const res = await api.delete(`/notifications/${id}`)
  return res.data
}
