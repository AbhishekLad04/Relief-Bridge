// src/pages/public/CampaignDetailPage.jsx
import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer
} from 'recharts'
import PublicLayout from '../../components/layout/PublicLayout'
import Badge from '../../components/ui/Badge'
import CampaignProgress from '../../components/campaigns/CampaignProgress'
import Spinner from '../../components/ui/Spinner'
import Alert from '../../components/ui/Alert'
import { getCampaignById, getFinancialSummary } from '../../services/campaignService'
import { getUtilizationsByCampaign } from '../../services/fundUtilizationService'
import { getMyDonations } from '../../services/donationService'
import { useAuth } from '../../context/AuthContext'

const TABS = ['Overview', 'Donations', 'Fund Utilization', 'Financial Summary']

function statusColor(s) {
  if (s === 'ACTIVE') return 'green'
  if (s === 'COMPLETED') return 'teal'
  if (s === 'CANCELLED') return 'red'
  return 'gray'
}

function verifyColor(s) {
  if (s === 'VERIFIED') return 'green'
  if (s === 'REJECTED') return 'red'
  return 'yellow'
}

export default function CampaignDetailPage() {
  const { id } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [campaign, setCampaign] = useState(null)
  const [summary, setSummary] = useState(null)
  const [utilizations, setUtilizations] = useState([])
  const [activeTab, setActiveTab] = useState('Overview')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    const load = async () => {
      setLoading(true)
      try {
        const [campData, sumData] = await Promise.all([
          getCampaignById(id),
          getFinancialSummary(id).catch(() => null),
        ])
        setCampaign(campData)
        setSummary(sumData)
        const utils = await getUtilizationsByCampaign(id).catch(() => [])
        setUtilizations(Array.isArray(utils) ? utils : utils?.content ?? [])
      } catch {
        setError('Failed to load campaign details.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [id])

  if (loading) return <PublicLayout><Spinner className="py-32" size="lg" /></PublicLayout>
  if (error || !campaign) return <PublicLayout><Alert type="error" message={error || 'Campaign not found'} className="m-8" /></PublicLayout>

  const chartData = summary ? [
    { name: 'Funds', Collected: summary.totalCollected || 0, Utilized: summary.totalUtilized || 0 }
  ] : []

  const handleDonate = () => {
    if (!user) navigate('/login')
    else navigate(`/donate/${id}`)
  }

  return (
    <PublicLayout>
      {/* Header */}
      <div className="bg-gradient-to-br from-slate-900 to-cyan-900 py-10">
        <div className="mx-auto max-w-5xl px-4">
          <div className="flex flex-wrap items-start gap-3 mb-3">
            <Badge label={campaign.status} color={statusColor(campaign.status)} />
            {campaign.disasterName && (
              <Badge label={`Disaster: ${campaign.disasterName}`} color="orange" />
            )}
          </div>
          <h1 className="text-2xl md:text-3xl font-bold text-white mb-2">{campaign.title}</h1>
          <p className="text-cyan-300 text-sm">by {campaign.ngoName || 'NGO'}</p>

          <div className="mt-5 flex flex-col sm:flex-row items-start sm:items-center gap-6">
            <div className="flex-1 w-full max-w-md">
              <CampaignProgress raised={campaign.raisedAmount || 0} target={campaign.targetAmount || 0} />
            </div>
            <button
              onClick={handleDonate}
              className="rounded-xl bg-orange-500 px-6 py-2.5 font-bold text-white hover:bg-orange-600 transition shrink-0"
            >
              Donate Now
            </button>
          </div>
        </div>
      </div>

      {/* Tabs */}
      <div className="mx-auto max-w-5xl px-4 py-8">
        <div className="flex gap-1 border-b border-slate-200 mb-6 overflow-x-auto">
          {TABS.map(tab => (
            <button
              key={tab}
              onClick={() => setActiveTab(tab)}
              className={`shrink-0 px-4 py-2.5 text-sm font-medium border-b-2 transition
                ${activeTab === tab
                  ? 'border-cyan-600 text-cyan-700'
                  : 'border-transparent text-slate-500 hover:text-slate-800'
                }`}
            >
              {tab}
            </button>
          ))}
        </div>

        {/* Overview */}
        {activeTab === 'Overview' && (
          <div className="prose max-w-none">
            <h2 className="text-lg font-semibold text-slate-900 mb-3">About this Campaign</h2>
            <p className="text-slate-600 whitespace-pre-line">{campaign.description || 'No description provided.'}</p>

            <div className="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-4">
              {[
                { label: 'Target Amount', value: `₹${(campaign.targetAmount || 0).toLocaleString('en-IN')}` },
                { label: 'Amount Raised', value: `₹${(campaign.raisedAmount || 0).toLocaleString('en-IN')}` },
                { label: 'End Date', value: campaign.endDate ? new Date(campaign.endDate).toLocaleDateString() : 'N/A' },
              ].map(item => (
                <div key={item.label} className="rounded-xl bg-slate-50 p-4 border border-slate-200">
                  <p className="text-xs text-slate-400 uppercase tracking-wider">{item.label}</p>
                  <p className="mt-1 text-xl font-bold text-slate-900">{item.value}</p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* Donations */}
        {activeTab === 'Donations' && (
          <div>
            <h2 className="text-lg font-semibold text-slate-900 mb-4">Recent Donors</h2>
            <p className="text-sm text-slate-500 mb-4">Showing public donation records for this campaign.</p>
            <div className="rounded-xl border border-slate-200 overflow-hidden">
              <table className="w-full text-sm">
                <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Donor</th>
                    <th className="px-4 py-3">Amount</th>
                    <th className="px-4 py-3">Date</th>
                    <th className="px-4 py-3">Message</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  <tr>
                    <td colSpan={4} className="px-4 py-6 text-center text-slate-400">
                      Donation history is available after logging in.
                    </td>
                  </tr>
                </tbody>
              </table>
            </div>
          </div>
        )}

        {/* Fund Utilization */}
        {activeTab === 'Fund Utilization' && (
          <div>
            <h2 className="text-lg font-semibold text-slate-900 mb-4">Fund Utilization Records</h2>
            {utilizations.length === 0 ? (
              <p className="text-center text-slate-400 py-8">No utilization records yet.</p>
            ) : (
              <div className="rounded-xl border border-slate-200 overflow-auto">
                <table className="w-full text-sm min-w-[600px]">
                  <thead className="bg-slate-50 text-left text-xs uppercase text-slate-500">
                    <tr>
                      <th className="px-4 py-3">Category</th>
                      <th className="px-4 py-3">Description</th>
                      <th className="px-4 py-3">Amount</th>
                      <th className="px-4 py-3">Date</th>
                      <th className="px-4 py-3">Status</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-slate-100">
                    {utilizations.map(u => (
                      <tr key={u.id} className="hover:bg-slate-50">
                        <td className="px-4 py-3 font-medium">{u.category}</td>
                        <td className="px-4 py-3 text-slate-600">{u.description}</td>
                        <td className="px-4 py-3 font-semibold text-slate-900">₹{(u.amount || 0).toLocaleString('en-IN')}</td>
                        <td className="px-4 py-3 text-slate-500">{u.date ? new Date(u.date).toLocaleDateString() : ''}</td>
                        <td className="px-4 py-3">
                          <Badge label={u.verificationStatus || 'PENDING'} color={verifyColor(u.verificationStatus)} />
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        )}

        {/* Financial Summary */}
        {activeTab === 'Financial Summary' && (
          <div>
            <h2 className="text-lg font-semibold text-slate-900 mb-4">Financial Summary</h2>
            {!summary ? (
              <p className="text-center text-slate-400 py-8">No financial data available.</p>
            ) : (
              <>
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-8">
                  {[
                    { label: 'Total Collected', value: `₹${(summary.totalCollected || 0).toLocaleString('en-IN')}` },
                    { label: 'Total Utilized', value: `₹${(summary.totalUtilized || 0).toLocaleString('en-IN')}` },
                    { label: 'Remaining', value: `₹${(summary.remainingFunds || 0).toLocaleString('en-IN')}` },
                    { label: 'Donations', value: summary.donationCount || 0 },
                  ].map(item => (
                    <div key={item.label} className="rounded-xl bg-slate-50 border border-slate-200 p-4 text-center">
                      <p className="text-xs text-slate-400 uppercase tracking-wider">{item.label}</p>
                      <p className="mt-1 text-xl font-bold text-slate-900">{item.value}</p>
                    </div>
                  ))}
                </div>
                <div className="rounded-xl bg-white border border-slate-200 p-4">
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={chartData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis tickFormatter={v => `₹${v.toLocaleString('en-IN')}`} />
                      <Tooltip formatter={v => `₹${v.toLocaleString('en-IN')}`} />
                      <Legend />
                      <Bar dataKey="Collected" fill="#0891b2" radius={[4, 4, 0, 0]} />
                      <Bar dataKey="Utilized" fill="#f97316" radius={[4, 4, 0, 0]} />
                    </BarChart>
                  </ResponsiveContainer>
                </div>
              </>
            )}
          </div>
        )}
      </div>
    </PublicLayout>
  )
}
