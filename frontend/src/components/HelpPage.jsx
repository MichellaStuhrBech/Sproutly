import { useState } from 'react'
import { Link } from 'react-router-dom'
import './PlaceholderPage.css'

const API_BASE = '/api'

function HelpPage() {
  const [displayName, setDisplayName] = useState(() => localStorage.getItem('displayName') || '')
  const [msg, setMsg] = useState('')
  const [err, setErr] = useState('')

  const saveName = async (e) => {
    e.preventDefault()
    setMsg('')
    setErr('')
    const token = localStorage.getItem('token')
    if (!token) {
      setErr('Log in first, then set your name here.')
      return
    }
    const name = displayName.trim()
    if (!name) {
      setErr('Enter a name.')
      return
    }
    try {
      const res = await fetch(`${API_BASE}/auth/profile`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ displayName: name }),
      })
      const data = await res.json().catch(() => ({}))
      if (!res.ok) {
        setErr(data.msg || 'Could not save.')
        return
      }
      localStorage.setItem('displayName', data.displayName || name)
      setMsg('Saved. Refresh the dashboard to see “Welcome back, ' + (data.displayName || name) + '!”')
    } catch {
      setErr('Could not reach the server.')
    }
  }

  return (
    <div className="placeholder-page">
      <Link to="/dashboard" className="placeholder-back">
        ← Back to Dashboard
      </Link>
      <h1 className="placeholder-title">Help</h1>
      <p className="placeholder-text">
        Set how your name appears on the dashboard (e.g. <strong>Mickey Mouse</strong> instead of the part before @ in your email).
      </p>
      <form onSubmit={saveName} style={{ maxWidth: 360, marginTop: '1.5rem' }}>
        <label htmlFor="help-displayName" style={{ display: 'block', marginBottom: 6, fontWeight: 600 }}>
          Your name
        </label>
        <input
          id="help-displayName"
          type="text"
          value={displayName}
          onChange={(e) => setDisplayName(e.target.value)}
          placeholder="e.g. Mickey Mouse"
          style={{ width: '100%', padding: '0.5rem 0.75rem', marginBottom: 8 }}
        />
        <button type="submit" style={{ padding: '0.5rem 1rem', cursor: 'pointer' }}>
          Save name
        </button>
      </form>
      {msg && <p style={{ color: '#2d4a2a', marginTop: 12 }}>{msg}</p>}
      {err && <p style={{ color: '#b91c1c', marginTop: 12 }}>{err}</p>}
    </div>
  )
}

export default HelpPage
