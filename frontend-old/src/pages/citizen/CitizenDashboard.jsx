// src/pages/citizen/CitizenDashboard.jsx
import { Bell, CheckSquare, ClipboardList, Heart, User } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import DashboardLayout from '../../components/layout/DashboardLayout'
import Spinner from '../../components/ui/Spinner'
import StatCard from '../../components/ui/StatCard'
import { useAuth } from '../../context/AuthContext'
import { getMyDonations, getDonationReceipt } from '../../services/donationService'
import { getMyVolunteerProfile, createVolunteerProfile, updateVolunteerProfile, getMyAssignments } from '../../services/volunteerService'
import { getMyNotifications, markAsRead, markAllAsRead, deleteNotification } from '../../services/notificationService'

export default function CitizenDashboard() {
  const { user, updateUser } = useAuth()
  const [tab, setTab] = useState('overview')

  const [donations, setDonations]           = useState([])
  const [volunteerProfile, setVProfile]     = useState(null)
  const [assignments, setAssignments]       = useState([])
  const [notifications, setNotifications]   = useState([])
  const [loading, setLoading]               = useState({})
  const [msg, setMsg]                       = useState('')
  const [err, setErr]                       = useState('')

  // Profile form
  const [profileForm, setPForm] = useState({
    bio: '', location: '', skills: '', availability: 'AVAILABLE', experienceYears: 0,
    isAvailable: true,
  })
  const [savingProfile, setSavingProfile] = useState(false)
  const [profileLoaded, setProfileLoaded] = useState(false)

  const flash = (m) => { setMsg(m); setTimeout(() => setMsg(''), 3000) }
  const load = (key, fn) => {
    setLoading(l => ({ ...l, [key]: true }))
    return fn().finally(() => setLoading(l => ({ ...l, [key]: false })))
  }

  useEffect(() => {
    load('donations', () => getMyDonations().then(d => setDonations(d.content || [])).catch(() => {}))
    load('assignments', () => getMyAssignments().then(d => setAssignments(Array.isArray(d) ? d : [])).catch(() => {}))
    load('notifications', () => getMyNotifications().then(d => setNotifications(d.content || [])).catch(() => {}))
    load('profile', () => getMyVolunteerProfile().then(p => {
      setVProfile(p)
      setPForm({
        bio: p.bio || '', location: p.location || '',
        skills: (p.skills || []).join(', '),
        availability: p.availability || 'AVAILABLE',
        experienceYears: p.experienceYears || 0,
        isAvailable: p.isAvailable !== false,
      })
      setProfileLoaded(true)
    }).catch(() => { setProfileLoaded(true) }))
  }, [])

  const handleSaveProfile = async (e) => {
    e.preventDefault()
    setSavingProfile(true)
    setErr('')
    try {
      const dto = {
        ...profileForm,
        skills: profileForm.skills.split(',').map(s => s.trim()).filter(Boolean),
        experienceYears: Number(profileForm.experienceYears),
      }
      const p = volunteerProfile
        ? await updateVolunteerProfile(dto)
        : await createVolunteerProfile(dto)
      setVProfile(p)
      flash('Volunteer profile saved successfully!')
    } catch (error) {
      setErr(error.response?.data?.message || 'Failed to save profile')
    } finally {
      setSavingProfile(false)
    }
  }

  const handleMarkRead = async (id) => {
    try {
      await markAsRead(id)
      setNotifications(n => n.map(x => x.id === id ? { ...x, read: true } : x))
    } catch { /* ignore */ }
  }

  const handleMarkAllRead = async () => {
    try {
      await markAllAsRead()
      setNotifications(n => n.map(x => ({ ...x, read: true })))
      flash('All notifications marked as read')
    } catch { /* ignore */ }
  }

  const handleDeleteNotif = async (id) => {
    try {
      await deleteNotification(id)
      setNotifications(n => n.filter(x => x.id !== id))
    } catch { /* ignore */ }
  }

  const totalDonated = donations.filter(d => d.status === 'SUCCESS').reduce((s, d) => s + Number(d.amount), 0)
  const unread = notifications.filter(n => !n.read).length

  return (
    <DashboardLayout role="CITIZEN" activeTab={tab} onTabChange={setTab}>
      {msg && <div className="mb-4 rounded-lg bg-green-50 border border-green-200 px-4 py-3 text-green-700 text-sm">{msg}</div>}
      {err && <div className="mb-4 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm flex justify-between"><span>{err}</span><button onClick={() => setErr('')}>✕</button></div>}

      {/* ---- OVERVIEW ---- */}
      {tab === 'overview' && (
        <div className="space-y-6">
          <div className="bg-gradient-to-r from-cyan-700 to-slate-800 rounded-xl p-6 text-white">
            <h2 className="text-xl font-bold mb-1">Welcome back, {user?.name}!</h2>
            <p className="text-cyan-200 text-sm">Thank you for being part of the ReliefBridge community.</p>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
            <StatCard title="Total Donated" value={`₹${totalDonated.toLocaleString('en-IN')}`} icon={Heart} color="text-green-600" />
            <StatCard title="Donations"     value={donations.filter(d => d.status === 'SUCCESS').length} icon={Heart} color="text-cyan-600" />
            <StatCard title="Assignments"   value={assignments.length} icon={CheckSquare} color="text-purple-600" />
            <StatCard title="Unread Alerts" value={unread} icon={Bell} color="text-orange-500" />
          </div>
          <div className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
            <div className="flex items-center justify-between mb-3">
              <h3 className="font-semibold text-slate-900">Recent Donations</h3>
              <button onClick={() => setTab('donations')} className="text-xs text-cyan-600 hover:underline">View All</button>
            </div>
            {donations.slice(0, 3).map(d => (
              <div key={d.id} className="flex items-center justify-between py-2 border-b border-slate-100 last:border-0">
                <span className="text-sm text-slate-700 truncate max-w-xs">{d.campaignTitle || '—'}</span>
                <span className="text-sm font-medium text-green-600">₹{Number(d.amount).toLocaleString('en-IN')}</span>
              </div>
            ))}
            {donations.length === 0 && (
              <p className="text-slate-400 text-sm text-center py-4">
                No donations yet.{' '}
                <Link to="/campaigns" className="text-cyan-600 hover:underline">Explore campaigns →</Link>
              </p>
            )}
          </div>
        </div>
      )}

      {/* ---- MY DONATIONS ---- */}
      {tab === 'donations' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">My Donation History</h2>
          {loading.donations ? <Spinner center /> : (
            <div className="bg-white rounded-xl border border-slate-200 shadow-sm overflow-x-auto">
              <table className="w-full text-sm min-w-[600px]">
                <thead className="bg-slate-50 text-slate-500 uppercase text-xs">
                  <tr>{['Campaign', 'Amount', 'Status', 'Date', 'Receipt'].map(h => <th key={h} className="px-4 py-3 text-left font-medium">{h}</th>)}</tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {donations.map(d => (
                    <tr key={d.id} className="hover:bg-slate-50">
                      <td className="px-4 py-3 text-slate-900 max-w-[200px] truncate">{d.campaignTitle || '—'}</td>
                      <td className="px-4 py-3 font-medium text-green-600">₹{Number(d.amount).toLocaleString('en-IN')}</td>
                      <td className="px-4 py-3"><StatusBadge status={d.status} /></td>
                      <td className="px-4 py-3 text-slate-400">{d.createdAt ? new Date(d.createdAt).toLocaleDateString('en-IN') : '—'}</td>
                      <td className="px-4 py-3">
                        {d.status === 'SUCCESS' && d.receiptNumber && (
                          <Link to={`/receipt/${d.receiptNumber}`} className="text-cyan-600 hover:underline text-xs">View Receipt</Link>
                        )}
                      </td>
                    </tr>
                  ))}
                  {donations.length === 0 && (
                    <tr><td colSpan={5} className="py-10 text-center text-slate-400">
                      No donations yet. <Link to="/campaigns" className="text-cyan-600 hover:underline">Donate to a campaign</Link>
                    </td></tr>
                  )}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* ---- VOLUNTEER PROFILE ---- */}
      {tab === 'volunteer-profile' && (
        <div className="max-w-2xl">
          <h2 className="text-xl font-bold text-slate-900 mb-6">
            {volunteerProfile ? 'Edit Volunteer Profile' : 'Create Volunteer Profile'}
          </h2>
          {!profileLoaded ? <Spinner center /> : (
            <form onSubmit={handleSaveProfile} className="bg-white rounded-xl border border-slate-200 p-6 shadow-sm space-y-4">
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Bio / About</label>
                <textarea rows={3} value={profileForm.bio}
                  onChange={e => setPForm(f => ({ ...f, bio: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none"
                  placeholder="Tell NGOs about yourself, your experience, what you can do..." />
              </div>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Location</label>
                  <input type="text" value={profileForm.location}
                    onChange={e => setPForm(f => ({ ...f, location: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none"
                    placeholder="e.g. Chennai, Tamil Nadu" />
                </div>
                <div>
                  <label className="block text-sm font-medium text-slate-700 mb-1">Years of Experience</label>
                  <input type="number" min={0} value={profileForm.experienceYears}
                    onChange={e => setPForm(f => ({ ...f, experienceYears: e.target.value }))}
                    className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none" />
                </div>
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Skills (comma separated)</label>
                <input type="text" value={profileForm.skills}
                  onChange={e => setPForm(f => ({ ...f, skills: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none"
                  placeholder="e.g. First Aid, Swimming, Cooking, Driving" />
              </div>
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Availability</label>
                <select value={profileForm.availability}
                  onChange={e => setPForm(f => ({ ...f, availability: e.target.value }))}
                  className="w-full rounded-lg border border-slate-300 px-3 py-2.5 text-sm focus:border-cyan-500 focus:outline-none">
                  <option value="AVAILABLE">Available Now</option>
                  <option value="WEEKENDS_ONLY">Weekends Only</option>
                  <option value="LIMITED">Limited Availability</option>
                  <option value="UNAVAILABLE">Currently Unavailable</option>
                </select>
              </div>
              <div className="flex items-center gap-3">
                <input type="checkbox" id="isAvail" checked={profileForm.isAvailable}
                  onChange={e => setPForm(f => ({ ...f, isAvailable: e.target.checked }))}
                  className="h-4 w-4 rounded border-slate-300 text-cyan-600 focus:ring-cyan-500" />
                <label htmlFor="isAvail" className="text-sm text-slate-700">I am currently available for volunteer work</label>
              </div>
              <button type="submit" disabled={savingProfile}
                className="w-full flex items-center justify-center gap-2 rounded-lg bg-cyan-600 py-3 text-sm font-semibold text-white hover:bg-cyan-700 disabled:opacity-60 transition">
                {savingProfile ? <Spinner size="sm" /> : (volunteerProfile ? 'Update Profile' : 'Create Profile')}
              </button>
            </form>
          )}
        </div>
      )}

      {/* ---- MY ASSIGNMENTS ---- */}
      {tab === 'assignments' && (
        <div>
          <h2 className="text-xl font-bold text-slate-900 mb-4">My Volunteer Assignments</h2>
          {loading.assignments ? <Spinner center /> : (
            <div className="space-y-4">
              {assignments.map(a => (
                <div key={a.id} className="bg-white rounded-xl border border-slate-200 p-5 shadow-sm">
                  <div className="flex items-start justify-between">
                    <div>
                      <h3 className="font-semibold text-slate-900">{a.requirementTitle || a.roleTitle || 'Volunteer Role'}</h3>
                      <p className="text-sm text-slate-500 mt-1">{a.campaignTitle || '—'}</p>
                      {a.location && <p className="text-xs text-slate-400 mt-0.5">📍 {a.location}</p>}
                    </div>
                    <StatusBadge status={a.status} />
                  </div>
                </div>
              ))}
              {assignments.length === 0 && (
                <div className="text-center py-16 text-slate-400 bg-white rounded-xl border border-slate-200">
                  No assignments yet. Create a volunteer profile and apply to requirements!
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* ---- NOTIFICATIONS ---- */}
      {tab === 'notifications' && (
        <div>
          <div className="flex items-center justify-between mb-4">
            <h2 className="text-xl font-bold text-slate-900">Notifications {unread > 0 && <span className="ml-2 text-sm font-normal text-orange-500">({unread} unread)</span>}</h2>
            {unread > 0 && (
              <button onClick={handleMarkAllRead}
                className="rounded-lg border border-slate-300 px-4 py-2 text-xs font-medium text-slate-700 hover:bg-slate-50 transition">
                Mark All Read
              </button>
            )}
          </div>
          {loading.notifications ? <Spinner center /> : (
            <div className="space-y-3">
              {notifications.map(n => (
                <div key={n.id} className={`bg-white rounded-xl border p-4 shadow-sm flex items-start justify-between gap-4 ${!n.read ? 'border-cyan-200 bg-cyan-50/30' : 'border-slate-200'}`}>
                  <div className="flex items-start gap-3 flex-1">
                    <div className={`mt-1 h-2 w-2 rounded-full shrink-0 ${!n.read ? 'bg-cyan-500' : 'bg-transparent'}`} />
                    <div>
                      <p className={`text-sm font-medium ${!n.read ? 'text-slate-900' : 'text-slate-600'}`}>{n.title}</p>
                      <p className="text-xs text-slate-500 mt-0.5">{n.message || n.description}</p>
                      <p className="text-xs text-slate-400 mt-1">{n.createdAt ? new Date(n.createdAt).toLocaleString('en-IN') : ''}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-2 shrink-0">
                    {!n.read && (
                      <button onClick={() => handleMarkRead(n.id)}
                        className="text-xs text-cyan-600 hover:underline">Read</button>
                    )}
                    <button onClick={() => handleDeleteNotif(n.id)}
                      className="text-xs text-slate-400 hover:text-red-500">✕</button>
                  </div>
                </div>
              ))}
              {notifications.length === 0 && (
                <div className="text-center py-16 text-slate-400 bg-white rounded-xl border border-slate-200">
                  No notifications yet.
                </div>
              )}
            </div>
          )}
        </div>
      )}
    </DashboardLayout>
  )
}

function StatusBadge({ status }) {
  const map = {
    ACTIVE: 'bg-green-100 text-green-700', PENDING: 'bg-yellow-100 text-yellow-700',
    COMPLETED: 'bg-blue-100 text-blue-700', SUCCESS: 'bg-green-100 text-green-700',
    FAILED: 'bg-red-100 text-red-700', CANCELLED: 'bg-slate-100 text-slate-500',
    ASSIGNED: 'bg-purple-100 text-purple-700',
  }
  return <span className={`inline-flex items-center px-2 py-0.5 rounded-full text-xs font-semibold ${map[status] || 'bg-slate-100 text-slate-500'}`}>{status}</span>
}
