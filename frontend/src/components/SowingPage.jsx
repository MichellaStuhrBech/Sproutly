import { useState } from 'react'
import { Link } from 'react-router-dom'
import logo from '../logo/logo.png'
import './SowingPage.css'

const initialPlants = [
  {
    id: '1',
    name: 'Tomatoes',
    sowingDate: '2026-03-15',
    harvestDate: '2026-07-15',
    location: 'Greenhouse',
  },
  {
    id: '2',
    name: 'Carrots',
    sowingDate: '2026-04-01',
    harvestDate: '2026-07-01',
    location: 'Raised Bed A',
  },
]

function formatDate(iso) {
  const d = new Date(iso)
  const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
  return `${months[d.getMonth()]} ${d.getDate()}, ${d.getFullYear()}`
}

function SowingPage() {
  const [plants, setPlants] = useState(initialPlants)

  const handleDelete = (id) => {
    setPlants((prev) => prev.filter((p) => p.id !== id))
  }

  return (
    <div className="sowing-page">
      <header className="sowing-header">
        <div className="sowing-header-left">
          <img src={logo} alt="" className="sowing-header-logo" />
          <div className="sowing-header-brand">
            <h1 className="sowing-header-app-name">Sproutly</h1>
            <p className="sowing-header-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
      </header>
      <div className="sowing-header-sep" />
      <nav className="sowing-nav">
        <Link to="/dashboard" className="sowing-back">
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="sowing-main">
        <div className="sowing-title-row">
          <div>
            <h2 className="sowing-title">Sowing Schedule</h2>
            <p className="sowing-subtitle">
              Plan and track your planting schedule.
            </p>
          </div>
          <button type="button" className="sowing-add-btn">
            <span className="sowing-add-icon">+</span> Add Plant
          </button>
        </div>

        <ul className="sowing-list">
          {plants.map((plant) => (
            <li key={plant.id} className="sowing-card">
              <button
                type="button"
                className="sowing-card-delete"
                onClick={() => handleDelete(plant.id)}
                aria-label={`Delete ${plant.name}`}
              >
                <svg
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  strokeWidth="2"
                  strokeLinecap="round"
                  strokeLinejoin="round"
                  aria-hidden
                >
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2" />
                  <line x1="10" y1="11" x2="10" y2="17" />
                  <line x1="14" y1="11" x2="14" y2="17" />
                </svg>
              </button>
              <h3 className="sowing-card-name">{plant.name}</h3>
              <p className="sowing-card-date">
                <span className="sowing-card-icon" aria-hidden>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </span>
                Sowing: {formatDate(plant.sowingDate)}
              </p>
              <p className="sowing-card-date">
                <span className="sowing-card-icon" aria-hidden>
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/></svg>
                </span>
                Harvest: {formatDate(plant.harvestDate)}
              </p>
              <span className="sowing-card-location">{plant.location}</span>
            </li>
          ))}
        </ul>
      </main>
    </div>
  )
}

export default SowingPage
