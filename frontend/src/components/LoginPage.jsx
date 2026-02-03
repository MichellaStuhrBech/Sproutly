import { Link, useNavigate } from 'react-router-dom'
import './LoginPage.css'

function LoginPage() {
  const navigate = useNavigate()

  const handleSubmit = (e) => {
    e.preventDefault()
    const form = e.target
    const email = form.querySelector('#email').value
    const username = email.includes('@') ? email.split('@')[0] : email || 'User'
    navigate('/dashboard', { state: { username } })
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

            <button type="submit" className="login-btn">
              Log In
            </button>
          </form>
        </div>
      </main>
    </div>
  )
}

export default LoginPage
