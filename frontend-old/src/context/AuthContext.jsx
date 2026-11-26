// src/context/AuthContext.jsx
import { createContext, useCallback, useContext, useEffect, useState } from 'react'
import { login as apiLogin, register as apiRegister } from '../services/authService'

const AuthContext = createContext(null)

function parseUserFromAuth(data) {
  return {
    id:          data.userId,
    name:        data.name,
    email:       data.email,
    role:        data.role,       // 'ADMIN' | 'NGO' | 'CITIZEN'
    isDonor:     data.isDonor,
    isVolunteer: data.isVolunteer,
    approved:    data.approved,
  }
}

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const storedToken = localStorage.getItem('jwtToken')
    const storedUser  = localStorage.getItem('reliefbridge_user')
    if (storedToken && storedUser) {
      try { setUser(JSON.parse(storedUser)) } catch { /* ignore */ }
    }
    setLoading(false)
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await apiLogin(email, password)
    const u = parseUserFromAuth(data)
    setUser(u)
    localStorage.setItem('reliefbridge_user', JSON.stringify(u))
    return { ...data, user: u }
  }, [])

  const register = useCallback(async (dto) => {
    const data = await apiRegister(dto)
    // register doesn't return a token; user must log in after
    return data
  }, [])

  const logout = useCallback(() => {
    setUser(null)
    localStorage.removeItem('jwtToken')
    localStorage.removeItem('reliefbridge_user')
  }, [])

  const updateUser = useCallback((updates) => {
    setUser(prev => {
      const updated = { ...prev, ...updates }
      localStorage.setItem('reliefbridge_user', JSON.stringify(updated))
      return updated
    })
  }, [])

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout, updateUser }}>
      {children}
    </AuthContext.Provider>
  )
}

export const useAuth = () => useContext(AuthContext)
