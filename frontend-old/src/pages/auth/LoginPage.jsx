// src/pages/auth/LoginPage.jsx
import { AlertCircle, Heart } from 'lucide-react'
import { useState } from 'react'
import { Link, Navigate, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Spinner from '../../components/ui/Spinner'

function roleDashboard(role) {
  if (role === 'ADMIN') return '/admin'
  if (role === 'NGO') return '/ngo'
  return '/citizen'
}

export default function LoginPage() {
  const { user, login } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({ email: '', password: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  if (user) return <Navigate to={roleDashboard(user.role)} replace />

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      const { user: u } = await login(form.email, form.password)
      navigate(roleDashboard(u.role))
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Invalid credentials')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex">
      {/* Left branding */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-slate-900 to-cyan-900 flex-col justify-center items-center text-white p-12">
        <div className="mb-6 flex items-center gap-3">
          <div className="h-14 w-14 rounded-2xl bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center shadow-lg">
            <Heart className="h-7 w-7 text-white" />
          </div>
          <span className="text-3xl font-bold">Relief<span className="text-orange-400">Bridge</span></span>
        </div>
        <h2 className="text-4xl font-bold mb-4 text-center">AI-Powered Disaster Relief</h2>
        <p className="text-lg text-cyan-200 text-center max-w-sm">
          Connecting NGOs, donors, and volunteers for effective humanitarian assistance across India.
        </p>
        <div className="mt-10 grid grid-cols-3 gap-6">
          {[['500+', 'Campaigns'], ['50K+', 'Donors'], ['10K+', 'Volunteers']].map(([v, l]) => (
            <div key={l} className="text-center">
              <div className="text-2xl font-bold text-orange-400">{v}</div>
              <div className="text-sm text-cyan-300">{l}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Right login form */}
      <div className="flex-1 flex items-center justify-center bg-slate-50 p-6">
        <div className="w-full max-w-md">
          <div className="bg-white rounded-2xl shadow-xl p-8 border border-slate-100">
            <div className="mb-6 text-center">
              <div className="lg:hidden flex justify-center mb-4">
                <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center">
                  <Heart className="h-6 w-6 text-white" />
                </div>
              </div>
              <h1 className="text-2xl font-bold text-slate-900">Welcome back</h1>
              <p className="text-slate-500 mt-1 text-sm">Sign in to your ReliefBridge account</p>
            </div>

            {error && (
              <div className="mb-4 flex items-center gap-2 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm">
                <AlertCircle className="h-4 w-4 shrink-0" />
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Email address</label>
                <input
                  type="email"
                  required
                  value={form.email}
                  onChange={e => setForm(f => ({ ...f, email: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  placeholder="you@example.com"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Password</label>
                <input
                  type="password"
                  required
                  value={form.password}
                  onChange={e => setForm(f => ({ ...f, password: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  placeholder="Your password"
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="w-full flex items-center justify-center gap-2 rounded-lg bg-cyan-600 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition"
              >
                {loading ? <Spinner size="sm" /> : 'Sign In'}
              </button>
            </form>

            <div className="mt-6 text-center space-y-2">
              <p className="text-sm text-slate-500">
                Don't have an account?{' '}
                <Link to="/register" className="font-medium text-cyan-600 hover:text-cyan-700">Create one</Link>
              </p>
              <p className="text-xs text-slate-400">
                Admin login: admin@reliefbridge.com
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
