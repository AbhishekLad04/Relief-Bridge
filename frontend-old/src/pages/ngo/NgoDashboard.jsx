// src/pages/ngo/NgoDashboard.jsx
import { BarChart3, BookOpen, CheckSquare, Heart, PlusCircle, Users } from 'lucide-react'
import { useEffect, useState } from 'react'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Spinner from '../../components/ui/Spinner'
import StatCard from '../../components/ui/StatCard'
import { useAuth } from '../../context/AuthContext'
import { getAllCampaigns, getMyCampaigns, createCampaign, getFinancialSummary } from '../../services/campaignService'
import { getAllDisasters } from '../../services/disasterService'
import { getNgoDonations } from '../../services/donationService'
import { getUtilizationsByCampaign, submitUtilization } from '../../services/fundUtilizationService'
import { generateAiMatches, getAiMatches, createRequirement, getMyRequirements } from '../../services/volunteerService'

const CATEGORIES = ['FOOD', 'SHELTER', 'MEDICAL', 'TRANSPORT', 'RESCUE', 'REHABILITATION', 'EQUIPMENT', 'COMMUNICATION', 'OTHER']
const CAMPAIGN_CATEGORIES = ['FLOOD_RELIEF', 'EARTHQUAKE_RELIEF', 'CYCLONE_RELIEF', 'DROUGHT_RELIEF', 'FIRE_RELIEF', 'MEDICAL_AID', 'SHELTER', 'FOOD_DISTRIBUTION', 'REHABILITATION', 'OTHER']

