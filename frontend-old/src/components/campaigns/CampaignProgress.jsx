// src/components/campaigns/CampaignProgress.jsx
export default function CampaignProgress({ raised = 0, target = 1, className = '' }) {
  const pct = Math.min(100, Math.round((raised / Math.max(target, 1)) * 100))
  return (
    <div className={className}>
      <div className="mb-1 flex items-center justify-between text-xs font-medium text-slate-600">
        <span>₹{raised.toLocaleString('en-IN')} raised</span>
        <span>{pct}%</span>
      </div>
      <div className="h-2 w-full overflow-hidden rounded-full bg-slate-200">
        <div
          className="h-full rounded-full bg-gradient-to-r from-cyan-500 to-cyan-700 transition-all duration-700"
          style={{ width: `${pct}%` }}
        />
      </div>
      <p className="mt-1 text-right text-xs text-slate-400">of ₹{target.toLocaleString('en-IN')} goal</p>
    </div>
  )
}
