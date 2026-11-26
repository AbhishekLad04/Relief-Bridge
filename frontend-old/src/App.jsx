// src/App.jsx – Complete application routing for ReliefBridge
import { Navigate, Route, BrowserRouter as Router, Routes } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext'
import Spinner from './components/ui/Spinner'

// Public pages
import HomePage             from './pages/public/HomePage'
import CampaignsPage        from './pages/public/CampaignsPage'
import CampaignDetailPage   from './pages/public/CampaignDetailPage'
import DisastersPage        from './pages/public/DisastersPage'
import DisasterDetailPage   from './pages/public/DisasterDetailPage'

// Auth pages
import LoginPage            from './pages/auth/LoginPage'
import RegisterPage         from './pages/auth/RegisterPage'

// Donation pages
import DonatePage           from './pages/donation/DonatePage'
import ReceiptPage          from './pages/donation/ReceiptPage'

// Role dashboards
import AdminDashboard       from './pages/admin/AdminDashboard'
import NgoDashboard         from './pages/ngo/NgoDashboard'
import CitizenDashboard     from './pages/citizen/CitizenDashboard'

/** Guard: redirect to login if not authenticated */
function PrivateRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" /></div>
  if (!user)   return <Navigate to="/login" replace />
  return children
}

/** Guard: redirect if already logged in */
function GuestRoute({ children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" /></div>
  if (user)   return <Navigate to={rolePath(user.role)} replace />
  return children
}

/** Guard: require specific role */
function RoleRoute({ role, children }) {
  const { user, loading } = useAuth()
  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" /></div>
  if (!user)         return <Navigate to="/login" replace />
  if (user.role !== role) return <Navigate to={rolePath(user.role)} replace />
  return children
}

function rolePath(role) {
  if (role === 'ADMIN') return '/admin'
  if (role === 'NGO')   return '/ngo'
  return '/citizen'
}

function NotFoundPage() {
  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-slate-50 text-center px-4">
      <div className="text-6xl font-bold text-slate-200 mb-4">404</div>
      <h1 className="text-2xl font-bold text-slate-900 mb-2">Page Not Found</h1>
      <p className="text-slate-500 mb-6">The page you're looking for doesn't exist or has been moved.</p>
      <a href="/" className="rounded-lg bg-cyan-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 transition">
        Go Home
      </a>
    </div>
  )
}

function AppRoutes() {
  return (
    <Routes>
      {/* Public */}
      <Route path="/"                    element={<HomePage />} />
      <Route path="/campaigns"           element={<CampaignsPage />} />
      <Route path="/campaigns/:id"       element={<CampaignDetailPage />} />
      <Route path="/disasters"           element={<DisastersPage />} />
      <Route path="/disasters/:id"       element={<DisasterDetailPage />} />

      {/* Auth */}
      <Route path="/login"    element={<GuestRoute><LoginPage /></GuestRoute>} />
      <Route path="/register" element={<GuestRoute><RegisterPage /></GuestRoute>} />

      {/* Protected – Donation */}
      <Route path="/donate/:campaignId" element={<PrivateRoute><DonatePage /></PrivateRoute>} />
      <Route path="/receipt/:id"        element={<PrivateRoute><ReceiptPage /></PrivateRoute>} />

      {/* Role dashboards */}
      <Route path="/admin/*" element={<RoleRoute role="ADMIN"><AdminDashboard /></RoleRoute>} />
      <Route path="/ngo/*"   element={<RoleRoute role="NGO"><NgoDashboard /></RoleRoute>} />
      <Route path="/citizen/*" element={<RoleRoute role="CITIZEN"><CitizenDashboard /></RoleRoute>} />

      {/* 404 */}
      <Route path="*" element={<NotFoundPage />} />
    </Routes>
  )
}

export default function App() {
  return (
    <Router>
      <AuthProvider>
        <AppRoutes />
      </AuthProvider>
    </Router>
  )
}
