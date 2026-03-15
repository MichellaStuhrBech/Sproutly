import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './AdminPage.css'

const API_BASE = '/api'

function AdminPage() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const [stats, setStats] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [notifications, setNotifications] = useState([])
  const [notificationMessage, setNotificationMessage] = useState('')
  const [notificationDate, setNotificationDate] = useState('')
  const [notificationError, setNotificationError] = useState('')
  const [notificationSaving, setNotificationSaving] = useState(false)

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in.' } })
      return
    }
    const load = async () => {
      try {
        const res = await fetch(`${API_BASE}/admin/stats`, {
          headers: { Authorization: `Bearer ${token}` },
        })
        if (res.status === 401 || res.status === 403) {
          navigate('/dashboard', { state: { message: 'Admin access required.' } })
          return
        }
        if (!res.ok) throw new Error('Failed to load admin stats')
        const data = await res.json()
        setStats(data)
      } catch (e) {
        setError('Could not load admin stats.')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [token, navigate])

  const loadNotifications = async () => {
    if (!token) return
    try {
      const res = await fetch(`${API_BASE}/admin/notifications`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.ok) {
        const data = await res.json()
        setNotifications(Array.isArray(data) ? data : [])
      }
    } catch {
      setNotifications([])
    }
  }

  useEffect(() => {
    if (token && !loading) loadNotifications()
  }, [token, loading])

  const handleCreateNotification = async (e) => {
    e.preventDefault()
    setNotificationError('')
    const message = (notificationMessage || '').trim()
    const showDate = (notificationDate || '').trim()
    if (!message) {
      setNotificationError('Message is required.')
      return
    }
    if (!showDate) {
      setNotificationError('Date is required.')
      return
    }
    setNotificationSaving(true)
    try {
      const res = await fetch(`${API_BASE}/admin/notifications`, {
        method: 'POST',
        headers: {
          Authorization: `Bearer ${token}`,
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ message, showDate }),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setNotificationError(data.msg || data.message || 'Failed to save notification.')
        return
      }
      setNotificationMessage('')
      setNotificationDate('')
      await loadNotifications()
    } catch {
      setNotificationError('Could not save notification.')
    } finally {
      setNotificationSaving(false)
    }
  }

  const handleDeleteNotification = async (id) => {
    if (!token) return
    try {
      const res = await fetch(`${API_BASE}/admin/notifications/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.ok || res.status === 204) await loadNotifications()
    } catch {}
  }

  if (!token) return null

  return (
    <div className="admin-page">
      <header className="admin-header">
        <div className="admin-header-left">
          <img src={logo} alt="" className="admin-header-logo" />
          <div className="admin-header-brand">
            <h1 className="admin-header-app-name">Sproutly</h1>
            <p className="admin-header-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
      </header>
      <div className="admin-header-sep" />
      <nav className="admin-nav">
        <Link to="/dashboard" className="admin-back">
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="admin-main">
        <div className="admin-card">
          <h1 className="admin-title">Admin overview</h1>
          <p className="admin-subtitle">
            Top sowing plants and latest tasks across all users
          </p>

          {error && (
            <div className="admin-error" role="alert">
              {error}
            </div>
          )}

          {loading ? (
            <p className="admin-loading">Loading…</p>
          ) : stats ? (
            <>
              <div className="admin-stats-summary">
                <p className="admin-user-count">
                  <strong>Total users:</strong> {stats.userCount ?? 0}
                </p>
              </div>
              <section className="admin-section">
                <h2 className="admin-section-title">Top 10 most picked plants (sowing list)</h2>
                {stats.topPlants && stats.topPlants.length > 0 ? (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Plant name</th>
                        <th>Times added</th>
                      </tr>
                    </thead>
                    <tbody>
                      {stats.topPlants.map((row, i) => (
                        <tr key={i}>
                          <td>{i + 1}</td>
                          <td>{row.name ?? '—'}</td>
                          <td>{row.picks ?? 0}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="admin-empty">No plants in sowing lists yet.</p>
                )}
              </section>

              <section className="admin-section">
                <h2 className="admin-section-title">Last 20 tasks (all users)</h2>
                {stats.lastTasks && stats.lastTasks.length > 0 ? (
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>#</th>
                        <th>Title</th>
                        <th>Notes</th>
                        <th>User</th>
                      </tr>
                    </thead>
                    <tbody>
                      {stats.lastTasks.map((row, i) => (
                        <tr key={row.id ?? i}>
                          <td>{i + 1}</td>
                          <td>{row.title ?? '—'}</td>
                          <td>{row.notes ?? '—'}</td>
                          <td>{row.userEmail ?? '—'}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                ) : (
                  <p className="admin-empty">No tasks yet.</p>
                )}
              </section>

              <section className="admin-section admin-section-notifications">
                <h2 className="admin-section-title">Broadcast notification to all users</h2>
                <p className="admin-notification-desc">
                  Add a message and the date when it should be shown. All users will see it as a notification on that day.
                </p>
                <form onSubmit={handleCreateNotification} className="admin-notification-form">
                  <div className="admin-notification-field">
                    <label htmlFor="admin-notification-message">Message</label>
                    <textarea
                      id="admin-notification-message"
                      value={notificationMessage}
                      onChange={(e) => setNotificationMessage(e.target.value)}
                      rows={3}
                      maxLength={2000}
                      placeholder="e.g. Garden sale this Saturday 10–14."
                    />
                  </div>
                  <div className="admin-notification-field">
                    <label htmlFor="admin-notification-date">Show on date</label>
                    <input
                      id="admin-notification-date"
                      type="date"
                      value={notificationDate}
                      onChange={(e) => setNotificationDate(e.target.value)}
                      required
                    />
                  </div>
                  {notificationError && (
                    <p className="admin-error" role="alert">{notificationError}</p>
                  )}
                  <button type="submit" className="admin-notification-submit" disabled={notificationSaving}>
                    {notificationSaving ? 'Saving…' : 'Save notification'}
                  </button>
                </form>
                <h3 className="admin-notification-list-title">Scheduled notifications</h3>
                {notifications.length > 0 ? (
                  <ul className="admin-notification-list">
                    {notifications.map((n) => (
                      <li key={n.id} className="admin-notification-item">
                        <span className="admin-notification-item-message">{n.message}</span>
                        <span className="admin-notification-item-date">{n.showDate}</span>
                        <button
                          type="button"
                          className="admin-notification-delete"
                          onClick={() => handleDeleteNotification(n.id)}
                          aria-label="Delete notification"
                        >
                          Delete
                        </button>
                      </li>
                    ))}
                  </ul>
                ) : (
                  <p className="admin-empty">No scheduled notifications.</p>
                )}
              </section>
            </>
          ) : null}
        </div>
      </main>
    </div>
  )
}

export default AdminPage
