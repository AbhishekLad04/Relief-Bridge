// src/pages/donation/ReceiptPage.jsx
import { ArrowLeft, Download, Heart } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Spinner from '../../components/ui/Spinner'
import { getDonationReceipt } from '../../services/donationService'

function InfoRow({ label, value }) {
  return (
    <div className="flex justify-between py-2.5 border-b border-slate-100 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm font-medium text-slate-900">{value}</span>
    </div>
  )
}

export default function ReceiptPage() {
  const { id } = useParams()
  const [receipt, setReceipt] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError]     = useState('')

  useEffect(() => {
    getDonationReceipt(id)
      .then(setReceipt)
      .catch(() => setError('Receipt not found'))
      .finally(() => setLoading(false))
  }, [id])

  const printReceipt = () => window.print()

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4">
      <div className="max-w-2xl mx-auto">
        <Link to="/citizen" className="inline-flex items-center gap-1 text-sm text-slate-500 hover:text-slate-700 mb-6">
          <ArrowLeft className="h-4 w-4" /> My Donations
        </Link>

        {loading && <Spinner center />}
        {error && <p className="text-red-500 text-center py-10">{error}</p>}

        {!loading && !error && receipt && (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 overflow-hidden">
            {/* Header */}
            <div className="bg-gradient-to-r from-slate-900 to-cyan-800 text-white p-8 text-center">
              <div className="mb-3 inline-flex h-14 w-14 items-center justify-center rounded-full bg-white/10">
                <Heart className="h-7 w-7 text-white" />
              </div>
              <h1 className="text-2xl font-bold">ReliefBridge</h1>
              <p className="text-cyan-200 text-sm">Official Donation Receipt</p>
            </div>

            {/* Body */}
            <div className="p-8">
              <div className="mb-6 text-center">
                <p className="text-xs text-slate-400 uppercase tracking-wide mb-1">Receipt Number</p>
                <p className="text-xl font-bold text-slate-900">{receipt.receiptNumber}</p>
              </div>

              <div className="rounded-xl bg-slate-50 p-5 mb-6">
                <InfoRow label="Donor Name"       value={receipt.donorName || 'Anonymous'} />
                <InfoRow label="Campaign"         value={receipt.campaignTitle} />
                <InfoRow label="Amount"           value={`₹${Number(receipt.amount).toLocaleString('en-IN')}`} />
                <InfoRow label="Transaction Ref"  value={receipt.donationReference} />
                <InfoRow label="Payment Status"   value={<span className="text-green-600 font-semibold">SUCCESS</span>} />
                <InfoRow label="Date"             value={receipt.paidAt ? new Date(receipt.paidAt).toLocaleDateString('en-IN', { dateStyle: 'long' }) : '—'} />
              </div>

              <div className="rounded-xl bg-amber-50 border border-amber-200 p-4 text-xs text-amber-700 mb-6">
                This is a system-generated receipt. For any queries, contact support@reliefbridge.com.
                ReliefBridge does not currently hold 80G certification.
              </div>

              <div className="flex flex-col sm:flex-row gap-3">
                <button
                  onClick={printReceipt}
                  className="flex-1 flex items-center justify-center gap-2 rounded-lg bg-slate-900 py-2.5 text-sm font-semibold text-white hover:bg-slate-800 transition"
                >
                  <Download className="h-4 w-4" /> Print / Save Receipt
                </button>
                <Link to="/campaigns" className="flex-1 text-center rounded-lg border border-slate-300 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition">
                  Donate to Another Campaign
                </Link>
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  )
}
