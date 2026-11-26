import { useEffect, useState } from 'react';
import api from './api';
import { CheckCircle, XCircle } from 'lucide-react';

export default function VolunteerProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/volunteer/profile')
      .then(res => setProfile(res))
      .catch(e => setError(e.message || 'Failed to load profile'))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setProfile({ ...profile, [name]: value });
  };

  const handleSave = (e) => {
    e.preventDefault();
    setSaving(true);
    api.put('/volunteer/profile', profile)
      .then(res => setProfile(res))
      .catch(e => setError(e.message || 'Failed to save profile'))
      .finally(() => setSaving(false));
  };

  if (loading) return <div className="p-8">Loading profile...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-3xl font-bold mb-6">Volunteer Profile</h1>
      <form onSubmit={handleSave} className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Phone</label>
          <input name="phone" value={profile.phone || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Address</label>
          <input name="address" value={profile.address || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" />
        </div>
        <div className="grid grid-cols-3 gap-4">
          <div>
            <label className="block text-sm font-medium mb-1">City</label>
            <input name="city" value={profile.city || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">State</label>
            <input name="state" value={profile.state || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Experience Years</label>
            <input type="number" name="experienceYears" value={profile.experienceYears || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" />
          </div>
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Experience Description</label>
          <textarea name="experienceDescription" value={profile.experienceDescription || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" rows={3} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Bio</label>
          <textarea name="bio" value={profile.bio || ''} onChange={handleChange} className="w-full border rounded px-3 py-2" rows={3} />
        </div>
        <div>
          <label className="block text-sm font-medium mb-1">Availability Status</label>
          <select name="availabilityStatus" value={profile.availabilityStatus || ''} onChange={handleChange} className="w-full border rounded px-3 py-2">
            <option value="AVAILABLE">AVAILABLE</option>
            <option value="UNAVAILABLE">UNAVAILABLE</option>
          </select>
        </div>
        {/* Skills input as comma‑separated IDs for simplicity */}
        <div>
          <label className="block text-sm font-medium mb-1">Skill IDs (comma separated)</label>
          <input name="skillIds" value={(profile.skillIds || []).join(',')} onChange={(e) => {
            const ids = e.target.value.split(',').map(v => v.trim()).filter(v => v).map(Number);
            setProfile({ ...profile, skillIds: ids });
          }} className="w-full border rounded px-3 py-2" />
        </div>
        <button type="submit" disabled={saving} className="bg-indigo-600 hover:bg-indigo-700 text-white font-bold px-5 py-2.5 rounded">
          {saving ? 'Saving...' : 'Save Profile'}
        </button>
      </form>
    </div>
  );
}
