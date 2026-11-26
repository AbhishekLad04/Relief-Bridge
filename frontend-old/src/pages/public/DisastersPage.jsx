// src/pages/public/DisastersPage.jsx
import { MapPin } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import PublicLayout from '../../components/layout/PublicLayout'
import Spinner from '../../components/ui/Spinner'
import { getAllDisasters } from '../../services/disasterService'

const SEVERITY_COLOR = {
  CRITICAL: { badge: 'bg-red-100 text-red-700', dot: 'bg-red-500' },
  HIGH:     { badge: 'bg-orange-100 text-orange-700', dot: 'bg-orange-500' },
  MEDIUM:   { badge: 'bg-amber-100 text-amber-700', dot: 'bg-amber-500' },
  LOW:      { badge: 'bg-green-100 text-green-700', dot: 'bg-green-500' },
}

function SeverityBadge({ severity }) {
  const { badge, dot } = SEVERITY_COLOR[severity] || SEVERITY_COLOR.LOW
  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-xs font-semibold ${badge}`}>
      <span className={`h-2 w-2 rounded-full ${dot}`} />
      {severity}
    </span>
  )
}

function DisasterCard({ disaster }) {
  const name = disaster.name || disaster.title || 'Unnamed'
  return (
    <div className="rounded-2xl bg-white shadow-md hover:shadow-lg transition overflow-hidden">
      <div className="bg-gradient-to-r from-slate-800 to-slate-700 h-2" />
      <div className="p-5">
        <div className="flex items-start justify-between gap-3 mb-3">
          <h3 className="text-base font-semibold text-slate-900 leading-snug">{name}</h3>
          <SeverityBadge severity={disaster.severity} />
        </div>

        <div className="flex flex-wrap gap-x-4 gap-y-1 mb-3 text-xs text-slate-500">
          {disaster.location && (
            <span className="flex items-center gap-1"><MapPin className="h-3 w-3" />{disaster.location}</span>
          )}
          {disaster.type && <span>Type: {disaster.type}</span>}
          {disaster.startDate && (
            <span>Since: {new Date(disaster.startDate).toLocaleDateString()}</span>
          )}
        </div>

        <p className="text-sm text-slate-500 line-clamp-2 mb-4">{disaster.description}</p>

        <Link
          to={`/disasters/${disaster.id}`}
          className="inline-flex items-center gap-1 rounded-lg bg-cyan-700 px-4 py-2 text-sm font-semibold text-white hover:bg-cyan-800 transition"
        >
          View Campaigns
        </Link>
      </div>
    </div>
  )
}

export default function DisastersPage() {
  const [disasters, setDisasters] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getAllDisasters()
      .then(data => {
        setDisasters(Array.isArray(data) ? data : data?.content ?? [])
      })
      .catch(() => setError('Failed to load disasters.'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <PublicLayout>
      <div className="bg-gradient-to-r from-slate-900 to-slate-800 py-10">
        <div className="mx-auto max-w-6xl px-4 text-center">
          <h1 className="text-3xl font-bold text-white mb-2">Active Disasters</h1>
          <p className="text-slate-300">Real-time disaster events requiring urgent humanitarian support</p>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 py-10">
        {loading && <Spinner className="py-20" size="lg" />}
        {!loading && error && <p className="text-center text-red-500 py-8">{error}</p>}
        {!loading && !error && disasters.length === 0 && (
          <p className="text-center text-slate-400 py-16">No active disasters at this time.</p>
        )}
        {!loading && !error && disasters.length > 0 && (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {disasters.map(d => <DisasterCard key={d.id} disaster={d} />)}
          </div>
        )}
      </div>
    </PublicLayout>
  )
}
