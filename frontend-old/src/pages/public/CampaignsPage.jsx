// src/pages/public/CampaignsPage.jsx
import { Search } from 'lucide-react'
import { useEffect, useState } from 'react'
import CampaignCard from '../../components/campaigns/CampaignCard'
import PublicLayout from '../../components/layout/PublicLayout'
import Spinner from '../../components/ui/Spinner'
import { getAllCampaigns } from '../../services/campaignService'

const STATUS_OPTIONS = [
  { value: '', label: 'All Statuses' },
  { value: 'ACTIVE', label: 'Active' },
  { value: 'COMPLETED', label: 'Completed' },
  { value: 'CANCELLED', label: 'Cancelled' },
]

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [search, setSearch] = useState('')
  const [status, setStatus] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(1)

  const fetchCampaigns = async (q, s, p) => {
    setLoading(true)
    setError('')
    try {
      const params = { size: 9, page: p }
      if (q) params.search = q
      if (s) params.status = s
      const data = await getAllCampaigns(params)
      if (Array.isArray(data)) {
        setCampaigns(data)
        setTotalPages(1)
      } else {
        setCampaigns(data.content || [])
        setTotalPages(data.totalPages || 1)
      }
    } catch {
      setError('Failed to load campaigns. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    fetchCampaigns(search, status, page)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [page])

  const handleSearch = (e) => {
    e.preventDefault()
    setPage(0)
    fetchCampaigns(search, status, 0)
  }

  return (
    <PublicLayout>
      <div className="bg-gradient-to-r from-slate-900 to-cyan-900 py-10">
        <div className="mx-auto max-w-6xl px-4 text-center">
          <h1 className="text-3xl font-bold text-white mb-2">Relief Campaigns</h1>
          <p className="text-cyan-200">Support verified NGO campaigns and make an impact</p>
        </div>
      </div>

      <div className="mx-auto max-w-6xl px-4 py-8">
        {/* Search & filter */}
        <form onSubmit={handleSearch} className="mb-8 flex flex-col sm:flex-row gap-3">
          <div className="relative flex-1">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-400" />
            <input
              type="text"
              value={search}
              onChange={e => setSearch(e.target.value)}
              placeholder="Search campaigns…"
              className="w-full rounded-xl border border-slate-200 bg-white pl-10 pr-4 py-2.5 text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>
          <select
            value={status}
            onChange={e => { setStatus(e.target.value); setPage(0); fetchCampaigns(search, e.target.value, 0) }}
            className="rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm shadow-sm focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            {STATUS_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
          </select>
          <button
            type="submit"
            className="rounded-xl bg-cyan-700 px-5 py-2.5 text-sm font-semibold text-white hover:bg-cyan-800 transition"
          >
            Search
          </button>
        </form>

        {loading && <Spinner className="py-16" />}
        {!loading && error && <p className="text-center text-red-500 py-8">{error}</p>}
        {!loading && !error && campaigns.length === 0 && (
          <p className="text-center text-slate-400 py-16">No campaigns found matching your criteria.</p>
        )}
        {!loading && !error && campaigns.length > 0 && (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-6">
              {campaigns.map(c => <CampaignCard key={c.id} campaign={c} />)}
            </div>

            {/* Pagination */}
            {totalPages > 1 && (
              <div className="mt-8 flex items-center justify-center gap-2">
                <button
                  disabled={page === 0}
                  onClick={() => setPage(p => p - 1)}
                  className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                >
                  Previous
                </button>
                <span className="text-sm text-slate-500">Page {page + 1} of {totalPages}</span>
                <button
                  disabled={page >= totalPages - 1}
                  onClick={() => setPage(p => p + 1)}
                  className="rounded-lg border border-slate-200 px-4 py-2 text-sm font-medium text-slate-700 hover:bg-slate-50 disabled:opacity-40"
                >
                  Next
                </button>
              </div>
            )}
          </>
        )}
      </div>
    </PublicLayout>
  )
}
