import { useState } from 'react'
import { Link, useNavigate, useLocation } from 'react-router-dom'
import './LoginPage.css'

const API_BASE = '/api'

function LoginPage() {
  const navigate = useNavigate()
  const location = useLocation()
  const successMessage = location.state?.message
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const form = e.target
    const email = form.querySelector('#email').value
    const password = form.querySelector('#password').value

    setLoading(true)
    try {
      const res = await fetch(`${API_BASE}/auth/login`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })

      const data = await res.json().catch(() => ({}))

      if (res.ok && res.status === 200) {
        if (data.token) {
          localStorage.setItem('token', data.token)
          localStorage.setItem('email', data.email || email)
        }
        const username = email.includes('@') ? email.split('@')[0] : email || 'User'
        navigate('/dashboard', { state: { username } })
        return
      }

      setError(res.status === 401 ? 'Email or password is incorrect.' : (data.msg || 'Email or password is incorrect.'))
    } catch (err) {
      setError('Could not reach the server. Is the backend running?')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      <Link to="/" className="login-back">
        ← Back
      </Link>

      <main className="login-main">
        <div className="login-card">
          <h1 className="login-title">Log In</h1>
          <p className="login-subtitle">
            Enter your credentials to access your garden
          </p>

          {successMessage && (
            <div className="login-success" role="status">
              {successMessage}
            </div>
          )}

          {error && (
            <div className="login-error" role="alert">
              {error}
            </div>
          )}

          <form className="login-form" onSubmit={handleSubmit}>
            <label htmlFor="email" className="login-label">
              Email
            </label>
            <input
              id="email"
              type="email"
              className="login-input"
              placeholder="your@email.com"
              autoComplete="email"
              required
            />

            <label htmlFor="password" className="login-label">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="login-input"
              placeholder="........"
              autoComplete="current-password"
              required
            />

            <button type="submit" className="login-btn" disabled={loading}>
              {loading ? 'Logging in…' : 'Log In'}
            </button>
          </form>
        </div>
      </main>
    </div>
  )
}

export default LoginPage
