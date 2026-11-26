// src/components/layout/Navbar.jsx
import { Menu, X } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import Badge from '../ui/Badge'
import NotificationBell from '../notifications/NotificationBell'

function roleDashboard(role) {
  if (role === 'ADMIN') return '/admin'
  if (role === 'NGO') return '/ngo'
  return '/citizen'
}

function roleBadgeColor(role) {
  if (role === 'ADMIN') return 'orange'
  if (role === 'NGO') return 'teal'
  return 'blue'
}

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [menuOpen, setMenuOpen] = useState(false)
  const [scrolled, setScrolled] = useState(false)

  useEffect(() => {
    const handler = () => setScrolled(window.scrollY > 10)
    window.addEventListener('scroll', handler)
    return () => window.removeEventListener('scroll', handler)
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const navLinks = [
    { label: 'Home', to: '/' },
    { label: 'Disasters', to: '/disasters' },
    { label: 'Campaigns', to: '/campaigns' },
    { label: 'About', to: '/about' },
  ]

  return (
    <nav className={`sticky top-0 z-40 transition-all duration-300 ${scrolled ? 'bg-slate-900 shadow-lg' : 'bg-slate-900'}`}>
      <div className="mx-auto max-w-7xl px-4 sm:px-6 lg:px-8">
        <div className="flex h-16 items-center justify-between">
          {/* Logo */}
          <Link to="/" className="flex items-center gap-2 shrink-0">
            <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center">
              <span className="text-sm font-black text-white">RB</span>
            </div>
            <span className="text-lg font-bold text-white">
              Relief<span className="text-orange-400">Bridge</span>
            </span>
          </Link>

          {/* Desktop nav */}
          <div className="hidden md:flex items-center gap-6">
            {navLinks.map(l => (
              <Link
                key={l.to}
                to={l.to}
                className="text-sm font-medium text-slate-300 hover:text-white transition"
              >
                {l.label}
              </Link>
            ))}
          </div>

          {/* Right side */}
          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <>
                <NotificationBell />
                <Link
                  to={roleDashboard(user.role)}
                  className="flex items-center gap-2 rounded-lg px-3 py-1.5 text-sm font-medium text-slate-300 hover:bg-slate-700 hover:text-white transition"
                >
                  <Badge label={user.role} color={roleBadgeColor(user.role)} />
                  <span>{user.name || user.email}</span>
                </Link>
                <button
                  onClick={handleLogout}
                  className="rounded-lg bg-orange-500 px-3 py-1.5 text-sm font-semibold text-white hover:bg-orange-600 transition"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link
                  to="/login"
                  className="text-sm font-medium text-slate-300 hover:text-white transition"
                >
                  Login
                </Link>
                <Link
                  to="/register"
                  className="rounded-lg bg-cyan-600 px-4 py-1.5 text-sm font-semibold text-white hover:bg-cyan-700 transition"
                >
                  Register
                </Link>
              </>
            )}
          </div>

          {/* Mobile hamburger */}
          <button
            onClick={() => setMenuOpen(v => !v)}
            className="md:hidden rounded-lg p-2 text-slate-300 hover:bg-slate-700 hover:text-white"
          >
            {menuOpen ? <X className="h-5 w-5" /> : <Menu className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {menuOpen && (
        <div className="md:hidden border-t border-slate-700 bg-slate-900 px-4 pb-4 pt-2 space-y-2">
          {navLinks.map(l => (
            <Link
              key={l.to}
              to={l.to}
              onClick={() => setMenuOpen(false)}
              className="block rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-700 hover:text-white"
            >
              {l.label}
            </Link>
          ))}
          <div className="border-t border-slate-700 pt-2">
            {user ? (
              <>
                <Link
                  to={roleDashboard(user.role)}
                  onClick={() => setMenuOpen(false)}
                  className="block rounded-lg px-3 py-2 text-sm font-medium text-slate-300 hover:bg-slate-700"
                >
                  Dashboard ({user.role})
                </Link>
                <button
                  onClick={() => { handleLogout(); setMenuOpen(false) }}
                  className="mt-1 block w-full rounded-lg bg-orange-500 px-3 py-2 text-left text-sm font-semibold text-white hover:bg-orange-600"
                >
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setMenuOpen(false)} className="block rounded-lg px-3 py-2 text-sm text-slate-300 hover:bg-slate-700">Login</Link>
                <Link to="/register" onClick={() => setMenuOpen(false)} className="mt-1 block rounded-lg bg-cyan-600 px-3 py-2 text-sm font-semibold text-white">Register</Link>
              </>
            )}
          </div>
        </div>
      )}
    </nav>
  )
}
