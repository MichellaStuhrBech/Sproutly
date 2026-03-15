import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import './CreateAccountPage.css'

const API_BASE = '/api'

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

      const data = await res.json().catch(() => ({}))

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
        setError(message || 'Server error. Please try again or log in if you already have an account.')
        return
      }

      setError(message || 'Something went wrong. Please try again.')
    } catch (err) {
      setError('Could not reach the server. Is the backend running on port 7070?')
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
            <p className="create-account-hint">At least 6 characters.</p>
            <input
              id="password"
              type="password"
              className="create-account-input"
              placeholder="........"
              autoComplete="new-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
              minLength={6}
            />

            <label htmlFor="confirmPassword" className="create-account-label">
              Confirm Password
            </label>
            <input
              id="confirmPassword"
              type="password"
              className="create-account-input"
              placeholder="........"
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
