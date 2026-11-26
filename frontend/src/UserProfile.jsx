import { useEffect, useState } from 'react';
import api from './api';

export default function UserProfile() {
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    api
      .get('/users/me')
      .then((data) => {
        setProfile(data);
        setLoading(false);
      })
      .catch((err) => {
        setError(err.message || 'Failed to load profile');
        setLoading(false);
      });
  }, []);

  if (loading) return <div className="p-8">Loading profile...</div>;
  if (error) return <div className="p-8 text-red-600">{error}</div>;

  return (
    <div className="max-w-3xl mx-auto p-8">
      <h1 className="text-2xl font-bold mb-4">My Profile</h1>
      <div className="bg-white rounded-xl shadow p-6">
        <p className="mb-2"><strong>Name:</strong> {profile.name || profile.fullName || 'N/A'}</p>
        <p className="mb-2"><strong>Email:</strong> {profile.email}</p>
        <p className="mb-2"><strong>Role:</strong> {profile.role}</p>
        <p className="mb-2"><strong>Donor:</strong> {profile.isDonor ? 'Yes' : 'No'}</p>
        <p className="mb-2"><strong>Volunteer:</strong> {profile.isVolunteer ? 'Yes' : 'No'}</p>
        <p className="mb-2"><strong>Approved:</strong> {profile.approved ? 'Yes' : 'No'}</p>
      </div>
    </div>
  );
}
