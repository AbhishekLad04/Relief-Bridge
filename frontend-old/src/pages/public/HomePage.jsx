// src/pages/public/HomePage.jsx
import { ArrowRight, Heart, Shield, Users, Zap } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import CampaignCard from '../../components/campaigns/CampaignCard'
import PublicLayout from '../../components/layout/PublicLayout'
import Spinner from '../../components/ui/Spinner'
import { getAllCampaigns } from '../../services/campaignService'
import { getAllDisasters } from '../../services/disasterService'
import { getAllDonations } from '../../services/donationService'

function StatsSection({ stats }) {
  const items = [
    { label: 'Total Raised', value: stats?.totalCollected ? `₹${Number(stats.totalCollected).toLocaleString('en-IN')}` : '₹0' },
    { label: 'Active Campaigns', value: stats?.activeCampaigns ?? 0 },
    { label: 'Total Donations', value: stats?.totalDonations ?? 0 },
    { label: 'Total Utilized', value: stats?.totalUtilized ? `₹${Number(stats.totalUtilized).toLocaleString('en-IN')}` : '₹0' },
  ]
  return (
    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 md:gap-6">
      {items.map(item => (
        <div key={item.label} className="rounded-2xl bg-white p-6 shadow-md text-center">
          <p className="text-2xl md:text-3xl font-bold text-cyan-700">{item.value}</p>
          <p className="mt-1 text-sm text-slate-500">{item.label}</p>
        </div>
      ))}
    </div>
  )
}

function SeverityBadge({ severity }) {
  const map = { CRITICAL: 'bg-red-100 text-red-700', HIGH: 'bg-orange-100 text-orange-700', MEDIUM: 'bg-amber-100 text-amber-700', LOW: 'bg-green-100 text-green-700' }
  return <span className={`inline-block rounded-full px-2.5 py-0.5 text-xs font-semibold ${map[severity] || 'bg-slate-100 text-slate-700'}`}>{severity}</span>
}

function DisasterCard({ disaster }) {
  return (
    <div className="rounded-2xl bg-white shadow-md p-5 hover:shadow-lg transition flex flex-col gap-3">
      <div className="flex items-start justify-between">
        <h3 className="text-base font-semibold text-slate-900">{disaster.name || disaster.title}</h3>
        <SeverityBadge severity={disaster.severity} />
      </div>
      <p className="text-sm text-slate-500 line-clamp-2">{disaster.description}</p>
      <div className="text-xs text-slate-400">{disaster.location} · {disaster.type}</div>
      <Link
        to={`/disasters/${disaster.id}`}
        className="mt-auto text-sm font-semibold text-cyan-700 hover:underline"
      >
        View Campaigns →
      </Link>
    </div>
  )
}

const steps = [
  { icon: Shield, title: 'Report Disaster', desc: 'Admin verifies and publishes disaster events on the platform.' },
  { icon: Heart, title: 'NGO Creates Campaign', desc: 'Verified NGOs create relief campaigns with fundraising goals.' },
  { icon: Zap, title: 'Donate & Volunteer', desc: 'Citizens donate or apply as volunteers matched by AI.' },
  { icon: Users, title: 'Track Impact', desc: 'Real-time fund utilization records ensure full transparency.' },
]

