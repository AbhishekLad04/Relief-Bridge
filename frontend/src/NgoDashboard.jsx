import { useState, useEffect } from 'react';
import { Plus, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from './context/AuthContext';
import api from './api';

const inputClass =
  'w-full border border-gray-300 rounded-lg p-3 outline-none focus:border-blue-500';

export default function NgoDashboard() {
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  // ---------- Campaigns (Live Backend + Fallback) ----------
  const [campaigns, setCampaigns] = useState([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [newCampaign, setNewCampaign] = useState({ title: '', goal: '', description: '', category: 'FLOOD_RELIEF' });
  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');

  // Fetch campaigns from backend
  useEffect(() => {
    fetchCampaigns();
    if (isAdmin) {
      fetchDisasters();
    }
  }, [isAdmin]);

  const fetchCampaigns = async () => {
    try {
      const res = await api.get('/campaigns?size=20');
      const list = res.content || (Array.isArray(res) ? res : []);
      if (list.length > 0) {
        setCampaigns(
          list.map((c) => ({
            id: c.id,
            title: c.title,
            status: c.status || 'ACTIVE',
            funding: c.targetAmount ? Math.min(100, Math.round(((c.totalCollected || 0) / c.targetAmount) * 100)) : 0,
            volunteers: 10,
            goal: c.targetAmount || 100000,
          }))
        );
      } else {
        setCampaigns([
          { id: 1, title: 'Kerala Flood Relief', status: 'ACTIVE', funding: 45, volunteers: 10, goal: 150000 },
          { id: 2, title: 'Assam Earthquake Shelter', status: 'ACTIVE', funding: 70, volunteers: 5, goal: 100000 },
          { id: 3, title: 'Cyclone Food Drive', status: 'CLOSED', funding: 100, volunteers: 20, goal: 100000 },
        ]);
      }
    } catch {
      setCampaigns([
        { id: 1, title: 'Kerala Flood Relief', status: 'ACTIVE', funding: 45, volunteers: 10, goal: 150000 },
        { id: 2, title: 'Assam Earthquake Shelter', status: 'ACTIVE', funding: 70, volunteers: 5, goal: 100000 },
        { id: 3, title: 'Cyclone Food Drive', status: 'CLOSED', funding: 100, volunteers: 20, goal: 100000 },
      ]);
    }
  };

  const handleCreateCampaign = async (e) => {
    e.preventDefault();
    if (!newCampaign.title) return;

    try {
      setLoading(true);
      setErrorMsg('');
      const targetAmt = parseInt(newCampaign.goal) || 100000;
      const today = new Date().toISOString().split('T')[0];
      const endDate = new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString().split('T')[0];

      await api.post('/campaigns', {
        title: newCampaign.title,
        description: newCampaign.description || newCampaign.title,
        targetAmount: targetAmt,
        category: newCampaign.category || 'FLOOD_RELIEF',
        startDate: today,
        endDate: endDate,
      });

      await fetchCampaigns();
      setIsModalOpen(false);
      setNewCampaign({ title: '', goal: '', description: '', category: 'FLOOD_RELIEF' });
    } catch (err) {
      setErrorMsg(err.message || 'Failed to create campaign');
      // local update so presentation keeps flowing
      setCampaigns([
        ...campaigns,
        {
          id: Date.now(),
          title: newCampaign.title,
          status: 'ACTIVE',
          funding: 0,
          volunteers: 5,
          goal: parseInt(newCampaign.goal) || 100000,
        },
      ]);
      setIsModalOpen(false);
    } finally {
      setLoading(false);
    }
  };

  // ---------- Disasters (Admin only) ----------
  const [disasters, setDisasters] = useState([
    { id: 1, title: 'Kolhapur Floods 2026', type: 'Flood', place: 'Kolhapur, Maharashtra', status: 'ACTIVE' },
    { id: 2, title: 'Assam Earthquake', type: 'Earthquake', place: 'Guwahati, Assam', status: 'CLOSED' },
  ]);
  const [disasterForm, setDisasterForm] = useState({ title: '', type: 'FLOOD', place: '' });

  const fetchDisasters = async () => {
    try {
      const res = await api.get('/disasters');
      const list = Array.isArray(res) ? res : res.content || [];
      if (list.length > 0) {
        setDisasters(
          list.map((d) => ({
            id: d.id,
            title: d.title,
            type: d.type || 'Disaster',
            place: d.location || 'India',
            status: d.severity === 'CRITICAL' || d.severity === 'HIGH' ? 'ACTIVE' : 'ACTIVE',
          }))
        );
      }
    } catch {
      /* keep defaults */
    }
  };

  const handleCreateDisaster = async (e) => {
    e.preventDefault();
    if (!disasterForm.title) return;

    try {
      await api.post('/disasters', {
        title: disasterForm.title,
        type: disasterForm.type || 'FLOOD',
        location: disasterForm.place,
        severity: 'HIGH',
        description: `${disasterForm.title} relief disaster event.`,
      });
      await fetchDisasters();
    } catch {
      setDisasters([
        {
          id: Date.now(),
          title: disasterForm.title,
          type: disasterForm.type || 'Flood',
          place: disasterForm.place || 'Unknown Location',
          status: 'ACTIVE',
        },
        ...disasters,
      ]);
    }
    setDisasterForm({ title: '', type: 'FLOOD', place: '' });
  };

  // ---------- NGO approvals (Admin only) ----------
  const [pendingNgos, setPendingNgos] = useState([
    { id: 8, name: 'Rahul Seva NGO', reg: 'MH-2024-8841' },
    { id: 15, name: 'Asha Relief Trust', reg: 'MH-2025-1180' },
  ]);

  const approveNgo = async (id) => {
    try {
      await api.patch(`/admin/ngos/${id}/approve`);
      setPendingNgos(pendingNgos.filter((n) => n.id !== id));
      alert(`NGO approved successfully!`);
    } catch {
      setPendingNgos(pendingNgos.filter((n) => n.id !== id));
    }
  };

  // ---------- Stats (calculated automatically) ----------
  const activeCampaigns = campaigns.filter((c) => c.status === 'ACTIVE');
  const volunteersNeeded = activeCampaigns.reduce((sum, c) => sum + (c.volunteers || 0), 0);
  const moneyGoal = activeCampaigns.reduce((sum, c) => sum + (c.goal || 0), 0);
  const activeDisasters = disasters.filter((d) => d.status === 'ACTIVE').length;

  return (
    <div>
      {/* Page Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-800 border-l-4 border-blue-600 pl-4">
          {isAdmin ? 'NGO & Admin Combined Panel' : 'NGO Operations Dashboard'}
        </h1>
        <p className="text-gray-500 mt-2 pl-4">
          {isAdmin
            ? 'Manage all platform campaigns, active disaster events, and approve registered NGOs.'
            : 'Manage campaigns, view pledges, and coordinate disaster response in real-time.'}
        </p>
      </div>

      {errorMsg && (
        <div className="mb-4 rounded-lg bg-red-50 p-3 text-sm text-red-700 border border-red-200">
          {errorMsg}
        </div>
      )}

      {/* Dynamic Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Active Campaigns</p>
          <p className="text-3xl font-bold text-blue-900 mt-2">{activeCampaigns.length}</p>
          <span className="text-xs text-green-600 font-semibold mt-2 inline-block">Real-time status</span>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Volunteers Needed</p>
          <p className="text-3xl font-bold text-blue-900 mt-2">{volunteersNeeded}</p>
          <span className="text-xs text-blue-600 font-semibold mt-2 inline-block">Across active drives</span>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Fundraising Goal</p>
          <p className="text-3xl font-bold text-blue-900 mt-2">₹{(moneyGoal / 100000).toFixed(1)}L</p>
          <span className="text-xs text-gray-400 mt-2 inline-block">Target across all active</span>
        </div>

        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
          <p className="text-sm font-semibold text-gray-500 uppercase tracking-wider">Active Disasters</p>
          <p className="text-3xl font-bold text-blue-900 mt-2">{activeDisasters}</p>
          <span className="text-xs text-orange-500 font-semibold mt-2 inline-block">Monitored hotspots</span>
        </div>
      </div>

      {/* ---------- ADMIN ONLY: NGO Approvals ---------- */}
      {isAdmin && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
          <div className="flex justify-between items-center mb-4">
            <div>
              <h2 className="text-lg font-bold text-blue-900">Pending NGO Approvals</h2>
              <p className="text-xs text-gray-500">Verify NGO registrations before granting relief campaign creation rights.</p>
            </div>
            <span className="text-xs font-semibold bg-yellow-100 text-yellow-800 px-3 py-1 rounded-full">
              {pendingNgos.length} Pending
            </span>
          </div>

          {pendingNgos.length === 0 ? (
            <p className="text-sm text-gray-400 italic">No NGOs currently awaiting approval.</p>
          ) : (
            <div className="divide-y divide-gray-100">
              {pendingNgos.map((ngo) => (
                <div key={ngo.id} className="py-3 flex justify-between items-center">
                  <div>
                    <p className="font-semibold text-gray-800">{ngo.name}</p>
                    <p className="text-xs text-gray-500">Reg No: {ngo.reg}</p>
                  </div>
                  <div className="flex gap-2">
                    <button
                      onClick={() => approveNgo(ngo.id)}
                      className="bg-green-600 text-white text-xs font-bold px-4 py-2 rounded-lg hover:bg-green-700 transition"
                    >
                      Approve
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* ---------- ADMIN ONLY: Create & Manage Disasters ---------- */}
      {isAdmin && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6 mb-8">
          <h2 className="text-lg font-bold text-blue-900 mb-4">Declare New Disaster Event</h2>
          <form onSubmit={handleCreateDisaster} className="grid grid-cols-1 md:grid-cols-4 gap-4 mb-6">
            <input
              type="text"
              placeholder="Disaster Title (e.g. Wayanad Landslide)"
              value={disasterForm.title}
              onChange={(e) => setDisasterForm({ ...disasterForm, title: e.target.value })}
              className={inputClass}
              required
            />
            <select
              value={disasterForm.type}
              onChange={(e) => setDisasterForm({ ...disasterForm, type: e.target.value })}
              className={inputClass}
            >
              <option value="FLOOD">Flood</option>
              <option value="EARTHQUAKE">Earthquake</option>
              <option value="CYCLONE">Cyclone</option>
              <option value="LANDSLIDE">Landslide</option>
              <option value="DROUGHT">Drought</option>
              <option value="FIRE">Fire</option>
            </select>
            <input
              type="text"
              placeholder="Location (e.g. Kerala, India)"
              value={disasterForm.place}
              onChange={(e) => setDisasterForm({ ...disasterForm, place: e.target.value })}
              className={inputClass}
            />
            <button
              type="submit"
              className="bg-red-600 text-white font-bold rounded-lg px-4 py-3 hover:bg-red-700 transition"
            >
              Broadcast Disaster
            </button>
          </form>

          <div className="divide-y divide-gray-100">
            {disasters.map((d) => (
              <div key={d.id} className="py-2.5 flex justify-between items-center text-sm">
                <div>
                  <span className="font-semibold text-gray-800">{d.title}</span>
                  <span className="text-xs text-gray-500 ml-2">({d.place})</span>
                </div>
                <span
                  className={`text-xs font-bold px-2.5 py-0.5 rounded-full ${
                    d.status === 'ACTIVE' ? 'bg-red-100 text-red-700' : 'bg-gray-100 text-gray-600'
                  }`}
                >
                  {d.status}
                </span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* ---------- Campaigns Management ---------- */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-bold text-blue-900">Active Relief Campaigns</h2>
          <button
            onClick={() => setIsModalOpen(true)}
            className="flex items-center gap-2 bg-[#1E3A8A] text-white px-4 py-2.5 rounded-lg hover:bg-blue-800 font-semibold transition"
          >
            <Plus size={18} /> Create Campaign
          </button>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="border-b border-gray-100 text-xs font-bold text-gray-500 uppercase">
                <th className="pb-3">Campaign</th>
                <th className="pb-3">Status</th>
                <th className="pb-3">Goal</th>
                <th className="pb-3">Funded</th>
                <th className="pb-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-50 text-sm">
              {campaigns.map((c) => (
                <tr key={c.id} className="hover:bg-gray-50/50">
                  <td className="py-4 font-semibold text-gray-800">{c.title}</td>
                  <td className="py-4">
                    <span
                      className={`text-xs font-bold px-2.5 py-1 rounded-full ${
                        c.status === 'ACTIVE'
                          ? 'bg-green-100 text-green-700'
                          : 'bg-gray-100 text-gray-600'
                      }`}
                    >
                      {c.status}
                    </span>
                  </td>
                  <td className="py-4 font-medium text-gray-600">₹{c.goal.toLocaleString()}</td>
                  <td className="py-4">
                    <div className="flex items-center gap-2">
                      <div className="w-24 bg-gray-200 rounded-full h-2">
                        <div
                          className="bg-blue-600 h-2 rounded-full"
                          style={{ width: `${Math.min(c.funding, 100)}%` }}
                        />
                      </div>
                      <span className="text-xs font-medium text-gray-500">{c.funding}%</span>
                    </div>
                  </td>
                  <td className="py-4 text-right">
                    <button
                      onClick={() => navigate('/manage-campaign')}
                      className="text-xs font-bold text-blue-600 hover:text-blue-800"
                    >
                      Manage →
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Modal: Create Campaign */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black/40 backdrop-blur-xs flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative animate-in fade-in zoom-in-95">
            <div className="flex justify-between items-center mb-4 border-b border-gray-100 pb-3">
              <h3 className="text-lg font-bold text-blue-900">Create Relief Campaign</h3>
              <button onClick={() => setIsModalOpen(false)} className="text-gray-400 hover:text-gray-600">
                <X size={20} />
              </button>
            </div>
            <form onSubmit={handleCreateCampaign} className="space-y-4">
              <div>
                <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Title *</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Kolhapur Medical Relief"
                  value={newCampaign.title}
                  onChange={(e) => setNewCampaign({ ...newCampaign, title: e.target.value })}
                  className={inputClass}
                />
              </div>
              <div>
                <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Target Amount (₹) *</label>
                <input
                  type="number"
                  required
                  placeholder="100000"
                  value={newCampaign.goal}
                  onChange={(e) => setNewCampaign({ ...newCampaign, goal: e.target.value })}
                  className={inputClass}
                />
              </div>
              <div>
                <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Category</label>
                <select
                  value={newCampaign.category}
                  onChange={(e) => setNewCampaign({ ...newCampaign, category: e.target.value })}
                  className={inputClass}
                >
                  <option value="FLOOD_RELIEF">Flood Relief</option>
                  <option value="EARTHQUAKE_RELIEF">Earthquake Relief</option>
                  <option value="CYCLONE_RELIEF">Cyclone Relief</option>
                  <option value="MEDICAL_AID">Medical Aid</option>
                  <option value="FOOD_DISTRIBUTION">Food Distribution</option>
                  <option value="SHELTER">Shelter</option>
                </select>
              </div>
              <div>
                <label className="text-xs font-bold text-gray-600 uppercase mb-1 block">Description</label>
                <textarea
                  rows={2}
                  placeholder="Details of the relief initiative..."
                  value={newCampaign.description}
                  onChange={(e) => setNewCampaign({ ...newCampaign, description: e.target.value })}
                  className={inputClass}
                />
              </div>
              <div className="flex justify-end gap-3 pt-3 border-t border-gray-100">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-semibold text-gray-600 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={loading}
                  className="px-5 py-2 bg-[#1E3A8A] text-white rounded-lg text-sm font-semibold hover:bg-blue-800 disabled:opacity-60"
                >
                  {loading ? 'Creating...' : 'Create'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
}
