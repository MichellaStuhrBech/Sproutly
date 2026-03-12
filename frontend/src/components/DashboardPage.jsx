import { useState, useEffect } from 'react'
import { Link, useLocation } from 'react-router-dom'
import logo from '../logo/logo.png'
import './DashboardPage.css'


const TOMATO_SALE_IMAGE = '/src/Advertising/tomatoSale.png'

const API_BASE = '/api'

function DashboardPage() {
  const location = useLocation()
  const username = location.state?.username ?? 'User'
  const [isAdmin, setIsAdmin] = useState(false)
  const [adImageError, setAdImageError] = useState(false)
  const [frostWarning, setFrostWarning] = useState(false)
  const [frostMessage, setFrostMessage] = useState('')
  const [frostDismissed, setFrostDismissed] = useState(false)
  const [adminNotifications, setAdminNotifications] = useState([])
  const [dismissedNotificationIds, setDismissedNotificationIds] = useState(() => [])

  useEffect(() => {
    const rolesJson = localStorage.getItem('roles')
    const roles = rolesJson ? (() => { try { return JSON.parse(rolesJson) } catch { return [] } })() : []
    if (Array.isArray(roles) && roles.includes('ADMIN')) {
      setIsAdmin(true)
      return
    }
    const token = localStorage.getItem('token')
    if (!token) return
    fetch(`${API_BASE}/protected/admin_demo`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (res.ok) {
          setIsAdmin(true)
        } else if (res.status === 401) {
          // Token invalid or expired; clear so user logs in again and gets fresh token + roles
          localStorage.removeItem('token')
          localStorage.removeItem('email')
          localStorage.removeItem('roles')
        }
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) return
    fetch(`${API_BASE}/weather/frost-warning`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.ok ? res.json() : null)
      .then((data) => {
        if (data?.frostWarning && data?.message) {
          setFrostWarning(true)
          setFrostMessage(data.message)
        }
      })
      .catch(() => {})
  }, [])

  useEffect(() => {
    const token = localStorage.getItem('token')
    if (!token) return
    fetch(`${API_BASE}/notifications/active`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => res.ok ? res.json() : null)
      .then((data) => {
        setAdminNotifications(Array.isArray(data) ? data : [])
      })
      .catch(() => setAdminNotifications([]))
  }, [])

  return (
    <div className="dashboard-page">
      <header className="dashboard-header">
        <div className="dashboard-header-left">
          <img src={logo} alt="Sproutly" className="dashboard-logo" />
          <div className="dashboard-brand">
            <h1 className="dashboard-app-name">Sproutly</h1>
            <p className="dashboard-tagline">PLAN. GROW. HARVEST.</p>
            <p className="dashboard-welcome">Welcome back, {username}!</p>
          </div>
        </div>
        <Link to="/" className="dashboard-logout">
          → Log Out
        </Link>
      </header>

      <main className="dashboard-main">
        <h2 className="dashboard-title">Your Garden Dashboard</h2>
        <p className="dashboard-subtitle">
          Choose what you'd like to work on today
        </p>

        {frostWarning && !frostDismissed && (
          <div className="dashboard-frost-banner" role="alert">
            <span className="dashboard-frost-icon" aria-hidden>❄</span>
            <p className="dashboard-frost-message">{frostMessage}</p>
            <button
              type="button"
              className="dashboard-frost-dismiss"
              onClick={() => setFrostDismissed(true)}
              aria-label="Dismiss frost warning"
            >
              ×
            </button>
          </div>
        )}

        {adminNotifications
          .filter((n) => n.id != null && !dismissedNotificationIds.includes(n.id))
          .map((n) => (
            <div key={n.id} className="dashboard-notification-banner" role="alert">
              <span className="dashboard-notification-icon" aria-hidden>📢</span>
              <p className="dashboard-notification-message">{n.message}</p>
              <button
                type="button"
                className="dashboard-notification-dismiss"
                onClick={() => setDismissedNotificationIds((prev) => [...prev, n.id])}
                aria-label="Dismiss notification"
              >
                ×
              </button>
            </div>
          ))}

        <div className="dashboard-cards">
          <div className="dashboard-card">
            <div className="dashboard-card-icon dashboard-card-icon-todo">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden
              >
                <path d="M9 11l3 3L22 4" />
                <path d="M21 12v7a2 2 0 01-2 2H5a2 2 0 01-2-2V5a2 2 0 012-2h11" />
              </svg>
            </div>
            <h3 className="dashboard-card-title">To Do List</h3>
            <p className="dashboard-card-desc">
              Manage your garden tasks and activities
            </p>
            <p className="dashboard-card-detail">
              Keep track of watering, weeding, pruning, and other garden
              maintenance tasks.
            </p>
            <Link to="/todo" className="dashboard-card-btn dashboard-card-btn-green">
              Open To Do List
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="dashboard-card-icon dashboard-card-icon-sowing">
              <svg
                viewBox="0 0 548.403 548.403"
                fill="currentColor"
                xmlns="http://www.w3.org/2000/svg"
                aria-hidden
              >
                <path d="M389.712,100.37c-58.596,15.385-78.961,65.835-75.574,86.918c1.074-0.648,2.125-1.273,3.211-1.95 c5.149-3.159,10.754-6.148,16.569-9.23c5.78-3.101,12.005-5.979,18.274-8.898c3.153-1.471,6.458-2.762,9.728-4.093l4.928-2.009 c2.125-0.835,3.433-1.179,5.185-1.792l9.879-3.287c3.434-0.98,6.843-1.938,10.218-2.896c6.831-1.886,13.429-3.55,19.851-4.875 c12.833-2.919,24.896-5.044,35.276-6.65c20.786-3.165,34.95-3.766,34.95-3.766s-3.234,1.39-8.91,3.812 c-5.698,2.371-13.825,5.512-23.611,9.272c-4.928,1.792-10.217,3.742-15.822,5.797c-5.616,1.997-11.549,4.087-17.667,6.253 c-6.061,2.137-12.284,4.367-18.648,6.638c-6.224,2.102-12.717,4.589-19.232,7.088c-6.072,2.271-12.378,5.296-18.566,7.748 c-6.142,2.796-16.675,6.458-22.63,9.003c-7.182,3.013-11.725,4.642-16.29,7.059c-51.116,29.829-73.764,82.633-87.578,138.508 c-2.452-52.489-13.464-103.413-51.356-140.972c0.035-0.023,0.087-0.035,0.123-0.064c-5.132-4.805-10.463-9.972-15.951-15.507 c-4.507-4.647-9.271-9.266-13.907-14.193c-4.758-4.636-9.435-9.826-14.205-14.252c-5.085-4.805-10.106-9.563-15.069-13.907 c-5.01-4.496-9.908-8.933-14.684-13.236c-4.84-4.338-9.517-8.524-13.942-12.518c-4.385-4.041-8.571-7.871-12.395-11.409 c-7.619-7.187-13.954-13.212-18.298-17.58c-4.304-4.408-6.756-6.93-6.756-6.93s12.845,5.967,30.852,16.821 c8.979,5.453,19.308,12.016,30.074,19.612c5.424,3.696,10.889,7.748,16.476,12.086c2.75,2.189,5.546,4.367,8.32,6.568l7.882,6.813 c1.384,1.244,2.464,2.043,4.099,3.637l3.807,3.743c2.534,2.487,5.08,4.951,7.421,7.497c4.723,5.08,9.341,10.13,13.516,15.198 c4.192,5.068,8.227,9.972,11.776,14.854c0.753,1.051,1.46,2.008,2.213,3.024c11.204-18.187,11.642-72.597-36.66-109.199 C103.99,39.287,30.693,57.153,0,42.422c30.991,47.409,25.041,79.06,77.104,131.087c34.792,34.809,67.284,36.964,89.172,30.909 c44.519,42.855,51.467,108.188,51.49,170.708c-4.554-3.69-10.089-6.586-16.476-6.586c-18.018,0-31.138,10.135-37.426,24.592 c-39.661-13.499-66.004,17.877-68.287,44.303c-18.87-7.018-42.809,14.689-42.809,33.549c0,18.625,14.719,33.699,33.157,34.482 l291.945,0.514c0,0,31.107-16.769,31.107-35.008c0-19.115-15.495-34.635-34.61-34.635c-2.651,0-5.186,0.35-7.637,0.91 c0.163-1.342-3.34-60.125-54.416-36.514c-3.97-18.392-9.178-32.193-39.865-32.193c-15.116,0-25.352,5.488-31.902,11.069 c12.781-65.019,32.533-132.944,90.101-166.544c0.631-0.356,0.958-0.835,1.401-1.267c17.796,14.275,48.904,25.304,95.075,6.13 c67.961-28.229,74.535-59.746,121.278-91.724C514.446,128.109,453.515,83.625,389.712,100.37z M230.611,390.552l1.909-1.915 c-0.963,1.495-1.553,2.581-1.553,2.581S230.774,390.844,230.611,390.552z" />
              </svg>
            </div>
            <h3 className="dashboard-card-title">Sowing List</h3>
            <p className="dashboard-card-desc">
              Plan and track your planting schedule
            </p>
            <p className="dashboard-card-detail">
              Schedule when to sow seeds and transplant seedlings for optimal
              growth.
            </p>
            <Link to="/sowing" className="dashboard-card-btn dashboard-card-btn-orange">
              Open Sowing List
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="dashboard-card-icon dashboard-card-icon-chat">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden
              >
                <path d="M21 15a2 2 0 01-2 2H7l-4 4V5a2 2 0 012-2h14a2 2 0 012 2z" />
              </svg>
            </div>
            <h3 className="dashboard-card-title">Plant Chat</h3>
            <p className="dashboard-card-desc">
              Ask questions about plants and gardening
            </p>
            <p className="dashboard-card-detail">
              Get answers powered by plant data from Trefle and Perenual.
            </p>
            <Link to="/chat" className="dashboard-card-btn dashboard-card-btn-teal">
              Open Plant Chat
            </Link>
          </div>

          <div className="dashboard-card">
            <div className="dashboard-card-icon dashboard-card-icon-search">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                strokeWidth="2"
                strokeLinecap="round"
                strokeLinejoin="round"
                aria-hidden
              >
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.35-4.35" />
              </svg>
            </div>
            <h3 className="dashboard-card-title">Plant search</h3>
            <p className="dashboard-card-desc">
              Look up any plant with Perenual
            </p>
            <p className="dashboard-card-detail">
              Search by name and see care info, watering, sunlight, hardiness, and more.
            </p>
            <Link to="/plant-search" className="dashboard-card-btn dashboard-card-btn-search">
              Search plants
            </Link>
          </div>

          {isAdmin && (
            <div className="dashboard-card">
              <div className="dashboard-card-icon dashboard-card-icon-admin">
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden
                >
                  <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
                </svg>
              </div>
              <h3 className="dashboard-card-title">Admin</h3>
              <p className="dashboard-card-desc">
                View top plants and latest tasks across all users
              </p>
              <p className="dashboard-card-detail">
                See the 10 most popular sowing plants and the 20 most recent todo items.
              </p>
              <Link to="/admin" className="dashboard-card-btn dashboard-card-btn-admin">
                Open Admin Page
              </Link>
            </div>
          )}

          <div className="dashboard-card dashboard-card-ad">
            <a
              href="https://www.fastershave.dk"
              target="_blank"
              rel="noopener noreferrer"
              className="dashboard-ad-link"
              aria-label="Visit Fastershave.dk"
            >
              {adImageError ? (
                <span className="dashboard-ad-fallback">Faster's Have</span>
              ) : (
                <img
                  src={TOMATO_SALE_IMAGE}
                  alt="Tomato sale - FasterShave"
                  className="dashboard-ad-image"
                  onError={() => setAdImageError(true)}
                />
              )}
            </a>
          </div>
        </div>
      </main>

      <Link to="/help" className="dashboard-help" aria-label="Help">
        ?
      </Link>
    </div>
  )
}

export default DashboardPage
