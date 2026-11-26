// src/pages/admin/AdminDashboard.jsx
import {
  AlertTriangle, BookOpen, CheckSquare, Heart, PlusCircle,
  RefreshCcw, Shield, Users, XCircle
} from 'lucide-react'
import { useEffect, useState } from 'react'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Spinner from '../../components/ui/Spinner'
import StatCard from '../../components/ui/StatCard'
import { approveNgo } from '../../services/adminService'
import { getAllCampaigns } from '../../services/campaignService'
import { getAllDisasters, createDisaster } from '../../services/disasterService'
import { getAllDonations, getDonationStats } from '../../services/donationService'
import { getAllUtilizations, verifyUtilization } from '../../services/fundUtilizationService'

const DISASTER_TYPES     = ['FLOOD', 'EARTHQUAKE', 'CYCLONE', 'DROUGHT', 'FIRE', 'LANDSLIDE', 'TSUNAMI', 'OTHER']
const DISASTER_SEVERITIES = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']

export default function AdminDashboard() {
  const [tab, setTab] = useState('overview')

  // Data
  const [stats, setStats]           = useState(null)
  const [campaigns, setCampaigns]   = useState([])
  const [disasters, setDisasters]   = useState([])
  const [donations, setDonations]   = useState([])
  const [utilizations, setUtils]    = useState([])
  const [pendingNgos, setPending]   = useState([])
  const [loading, setLoading]       = useState({})
  const [error, setError]           = useState('')
  const [msg, setMsg]               = useState('')

  // Create disaster form
  const [disasterForm, setDFrm] = useState({
    title: '', description: '', type: 'FLOOD', severity: 'MEDIUM', location: '', occurredAt: ''
  })
  const [creatingDisaster, setCreatingDisaster] = useState(false)
  const [showDisasterForm, setShowDisasterForm] = useState(false)

  const load = (key, fn) => {
    setLoading(l => ({ ...l, [key]: true }))
    return fn().finally(() => setLoading(l => ({ ...l, [key]: false })))
  }

  useEffect(() => {
    load('stats', () => getDonationStats().then(setStats).catch(() => {}))
    load('campaigns', () => getAllCampaigns({ size: 100 }).then(d => setCampaigns(d.content || [])).catch(() => {}))
    load('disasters', () => getAllDisasters().then(d => setDisasters(Array.isArray(d) ? d : d.content || [])).catch(() => {}))
    load('donations', () => getAllDonations({ size: 50 }).then(d => setDonations(d.content || [])).catch(() => {}))
    load('utils', () => getAllUtilizations({ size: 50 }).then(d => setUtils(d.content || d || [])).catch(() => {}))
  }, [])

  const flash = (m) => { setMsg(m); setTimeout(() => setMsg(''), 3000) }

  const handleApprove = async (ngoId) => {
    try {
      await approveNgo(ngoId)
      flash('NGO approved successfully!')
      setPending(p => p.filter(u => u.id !== ngoId))
    } catch { setError('Failed to approve NGO') }
  }

  const handleVerifyUtil = async (utilId) => {
    try {
      await verifyUtilization(utilId)
      setUtils(u => u.map(x => x.id === utilId ? { ...x, status: 'VERIFIED' } : x))
      flash('Utilization record verified!')
    } catch { setError('Verification failed') }
  }

  const handleCreateDisaster = async (e) => {
    e.preventDefault()
    setCreatingDisaster(true)
    try {
      const d = await createDisaster(disasterForm)
      setDisasters(prev => [d, ...prev])
      setShowDisasterForm(false)
      setDFrm({ title: '', description: '', type: 'FLOOD', severity: 'MEDIUM', location: '', occurredAt: '' })
      flash('Disaster created successfully!')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create disaster')
    } finally {
      setCreatingDisaster(false)
    }
  }

  return (
    <DashboardLayout role="ADMIN" activeTab={tab} onTabChange={setTab}>
      {msg   && <div className="mb-4 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-green-700 text-sm">{msg}</div>}
      {error && <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm flex justify-between"><span>{error}</span><button onClick={() => setError('')}>✕</button></div>}

      {/* ---- OVERVIEW ---- */}
      {tab === 'overview' && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold text-slate-900">Platform Overview</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard title="Total Raised" value={stats ? `₹${Number(stats.totalCollected || 0).toLocaleString('en-IN')}` : '—'} icon={Heart} color="text-green-600" />
            <StatCard title="Funds Utilized" value={stats ? `₹${Number(stats.totalUtilized || 0).toLocaleString('en-IN')}` : '—'} icon={CheckSquare} color="text-cyan-600" />
            <StatCard title="Total Donations" value={stats?.totalDonations ?? '—'} icon={Heart} color="text-orange-500" />
            <StatCard title="Active Campaigns" value={stats?.activeCampaigns ?? campaigns.filter(c => c.status === 'ACTIVE').length} icon={BookOpen} color="text-purple-600" />
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
              <h3 className="font-semibold text-slate-900 mb-3">Recent Campaigns</h3>
              {campaigns.slice(0, 5).map(c => (
                <div key={c.id} className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
                  <span className="text-sm text-slate-700 truncate max-w-xs">{c.title}</span>
                  <StatusBadge status={c.status} />
                </div>
              ))}
            </div>
            <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
              <h3 className="font-semibold text-slate-900 mb-3">Recent Donations</h3>
              {donations.slice(0, 5).map(d => (
                <div key={d.id} className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
                  <span className="text-sm text-slate-700">{d.donorName || 'Anonymous'}</span>
                  <span className="text-sm font-medium text-green-600">₹{Number(d.amount).toLocaleString('en-IN')}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* ---- NGO APPROVAL ---- */}
      {tab === 'ngo-approval' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">NGO Approval Requests</h2>
          <div className="bg-amber-50 border border-amber-200 rounded-xl p-4 text-sm text-amber-700 mb-4">
            To view pending NGOs: query the user database for NGOs with approved=false. Contact the database or use the platform API.
            Approve any NGO by their User ID below.
          </div>
          <ApproveNgoForm onApprove={handleApprove} />
        </div>
      )}

      {/* ---- DISASTERS ---- */}
      {tab === 'disasters' && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-slate-900">Disaster Management</h2>
            <button onClick={() => setShowDisasterForm(v => !v)}
              className="flex items-center gap-2 rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold text-white hover:bg-cyan-700 transition">
              <PlusCircle className="h-4 w-4" /> Create Disaster
            </button>
          </div>

          {showDisasterForm && (
            <form onSubmit={handleCreateDisaster} className="bg-white rounded-xl border border-slate-200 p-6 mb-6 shadow-sm space-y-4">
              <h3 className="font-semibold text-slate-900">New Disaster</h3>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <Field label="Title *" value={disasterForm.title} onChange={v => setDFrm(f => ({ ...f, title: v }))} placeholder="e.g. Kerala Floods 2026" required />
                <Field label="Location" value={disasterForm.location} onChange={v => setDFrm(f => ({ ...f, location: v }))} placeholder="e.g. Wayanad, Kerala" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                <textarea rows={2} value={disasterForm.description}
                  onChange={e => setDFrm(f => ({ ...f, description: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Type</label>
                  <select value={disasterForm.type} onChange={e => setDFrm(f => ({ ...f, type: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    {DISASTER_TYPES.map(t => <option key={t}>{t}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Severity</label>
                  <select value={disasterForm.severity} onChange={e => setDFrm(f => ({ ...f, severity: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    {DISASTER_SEVERITIES.map(s => <option key={s}>{s}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Occurred At</label>
                  <input type="date" value={disasterForm.occurredAt}
                    onChange={e => setDFrm(f => ({ ...f, occurredAt: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
                </div>
              </div>
              <div className="flex gap-3">
                <button type="submit" disabled={creatingDisaster}
                  className="flex items-center gap-2 rounded-lg bg-cyan-600 px-5 py-2 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition">
                  {creatingDisaster ? <Spinner size="sm" /> : 'Create'}
                </button>
                <button type="button" onClick={() => setShowDisasterForm(false)}
                  className="rounded-lg border border-slate-300 px-5 py-2 text-sm text-slate-700 hover:bg-slate-50 transition">Cancel</button>
              </div>
            </form>
          )}

          <div className="space-y-3">
            {loading.disasters ? <Spinner center /> : disasters.map(d => (
              <div key={d.id} className="bg-white rounded-xl border border-slate-200 p-4 flex items-start justify-between shadow-sm">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${d.severity === 'CRITICAL' ? 'bg-red-100 text-red-700' : 'bg-orange-100 text-orange-700'}`}>{d.severity}</span>
                    <span className="text-xs text-slate-400">{d.type}</span>
                  </div>
                  <h4 className="font-medium text-slate-900">{d.title}</h4>
                  {d.location && <p className="text-xs text-slate-400 mt-0.5">{d.location}</p>}
                </div>
                <span className="text-xs text-slate-400">{d.occurredAt ? new Date(d.occurredAt).toLocaleDateString('en-IN') : ''}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ---- CAMPAIGNS ---- */}
      {tab === 'campaigns' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">All Campaigns</h2>
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-hidden">
            <table className="w-full text-sm">
              <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
                <tr>
                  {['Title', 'NGO', 'Status', 'Target', 'Start', 'End'].map(h => (
                    <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {loading.campaigns ? (
                  <tr><td colSpan={6} className="py-8 text-center"><Spinner center /></td></tr>
                ) : campaigns.map(c => (
                  <tr key={c.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 font-medium text-slate-900 max-w-xs truncate">{c.title}</td>
                    <td className="px-4 py-3 text-slate-500">{c.ngoName || c.ngo?.name || '—'}</td>
                    <td className="px-4 py-3"><StatusBadge status={c.status} /></td>
                    <td className="px-4 py-3 text-slate-700">₹{Number(c.targetAmount || 0).toLocaleString('en-IN')}</td>
                    <td className="px-4 py-3 text-slate-400">{c.startDate ? new Date(c.startDate).toLocaleDateString('en-IN') : '—'}</td>
                    <td className="px-4 py-3 text-slate-400">{c.endDate   ? new Date(c.endDate).toLocaleDateString('en-IN') : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ---- DONATIONS ---- */}
      {tab === 'donations' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">All Donations</h2>
          <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
            <table className="w-full text-sm min-w-[700px]">
              <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
                <tr>
                  {['Donor', 'Campaign', 'Amount', 'Status', 'Date', 'Reference'].map(h => (
                    <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {loading.donations ? (
                  <tr><td colSpan={6} className="py-8 text-center"><Spinner center /></td></tr>
                ) : donations.map(d => (
                  <tr key={d.id} className="hover:bg-slate-50">
                    <td className="px-4 py-3 text-slate-900">{d.donorName || 'Anonymous'}</td>
                    <td className="px-4 py-3 text-slate-500 max-w-[180px] truncate">{d.campaignTitle || '—'}</td>
                    <td className="px-4 py-3 font-medium text-green-600">₹{Number(d.amount).toLocaleString('en-IN')}</td>
                    <td className="px-4 py-3"><StatusBadge status={d.status} /></td>
                    <td className="px-4 py-3 text-slate-400">{d.createdAt ? new Date(d.createdAt).toLocaleDateString('en-IN') : '—'}</td>
                    <td className="px-4 py-3 text-slate-400 font-mono text-xs">{d.donationReference}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* ---- FUND VERIFICATION ---- */}
      {tab === 'fund-verification' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">Fund Utilization Verification</h2>
          <div className="space-y-3">
            {loading.utils ? <Spinner center /> : utilizations.map(u => (
              <div key={u.id} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm flex items-start justify-between gap-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-1">
                    <span className="text-xs text-slate-400">{u.category}</span>
                    <span className={`text-xs font-semibold px-2 py-0.5 rounded-full ${u.status === 'VERIFIED' ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'}`}>{u.status}</span>
                  </div>
                  <h4 className="font-medium text-slate-900">{u.description}</h4>
                  <p className="text-sm text-slate-500 mt-0.5">Campaign: {u.campaignTitle || '—'} • Amount: ₹{Number(u.amount).toLocaleString('en-IN')}</p>
                  <p className="text-xs text-slate-400 mt-1">{u.expenseDate ? new Date(u.expenseDate).toLocaleDateString('en-IN') : ''}</p>
                </div>
                {u.status !== 'VERIFIED' && (
                  <button onClick={() => handleVerifyUtil(u.id)}
                    className="flex items-center gap-1 rounded-lg bg-green-600 px-4 py-2 text-xs font-semibold text-white hover:bg-green-700 transition shrink-0">
                    <CheckSquare className="h-3 w-3" /> Verify
                  </button>
                )}
              </div>
            ))}
            {!loading.utils && utilizations.length === 0 && (
              <div className="text-center py-16 text-slate-400 bg-white rounded-xl border border-slate-200">No utilization records yet.</div>
            )}
          </div>
        </div>
      )}

      {/* ---- REPORTS ---- */}
      {tab === 'reports' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">Reports & Analytics</h2>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-6">
            <StatCard title="Total Raised"      value={stats ? `₹${Number(stats.totalCollected || 0).toLocaleString('en-IN')}` : '—'} icon={Heart} color="text-green-600" />
            <StatCard title="Total Utilized"    value={stats ? `₹${Number(stats.totalUtilized || 0).toLocaleString('en-IN')}` : '—'} icon={CheckSquare} color="text-cyan-600" />
            <StatCard title="Total Donations"   value={stats?.totalDonations ?? '—'} icon={Heart} color="text-orange-500" />
            <StatCard title="Active Campaigns"  value={stats?.activeCampaigns ?? '—'} icon={BookOpen} color="text-purple-600" />
          </div>
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm text-center text-slate-400">
            Advanced charts and reports coming soon. Financial summary available per campaign.
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

function ApproveNgoForm({ onApprove }) {
  const [ngoId, setNgoId] = useState('')
  return (
    <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm max-w-md">
      <h3 className="font-semibold text-slate-900 mb-3">Approve NGO by User ID</h3>
      <div className="flex gap-3">
        <input type="number" placeholder="Enter NGO User ID" value={ngoId}
          onChange={e => setNgoId(e.target.value)}
          className="flex-1 rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
        <button onClick={() => onApprove(ngoId)}
          className="rounded-lg bg-green-600 px-5 py-2.5 text-sm font-semibold text-white hover:bg-green-700 transition">
          Approve
        </button>
      </div>
    </div>
  )
}

function StatusBadge({ status }) {
  const map = {
    ACTIVE:    'bg-green-100 text-green-700',
    PENDING:   'bg-yellow-100 text-yellow-700',
    COMPLETED: 'bg-blue-100 text-blue-700',
    SUCCESS:   'bg-green-100 text-green-700',
    FAILED:    'bg-red-100 text-red-700',
    CANCELLED: 'bg-slate-100 text-slate-500',
    VERIFIED:  'bg-green-100 text-green-700',
    INACTIVE:  'bg-slate-100 text-slate-500',
    DRAFT:     'bg-slate-100 text-slate-500',
  }
  return (
    <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${map[status] || 'bg-slate-100 text-slate-500'}`}>
      {status}
    </span>
  )
}

function Field({ label, value, onChange, placeholder, required }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      <input type="text" value={value} required={required} placeholder={placeholder}
        onChange={e => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
    </div>
  )
}
