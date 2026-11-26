import { useEffect, useState } from 'react';
import api from './api';
import { CheckCircle, XCircle } from 'lucide-react';

export default function AdminDonations() {
  const [donations, setDonations] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/donations/admin/all?page=0&size=20')
      .then(res => {
        const list = Array.isArray(res) ? res : (res.content || []);
        setDonations(list);
      })
      .catch(e => setError(e.message || 'Failed to load admin donations'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="p-8">Loading admin donations...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">All Donations (Admin)</h1>
      {donations.length === 0 ? (
        <p>No donations recorded.</p>
      ) : (
        <table className="w-full table-auto border-collapse">
          <thead className="bg-gray-100">
            <tr>
              <th className="p-2 text-left">Donor</th>
              <th className="p-2 text-left">Campaign</th>
              <th className="p-2 text-left">Amount</th>
              <th className="p-2 text-left">Date</th>
              <th className="p-2 text-left">Status</th>
              <th className="p-2 text-left">Receipt</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {donations.map(d => (
              <tr key={d.id} className="hover:bg-gray-50">
                <td className="p-2">{d.donorName || 'Anonymous'}</td>
                <td className="p-2">{d.campaignTitle || '—'}</td>
                <td className="p-2">₹{Number(d.amount).toLocaleString()}</td>
                <td className="p-2">{new Date(d.createdAt).toLocaleDateString()}</td>
                <td className="p-2">
                  {d.status === 'SUCCESS' ? (
                    <span className="flex items-center text-green-600"><CheckCircle size={14} className="mr-1"/>Success</span>
                  ) : (
                    <span className="flex items-center text-red-600"><XCircle size={14} className="mr-1"/>Failed</span>
                  )}
                </td>
                <td className="p-2">
                  {d.receiptNumber ? (
                    <a href={`/api/donations/receipt/${d.receiptNumber}`} target="_blank" rel="noopener" className="text-blue-600 hover:underline">PDF</a>
                  ) : '—'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
