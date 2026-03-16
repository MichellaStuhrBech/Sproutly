import { useState, useEffect, useCallback, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './SowingPage.css'

import { API_BASE } from '../api'
const MONTHS = ['', 'January', 'February', 'March', 'April', 'May', 'June', 'July', 'August', 'September', 'October', 'November', 'December']

function SowingPage() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const [plants, setPlants] = useState([])
  const [loading, setLoading] = useState(true)
  const [showAddForm, setShowAddForm] = useState(false)
  const [searchQuery, setSearchQuery] = useState('')
  const [suggestions, setSuggestions] = useState([])
  const [suggestionsLoading, setSuggestionsLoading] = useState(false)
  const [selectedName, setSelectedName] = useState('')
  const [selectedLatinName, setSelectedLatinName] = useState('')
  const [sowingMonth, setSowingMonth] = useState(3)
  const [addError, setAddError] = useState('')
  const [adding, setAdding] = useState(false)
  const skipSearchRef = useRef(false)

  const loadPlants = useCallback(async () => {
    if (!token) return
    try {
      const res = await fetch(`${API_BASE}/sowinglist`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('roles')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) throw new Error('Failed to load plants')
      const data = await res.json()
      setPlants(Array.isArray(data) ? data : [])
    } catch {
      setPlants((prev) => (prev.length > 0 ? prev : []))
    } finally {
      setLoading(false)
    }
  }, [token, navigate])

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in to view your sowing schedule.' } })
      return
    }
    loadPlants()
  }, [token, navigate, loadPlants])

  useEffect(() => {
    if (!searchQuery.trim()) {
      setSuggestions([])
      return
    }
    if (skipSearchRef.current) {
      skipSearchRef.current = false
      return
    }
    const t = setTimeout(async () => {
      setSuggestionsLoading(true)
      try {
        const res = await fetch(
          `${API_BASE}/sowinglist/search?q=${encodeURIComponent(searchQuery.trim())}`,
          { headers: { Authorization: `Bearer ${token}` } }
        )
        if (!res.ok) {
          setSuggestions([])
          return
        }
        const data = await res.json()
        setSuggestions(Array.isArray(data) ? data : [])
      } catch {
        setSuggestions([])
      } finally {
        setSuggestionsLoading(false)
      }
    }, 300)
    return () => clearTimeout(t)
  }, [searchQuery, token])

  const getSuggestionDisplayName = (s) =>
    s.commonName ?? s.common_name ?? s.scientificName ?? s.scientific_name ?? 'Unknown'
  const getSuggestionLatinName = (s) =>
    s.scientificName ?? s.scientific_name ?? ''

  const handleSelectSuggestion = (s) => {
    const displayName = getSuggestionDisplayName(s)
    const latinName = getSuggestionLatinName(s)
    skipSearchRef.current = true
    setSelectedName(displayName)
    setSelectedLatinName(latinName)
    setSuggestions([])
    setSearchQuery(displayName)
  }

  const handleSubmitAdd = async (e) => {
    e.preventDefault()
    setAddError('')
    const name = selectedName.trim() || searchQuery.trim()
    if (!name) {
      setAddError('Select or enter a plant name.')
      return
    }
    setAdding(true)
    try {
      const res = await fetch(`${API_BASE}/sowinglist`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          name,
          latinName: (selectedLatinName || '').trim(),
          sowingMonth: Number(sowingMonth) || 3,
        }),
      })
      if (res.status === 401) {
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) {
        const err = await res.json().catch(() => ({}))
        setAddError(err.msg || err.message || 'Failed to add plant.')
        return
      }
      const created = await res.json()
      setShowAddForm(false)
      setSearchQuery('')
      setSelectedName('')
      setSelectedLatinName('')
      setSowingMonth(3)
      setPlants((prev) => [...prev, created])
      await loadPlants()
    } catch {
      setAddError('Could not add plant.')
    } finally {
      setAdding(false)
    }
  }

  const handleUpdate = async (plant, updates) => {
    const next = { ...plant, ...updates }
    setPlants((prev) => prev.map((p) => (p.id === plant.id ? next : p)))
    try {
      const res = await fetch(`${API_BASE}/sowinglist/${plant.id}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({
          id: next.id,
          name: next.name,
          latinName: next.latinName ?? '',
          sowingMonth: next.sowingMonth,
          note: next.note ?? '',
          completed: next.completed ?? false,
        }),
      })
      if (!res.ok) setPlants((prev) => prev.map((p) => (p.id === plant.id ? plant : p)))
    } catch {
      setPlants((prev) => prev.map((p) => (p.id === plant.id ? plant : p)))
    }
  }

  const handleDelete = async (id) => {
    if (!token) return
    try {
      const res = await fetch(`${API_BASE}/sowinglist/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.ok) loadPlants()
    } catch {
      // ignore
    }
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
          <button
            type="button"
            className="sowing-add-btn"
            onClick={() => setShowAddForm((v) => !v)}
          >
            <span className="sowing-add-icon">+</span> Add Plant
          </button>
        </div>

        {showAddForm && (
          <form className="sowing-add-form" onSubmit={handleSubmitAdd}>
            <div className="sowing-add-field">
              <label htmlFor="sowing-search">Search plant (e.g. tomato)</label>
              <input
                id="sowing-search"
                type="text"
                className="sowing-search-input"
                placeholder="Type to search Trefle..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
                autoComplete="off"
              />
              {suggestionsLoading && (
                <p className="sowing-suggestions-loading">Searching…</p>
              )}
              {!suggestionsLoading && suggestions.length > 0 && (
                <ul className="sowing-suggestions" role="listbox">
                  {suggestions.slice(0, 10).map((s) => (
                    <li
                      key={s.id}
                      role="option"
                      className="sowing-suggestion-item"
                      onClick={() => handleSelectSuggestion(s)}
                    >
                      <span className="sowing-suggestion-common">
                        {getSuggestionDisplayName(s)}
                      </span>
                      {getSuggestionLatinName(s) && (
                        <span className="sowing-suggestion-latin">
                          {getSuggestionLatinName(s)}
                        </span>
                      )}
                    </li>
                  ))}
                </ul>
              )}
            </div>
            <div className="sowing-add-field">
              <label htmlFor="sowing-month">Sowing month</label>
              <select
                id="sowing-month"
                value={sowingMonth}
                onChange={(e) => setSowingMonth(Number(e.target.value))}
              >
                {MONTHS.slice(1).map((m, i) => (
                  <option key={i} value={i + 1}>
                    {m}
                  </option>
                ))}
              </select>
            </div>
            {addError && (
              <p className="sowing-add-error" role="alert">
                {addError}
              </p>
            )}
            <div className="sowing-add-actions">
              <button
                type="button"
                className="sowing-add-cancel"
                onClick={() => setShowAddForm(false)}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="sowing-add-submit"
                disabled={adding}
              >
                {adding ? 'Adding…' : 'Add plant'}
              </button>
            </div>
          </form>
        )}

        {loading ? (
          <p className="sowing-loading">Loading plants…</p>
        ) : (
          <ul className="sowing-list">
            {plants.length === 0 ? (
              <li className="sowing-empty">No plants yet. Add one above.</li>
            ) : (
              plants.map((plant) => (
                <li
                  key={plant.id}
                  className={`sowing-card ${plant.completed ? 'sowing-card--completed' : ''}`}
                >
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
                  <div className="sowing-card-row">
                    <button
                      type="button"
                      className="sowing-card-checkbox"
                      onClick={() => handleUpdate(plant, { completed: !plant.completed })}
                      aria-label={plant.completed ? 'Mark not done' : 'Mark done'}
                      aria-pressed={plant.completed}
                    >
                      {plant.completed ? (
                        <svg viewBox="0 0 24 24" fill="currentColor" aria-hidden>
                          <path d="M9 16.17L4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z" />
                        </svg>
                      ) : (
                        <span className="sowing-card-checkbox-empty" />
                      )}
                    </button>
                    <div className="sowing-card-body">
                      <h3 className="sowing-card-name">{plant.name}</h3>
                      {plant.latinName && (
                        <p className="sowing-card-latin">{plant.latinName}</p>
                      )}
                      <div className="sowing-card-date-row">
                        <label className="sowing-card-label">Sowing month</label>
                        <select
                          className="sowing-card-month-select"
                          value={plant.sowingMonth ?? 1}
                          onChange={(e) =>
                            handleUpdate(plant, { sowingMonth: Number(e.target.value) })
                          }
                          onClick={(e) => e.stopPropagation()}
                        >
                          {MONTHS.slice(1).map((m, i) => (
                            <option key={i} value={i + 1}>
                              {m}
                            </option>
                          ))}
                        </select>
                      </div>
                      <div className="sowing-card-note-row">
                        <label className="sowing-card-label">Note (e.g. date sowed)</label>
                        <input
                          type="text"
                          className="sowing-card-note-input"
                          placeholder="e.g. Sowed 15 March 2026"
                          value={plant.note ?? ''}
                          onChange={(e) =>
                            setPlants((prev) =>
                              prev.map((p) =>
                                p.id === plant.id ? { ...p, note: e.target.value } : p
                              )
                            )
                          }
                          onBlur={(e) =>
                            handleUpdate(plant, { note: e.target.value.trim() })
                          }
                        />
                      </div>
                    </div>
                  </div>
                </li>
              ))
            )}
          </ul>
        )}
      </main>
    </div>
  )
}

export default SowingPage
