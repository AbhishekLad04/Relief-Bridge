import { useEffect, useState } from 'react';
import api from './api';
import { CheckCircle, XCircle } from 'lucide-react';

export default function VolunteerApplications() {
  const [requirements, setRequirements] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/volunteer/requirements/my')
      .then(res => setRequirements(res))
      .catch(e => setError(e.message || 'Failed to load requirements'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="p-8">Loading requirements...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">My Volunteer Requirements</h1>
      {requirements.length === 0 ? (
        <p>No requirements found.</p>
      ) : (
        <table className="w-full table-auto border-collapse">
          <thead className="bg-gray-100">
            <tr>
              <th className="p-2 text-left">Title</th>
              <th className="p-2 text-left">Description</th>
              <th className="p-2 text-left">Status</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {requirements.map(r => (
              <tr key={r.id} className="hover:bg-gray-50">
                <td className="p-2">{r.title}</td>
                <td className="p-2">{r.description}</td>
                <td className="p-2">{r.status}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
