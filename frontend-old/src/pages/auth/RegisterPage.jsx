// src/pages/auth/RegisterPage.jsx
import { AlertCircle, Heart } from 'lucide-react'
import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Spinner from '../../components/ui/Spinner'

export default function RegisterPage() {
  const { register } = useAuth()
  const navigate = useNavigate()
  const [form, setForm] = useState({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
    role: 'CITIZEN',
    isDonor: true,
    isVolunteer: false,
    skills: '',
    orgName: '',
    regNo: '',
  })
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [loading, setLoading] = useState(false)

  const isNgo = form.role === 'NGO'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    if (form.password !== form.confirmPassword) {
      setError('Passwords do not match.')
      return
    }
    if (form.password.length < 12) {
      setError('Password must contain at least 12 characters.')
      return
    }
    if (form.role === 'CITIZEN' && form.isVolunteer && !form.skills.trim()) {
      setError('Skills are required when volunteer option is selected.')
      return
    }
    if (form.role === 'NGO') {
      if (!form.orgName.trim()) {
        setError('Organisation name is required for NGO.')
        return
      }
      if (!form.regNo.trim()) {
        setError('Registration number is required for NGO.')
        return
      }
    }

    setLoading(true)
    try {
      const dto = {
        name: form.name.trim(),
        email: form.email.trim(),
        password: form.password,
        role: form.role,
        isDonor: form.role === 'CITIZEN' ? form.isDonor : false,
        isVolunteer: form.role === 'CITIZEN' ? form.isVolunteer : false,
        skills: form.role === 'CITIZEN' && form.isVolunteer ? form.skills.trim() : null,
        orgName: form.role === 'NGO' ? form.orgName.trim() : null,
        regNo: form.role === 'NGO' ? form.regNo.trim() : null,
      }
      await register(dto)
      setSuccess(
        isNgo
          ? 'Registration submitted! Your NGO account is pending admin approval. You can login once approved.'
          : 'Registration successful! You can now log in.'
      )
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Registration failed')
    } finally {
      setLoading(false)
    }
  }

  if (success) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-slate-50 p-6">
        <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center border border-slate-100">
          <div className="mb-4 inline-flex h-16 w-16 items-center justify-center rounded-full bg-green-100">
            <Heart className="h-8 w-8 text-green-600" />
          </div>
          <h2 className="text-xl font-bold text-slate-900 mb-2">Registration Successful!</h2>
          <p className="text-slate-500 mb-6 text-sm">{success}</p>
          <Link
            to="/login"
            className="inline-block rounded-lg bg-cyan-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 transition"
          >
            Go to Login
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
      <div className="w-full max-w-lg">
        <div className="bg-white rounded-2xl shadow-xl p-8 border border-slate-100">
          <div className="mb-6 text-center">
            <div className="flex justify-center mb-3">
              <div className="h-12 w-12 rounded-xl bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center">
                <Heart className="h-6 w-6 text-white" />
              </div>
            </div>
            <h1 className="text-2xl font-bold text-slate-900">Join ReliefBridge</h1>
            <p className="text-slate-500 mt-1 text-sm">Create your account to make a difference</p>
          </div>

          {error && (
            <div className="mb-4 flex items-center gap-2 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm">
              <AlertCircle className="h-4 w-4 shrink-0" />
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Role selector */}
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">I am registering as</label>
              <div className="flex gap-3">
                {['CITIZEN', 'NGO'].map((r) => (
                  <button
                    type="button"
                    key={r}
                    onClick={() => setForm((f) => ({ ...f, role: r }))}
                    className={`flex-1 rounded-lg border py-2.5 text-sm font-medium transition ${
                      form.role === r
                        ? 'border-cyan-600 bg-cyan-50 text-cyan-700'
                        : 'border-slate-300 text-slate-600 hover:border-slate-400'
                    }`}
                  >
                    {r === 'CITIZEN' ? '👤 Citizen / Volunteer' : '🏢 Registered NGO'}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Full Name *</label>
              <input
                type="text"
                required
                value={form.name}
                onChange={(e) => setForm((f) => ({ ...f, name: e.target.value }))}
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                placeholder="Your full name"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Email address *</label>
              <input
                type="email"
                required
                value={form.email}
                onChange={(e) => setForm((f) => ({ ...f, email: e.target.value }))}
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                placeholder="you@example.com"
              />
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Password *</label>
                <input
                  type="password"
                  required
                  value={form.password}
                  onChange={(e) => setForm((f) => ({ ...f, password: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  placeholder="Min 12 characters"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Confirm Password *</label>
                <input
                  type="password"
                  required
                  value={form.confirmPassword}
                  onChange={(e) => setForm((f) => ({ ...f, confirmPassword: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  placeholder="Repeat password"
                />
              </div>
            </div>

            {/* Citizen capability options */}
            {!isNgo && (
              <div className="rounded-lg bg-slate-50 border border-slate-200 p-4 space-y-3">
                <p className="text-xs font-semibold text-slate-700 uppercase tracking-wide">
                  Citizen Participation
                </p>
                <div className="flex gap-4">
                  <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={form.isDonor}
                      onChange={(e) => setForm((f) => ({ ...f, isDonor: e.target.checked }))}
                      className="rounded border-slate-300 text-cyan-600 focus:ring-cyan-500"
                    />
                    I want to be a Donor
                  </label>
                  <label className="flex items-center gap-2 text-sm text-slate-700 cursor-pointer">
                    <input
                      type="checkbox"
                      checked={form.isVolunteer}
                      onChange={(e) => setForm((f) => ({ ...f, isVolunteer: e.target.checked }))}
                      className="rounded border-slate-300 text-cyan-600 focus:ring-cyan-500"
                    />
                    I want to Volunteer
                  </label>
                </div>
                {form.isVolunteer && (
                  <div>
                    <label className="block text-xs font-medium text-slate-700 mb-1">
                      Skills & Expertise * (Required for Volunteers)
                    </label>
                    <input
                      type="text"
                      required={form.isVolunteer}
                      value={form.skills}
                      onChange={(e) => setForm((f) => ({ ...f, skills: e.target.value }))}
                      className="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm focus:border-cyan-500 focus:outline-none"
                      placeholder="e.g. First Aid, Disaster Management, Driving, Medical"
                    />
                  </div>
                )}
              </div>
            )}

            {/* NGO Details */}
            {isNgo && (
              <div className="space-y-4 rounded-lg bg-cyan-50 border border-cyan-200 p-4">
                <p className="text-xs font-semibold text-cyan-700 uppercase tracking-wide">
                  NGO Verification Details
                </p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">
                      Organisation Name *
                    </label>
                    <input
                      type="text"
                      required={isNgo}
                      value={form.orgName}
                      onChange={(e) => setForm((f) => ({ ...f, orgName: e.target.value }))}
                      className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                      placeholder="Official registered name"
                    />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">
                      Registration Number *
                    </label>
                    <input
                      type="text"
                      required={isNgo}
                      value={form.regNo}
                      onChange={(e) => setForm((f) => ({ ...f, regNo: e.target.value }))}
                      className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                      placeholder="e.g. DL/2020/0123456"
                    />
                  </div>
                </div>
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full flex items-center justify-center gap-2 rounded-lg bg-cyan-600 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition"
            >
              {loading ? <Spinner size="sm" /> : 'Create Account'}
            </button>
          </form>

          <p className="mt-4 text-center text-sm text-slate-500">
            Already have an account?{' '}
            <Link to="/login" className="font-medium text-cyan-600 hover:text-cyan-700">
              Sign in
            </Link>
          </p>
        </div>
      </div>
    </div>
  )
}
