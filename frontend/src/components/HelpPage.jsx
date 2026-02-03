import { Link } from 'react-router-dom'
import './PlaceholderPage.css'

function HelpPage() {
  return (
    <div className="placeholder-page">
      <Link to="/dashboard" className="placeholder-back">
        ← Back to Dashboard
      </Link>
      <h1 className="placeholder-title">Help</h1>
      <p className="placeholder-text">Coming soon.</p>
    </div>
  )
}

export default HelpPage
