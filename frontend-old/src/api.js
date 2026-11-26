// src/api.js – centralised Axios instance with JWT and proxy support
import axios from 'axios'

const api = axios.create({
  baseURL: '/api',  // proxied to http://localhost:8081/api by Vite
})

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('jwtToken')
  if (token) config.headers['Authorization'] = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  (res) => res,
  (err) => {
    if (err.response?.status === 401) {
      localStorage.removeItem('jwtToken')
      localStorage.removeItem('reliefbridge_user')
      window.location.href = '/login'
    }
    return Promise.reject(err)
  }
)

export default api
