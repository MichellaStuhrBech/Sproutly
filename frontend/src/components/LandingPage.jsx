import { Link } from 'react-router-dom'
import './LandingPage.css'
import logo from '../logo/logo.png'

function LandingPage() {
  return (
    <div className="landing-page">
      <header className="landing-header">
        <img src={logo} alt="Sproutly" className="logo-img" />
        <h1 className="app-name">Sproutly</h1>
        <p className="tagline">PLAN. GROW. HARVEST.</p>
      </header>

      <main className="landing-main">
        <div className="welcome-card">
          <h2 className="welcome-title">Welcome to Sproutly</h2>
          <p className="welcome-tagline">Plan. Grow. Harvest.</p>
          <p className="welcome-description">
            Your personal garden planning companion. Organize your tasks and
            track your sowing schedule.
          </p>
          <div className="welcome-actions">
            <Link to="/login" className="btn btn-primary">
              Log In
            </Link>
            <Link to="/create-account" className="btn btn-secondary">
              Create Account
            </Link>
          </div>
        </div>
      </main>
    </div>
  )
}

export default LandingPage
