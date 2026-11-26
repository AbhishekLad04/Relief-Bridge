// src/components/layout/DashboardLayout.jsx
import {
  BarChart3,
  BookOpen,
  CheckSquare,
  ChevronLeft,
  ChevronRight,
  Clipboard,
  FileText,
  Globe,
  Heart,
  Home,
  LayoutDashboard,
  LogOut,
  Menu,
  Plus,
  Shield,
  Users,
  UserCheck,
  Wallet,
  Bell,
} from 'lucide-react'
import { useState } from 'react'
import { Link, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'

const sidebarLinks = {
  ADMIN: [
    { label: 'Dashboard', icon: LayoutDashboard, to: '/admin', tab: 'overview' },
    { label: 'Users', icon: Users, to: '/admin', tab: 'users' },
    { label: 'NGO Approval', icon: UserCheck, to: '/admin', tab: 'ngo-approval' },
    { label: 'Disasters', icon: Globe, to: '/admin', tab: 'disasters' },
    { label: 'Campaigns', icon: BookOpen, to: '/admin', tab: 'campaigns' },
    { label: 'Donations', icon: Heart, to: '/admin', tab: 'donations' },
    { label: 'Fund Verification', icon: CheckSquare, to: '/admin', tab: 'fund-verification' },
    { label: 'Volunteers', icon: Users, to: '/admin', tab: 'volunteers' },
    { label: 'Reports', icon: BarChart3, to: '/admin', tab: 'reports' },
  ],
  NGO: [
    { label: 'Overview', icon: LayoutDashboard, to: '/ngo', tab: 'overview' },
    { label: 'My Campaigns', icon: BookOpen, to: '/ngo', tab: 'campaigns' },
    { label: 'Create Campaign', icon: Plus, to: '/ngo', tab: 'create-campaign' },
    { label: 'Donations', icon: Heart, to: '/ngo', tab: 'donations' },
    { label: 'Fund Utilization', icon: Wallet, to: '/ngo', tab: 'fund-utilization' },
    { label: 'Volunteers', icon: Users, to: '/ngo', tab: 'volunteers' },
    { label: 'Reports', icon: BarChart3, to: '/ngo', tab: 'reports' },
  ],
  CITIZEN: [
    { label: 'Overview', icon: Home, to: '/citizen', tab: 'overview' },
    { label: 'My Donations', icon: Heart, to: '/citizen', tab: 'donations' },
    { label: 'Volunteer Profile', icon: UserCheck, to: '/citizen', tab: 'volunteer-profile' },
    { label: 'My Applications', icon: Clipboard, to: '/citizen', tab: 'applications' },
    { label: 'My Assignments', icon: CheckSquare, to: '/citizen', tab: 'assignments' },
    { label: 'Notifications', icon: Bell, to: '/citizen', tab: 'notifications' },
  ],
}

export default function DashboardLayout({ role, children, activeTab, onTabChange }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)

  const links = sidebarLinks[role] || []

  const handleLogout = () => {
    logout()
    navigate('/')
  }

  const SidebarContent = () => (
    <div className="flex h-full flex-col">
      {/* Logo */}
      <div className={`flex items-center gap-2 px-4 py-5 border-b border-slate-700 ${collapsed ? 'justify-center' : ''}`}>
        <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center shrink-0">
          <span className="text-sm font-black text-white">RB</span>
        </div>
        {!collapsed && (
          <span className="text-base font-bold text-white">
            Relief<span className="text-orange-400">Bridge</span>
          </span>
        )}
      </div>

      {/* Role badge */}
      {!collapsed && (
        <div className="px-4 py-3 border-b border-slate-700">
          <p className="text-xs text-slate-400 uppercase tracking-widest">{role} Portal</p>
          <p className="mt-0.5 text-sm font-medium text-white truncate">{user?.name || user?.email}</p>
        </div>
      )}

      {/* Nav links */}
      <nav className="flex-1 overflow-y-auto py-4 px-2">
        {links.map(({ label, icon: Icon, tab }) => {
          const isActive = activeTab === tab
          return (
            <button
              key={tab}
              onClick={() => { onTabChange(tab); setMobileOpen(false) }}
              title={collapsed ? label : undefined}
              className={`w-full flex items-center gap-3 rounded-lg px-3 py-2.5 mb-1 text-left text-sm font-medium transition
                ${isActive
                  ? 'bg-cyan-700 text-white'
                  : 'text-slate-300 hover:bg-slate-700 hover:text-white'
                }
                ${collapsed ? 'justify-center' : ''}
              `}
            >
              <Icon className="h-4 w-4 shrink-0" />
              {!collapsed && <span>{label}</span>}
            </button>
          )
        })}
      </nav>

      {/* Bottom actions */}
      <div className={`border-t border-slate-700 px-2 py-3 space-y-1`}>
        <Link
          to="/"
          className={`flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-slate-400 hover:bg-slate-700 hover:text-white transition ${collapsed ? 'justify-center' : ''}`}
          title={collapsed ? 'Public Site' : undefined}
        >
          <Globe className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Public Site</span>}
        </Link>
        <button
          onClick={handleLogout}
          className={`w-full flex items-center gap-3 rounded-lg px-3 py-2 text-sm text-slate-400 hover:bg-red-700 hover:text-white transition ${collapsed ? 'justify-center' : ''}`}
          title={collapsed ? 'Logout' : undefined}
        >
          <LogOut className="h-4 w-4 shrink-0" />
          {!collapsed && <span>Logout</span>}
        </button>
      </div>
    </div>
  )

  return (
    <div className="flex h-screen overflow-hidden bg-slate-100">
      {/* Desktop sidebar */}
      <aside
        className={`hidden md:flex flex-col bg-slate-900 transition-all duration-300 ${collapsed ? 'w-16' : 'w-60'} shrink-0`}
      >
        <SidebarContent />
        {/* Collapse toggle */}
        <button
          onClick={() => setCollapsed(v => !v)}
          className="absolute top-16 left-0 hidden md:flex translate-x-full items-center justify-center h-6 w-6 rounded-full bg-slate-700 text-white hover:bg-cyan-700 transition z-10"
          style={{ left: collapsed ? '3.5rem' : '14.5rem' }}
        >
          {collapsed ? <ChevronRight className="h-3 w-3" /> : <ChevronLeft className="h-3 w-3" />}
        </button>
      </aside>

      {/* Mobile drawer */}
      {mobileOpen && (
        <div className="fixed inset-0 z-50 flex md:hidden">
          <div className="w-60 bg-slate-900 flex flex-col">
            <SidebarContent />
          </div>
          <div className="flex-1 bg-black/50" onClick={() => setMobileOpen(false)} />
        </div>
      )}

      {/* Main content */}
      <div className="flex flex-1 flex-col overflow-hidden">
        {/* Top bar */}
        <header className="flex h-14 items-center gap-3 border-b border-slate-200 bg-white px-4 shadow-sm">
          <button
            className="md:hidden rounded-lg p-1.5 text-slate-500 hover:bg-slate-100"
            onClick={() => setMobileOpen(true)}
          >
            <Menu className="h-5 w-5" />
          </button>
          <h1 className="text-base font-semibold text-slate-900 capitalize">
            {(activeTab || 'dashboard').replace(/-/g, ' ')}
          </h1>
        </header>

        {/* Page content */}
        <main className="flex-1 overflow-y-auto p-4 md:p-6">
          {children}
        </main>
      </div>
    </div>
  )
}
