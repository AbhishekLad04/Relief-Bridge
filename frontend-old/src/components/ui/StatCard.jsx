// src/components/ui/StatCard.jsx
import { TrendingDown, TrendingUp } from 'lucide-react'

const colorMap = {
  teal:   'from-cyan-600 to-cyan-800',
  orange: 'from-orange-500 to-orange-700',
  green:  'from-emerald-500 to-emerald-700',
  purple: 'from-purple-600 to-purple-800',
  navy:   'from-slate-700 to-slate-900',
  red:    'from-red-500 to-red-700',
}

export default function StatCard({ title, value, icon: Icon, color = 'teal', change }) {
  const gradient = colorMap[color] || colorMap.teal
  const isPositive = change && parseFloat(change) >= 0
  return (
    <div className="rounded-2xl bg-white shadow-md overflow-hidden">
      <div className={`bg-gradient-to-br ${gradient} px-5 py-4 flex items-center justify-between`}>
        <div>
          <p className="text-sm font-medium text-white/80">{title}</p>
          <p className="mt-1 text-3xl font-bold text-white">{value}</p>
          {change !== undefined && (
            <div className="mt-1 flex items-center gap-1">
              {isPositive
                ? <TrendingUp className="h-4 w-4 text-emerald-300" />
                : <TrendingDown className="h-4 w-4 text-red-300" />}
              <span className={`text-xs font-medium ${isPositive ? 'text-emerald-300' : 'text-red-300'}`}>
                {change}
              </span>
            </div>
          )}
        </div>
        {Icon && (
          <div className="rounded-xl bg-white/20 p-3">
            <Icon className="h-7 w-7 text-white" />
          </div>
        )}
      </div>
    </div>
  )
}
