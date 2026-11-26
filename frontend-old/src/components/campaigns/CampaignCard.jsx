// src/components/campaigns/CampaignCard.jsx
import { Calendar, MapPin } from 'lucide-react'
import { Link } from 'react-router-dom'
import Badge from '../ui/Badge'
import CampaignProgress from './CampaignProgress'

function statusColor(s) {
  if (s === 'ACTIVE') return 'green'
  if (s === 'COMPLETED') return 'teal'
  if (s === 'CANCELLED') return 'red'
  return 'gray'
}

function daysLeft(endDate) {
  if (!endDate) return null
  const diff = Math.ceil((new Date(endDate) - Date.now()) / 86400000)
  return diff > 0 ? diff : 0
}

export default function CampaignCard({ campaign }) {
  const {
    id, title, ngoName, status, targetAmount = 0,
    raisedAmount = 0, endDate, disasterName,
  } = campaign

  const days = daysLeft(endDate)

  return (
    <div className="flex flex-col rounded-2xl bg-white shadow-md hover:shadow-xl transition-shadow overflow-hidden">
      {/* Placeholder image */}
      <div className="relative h-40 bg-gradient-to-br from-slate-800 to-cyan-900 flex items-center justify-center">
        <span className="text-4xl opacity-30">🏕️</span>
        <div className="absolute top-3 right-3">
          <Badge label={status} color={statusColor(status)} />
        </div>
      </div>

      <div className="flex flex-col flex-1 p-4 gap-3">
        {disasterName && (
          <div className="flex items-center gap-1 text-xs text-orange-600 font-medium">
            <MapPin className="h-3 w-3" />
            {disasterName}
          </div>
        )}

        <h3 className="text-base font-semibold text-slate-900 line-clamp-2 leading-snug">{title}</h3>

        {ngoName && (
          <p className="text-xs text-slate-500">by <span className="font-medium text-slate-700">{ngoName}</span></p>
        )}

        <CampaignProgress raised={raisedAmount} target={targetAmount} />

        <div className="flex items-center justify-between mt-auto pt-2">
          {days !== null && (
            <div className="flex items-center gap-1 text-xs text-slate-500">
              <Calendar className="h-3 w-3" />
              <span>{days === 0 ? 'Ended' : `${days} days left`}</span>
            </div>
          )}
          <Link
            to={`/campaigns/${id}`}
            className="ml-auto rounded-lg bg-cyan-700 px-3 py-1.5 text-xs font-semibold text-white hover:bg-cyan-800 transition"
          >
            Donate
          </Link>
        </div>
      </div>
    </div>
  )
}
