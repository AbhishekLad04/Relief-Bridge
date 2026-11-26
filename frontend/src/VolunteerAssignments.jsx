import { useEffect, useState } from 'react';
import api from './api';
import { CheckCircle, XCircle } from 'lucide-react';

export default function VolunteerAssignments() {
  const [assignments, setAssignments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/volunteer/assignments/my')
      .then(res => setAssignments(res))
      .catch(e => setError(e.message || 'Failed to load assignments'))
      .finally(() => setLoading(false));
  }, []);

  const updateStatus = (id, status) => {
    api.patch(`/volunteer/assignments/${id}/status`, { status })
      .then(() => setAssignments(assignments.map(a => a.id===id?{...a,status}:a)))
      .catch(e => setError(e.message || 'Failed to update status'));
  };

  if (loading) return <div className="p-8">Loading assignments...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">My Volunteer Assignments</h1>
      {assignments.length===0 ? (
        <p>No assignments.</p>
      ) : (
        <table className="w-full table-auto border-collapse">
          <thead className="bg-gray-100">
            <tr>
              <th className="p-2 text-left">Task</th>
              <th className="p-2 text-left">Status</th>
              <th className="p-2 text-left">Actions</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {assignments.map(a => (
              <tr key={a.id} className="hover:bg-gray-50">
                <td className="p-2">{a.task}</td>
                <td className="p-2">{a.status}</td>
                <td className="p-2 space-x-2">
                  {a.status !== 'COMPLETED' && (
                    <button onClick={() => updateStatus(a.id,'COMPLETED')} className="bg-green-600 text-white px-3 py-1 rounded">Complete</button>
                  )}
                  {a.status !== 'REJECTED' && (
                    <button onClick={() => updateStatus(a.id,'REJECTED')} className="bg-red-600 text-white px-3 py-1 rounded">Reject</button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
