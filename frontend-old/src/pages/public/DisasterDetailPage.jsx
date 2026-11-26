// src/pages/public/DisasterDetailPage.jsx
import { AlertTriangle, ArrowLeft, Calendar, MapPin } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import PublicLayout from '../../components/layout/PublicLayout'
import Spinner from '../../components/ui/Spinner'
import CampaignCard from '../../components/campaigns/CampaignCard'
import { getDisasterById } from '../../services/disasterService'
import { getAllCampaigns } from '../../services/campaignService'

const severityConfig = {
  CRITICAL: 'bg-red-100 text-red-700',
  HIGH:     'bg-orange-100 text-orange-700',
  MEDIUM:   'bg-yellow-100 text-yellow-700',
  LOW:      'bg-green-100 text-green-700',
}

export default function DisasterDetailPage() {
  const { id } = useParams()
  const [disaster, setDisaster]   = useState(null)
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading]     = useState(true)
  const [error, setError]         = useState('')

  useEffect(() => {
    setLoading(true)
    Promise.all([
      getDisasterById(id),
      getAllCampaigns({ disasterId: id, size: 6 }),
    ])
      .then(([d, c]) => {
        setDisaster(d)
        setCampaigns(c.content || c || [])
      })
      .catch(() => setError('Failed to load disaster details'))
      .finally(() => setLoading(false))
  }, [id])

  return (
    <PublicLayout>
      <div className="max-w-5xl mx-auto px-4 py-10">
        <Link to="/disasters" className="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-6">
          <ArrowLeft className="h-4 w-4" /> Back to Disasters
        </Link>

        {loading && <Spinner center />}
        {error && <p className="text-red-500 text-center py-10">{error}</p>}

        {!loading && !error && disaster && (
          <>
            <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-6 mb-8">
              <div className="flex items-start justify-between mb-4">
                <div className="flex items-center gap-3">
                  <div className="h-12 w-12 rounded-xl bg-red-100 flex items-center justify-center">
                    <AlertTriangle className="h-6 w-6 text-red-500" />
                  </div>
                  <div>
                    <span className="text-xs text-slate-400 uppercase tracking-wide">{disaster.type}</span>
                    <h1 className="text-2xl font-bold text-slate-900">{disaster.title}</h1>
                  </div>
                </div>
                <span className={`px-3 py-1 rounded-full text-sm font-semibold ${severityConfig[disaster.severity] || 'bg-slate-100 text-slate-600'}`}>
                  {disaster.severity}
                </span>
              </div>
              <div className="flex flex-wrap gap-4 text-sm text-slate-500 mb-4">
                {disaster.location && <span className="flex items-center gap-1"><MapPin className="h-4 w-4" />{disaster.location}</span>}
                {disaster.occurredAt && <span className="flex items-center gap-1"><Calendar className="h-4 w-4" />{new Date(disaster.occurredAt).toLocaleDateString('en-IN', { dateStyle: 'long' })}</span>}
              </div>
              <p className="text-slate-600 leading-relaxed">{disaster.description}</p>
            </div>

            <div>
              <h2 className="text-xl font-bold text-slate-900 mb-4">Relief Campaigns</h2>
              {campaigns.length === 0
                ? <div className="text-center py-12 text-slate-400 bg-white rounded-xl border border-slate-200">No campaigns linked to this disaster yet.</div>
                : <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                    {campaigns.map(c => <CampaignCard key={c.id} campaign={c} />)}
                  </div>
              }
            </div>
          </>
        )}
      </div>
    </PublicLayout>
  )
}
