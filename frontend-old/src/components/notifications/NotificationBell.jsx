// src/components/notifications/NotificationBell.jsx
import { Bell, CheckCheck } from 'lucide-react'
import { useEffect, useRef, useState } from 'react'
import { Link } from 'react-router-dom'
import * as notifService from '../../services/notificationService'

export default function NotificationBell() {
  const [unread, setUnread] = useState(0)
  const [notifications, setNotifications] = useState([])
  const [open, setOpen] = useState(false)
  const [loading, setLoading] = useState(false)
  const ref = useRef(null)

  // Fetch unread count on mount
  useEffect(() => {
    notifService.getUnreadCount()
      .then(d => setUnread(typeof d === 'number' ? d : d?.count ?? 0))
      .catch(() => {})
  }, [])

  // Close on outside click
  useEffect(() => {
    const handler = (e) => { if (ref.current && !ref.current.contains(e.target)) setOpen(false) }
    document.addEventListener('mousedown', handler)
    return () => document.removeEventListener('mousedown', handler)
  }, [])

  const handleOpen = async () => {
    setOpen(v => !v)
    if (!open && notifications.length === 0) {
      setLoading(true)
      try {
        const data = await notifService.getMyNotifications(0, 5)
        setNotifications(Array.isArray(data) ? data : data?.content ?? [])
      } catch { /* ignore */ } finally {
        setLoading(false)
      }
    }
  }

  const handleMarkAll = async () => {
    await notifService.markAllAsRead().catch(() => {})
    setUnread(0)
    setNotifications(prev => prev.map(n => ({ ...n, read: true })))
  }

  return (
    <div ref={ref} className="relative">
      <button
        onClick={handleOpen}
        className="relative rounded-full p-2 text-slate-300 hover:bg-slate-700 hover:text-white transition"
        aria-label="Notifications"
      >
        <Bell className="h-5 w-5" />
        {unread > 0 && (
          <span className="absolute -right-0.5 -top-0.5 flex h-4 w-4 items-center justify-center rounded-full bg-orange-500 text-[10px] font-bold text-white">
            {unread > 9 ? '9+' : unread}
          </span>
        )}
      </button>

      {open && (
        <div className="absolute right-0 top-full z-50 mt-2 w-80 rounded-xl bg-white shadow-2xl border border-slate-200 overflow-hidden">
          <div className="flex items-center justify-between px-4 py-3 border-b border-slate-100 bg-slate-50">
            <span className="text-sm font-semibold text-slate-900">Notifications</span>
            {unread > 0 && (
              <button
                onClick={handleMarkAll}
                className="flex items-center gap-1 text-xs text-cyan-700 hover:underline"
              >
                <CheckCheck className="h-3.5 w-3.5" />
                Mark all read
              </button>
            )}
          </div>

          <div className="max-h-72 overflow-y-auto divide-y divide-slate-100">
            {loading && (
              <div className="py-6 text-center text-sm text-slate-400">Loading…</div>
            )}
            {!loading && notifications.length === 0 && (
              <div className="py-6 text-center text-sm text-slate-400">No notifications</div>
            )}
            {notifications.map(n => (
              <div
                key={n.id}
                className={`px-4 py-3 text-sm ${n.read ? 'text-slate-500' : 'text-slate-900 font-medium bg-cyan-50'}`}
              >
                <p className="line-clamp-2">{n.message}</p>
                <p className="mt-0.5 text-xs text-slate-400">
                  {n.createdAt ? new Date(n.createdAt).toLocaleString() : ''}
                </p>
              </div>
            ))}
          </div>

          <div className="border-t border-slate-100 px-4 py-2 text-center">
            <Link
              to="/citizen"
              onClick={() => setOpen(false)}
              className="text-xs font-medium text-cyan-700 hover:underline"
            >
              View all notifications →
            </Link>
          </div>
        </div>
      )}
    </div>
  )
}
