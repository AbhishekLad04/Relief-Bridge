import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from './api';
import { AlertTriangle, CheckCircle } from 'lucide-react';

export default function Donate() {
  const { id: campaignId } = useParams();
  const navigate = useNavigate();
  const [campaign, setCampaign] = useState(null);
  const [amount, setAmount] = useState('');
  const [anonymous, setAnonymous] = useState(false);
  const [donorName, setDonorName] = useState('');
  const [donorEmail, setDonorEmail] = useState('');
  const [donorPhone, setDonorPhone] = useState('');
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const [receiptInfo, setReceiptInfo] = useState(null);

  // Load campaign title for display
  useEffect(() => {
    api.get(`/campaigns/${campaignId}`)
      .then(res => setCampaign(res))
      .catch(() => setCampaign({ title: 'Campaign' }));
  }, [campaignId]);

  const handleSubmit = async e => {
    e.preventDefault();
    setError('');
    if (!amount || Number(amount) < 10) {
      setError('Minimum donation amount is ₹10');
      return;
    }
    if (!anonymous && (!donorName || !donorEmail)) {
      setError('Name and email are required unless donating anonymously');
      return;
    }
    setLoading(true);
    try {
      // 1. Create order
      const orderRes = await api.post('/donations/order', {
        campaignId: Number(campaignId),
        amount: Number(amount),
        donorName: donorName || undefined,
        donorEmail: donorEmail || undefined,
        donorPhone: donorPhone || undefined,
        message: message || undefined,
        isAnonymous: anonymous,
        paymentMethod: 'RAZORPAY',
      });

      // 2. Mock payment (in real env Razorpay checkout would be used)
      const paymentId = 'pay_demo_' + Date.now();
      const sigRes = await api.get(`/donations/mock-signature?orderId=${orderRes.orderId}&paymentId=${paymentId}`);

      // 3. Verify payment
      const verified = await api.post('/donations/verify', {
        orderId: sigRes.orderId,
        paymentId: sigRes.paymentId,
        signature: sigRes.signature,
      });

      setReceiptInfo(verified);
      // redirect to thank you page or show receipt
    } catch (err) {
      setError(err.message || 'Donation failed');
    } finally {
      setLoading(false);
    }
  };

  if (error) {
    return <div className="p-8 text-red-600">{error}</div>;
  }

  return (
    <div className="max-w-2xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-4">Donate to {campaign?.title || 'Campaign'}</h1>
      {receiptInfo ? (
        <div className="p-6 bg-green-50 border border-green-200 rounded-lg">
          <h2 className="text-xl font-semibold mb-2 flex items-center"><CheckCircle className="mr-2" size={20} /> Donation Successful</h2>
          <p>Reference: {receiptInfo.donationReference || receiptInfo.id}</p>
          <p>Amount: ₹{Number(receiptInfo.amount).toLocaleString()}</p>
          <p>Status: {receiptInfo.status}</p>
          {receiptInfo.receiptNumber && (
            <a href={`/api/donations/receipt/${receiptInfo.receiptNumber}`} target="_blank" rel="noopener" className="mt-2 inline-block text-blue-600 hover:underline">Download Receipt (PDF)</a>
          )}
          <button onClick={() => navigate('/dashboard')} className="mt-4 px-4 py-2 bg-blue-600 text-white rounded">Back to Dashboard</button>
        </div>
      ) : (
        <form onSubmit={handleSubmit} className="grid gap-4">
          <input type="number" min="10" placeholder="Amount (₹)" value={amount} onChange={e => setAmount(e.target.value)} className="border p-2" required />
          <label className="flex items-center space-x-2">
            <input type="checkbox" checked={anonymous} onChange={e => setAnonymous(e.target.checked)} />
            <span>Donate anonymously</span>
          </label>
          {!anonymous && (
            <>
              <input type="text" placeholder="Your Name" value={donorName} onChange={e => setDonorName(e.target.value)} className="border p-2" required />
              <input type="email" placeholder="Your Email" value={donorEmail} onChange={e => setDonorEmail(e.target.value)} className="border p-2" required />
            </>
          )}
          <input type="tel" placeholder="Phone (optional)" value={donorPhone} onChange={e => setDonorPhone(e.target.value)} className="border p-2" />
          <textarea placeholder="Message (optional)" value={message} onChange={e => setMessage(e.target.value)} className="border p-2" rows={3} />
          <button type="submit" disabled={loading} className="px-4 py-2 bg-indigo-600 text-white rounded hover:bg-indigo-700">
            {loading ? 'Processing...' : 'Donate'}
          </button>
        </form>
      )}
    </div>
  );
}
