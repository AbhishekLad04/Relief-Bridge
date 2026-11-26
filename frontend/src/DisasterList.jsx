import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import api from './api';
import { AlertTriangle, CheckCircle, XCircle, Plus, X } from 'lucide-react';
import { useAuth } from './context/AuthContext';

export default function DisasterList() {
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';
  const [disasters, setDisasters] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [form, setForm] = useState({ title: '', type: 'FLOOD', location: '' });

  const fetchDisasters = async () => {
    try {
      const res = await api.get('/disasters');
      const list = Array.isArray(res) ? res : res.content || [];
      setDisasters(list);
    } catch (e) {
      setError(e.message || 'Failed to load disasters');
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchDisasters(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    if (!form.title) return;
    try {
      await api.post('/disasters', { ...form, severity: 'HIGH', description: `${form.title} disaster`});
      await fetchDisasters();
      setShowCreate(false);
      setForm({ title: '', type: 'FLOOD', location: '' });
    } catch (e) {
      setError(e.message || 'Create failed');
    }
  };

  const handleDelete = async (id) => {
    if (!window.confirm('Delete disaster?')) return;
    try { await api.delete(`/disasters/${id}`); await fetchDisasters(); } catch (e) { setError(e.message); }
  };

  const handleStatus = async (id, action) => {
    try { await api.patch(`/disasters/${id}/${action}`); await fetchDisasters(); } catch (e) { setError(e.message); }
  };

  if (loading) return <div className="p-8">Loading disasters...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-5xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">Disaster Events</h1>
      {isAdmin && (
        <button onClick={()=>setShowCreate(true)} className="mb-4 px-4 py-2 bg-blue-600 text-white rounded">
          <Plus size={16} className="inline mr-1"/> Create Disaster
        </button>
      )}
      {showCreate && (
        <form onSubmit={handleCreate} className="mb-6 p-4 border rounded">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <input className="border p-2" placeholder="Title" value={form.title} onChange={e=>setForm({...form, title:e.target.value})} required />
            <input className="border p-2" placeholder="Type" value={form.type} onChange={e=>setForm({...form, type:e.target.value})} />
            <input className="border p-2" placeholder="Location" value={form.location} onChange={e=>setForm({...form, location:e.target.value})} />
          </div>
          <div className="mt-3 flex space-x-2">
            <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded">Save</button>
            <button type="button" onClick={()=>setShowCreate(false)} className="px-4 py-2 bg-gray-300 rounded">Cancel</button>
          </div>
        </form>
      )}
      {disasters.length===0 ? <p>No disasters.</p> : (
        <table className="w-full table-auto border-collapse">
          <thead className="bg-gray-100 text-gray-600">
            <tr>
              <th className="p-3 text-left">Title</th>
              <th className="p-3 text-left">Type</th>
              <th className="p-3 text-left">Location</th>
              <th className="p-3 text-left">Status</th>
              {isAdmin && <th className="p-3">Actions</th>}
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-200">
            {disasters.map(d=> (
              <tr key={d.id} className="hover:bg-gray-50">
                <td className="p-3">
                  <Link to={`/disaster/${d.id}`} className="text-blue-600 hover:underline">{d.title}</Link>
                </td>
                <td className="p-3">{d.type}</td>
                <td className="p-3">{d.location}</td>
                <td className="p-3">
                  {d.severity==='CRITICAL'||d.severity==='HIGH' ? (
                    <span className="flex items-center text-green-600"><CheckCircle size={14} className="mr-1"/>Active</span>
                  ) : (
                    <span className="flex items-center text-gray-600"><XCircle size={14} className="mr-1"/>Inactive</span>
                  )}
                </td>
                {isAdmin && (
                  <td className="p-3 space-x-1">
                    <button onClick={()=>handleStatus(d.id,'verify')} className="text-sm text-blue-600">Verify</button>
                    <button onClick={()=>handleStatus(d.id,'reject')} className="text-sm text-red-600">Reject</button>
                    <button onClick={()=>handleDelete(d.id)} className="text-sm text-red-600"><X size={14}/></button>
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
