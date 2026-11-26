import { useEffect, useState } from 'react';
import { useAuth } from './context/AuthContext';
import api from './api';
import { CheckCircle, XCircle, AlertTriangle } from 'lucide-react';

// Simple Fund Utilization component handling NGO submission and ADMIN verification with optional file uploads
export default function FundUtilization() {
  const { user } = useAuth();
  const role = user?.role;
  const isAdmin = role === 'ADMIN';
  const isNgo = role === 'NGO';

  const [form, setForm] = useState({
    campaignId: '',
    amount: '',
    expenseDate: '',
    category: '',
    description: '',
    invoiceFile: null,
    photoFile: null,
  });
  const [utilizations, setUtilizations] = useState([]);
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  // Fetch data based on role
  const fetchData = async () => {
    try {
      if (isAdmin) {
        const res = await api.get('/fund-utilizations/pending');
        setPending(Array.isArray(res) ? res : res.content || []);
      }
      if (isNgo) {
        const res = await api.get('/fund-utilizations/my');
        setUtilizations(Array.isArray(res) ? res : res.content || []);
      }
    } catch (e) {
      setError(e.message || 'Failed to load fund data');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  // Helper to convert file to Base64 (or you can upload to storage service later)
  const toBase64 = file =>
    new Promise((resolve, reject) => {
      const reader = new FileReader();
      reader.readAsDataURL(file);
      reader.onload = () => resolve(reader.result);
      reader.onerror = error => reject(error);
    });

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      const payload = {
        ...form,
        amount: Number(form.amount),
        invoiceUrl: form.invoiceFile ? await toBase64(form.invoiceFile) : undefined,
        photoUrl: form.photoFile ? await toBase64(form.photoFile) : undefined,
      };
      await api.post('/fund-utilizations', payload);
      setForm({ campaignId: '', amount: '', expenseDate: '', category: '', description: '', invoiceFile: null, photoFile: null });
      await fetchData();
    } catch (e) {
      setError(e.message || 'Submit failed');
    }
  };

  const handleVerify = async (id, approve) => {
    try {
      await api.patch(`/fund-utilizations/${id}/verify`, { approved: approve });
      await fetchData();
    } catch (e) {
      setError(e.message);
    }
  };

  if (loading) return <div className="p-8">Loading...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">Fund Utilization</h1>

      {isNgo && (
        <section className="mb-8">
          <h2 className="text-xl font-semibold mb-2">Submit Expense</h2>
          <form onSubmit={handleSubmit} className="grid gap-3">
            <input className="border p-2" placeholder="Campaign ID" value={form.campaignId} onChange={e => setForm({ ...form, campaignId: e.target.value })} required />
            <input className="border p-2" placeholder="Amount" type="number" value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} required />
            <input className="border p-2" placeholder="Expense Date (YYYY-MM-DD)" type="date" value={form.expenseDate} onChange={e => setForm({ ...form, expenseDate: e.target.value })} required />
            <input className="border p-2" placeholder="Category" value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} required />
            <textarea className="border p-2" placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
            <label className="block">
              Invoice File (optional)
              <input type="file" accept="application/pdf,image/*" onChange={e => setForm({ ...form, invoiceFile: e.target.files[0] })} className="mt-1" />
            </label>
            <label className="block">
              Photo File (optional)
              <input type="file" accept="image/*" onChange={e => setForm({ ...form, photoFile: e.target.files[0] })} className="mt-1" />
            </label>
            <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded">Submit</button>
          </form>
        </section>
      )}

      {/* Existing tables unchanged */}
      {isNgo && (
        <section>
          <h2 className="text-xl font-semibold mb-2">My Submissions</h2>
          {utilizations.length === 0 ? (
            <p>No expenses submitted.</p>
          ) : (
            <table className="w-full table-auto border-collapse">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">Campaign</th>
                  <th className="p-2 text-left">Amount</th>
                  <th className="p-2 text-left">Date</th>
                  <th className="p-2 text-left">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {utilizations.map(u => (
                  <tr key={u.id} className="hover bg-gray-50">
                    <td className="p-2">{u.campaignId}</td>
                    <td className="p-2">{u.amount}</td>
                    <td className="p-2">{u.expenseDate?.split('T')[0]}</td>
                    <td className="p-2">
                      {u.approved ? (
                        <span className="flex items-center text-green-600"><CheckCircle size={14} className="mr-1"/>Approved</span>
                      ) : (
                        <span className="flex items-center text-gray-600"><AlertTriangle size={14} className="mr-1"/>Pending</span>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      )}

      {isAdmin && (
        <section>
          <h2 className="text-xl font-semibold mb-2">Pending Verifications</h2>
          {pending.length === 0 ? (
            <p>No pending submissions.</p>
          ) : (
            <table className="w-full table-auto border-collapse">
              <thead className="bg-gray-100">
                <tr>
                  <th className="p-2 text-left">NGO</th>
                  <th className="p-2 text-left">Campaign</th>
                  <th className="p-2 text-left">Amount</th>
                  <th className="p-2 text-left">Date</th>
                  <th className="p-2 text-left">Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {pending.map(p => (
                  <tr key={p.id} className="hover bg-gray-50">
                    <td className="p-2">{p.submittedByName}</td>
                    <td className="p-2">{p.campaignId}</td>
                    <td className="p-2">{p.amount}</td>
                    <td className="p-2">{p.expenseDate?.split('T')[0]}</td>
                    <td className="p-2 space-x-2">
                      <button onClick={() => handleVerify(p.id, true)} className="text-sm text-green-600">Approve</button>
                      <button onClick={() => handleVerify(p.id, false)} className="text-sm text-red-600">Reject</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </section>
      )}
    </div>
  );
}
