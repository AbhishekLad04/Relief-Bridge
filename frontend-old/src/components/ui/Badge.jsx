// src/components/ui/Badge.jsx
const variants = {
  teal:    'bg-cyan-100 text-cyan-800',
  orange:  'bg-orange-100 text-orange-800',
  green:   'bg-emerald-100 text-emerald-800',
  red:     'bg-red-100 text-red-800',
  yellow:  'bg-amber-100 text-amber-800',
  gray:    'bg-slate-100 text-slate-700',
  navy:    'bg-slate-800 text-white',
  purple:  'bg-purple-100 text-purple-800',
  blue:    'bg-blue-100 text-blue-800',
}

export default function Badge({ label, color = 'gray', className = '' }) {
  const cls = variants[color] || variants.gray
  return (
    <span className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold ${cls} ${className}`}>
      {label}
    </span>
  )
}