export default function HomePage() {
  const [stats, setStats] = useState(null)
  const [disasters, setDisasters] = useState([])
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    const load = async () => {
      try {
        const [disData, campData] = await Promise.all([
          getAllDisasters({ size: 3 }),
          getAllCampaigns({ status: 'ACTIVE', size: 3 }),
        ])
        setDisasters(Array.isArray(disData) ? disData.slice(0, 3) : disData?.content?.slice(0, 3) ?? [])
        setCampaigns(Array.isArray(campData) ? campData.slice(0, 3) : campData?.content?.slice(0, 3) ?? [])
      } catch { /* ignore */ }

      // Stats may need auth — try silently
      try {
        const s = await getAllDonations({ size: 1 })
        // The endpoint /api/donations/stats is public in backend
        const { default: api } = await import('../../api')
        const r = await api.get('/donations/stats')
        setStats(r.data)
      } catch { /* ignore */ }

      setLoading(false)
    }
    load()
  }, [])

  return (
    <PublicLayout>
      {/* Hero */}
      <section className="relative overflow-hidden bg-gradient-to-br from-slate-900 via-slate-800 to-cyan-900 py-20 md:py-28">
        <div className="absolute inset-0 opacity-10 bg-[radial-gradient(ellipse_at_top,_var(--tw-gradient-stops))] from-cyan-400 via-transparent to-transparent" />
        <div className="relative mx-auto max-w-4xl px-4 text-center">
          <div className="inline-flex items-center gap-2 rounded-full bg-cyan-900/60 px-4 py-1.5 text-sm font-medium text-cyan-300 ring-1 ring-cyan-700 mb-6">
            <Zap className="h-4 w-4" />
            AI-Powered Disaster Relief
          </div>
          <h1 className="text-4xl md:text-6xl font-extrabold text-white leading-tight">
            ReliefBridge
          </h1>
          <p className="mt-4 text-xl md:text-2xl text-cyan-300 font-semibold">
            Connecting Relief. Maximising Impact.
          </p>
          <p className="mt-5 text-base md:text-lg text-slate-300 max-w-2xl mx-auto">
            A transparent platform that connects donors, NGOs, and volunteers to deliver aid where it's needed most — powered by real-time data and AI matching.
          </p>
          <div className="mt-8 flex flex-col sm:flex-row gap-4 justify-center">
            <Link
              to="/campaigns"
              className="rounded-xl bg-orange-500 px-6 py-3 text-base font-bold text-white hover:bg-orange-600 transition flex items-center gap-2 justify-center"
            >
              Donate Now <ArrowRight className="h-4 w-4" />
            </Link>
            <Link
              to="/register"
              className="rounded-xl bg-white/10 backdrop-blur border border-white/20 px-6 py-3 text-base font-bold text-white hover:bg-white/20 transition"
            >
              Join as NGO / Volunteer
            </Link>
          </div>
        </div>
      </section>

      {/* Stats */}
      <section className="bg-slate-50 py-12">
        <div className="mx-auto max-w-6xl px-4">
          <h2 className="mb-6 text-center text-xl font-bold text-slate-700">Platform Impact at a Glance</h2>
          {loading ? <Spinner className="py-8" /> : <StatsSection stats={stats} />}
        </div>
      </section>

      {/* Active Disasters */}
      <section className="py-14 bg-white">
        <div className="mx-auto max-w-6xl px-4">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Active Disasters</h2>
              <p className="text-slate-500 text-sm mt-1">Ongoing events requiring urgent relief</p>
            </div>
            <Link to="/disasters" className="text-sm font-semibold text-cyan-700 hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          {loading ? <Spinner className="py-8" /> : (
            disasters.length === 0
              ? <p className="text-center text-slate-400 py-8">No active disasters at this time.</p>
              : <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                  {disasters.map(d => <DisasterCard key={d.id} disaster={d} />)}
                </div>
          )}
        </div>
      </section>

      {/* Featured Campaigns */}
      <section className="py-14 bg-slate-50">
        <div className="mx-auto max-w-6xl px-4">
          <div className="flex items-center justify-between mb-8">
            <div>
              <h2 className="text-2xl font-bold text-slate-900">Featured Campaigns</h2>
              <p className="text-slate-500 text-sm mt-1">Support verified NGO campaigns in progress</p>
            </div>
            <Link to="/campaigns" className="text-sm font-semibold text-cyan-700 hover:underline flex items-center gap-1">
              View all <ArrowRight className="h-4 w-4" />
            </Link>
          </div>
          {loading ? <Spinner className="py-8" /> : (
            campaigns.length === 0
              ? <p className="text-center text-slate-400 py-8">No active campaigns right now.</p>
              : <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
                  {campaigns.map(c => <CampaignCard key={c.id} campaign={c} />)}
                </div>
          )}
        </div>
      </section>

      {/* How it works */}
      <section className="py-16 bg-white">
        <div className="mx-auto max-w-6xl px-4">
          <h2 className="mb-10 text-center text-2xl font-bold text-slate-900">How ReliefBridge Works</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-6">
            {steps.map((step, i) => (
              <div key={i} className="text-center px-4">
                <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-2xl bg-cyan-700/10">
                  <step.icon className="h-7 w-7 text-cyan-700" />
                </div>
                <div className="mb-1 text-xs font-bold uppercase tracking-widest text-orange-500">Step {i + 1}</div>
                <h3 className="mb-2 text-base font-semibold text-slate-900">{step.title}</h3>
                <p className="text-sm text-slate-500">{step.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-16 bg-gradient-to-r from-cyan-700 to-slate-800">
        <div className="mx-auto max-w-2xl px-4 text-center">
          <h2 className="text-2xl md:text-3xl font-bold text-white mb-4">Ready to Make a Difference?</h2>
          <p className="text-cyan-200 mb-8">Join thousands of donors, volunteers, and NGOs working together on ReliefBridge.</p>
          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <Link to="/register" className="rounded-xl bg-orange-500 px-6 py-3 font-bold text-white hover:bg-orange-600 transition">
              Create an Account
            </Link>
            <Link to="/campaigns" className="rounded-xl bg-white/10 border border-white/30 px-6 py-3 font-bold text-white hover:bg-white/20 transition">
              Browse Campaigns
            </Link>
          </div>
        </div>
      </section>
    </PublicLayout>
  )
}