export default function NgoDashboard() {
  const { user } = useAuth()
  const [tab, setTab] = useState('overview')

  const [campaigns, setCampaigns]       = useState([])
  const [donations, setDonations]       = useState([])
  const [utilizations, setUtils]        = useState([])
  const [disasters, setDisasters]       = useState([])
  const [requirements, setRequirements] = useState([])
  const [matches, setMatches]           = useState(null)
  const [loading, setLoading]           = useState({})
  const [msg, setMsg]                   = useState('')
  const [err, setErr]                   = useState('')

  const [selectedCampaign, setSelectedCampaign] = useState('')

  // Create campaign form
  const [campaignForm, setCampForm] = useState({
    title: '', description: '', targetAmount: '', startDate: '', endDate: '', disasterId: '', category: 'FLOOD_RELIEF',
  })
  const [creatingCamp, setCreatingCamp] = useState(false)

  // Fund utilization form
  const [utilForm, setUtilForm] = useState({
    category: 'FOOD', description: '', amount: '', expenseDate: '', campaignId: ''
  })
  const [submittingUtil, setSubmittingUtil] = useState(false)

  // Volunteer requirement form
  const [reqForm, setReqForm] = useState({
    roleTitle: '', description: '', requiredCount: 1, urgency: 'NORMAL', campaignId: '', skills: '', location: ''
  })
  const [creatingReq, setCreatingReq] = useState(false)

  const flash = (m) => { setMsg(m); setTimeout(() => setMsg(''), 4000) }
  const load = (key, fn) => {
    setLoading(l => ({ ...l, [key]: true }))
    return fn().finally(() => setLoading(l => ({ ...l, [key]: false })))
  }

  useEffect(() => {
    load('campaigns', () => getMyCampaigns().then(setCampaigns).catch(() => {}))
    load('disasters', () => getAllDisasters().then(d => setDisasters(Array.isArray(d) ? d : d.content || [])).catch(() => {}))
    load('donations', () => getNgoDonations().then(d => setDonations(d.content || [])).catch(() => {}))
    load('requirements', () => getMyRequirements().then(setRequirements).catch(() => {}))
  }, [])

  useEffect(() => {
    if (selectedCampaign) {
      load('utils', () => getUtilizationsByCampaign(selectedCampaign).then(d => setUtils(d.content || d || [])).catch(() => {}))
    }
  }, [selectedCampaign])

  const handleCreateCampaign = async (e) => {
    e.preventDefault()
    setErr('')
    setCreatingCamp(true)
    try {
      const dto = {
        ...campaignForm,
        targetAmount: Number(campaignForm.targetAmount),
        disasterId: campaignForm.disasterId || null,
      }
      const c = await createCampaign(dto)
      setCampaigns(prev => [c, ...prev])
      setCampForm({ title: '', description: '', targetAmount: '', startDate: '', endDate: '', disasterId: '', category: 'FLOOD_RELIEF' })
      flash('Campaign created successfully!')
      setTab('campaigns')
    } catch (error) {
      setErr(error.response?.data?.message || 'Failed to create campaign')
    } finally {
      setCreatingCamp(false)
    }
  }

  const handleSubmitUtil = async (e) => {
    e.preventDefault()
    setErr('')
    if (!utilForm.campaignId) { setErr('Please select a campaign'); return }
    setSubmittingUtil(true)
    try {
      const dto = { ...utilForm, amount: Number(utilForm.amount) }
      await submitUtilization(utilForm.campaignId, dto)
      setUtilForm({ category: 'FOOD', description: '', amount: '', expenseDate: '', campaignId: utilForm.campaignId })
      flash('Expense submitted for verification!')
      setSelectedCampaign(utilForm.campaignId)
    } catch (error) {
      setErr(error.response?.data?.message || 'Failed to submit expense')
    } finally {
      setSubmittingUtil(false)
    }
  }

  const handleCreateRequirement = async (e) => {
    e.preventDefault()
    setErr('')
    setCreatingReq(true)
    try {
      const dto = {
        ...reqForm,
        requiredCount: Number(reqForm.requiredCount),
        campaignId: reqForm.campaignId || null,
        skills: reqForm.skills.split(',').map(s => s.trim()).filter(Boolean),
      }
      const r = await createRequirement(dto)
      setRequirements(prev => [r, ...prev])
      setReqForm({ roleTitle: '', description: '', requiredCount: 1, urgency: 'NORMAL', campaignId: '', skills: '', location: '' })
      flash('Volunteer requirement posted!')
    } catch (error) {
      setErr(error.response?.data?.message || 'Failed to create requirement')
    } finally {
      setCreatingReq(false)
    }
  }

  const handleRunMatching = async (reqId) => {
    setErr('')
    try {
      const result = await generateAiMatches(reqId)
      setMatches(result)
      flash('AI matching completed!')
    } catch (error) {
      setErr('AI matching failed. Ensure the AI service is running.')
    }
  }

  const totalRaised = donations.reduce((s, d) => s + (d.status === 'SUCCESS' ? Number(d.amount) : 0), 0)

  return (
    <DashboardLayout role="NGO" activeTab={tab} onTabChange={setTab}>
      {msg && <div className="mb-4 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-green-700 text-sm">{msg}</div>}
      {err && <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm flex justify-between"><span>{err}</span><button onClick={() => setErr('')}>✕</button></div>}

      {/* ---- OVERVIEW ---- */}
      {tab === 'overview' && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold text-slate-900">NGO Overview</h2>
          <div className="grid grid-cols-2 md:grid-cols-3 gap-4">
            <StatCard title="My Campaigns"    value={campaigns.length} icon={BookOpen}    color="text-cyan-600" />
            <StatCard title="Total Raised"    value={`₹${totalRaised.toLocaleString('en-IN')}`} icon={Heart} color="text-green-600" />
            <StatCard title="Volunteer Reqs." value={requirements.length} icon={Users}    color="text-purple-600" />
          </div>
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <h3 className="font-semibold text-slate-900 mb-3">My Campaigns</h3>
            {campaigns.length === 0
              ? <p className="text-slate-400 text-sm">No campaigns yet. <button onClick={() => setTab('create-campaign')} className="text-cyan-600 hover:underline">Create one →</button></p>
              : campaigns.slice(0, 5).map(c => (
                <div key={c.id} className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
                  <span className="text-sm text-slate-700 truncate max-w-xs">{c.title}</span>
                  <StatusBadge status={c.status} />
                </div>
              ))
            }
          </div>
        </div>
      )}

      {/* ---- MY CAMPAIGNS ---- */}
      {tab === 'campaigns' && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-slate-900">My Campaigns</h2>
            <button onClick={() => setTab('create-campaign')}
              className="flex items-center gap-2 rounded-lg bg-cyan-600 px-4 py-2 text-sm font-semibold text-white hover:bg-cyan-700 transition">
              <PlusCircle className="h-4 w-4" /> New Campaign
            </button>
          </div>
          {loading.campaigns ? <Spinner center /> : (
            <div className="space-y-4">
              {campaigns.map(c => (
                <div key={c.id} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
                  <div className="flex items-start justify-between">
                    <div className="flex-1">
                      <div className="flex items-center gap-2 mb-1">
                        <StatusBadge status={c.status} />
                        {c.category && <span className="text-xs text-slate-400">{c.category}</span>}
                      </div>
                      <h3 className="font-semibold text-slate-900">{c.title}</h3>
                      <p className="text-sm text-slate-500 mt-1 line-clamp-1">{c.description}</p>
                      <div className="mt-2 flex gap-6 text-xs text-slate-400">
                        <span>Target: ₹{Number(c.targetAmount || 0).toLocaleString('en-IN')}</span>
                        {c.startDate && <span>Starts: {new Date(c.startDate).toLocaleDateString('en-IN')}</span>}
                      </div>
                    </div>
                  </div>
                </div>
              ))}
              {campaigns.length === 0 && <EmptyState msg="No campaigns yet." action="Create Campaign" onAction={() => setTab('create-campaign')} />}
            </div>
          )}
        </div>
      )}

      {/* ---- CREATE CAMPAIGN ---- */}
      {tab === 'create-campaign' && (
        <div className="max-w-2xl">
          <h2 className="text-xl font-bold text-slate-900 mb-6">Create New Campaign</h2>
          <form onSubmit={handleCreateCampaign} className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-4">
            <FormField label="Campaign Title *" value={campaignForm.title} onChange={v => setCampForm(f => ({ ...f, title: v }))} placeholder="e.g. Flood Relief for Wayanad" required />
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Description *</label>
              <textarea rows={3} value={campaignForm.description} required
                onChange={e => setCampForm(f => ({ ...f, description: e.target.value }))}
                className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none"
                placeholder="Describe what this campaign aims to achieve..." />
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormField label="Target Amount (₹) *" type="number" value={campaignForm.targetAmount} onChange={v => setCampForm(f => ({ ...f, targetAmount: v }))} placeholder="e.g. 500000" required />
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Category</label>
                <select value={campaignForm.category} onChange={e => setCampForm(f => ({ ...f, category: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                  {CAMPAIGN_CATEGORIES.map(c => <option key={c} value={c}>{c.replace(/_/g, ' ')}</option>)}
                </select>
              </div>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Start Date *</label>
                <input type="date" required value={campaignForm.startDate}
                  onChange={e => setCampForm(f => ({ ...f, startDate: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">End Date *</label>
                <input type="date" required value={campaignForm.endDate}
                  onChange={e => setCampForm(f => ({ ...f, endDate: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
              </div>
            </div>
            <div>
              <label className="block text-sm font-medium text-slate-700 mb-1">Associated Disaster (Optional)</label>
              <select value={campaignForm.disasterId} onChange={e => setCampForm(f => ({ ...f, disasterId: e.target.value }))}
                className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                <option value="">— Select Disaster —</option>
                {disasters.map(d => <option key={d.id} value={d.id}>{d.title}</option>)}
              </select>
            </div>
            <button type="submit" disabled={creatingCamp}
              className="w-full flex items-center justify-center gap-2 rounded-lg bg-cyan-600 py-3 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition">
              {creatingCamp ? <Spinner size="sm" /> : <><PlusCircle className="h-4 w-4" /> Create Campaign</>}
            </button>
          </form>
        </div>
      )}

      {/* ---- DONATIONS ---- */}
      {tab === 'donations' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">Campaign Donations</h2>
          {loading.donations ? <Spinner center /> : (
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
              <table className="w-full text-sm min-w-[600px]">
                <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
                  <tr>{['Donor', 'Campaign', 'Amount', 'Status', 'Date'].map(h => <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>)}</tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {donations.map(d => (
                    <tr key={d.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3">{d.donorName || 'Anonymous'}</td>
                      <td className="px-4 py-3 text-slate-500 max-w-[180px] truncate">{d.campaignTitle || '—'}</td>
                      <td className="px-4 py-3 font-medium text-green-600">₹{Number(d.amount).toLocaleString('en-IN')}</td>
                      <td className="px-4 py-3"><StatusBadge status={d.status} /></td>
                      <td className="px-4 py-3 text-slate-400">{d.createdAt ? new Date(d.createdAt).toLocaleDateString('en-IN') : '—'}</td>
                    </tr>
                  ))}
                  {donations.length === 0 && <tr><td colSpan={5} className="py-10 text-center text-slate-400">No donations yet</td></tr>}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ---- FUND UTILIZATION ---- */}
      {tab === 'fund-utilization' && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold text-slate-900">Fund Utilization</h2>
          {/* Submit expense */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <h3 className="font-semibold text-slate-900 mb-4">Submit New Expense</h3>
            <form onSubmit={handleSubmitUtil} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Campaign *</label>
                  <select required value={utilForm.campaignId} onChange={e => setUtilForm(f => ({ ...f, campaignId: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    <option value="">— Select Campaign —</option>
                    {campaigns.map(c => <option key={c.id} value={c.id}>{c.title}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Category *</label>
                  <select value={utilForm.category} onChange={e => setUtilForm(f => ({ ...f, category: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    {CATEGORIES.map(c => <option key={c}>{c}</option>)}
                  </select>
                </div>
              </div>
              <FormField label="Description *" value={utilForm.description} required
                onChange={v => setUtilForm(f => ({ ...f, description: v }))} placeholder="e.g. 200 food packets distributed in Wayanad" />
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <FormField label="Amount (₹) *" type="number" value={utilForm.amount} required
                  onChange={v => setUtilForm(f => ({ ...f, amount: v }))} placeholder="e.g. 25000" />
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Expense Date *</label>
                  <input type="date" required value={utilForm.expenseDate}
                    onChange={e => setUtilForm(f => ({ ...f, expenseDate: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
                </div>
              </div>
              <button type="submit" disabled={submittingUtil}
                className="flex items-center gap-2 rounded-lg bg-cyan-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition">
                {submittingUtil ? <Spinner size="sm" /> : 'Submit Expense'}
              </button>
            </form>
          </div>

          {/* View utilizations */}
          {selectedCampaign && (
            <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
              <h3 className="font-semibold text-slate-900 mb-3">Expense Records</h3>
              {loading.utils ? <Spinner center /> : (
                <div className="space-y-3">
                  {utilizations.map(u => (
                    <div key={u.id} className="border border-slate-100 rounded-lg p-4">
                      <div className="flex items-center justify-between mb-1">
                        <span className="text-sm font-medium text-slate-900">{u.description}</span>
                        <StatusBadge status={u.status} />
                      </div>
                      <div className="text-xs text-slate-400 flex gap-4">
                        <span>Category: {u.category}</span>
                        <span>Amount: ₹{Number(u.amount).toLocaleString('en-IN')}</span>
                        {u.expenseDate && <span>{new Date(u.expenseDate).toLocaleDateString('en-IN')}</span>}
                      </div>
                    </div>
                  ))}
                  {utilizations.length === 0 && <p className="text-slate-400 text-sm text-center py-6">No expense records for this campaign.</p>}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* ---- VOLUNTEERS ---- */}
      {tab === 'volunteers' && (
        <div className="space-y-6">
          <h2 className="text-xl font-bold text-slate-900">Volunteer Management</h2>

          {/* Post requirement */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <h3 className="font-semibold text-slate-900 mb-4">Post Volunteer Requirement</h3>
            <form onSubmit={handleCreateRequirement} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <FormField label="Role Title *" value={reqForm.roleTitle} required
                  onChange={v => setReqForm(f => ({ ...f, roleTitle: v }))} placeholder="e.g. Medical Volunteer" />
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Required Count</label>
                  <input type="number" min={1} value={reqForm.requiredCount}
                    onChange={e => setReqForm(f => ({ ...f, requiredCount: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                <textarea rows={2} value={reqForm.description}
                  onChange={e => setReqForm(f => ({ ...f, description: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none"
                  placeholder="Describe what this volunteer will do..." />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <FormField label="Required Skills (comma separated)" value={reqForm.skills}
                  onChange={v => setReqForm(f => ({ ...f, skills: v }))} placeholder="e.g. First Aid, Swimming" />
                <FormField label="Location" value={reqForm.location}
                  onChange={v => setReqForm(f => ({ ...f, location: v }))} placeholder="e.g. Wayanad, Kerala" />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Urgency</label>
                  <select value={reqForm.urgency} onChange={e => setReqForm(f => ({ ...f, urgency: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    {['LOW', 'NORMAL', 'HIGH', 'CRITICAL'].map(u => <option key={u}>{u}</option>)}
                  </select>
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Campaign (Optional)</label>
                  <select value={reqForm.campaignId} onChange={e => setReqForm(f => ({ ...f, campaignId: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                    <option value="">— None —</option>
                    {campaigns.map(c => <option key={c.id} value={c.id}>{c.title}</option>)}
                  </select>
                </div>
              </div>
              <button type="submit" disabled={creatingReq}
                className="flex items-center gap-2 rounded-lg bg-purple-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-purple-700 disabled:opacity-60 transition">
                {creatingReq ? <Spinner size="sm" /> : 'Post Requirement'}
              </button>
            </form>
          </div>

          {/* Requirements list */}
          <div className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm">
            <h3 className="font-semibold text-slate-900 mb-4">My Requirements</h3>
            {loading.requirements ? <Spinner center /> : (
              <div className="space-y-4">
                {requirements.map(r => (
                  <div key={r.id} className="border border-slate-200 rounded-xl p-4">
                    <div className="flex items-start justify-between mb-2">
                      <div>
                        <h4 className="font-medium text-slate-900">{r.roleTitle}</h4>
                        <p className="text-xs text-slate-400 mt-0.5">Required: {r.requiredCount} • Urgency: {r.urgency}</p>
                      </div>
                      <button
                        onClick={() => handleRunMatching(r.id)}
                        className="flex items-center gap-1 rounded-lg bg-indigo-600 px-3 py-1.5 text-xs font-semibold text-white hover:bg-indigo-700 transition"
                      >
                        🤖 Run AI Match
                      </button>
                    </div>
                    {r.location && <p className="text-xs text-slate-400">📍 {r.location}</p>}
                  </div>
                ))}
                {requirements.length === 0 && <p className="text-slate-400 text-sm text-center py-6">No requirements posted yet.</p>}
              </div>
            )}

            {/* AI match results */}
            {matches && (
              <div className="mt-4 rounded-xl bg-indigo-50 border border-indigo-200 p-4">
                <h4 className="font-semibold text-indigo-900 mb-3">AI Match Results</h4>
                <div className="space-y-2">
                  {(matches.results || []).map((m, i) => (
                    <div key={i} className="flex items-center justify-between bg-white rounded-lg p-3 border border-indigo-100">
                      <div>
                        <span className="font-medium text-slate-900">{m.volunteerName}</span>
                        {m.skills && <span className="ml-2 text-xs text-slate-400">{m.skills.join(', ')}</span>}
                      </div>
                      <span className="text-sm font-bold text-indigo-600">{Math.round(m.score * 100)}% match</span>
                    </div>
                  ))}
                  {(!matches.results || matches.results.length === 0) && (
                    <p className="text-indigo-700 text-sm">No suitable volunteers found at this time.</p>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </DashboardLayout>
  )
}

function StatusBadge({ status }) {
  const map = {
    ACTIVE:    'bg-green-100 text-green-700',
    PENDING:   'bg-yellow-100 text-yellow-700',
    COMPLETED: 'bg-blue-100 text-blue-700',
    SUCCESS:   'bg-green-100 text-green-700',
    FAILED:    'bg-red-100 text-red-700',
    VERIFIED:  'bg-green-100 text-green-700',
    DRAFT:     'bg-slate-100 text-slate-500',
    INACTIVE:  'bg-slate-100 text-slate-500',
  }
  return <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${map[status] || 'bg-slate-100 text-slate-500'}`}>{status}</span>
}

function FormField({ label, value, onChange, placeholder, type = 'text', required }) {
  return (
    <div>
      <label className="block text-sm font-medium text-slate-700 mb-1">{label}</label>
      <input type={type} value={value} required={required} placeholder={placeholder}
        onChange={e => onChange(e.target.value)}
        className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
    </div>
  )
}

function EmptyState({ msg, action, onAction }) {
  return (
    <div className="text-center py-16 text-slate-400 bg-white rounded-xl border border-slate-200">
      <p className="mb-3">{msg}</p>
      {action && <button onClick={onAction} className="text-cyan-600 hover:underline text-sm">{action} →</button>}
    </div>
  )
}
