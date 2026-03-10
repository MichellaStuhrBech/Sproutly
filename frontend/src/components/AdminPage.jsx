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
            </>
          ) : null}
        </div>
      </main>
    </div>
  )
}

export default AdminPage
