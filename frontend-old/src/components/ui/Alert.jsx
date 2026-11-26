// src/components/ui/Alert.jsx
import { AlertCircle, CheckCircle, Info, XCircle } from 'lucide-react'

const configs = {
  success: { bg: 'bg-emerald-50 border-emerald-400', text: 'text-emerald-800', Icon: CheckCircle, iconColor: 'text-emerald-500' },
  error:   { bg: 'bg-red-50 border-red-400',         text: 'text-red-800',     Icon: XCircle,    iconColor: 'text-red-500' },
  warning: { bg: 'bg-amber-50 border-amber-400',     text: 'text-amber-800',   Icon: AlertCircle,iconColor: 'text-amber-500' },
  info:    { bg: 'bg-cyan-50 border-cyan-400',        text: 'text-cyan-800',    Icon: Info,       iconColor: 'text-cyan-500' },
}

export default function Alert({ type = 'info', message, className = '' }) {
  const { bg, text, Icon, iconColor } = configs[type] || configs.info
  if (!message) return null
  return (
    <div className={`flex items-start gap-3 rounded-lg border px-4 py-3 ${bg} ${className}`}>
      <Icon className={`mt-0.5 h-5 w-5 shrink-0 ${iconColor}`} />
      <p className={`text-sm font-medium ${text}`}>{message}</p>
    </div>
  )
}
