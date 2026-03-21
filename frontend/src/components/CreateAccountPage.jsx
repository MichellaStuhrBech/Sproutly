import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { API_BASE } from '../api'
import './CreateAccountPage.css'

function CreateAccountPage() {
  const navigate = useNavigate()
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')

    if (password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    if (password.length < 6) {
      setError('Password must be at least 6 characters')
      return
    }

    if (!name.trim()) {
      setError('Please enter your name')
      return
    }

    setLoading(true)
    try {
      const res = await fetch(`${API_BASE}/users/register`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password, name: name.trim() }),
      })

      let data = {}
      const contentType = res.headers.get('content-type')
      if (contentType && contentType.includes('application/json')) {
        data = await res.json().catch(() => ({}))
      }

      if (res.ok && res.status === 201) {
        navigate('/login', { state: { message: 'Account created. Please log in.' } })
        return
      }

      const message = data.msg ?? data.message
      if (res.status === 409) {
        setError(message || 'An account with this email already exists.')
        return
      }

      if (res.status === 500) {
        setError(
          message ||
          'Server error. On a live site, the administrator may need to set SECRET_KEY. Please try again or log in if you already have an account.'
        )
        return
      }

      if (res.status === 404) {
        setError(
          message ||
          'Registration service not found. If this is the live site, the API URL may be wrong or the backend is not running.'
        )
        return
      }

      setError(
        message ||
        'Something went wrong. If this is the live site, check that the backend is running and reachable (and that SECRET_KEY is set). Try again later.'
      )
    } catch (err) {
      setError(
        'Could not reach the server. Check your connection. If this is the live site, the backend may be down or the API URL may be wrong.'
      )
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="create-account-page">
      <Link to="/" className="create-account-back">
        ← Back
      </Link>

      <main className="create-account-main">
        <div className="create-account-card">
          <h1 className="create-account-title">Create Account</h1>
          <p className="create-account-subtitle">
            Join Sproutly and start planning your garden
          </p>

          {error && (
            <div className="create-account-error" role="alert">
              {error}
            </div>
          )}

          <form className="create-account-form" onSubmit={handleSubmit}>
            <label htmlFor="name" className="create-account-label">
              Name
            </label>
            <input
              id="name"
              type="text"
              className="create-account-input"
              placeholder="Your name"
              autoComplete="name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />

            <label htmlFor="email" className="create-account-label">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="create-account-input"
              placeholder="your@email.com"
              autoComplete="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
            />

            <label htmlFor="password" className="create-account-label">
              Password
            </label>
            <p className="create-account-hint" aria-live="polite">
              Password must be at least 6 characters.
            </p>
            <input
              id="password"
              type="password"
              className="create-account-input"
              placeholder="Min. 6 characters"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
            />

            <label htmlFor="confirmPassword" className="create-account-label">
              Confirm Password
            </label>
            <p className="create-account-hint" aria-live="polite">
            </p>
            <input
              id="confirmPassword"
              type="password"
              className="create-account-input"
              placeholder="Repeat password"
              autoComplete="new-password"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
              minLength={6}
            />

            <button
              type="submit"
              className="create-account-btn"
              disabled={loading}
            >
              {loading ? 'Creating…' : 'Create Account'}
            </button>
          </form>
        </div>
      </main>
    </div>
  )
}

export default CreateAccountPage
