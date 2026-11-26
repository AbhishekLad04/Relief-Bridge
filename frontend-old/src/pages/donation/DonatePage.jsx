// src/pages/donation/DonatePage.jsx
import { AlertCircle, CheckCircle, Heart } from 'lucide-react'
import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import Spinner from '../../components/ui/Spinner'
import { getCampaignById } from '../../services/campaignService'
import { createDonationOrder, getMockSignature, verifyDonationPayment } from '../../services/donationService'
import { useAuth } from '../../context/AuthContext'

const PRESETS = [100, 500, 1000, 2500, 5000, 10000]

export default function DonatePage() {
  const { campaignId } = useParams()
  const { user } = useAuth()
  const navigate = useNavigate()

  const [campaign, setCampaign]   = useState(null)
  const [loading, setLoading]     = useState(true)

  const [amount, setAmount]       = useState(500)
  const [customAmt, setCustomAmt] = useState('')
  const [isAnon, setIsAnon]       = useState(false)
  const [donorName, setDonorName] = useState(user?.name || '')
  const [donorEmail, setDonorEmail] = useState(user?.email || '')
  const [message, setMessage]     = useState('')

  const [step, setStep]           = useState('form')   // 'form' | 'order' | 'processing' | 'done'
  const [orderData, setOrderData] = useState(null)
  const [donation, setDonation]   = useState(null)
  const [error, setError]         = useState('')
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    getCampaignById(campaignId)
      .then(setCampaign)
      .catch(() => setError('Campaign not found'))
      .finally(() => setLoading(false))
  }, [campaignId])

  const finalAmount = customAmt ? Number(customAmt) : amount

  // Step 1: Create Order
  const handleProceed = async (e) => {
    e.preventDefault()
    setError('')
    if (!finalAmount || finalAmount < 10) { setError('Minimum donation amount is ₹10'); return }
    setSubmitting(true)
    try {
      const dto = {
        campaignId: Number(campaignId),
        amount: finalAmount,
        anonymous: isAnon,
        donorName: isAnon ? 'Anonymous' : donorName,
        donorEmail,
        message,
      }
      const data = await createDonationOrder(dto)
      setOrderData(data)
      setStep('order')
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create payment order. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  // Step 2: Mock payment confirmation → verify
  const handleConfirmPayment = async () => {
    setError('')
    setStep('processing')
    try {
      const paymentId = `pay_demo_${Date.now()}`
      const sigData   = await getMockSignature(orderData.orderId, paymentId)
      const verified  = await verifyDonationPayment({
        orderId:   sigData.orderId,
        paymentId: sigData.paymentId,
        signature: sigData.signature,
      })
      setDonation(verified)
      setStep('done')
    } catch (err) {
      setError(err.response?.data?.message || 'Payment verification failed.')
      setStep('order')
    }
  }

  if (loading) return <div className="min-h-screen flex items-center justify-center"><Spinner size="lg" /></div>
  if (error && !campaign) return (
    <div className="min-h-screen flex items-center justify-center">
      <div className="text-center">
        <p className="text-red-500 mb-4">{error}</p>
        <Link to="/campaigns" className="text-cyan-600 hover:underline">← Back to Campaigns</Link>
      </div>
    </div>
  )

  return (
    <div className="min-h-screen bg-slate-50 py-10 px-4">
      <div className="max-w-2xl mx-auto">
        <div className="mb-6">
          <Link to={`/campaigns/${campaignId}`} className="text-sm text-slate-500 hover:text-slate-700">← Back to Campaign</Link>
        </div>

        {step === 'done' && donation ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-10 text-center">
            <div className="mb-4 inline-flex h-20 w-20 items-center justify-center rounded-full bg-green-100">
              <CheckCircle className="h-10 w-10 text-green-500" />
            </div>
            <h2 className="text-2xl font-bold text-slate-900 mb-2">Donation Successful!</h2>
            <p className="text-slate-500 mb-6">Thank you for your generous contribution to ReliefBridge.</p>
            <div className="rounded-xl bg-slate-50 p-5 text-left space-y-2 mb-6">
              <Row label="Reference"   value={donation.donationReference} />
              <Row label="Amount"      value={`₹${donation.amount?.toLocaleString('en-IN')}`} />
              <Row label="Campaign"    value={campaign?.title} />
              <Row label="Status"      value={<span className="text-green-600 font-semibold">SUCCESS</span>} />
              {donation.receiptNumber && <Row label="Receipt #" value={donation.receiptNumber} />}
            </div>
            <div className="flex flex-col sm:flex-row gap-3 justify-center">
              {donation.receiptNumber && (
                <Link
                  to={`/receipt/${donation.receiptNumber}`}
                  className="rounded-lg bg-cyan-600 px-6 py-2.5 text-sm font-semibold text-white hover:bg-cyan-700 transition"
                >
                  View Receipt
                </Link>
              )}
              <Link to="/campaigns" className="rounded-lg border border-slate-300 px-6 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition">
                Explore More Campaigns
              </Link>
              <Link to="/citizen" className="rounded-lg border border-slate-300 px-6 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50 transition">
                My Donations
              </Link>
            </div>
          </div>
        ) : step === 'processing' ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-16 text-center">
            <Spinner size="lg" center />
            <p className="mt-4 text-slate-600">Processing your payment securely…</p>
          </div>
        ) : step === 'order' && orderData ? (
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
            <div className="mb-6 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-cyan-100 flex items-center justify-center">
                <Heart className="h-5 w-5 text-cyan-600" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900">Confirm Payment</h2>
                <p className="text-sm text-slate-500">Demo / Sandbox Mode</p>
              </div>
            </div>

            <div className="rounded-xl bg-amber-50 border border-amber-200 p-4 mb-6 text-sm text-amber-700">
              ⚠️ This is a <strong>sandbox demo</strong>. No real payment will be charged. Click "Confirm" to simulate a successful payment.
            </div>

            <div className="rounded-xl bg-slate-50 p-5 space-y-2 mb-6">
              <Row label="Order ID"   value={orderData.orderId} />
              <Row label="Amount"     value={`₹${orderData.amount?.toLocaleString('en-IN')} ${orderData.currency || 'INR'}`} />
              <Row label="Campaign"   value={campaign?.title} />
              <Row label="Reference"  value={orderData.donationReference} />
            </div>

            {error && (
              <div className="mb-4 flex items-center gap-2 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm">
                <AlertCircle className="h-4 w-4 shrink-0" />{error}
              </div>
            )}

            <div className="flex gap-3">
              <button
                onClick={handleConfirmPayment}
                className="flex-1 rounded-lg bg-green-600 py-3 text-sm font-semibold text-white hover:bg-green-700 transition"
              >
                ✓ Confirm Payment (Demo)
              </button>
              <button
                onClick={() => { setStep('form'); setError('') }}
                className="rounded-lg border border-slate-300 px-5 py-3 text-sm text-slate-700 hover:bg-slate-50 transition"
              >
                Cancel
              </button>
            </div>
          </div>
        ) : (
          /* FORM */
          <div className="bg-white rounded-2xl shadow-sm border border-slate-200 p-8">
            <div className="mb-6 flex items-center gap-3">
              <div className="h-10 w-10 rounded-xl bg-orange-100 flex items-center justify-center">
                <Heart className="h-5 w-5 text-orange-500" />
              </div>
              <div>
                <h2 className="text-lg font-bold text-slate-900">Make a Donation</h2>
                <p className="text-sm text-slate-500">{campaign?.title}</p>
              </div>
            </div>

            {error && (
              <div className="mb-4 flex items-center gap-2 rounded-lg bg-red-50 border border-red-200 px-4 py-3 text-red-700 text-sm">
                <AlertCircle className="h-4 w-4 shrink-0" />{error}
              </div>
            )}

            <form onSubmit={handleProceed} className="space-y-6">
              {/* Amount */}
              <div>
                <label className="block text-sm font-medium text-slate-700 mb-3">Select Amount (₹)</label>
                <div className="grid grid-cols-3 gap-2 mb-3">
                  {PRESETS.map(p => (
                    <button key={p} type="button"
                      onClick={() => { setAmount(p); setCustomAmt('') }}
                      className={`rounded-lg border py-2.5 text-sm font-semibold transition ${!customAmt && amount === p ? 'border-cyan-600 bg-cyan-50 text-cyan-700' : 'border-slate-300 text-slate-700 hover:border-cyan-400'}`}
                    >
                      ₹{p.toLocaleString('en-IN')}
                    </button>
                  ))}
                </div>
                <input
                  type="number" min={10} placeholder="Custom amount (₹)"
                  value={customAmt}
                  onChange={e => { setCustomAmt(e.target.value); setAmount(0) }}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                />
              </div>

              {/* Anonymous toggle */}
              <div className="flex items-center gap-3">
                <input type="checkbox" id="anon" checked={isAnon}
                  onChange={e => setIsAnon(e.target.checked)}
                  className="h-4 w-4 rounded border-slate-300 text-cyan-600 focus:ring-cyan-500" />
                <label htmlFor="anon" className="text-sm text-slate-700">Make this donation anonymous</label>
              </div>

              {!isAnon && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Your Name</label>
                    <input type="text" value={donorName}
                      onChange={e => setDonorName(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium text-slate-700 mb-1">Email (for receipt)</label>
                    <input type="email" value={donorEmail}
                      onChange={e => setDonorEmail(e.target.value)}
                      className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500" />
                  </div>
                </div>
              )}

              <div>
                <label className="block text-sm font-medium text-slate-700 mb-1">Message (Optional)</label>
                <textarea value={message} rows={2}
                  onChange={e => setMessage(e.target.value)}
                  className="w-full rounded-lg border border-slate-300 px-4 py-2.5 text-sm focus:border-cyan-500 focus:outline-none focus:ring-1 focus:ring-cyan-500"
                  placeholder="A message of hope or encouragement..." />
              </div>

              <div className="rounded-xl bg-slate-50 p-4 flex items-center justify-between">
                <span className="text-sm text-slate-500">Total Donation</span>
                <span className="text-2xl font-bold text-slate-900">₹{(finalAmount || 0).toLocaleString('en-IN')}</span>
              </div>

              <button type="submit" disabled={submitting}
                className="w-full flex items-center justify-center gap-2 rounded-lg bg-orange-500 py-3 text-sm font-semibold text-white hover:bg-orange-600 disabled:opacity-60 transition">
                {submitting ? <Spinner size="sm" /> : <><Heart className="h-4 w-4" /> Proceed to Pay</>}
              </button>
            </form>
          </div>
        )}
      </div>
    </div>
  )
}

function Row({ label, value }) {
  return (
    <div className="flex items-center justify-between text-sm">
      <span className="text-slate-500">{label}</span>
      <span className="font-medium text-slate-900">{value}</span>
    </div>
  )
}
