// src/Notifications.jsx - List user notifications and allow marking as read
import { useEffect, useState } from 'react';
import api from './api';
import { BellOff, CheckCircle, XCircle } from 'lucide-react';

export default function Notifications() {
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchNotifications = async () => {
    try {
      const data = await api.get('/notifications'); // expects paginated response
      // If response is a Page object (Spring Data), data.content holds array
      const list = data.content ? data.content : data;
      setNotifications(list);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchNotifications();
  }, []);

  const markAsRead = async (id) => {
    try {
      await api.patch(`/notifications/${id}/read`);
      // Update local state
      setNotifications((prev) =>
        prev.map((n) => (n.id === id ? { ...n, read: true } : n))
      );
    } catch (e) {
      console.error('Failed to mark as read', e);
    }
  };

  const markAllAsRead = async () => {
    try {
      await api.patch('/notifications/read-all');
      setNotifications((prev) => prev.map((n) => ({ ...n, read: true })));
    } catch (e) {
      console.error('Failed to mark all as read', e);
    }
  };

  if (loading) return <div className="p-8 text-gray-600">Loading notifications…</div>;
  if (error) return <div className="p-8 text-red-600">Error: {error}</div>;

  return (
    <div className="p-8">
      <div className="flex items-center justify-between mb-4">
        <h2 className="text-2xl font-semibold">Notifications</h2>
        <button
          onClick={markAllAsRead}
          className="flex items-center gap-1 text-sm text-blue-600 hover:underline"
        >
          <CheckCircle size={16} /> Mark all as read
        </button>
      </div>
      {notifications.length === 0 ? (
        <div className="flex items-center text-gray-500">
          <BellOff size={24} className="mr-2" /> No notifications.
        </div>
      ) : (
        <ul className="space-y-3">
          {notifications.map((n) => (
            <li
              key={n.id}
              className={`p-4 rounded border ${n.read ? 'bg-white' : 'bg-blue-50'} `}
            >
              <div className="flex justify-between items-start">
                <div>
                  <p className="font-medium text-gray-800">{n.title || 'Notification'}</p>
                  <p className="text-sm text-gray-600">{n.message || ''}</p>
                </div>
                {!n.read && (
                  <button
                    onClick={() => markAsRead(n.id)}
                    className="text-sm text-green-600 hover:underline"
                  >
                    Mark as read
                  </button>
                )}
                {n.read && <XCircle size={16} className="text-gray-400" />}
              </div>
              <div className="text-xs text-gray-400 mt-1">
                {new Date(n.createdAt || n.timestamp).toLocaleString()}
              </div>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
