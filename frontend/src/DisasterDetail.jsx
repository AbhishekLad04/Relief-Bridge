import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from './api';
import { useAuth } from './context/AuthContext';
import { CheckCircle, XCircle, AlertTriangle, Edit, X } from 'lucide-react';

export default function DisasterDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const isAdmin = user?.role === 'ADMIN';

  const [disaster, setDisaster] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [editing, setEditing] = useState(false);
  const [form, setForm] = useState({ title: '', type: '', location: '', description: '' });

  const fetchDisaster = async () => {
    try {
      const res = await api.get(`/disasters/${id}`);
      setDisaster(res);
      setForm({
        title: res.title || '',
        type: res.type || '',
        location: res.location || '',
        description: res.description || '',
      });
    } catch (e) {
      setError(e.message || 'Failed to load disaster');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchDisaster();
  }, [id]);

  const handleUpdate = async (e) => {
    e.preventDefault();
    try {
      await api.put(`/disasters/${id}`, { ...form, severity: disaster.severity });
      setEditing(false);
      await fetchDisaster();
    } catch (e) {
      setError(e.message || 'Update failed');
    }
  };

  const handleAction = async (action) => {
    try {
      await api.patch(`/disasters/${id}/${action}`);
      await fetchDisaster();
    } catch (e) {
      setError(e.message);
    }
  };

  const handleDelete = async () => {
    if (!window.confirm('Delete this disaster?')) return;
    try {
      await api.delete(`/disasters/${id}`);
      navigate('/disasters');
    } catch (e) {
      setError(e.message);
    }
  };

  if (loading) return <div className="p-8">Loading...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;
  if (!disaster) return <div className="p-8">No data.</div>;

  return (
    <div className="max-w-4xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">Disaster Detail</h1>

      {editing ? (
        <form onSubmit={handleUpdate} className="grid gap-3 mb-4">
          <input className="border p-2" placeholder="Title" value={form.title} onChange={e => setForm({ ...form, title: e.target.value })} required />
          <input className="border p-2" placeholder="Type" value={form.type} onChange={e => setForm({ ...form, type: e.target.value })} />
          <input className="border p-2" placeholder="Location" value={form.location} onChange={e => setForm({ ...form, location: e.target.value })} />
          <textarea className="border p-2" placeholder="Description" value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} />
          <div className="flex space-x-2">
            <button type="submit" className="px-4 py-2 bg-green-600 text-white rounded">Save</button>
            <button type="button" onClick={() => setEditing(false)} className="px-4 py-2 bg-gray-300 rounded">Cancel</button>
          </div>
        </form>
      ) : (
        <div className="mb-4">
          <p><strong>Title:</strong> {disaster.title}</p>
          <p><strong>Type:</strong> {disaster.type}</p>
          <p><strong>Location:</strong> {disaster.location}</p>
          <p><strong>Description:</strong> {disaster.description}</p>
          <p><strong>Status:</strong> {disaster.severity === 'HIGH' || disaster.severity === 'CRITICAL' ? <span className="flex items-center text-green-600"><CheckCircle size={14} className="mr-1"/>Active</span> : <span className="flex items-center text-gray-600"><XCircle size={14} className="mr-1"/>Inactive</span>}</p>
        </div>
      )}

      {isAdmin && (
        <div className="flex space-x-3 mb-4">
          <button onClick={() => setEditing(true)} className="text-sm text-blue-600">Edit</button>
          <button onClick={() => handleAction('verify')} className="text-sm text-green-600">Verify</button>
          <button onClick={() => handleAction('reject')} className="text-sm text-red-600">Reject</button>
          <button onClick={handleDelete} className="text-sm text-red-600"><X size={14} /></button>
        </div>
      )}
    </div>
  );
}
