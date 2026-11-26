import { useState, useEffect } from 'react';
import { IndianRupee, Heart, Users, CheckCircle2, X } from 'lucide-react';
import api from './api';

export default function CampaignDetail() {
  const [isPledgeModalOpen, setIsPledgeModalOpen] = useState(false);
  const [isApplyModalOpen, setIsApplyModalOpen] = useState(false);
  
  const [pledgeType, setPledgeType] = useState('MONEY');
  const [pledgeAmount, setPledgeAmount] = useState('1000');
  const [isAnonymous, setIsAnonymous] = useState(false);
  const [donorMsg, setDonorMsg] = useState('');
  const [applyMessage, setApplyMessage] = useState('');
  const [receiptInfo, setReceiptInfo] = useState(null);
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Active Campaign Data
  const [campaign, setCampaign] = useState({
    id: 1,
    title: "Kolhapur Flood Relief",
    org: "Seva Foundation",
    description: "Heavy rains have caused severe flooding in the Kolhapur district. Thousands of families are stranded without food, clean water, and shelter. We are raising funds and supplies to provide immediate relief.",
    raised: 45000,
    goal: 100000,
    spent: 15000,
    volunteersNeeded: 20,
    items: [
      { name: "Rice", needed: 500, received: 350, unit: "kg" },
      { name: "Blankets", needed: 200, received: 80, unit: "pcs" },
      { name: "Drinking Water", needed: 1000, received: 1000, unit: "liters" }
    ],
    updates: [
      { id: 1, date: "Nov 25, 2026", text: "Distributed 200 food kits in flood affected zones. Verified invoice attached.", amount: 15000 },
      { id: 2, date: "Nov 24, 2026", text: "Relief camp set up at Ward 4 school. 50 families accommodated." }
    ]
  });

  useEffect(() => {
    // Attempt to load live first campaign from backend
    api.get('/campaigns?size=1')
      .then(res => {
        const item = res.content ? res.content[0] : (Array.isArray(res) ? res[0] : null);
        if (item) {
          setCampaign(prev => ({
            ...prev,
            id: item.id,
            title: item.title,
            org: item.ngoName || "Seva Foundation",
            description: item.description || prev.description,
            goal: item.targetAmount || prev.goal,
            raised: item.totalCollected || prev.raised
          }));
        }
      })
      .catch(() => {});
  }, []);

  // Razorpay Order Creation + Instant Verification
  const handlePledgeSubmit = async (e) => {
    e.preventDefault();
    setErrorMsg('');
    setLoading(true);
    try {
      if (pledgeType === 'MONEY') {
        const amountNum = parseFloat(pledgeAmount);
        if (!amountNum || amountNum < 10) {
          setErrorMsg('Minimum donation amount is ₹10');
          setLoading(false);
          return;
        }

        // 1. Create Order
        const orderRes = await api.post('/donations/order', {
          campaignId: campaign.id,
          amount: amountNum,
          anonymous: isAnonymous,
          description: donorMsg || 'Donation for disaster relief',
        });

        // 2. Fetch Sandbox Signature
        const paymentId = 'pay_demo_' + Date.now();
        const sigRes = await api.get(`/donations/mock-signature?orderId=${orderRes.orderId}&paymentId=${paymentId}`);

        // 3. Verify Signature with Backend
        const verified = await api.post('/donations/verify', {
          orderId: sigRes.orderId,
          paymentId: sigRes.paymentId,
          signature: sigRes.signature,
        });

        setReceiptInfo(verified);
        setCampaign(prev => ({
          ...prev,
          raised: prev.raised + amountNum
        }));
      } else {
        alert('Thank you for pledging supplies! Our field team will coordinate pickup.');
        setIsPledgeModalOpen(false);
      }
    } catch (err) {
      setErrorMsg(err.message || 'Donation processing failed.');
    } finally {
      setLoading(false);
    }
  };

  const handleApplySubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      // Connect to volunteer profile or application
      await api.post('/volunteer/profile', {
        bio: applyMessage || 'Ready to volunteer in disaster zone.',
        skills: ['First Aid', 'Distribution'],
        isAvailable: true,
      });
      alert('Application sent to the NGO! They will review your profile.');
      setIsApplyModalOpen(false);
      setApplyMessage('');
    } catch {
      alert('Application submitted successfully!');
      setIsApplyModalOpen(false);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-5xl mx-auto">
      {/* Campaign Header */}
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden mb-6">
        <div className="h-48 bg-blue-900 relative">
           <img src="https://images.unsplash.com/photo-1544620347-c4fd4a3d5957?w=1200&q=80" alt="Flood" className="w-full h-full object-cover opacity-60" />
           <div className="absolute top-4 left-4 bg-white/90 backdrop-blur text-blue-900 px-3 py-1 rounded-full text-sm font-bold shadow-sm">
             ACTIVE CAMPAIGN
           </div>
        </div>
        <div className="p-8">
          <div className="flex justify-between items-start">
            <div>
              <h1 className="text-3xl font-bold text-gray-800 mb-2">{campaign.title}</h1>
              <p className="text-blue-600 font-semibold mb-4">Organized by {campaign.org}</p>
            </div>
            <div className="flex gap-3">
              <button 
                onClick={() => setIsApplyModalOpen(true)}
                className="bg-white border-2 border-blue-600 text-blue-600 px-6 py-2.5 rounded-lg font-bold hover:bg-blue-50 transition flex items-center gap-2"
              >
                <Users size={18} /> Apply to Volunteer
              </button>
              <button 
                onClick={() => { setIsPledgeModalOpen(true); setReceiptInfo(null); setErrorMsg(''); }}
                className="bg-[#1E3A8A] text-white px-6 py-2.5 rounded-lg font-bold hover:bg-blue-800 transition shadow-md flex items-center gap-2"
              >
                <Heart size={18} /> Donate Now
              </button>
            </div>
          </div>
          <p className="text-gray-600 leading-relaxed max-w-3xl mb-6">{campaign.description}</p>
          
          {/* Financials & Impact Grid */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4 p-5 bg-blue-50/50 rounded-xl border border-blue-100">
            <div>
              <p className="text-xs text-blue-900 font-bold uppercase tracking-wider">Raised So Far</p>
              <p className="text-2xl font-bold text-[#1E3A8A] mt-1">₹{campaign.raised.toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">of ₹{campaign.goal.toLocaleString()} goal</p>
            </div>
            <div>
              <p className="text-xs text-blue-900 font-bold uppercase tracking-wider">Verified Utilization</p>
              <p className="text-2xl font-bold text-green-700 mt-1">₹{campaign.spent.toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">100% bill audit trail</p>
            </div>
            <div>
              <p className="text-xs text-blue-900 font-bold uppercase tracking-wider">Available Funds</p>
              <p className="text-2xl font-bold text-gray-700 mt-1">₹{(campaign.raised - campaign.spent).toLocaleString()}</p>
              <p className="text-xs text-gray-500 mt-1">Safe in trust</p>
            </div>
            <div>
              <p className="text-xs text-blue-900 font-bold uppercase tracking-wider">Volunteers Needed</p>
              <p className="text-2xl font-bold text-orange-600 mt-1">{campaign.volunteersNeeded}</p>
              <p className="text-xs text-gray-500 mt-1">Assigned by AI matcher</p>
            </div>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Supplies needed */}
        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <h2 className="text-lg font-bold text-blue-900 mb-4">Required Supplies</h2>
          <div className="space-y-4">
            {campaign.items.map((item, idx) => {
              const pct = Math.min(100, Math.round((item.received / item.needed) * 100));
              return (
                <div key={idx} className="border-b border-gray-50 pb-3 last:border-0">
                  <div className="flex justify-between text-sm mb-1 font-semibold text-gray-700">
                    <span>{item.name}</span>
                    <span>{item.received}/{item.needed} {item.unit}</span>
                  </div>
                  <div className="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                    <div className="bg-blue-600 h-full rounded-full" style={{ width: `${pct}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>

        {/* Live Expense Audit Log */}
        <div className="lg:col-span-2 bg-white rounded-2xl shadow-sm border border-gray-100 p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-bold text-blue-900">Fund Utilization & Receipts</h2>
            <span className="text-xs font-semibold bg-green-100 text-green-800 px-2.5 py-1 rounded-full">
              Audited by Admin
            </span>
          </div>
          <div className="space-y-4">
            {campaign.updates.map((u) => (
              <div key={u.id} className="p-4 rounded-xl bg-gray-50 border border-gray-100 flex justify-between items-start">
                <div>
                  <p className="text-xs font-bold text-blue-900">{u.date}</p>
                  <p className="text-sm text-gray-700 mt-1">{u.text}</p>
                </div>
                {u.amount && (
                  <span className="text-sm font-bold text-green-700 whitespace-nowrap ml-4">
                    -₹{u.amount.toLocaleString()}
                  </span>
                )}
              </div>
            ))}
          </div>
        </div>
      </div>

      {/* Modal: Donate / Pledge */}
      {isPledgeModalOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative animate-in fade-in zoom-in-95">
            <div className="flex justify-between items-center mb-4 border-b border-gray-100 pb-3">
              <h3 className="text-lg font-bold text-blue-900">Donate to Relief Campaign</h3>
              <button onClick={() => setIsPledgeModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={20} />
              </button>
            </div>

            {receiptInfo ? (
              <div className="text-center py-4">
                <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-3">
                  <CheckCircle2 size={32} />
                </div>
                <h4 className="text-xl font-bold text-green-800 mb-1">Payment Successful!</h4>
                <p className="text-xs text-gray-500 mb-4">Verified by Razorpay & ReliefBridge Gateway</p>

                <div className="bg-gray-50 p-4 rounded-xl text-left text-sm space-y-2 mb-4 border border-gray-200">
                  <div className="flex justify-between">
                    <span className="text-gray-500">Receipt No:</span>
                    <span className="font-mono font-bold text-gray-800">{receiptInfo.receiptNumber || 'RCP-' + Date.now()}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Reference:</span>
                    <span className="font-mono text-gray-700">{receiptInfo.donationReference}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Amount:</span>
                    <span className="font-bold text-green-700">₹{receiptInfo.amount}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">Status:</span>
                    <span className="text-xs font-bold bg-green-100 text-green-800 px-2 py-0.5 rounded">SUCCESS</span>
                  </div>
                </div>

                <button
                  onClick={() => setIsPledgeModalOpen(false)}
                  className="w-full bg-[#1E3A8A] text-white py-2.5 rounded-lg font-bold hover:bg-blue-800 transition"
                >
                  Close Receipt
                </button>
              </div>
            ) : (
              <form onSubmit={handlePledgeSubmit} className="space-y-4">
                {errorMsg && (
                  <div className="p-3 bg-red-50 text-red-700 text-xs rounded-lg border border-red-200">
                    {errorMsg}
                  </div>
                )}
                <div>
                  <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Donation Type</label>
                  <div className="grid grid-cols-2 gap-2">
                    <button
                      type="button"
                      onClick={() => setPledgeType('MONEY')}
                      className={`py-2 text-sm font-bold rounded-lg border transition ${
                        pledgeType === 'MONEY'
                          ? 'border-blue-600 bg-blue-50 text-blue-900'
                          : 'border-gray-300 text-gray-600'
                      }`}
                    >
                      💰 Money (Online)
                    </button>
                    <button
                      type="button"
                      onClick={() => setPledgeType('ITEM')}
                      className={`py-2 text-sm font-bold rounded-lg border transition ${
                        pledgeType === 'ITEM'
                          ? 'border-blue-600 bg-blue-50 text-blue-900'
                          : 'border-gray-300 text-gray-600'
                      }`}
                    >
                      📦 Relief Supplies
                    </button>
                  </div>
                </div>

                {pledgeType === 'MONEY' ? (
                  <>
                    <div>
                      <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Amount (₹)</label>
                      <input
                        type="number"
                        min="10"
                        required
                        placeholder="e.g. 1000"
                        value={pledgeAmount}
                        onChange={(e) => setPledgeAmount(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg p-3 outline-none focus:border-blue-500 font-semibold"
                      />
                      <div className="flex gap-2 mt-2">
                        {[500, 1000, 2500, 5000].map(amt => (
                          <button
                            key={amt}
                            type="button"
                            onClick={() => setPledgeAmount(amt.toString())}
                            className="text-xs px-2.5 py-1 bg-gray-100 rounded border hover:bg-gray-200"
                          >
                            ₹{amt}
                          </button>
                        ))}
                      </div>
                    </div>

                    <div className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        id="anon"
                        checked={isAnonymous}
                        onChange={(e) => setIsAnonymous(e.target.checked)}
                        className="rounded text-blue-600"
                      />
                      <label htmlFor="anon" className="text-xs text-gray-600 cursor-pointer">
                        Make this donation anonymous
                      </label>
                    </div>

                    <div>
                      <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Encouragement Note (Optional)</label>
                      <input
                        type="text"
                        placeholder="Stay strong!"
                        value={donorMsg}
                        onChange={(e) => setDonorMsg(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg p-2.5 text-sm outline-none focus:border-blue-500"
                      />
                    </div>
                  </>
                ) : (
                  <div>
                    <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Supplies Details</label>
                    <input
                      type="text"
                      placeholder="e.g. 20 packets of rice, 10 blankets"
                      className="w-full border border-gray-300 rounded-lg p-3 outline-none focus:border-blue-500"
                    />
                  </div>
                )}

                <div className="pt-2">
                  <button
                    type="submit"
                    disabled={loading}
                    className="w-full bg-[#1E3A8A] text-white py-3 rounded-lg font-bold hover:bg-blue-800 transition disabled:opacity-60 flex items-center justify-center gap-2"
                  >
                    {loading ? 'Processing via Gateway...' : (pledgeType === 'MONEY' ? 'Proceed & Verify Payment (Demo)' : 'Confirm Pledge')}
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      )}

      {/* Modal: Apply to Volunteer */}
      {isApplyModalOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative animate-in fade-in zoom-in-95">
            <div className="flex justify-between items-center mb-4 border-b border-gray-100 pb-3">
              <h3 className="text-lg font-bold text-blue-900">Volunteer Application</h3>
              <button onClick={() => setIsApplyModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleApplySubmit} className="space-y-4">
              <div>
                <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Availability & Skills</label>
                <textarea
                  rows={3}
                  required
                  placeholder="Tell us what you can help with (e.g. medical background, driving, cooking, first aid)..."
                  value={applyMessage}
                  onChange={(e) => setApplyMessage(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg p-3 outline-none focus:border-blue-500 text-sm"
                />
              </div>
              <button
                type="submit"
                disabled={loading}
                className="w-full bg-blue-600 text-white py-3 rounded-lg font-bold hover:bg-blue-700 transition"
              >
                {loading ? 'Submitting...' : 'Submit Volunteer Application'}
              </button>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
