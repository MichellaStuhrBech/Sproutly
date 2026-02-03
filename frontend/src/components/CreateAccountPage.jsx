import { Link } from 'react-router-dom'
import './CreateAccountPage.css'

function CreateAccountPage() {
  const handleSubmit = (e) => {
    e.preventDefault()
    // TODO: call create account API
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
              required
            />

            <label htmlFor="password" className="create-account-label">
              Password
            </label>
            <input
              id="password"
              type="password"
              className="create-account-input"
              placeholder="........"
              autoComplete="new-password"
              required
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
              required
            />

            <button type="submit" className="create-account-btn">
              Create Account
            </button>
          </form>
        </div>
      </main>
    </div>
  )
}

export default CreateAccountPage
