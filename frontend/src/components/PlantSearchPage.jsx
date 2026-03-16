import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './PlantSearchPage.css'

import { API_BASE } from '../api'

function PlantSearchPage() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [searching, setSearching] = useState(false)
  const [selectedId, setSelectedId] = useState(null)
  const [selectedPlant, setSelectedPlant] = useState(null)
  const [details, setDetails] = useState(null)
  const [detailsLoading, setDetailsLoading] = useState(false)
  const [error, setError] = useState('')
  const skipSearchRef = useRef(false)

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in to search plants.' } })
      return
    }
  }, [token, navigate])

  // Debounced search as you type (like Trefle on Sowing list)
  useEffect(() => {
    const q = (query || '').trim()
    if (q.length < 2) {
      setResults([])
      setError('')
      return
    }
    if (skipSearchRef.current) {
      skipSearchRef.current = false
      return
    }
    const t = setTimeout(async () => {
      setSearching(true)
      setError('')
      try {
        const res = await fetch(
          `${API_BASE}/plants/search?q=${encodeURIComponent(q)}`,
          { headers: { Authorization: `Bearer ${token}` } }
        )
        if (res.status === 401) {
          localStorage.removeItem('token')
          localStorage.removeItem('email')
          localStorage.removeItem('roles')
          navigate('/login', { state: { message: 'Please log in again.' } })
          return
        }
        if (!res.ok) {
          setResults([])
          setError('Search failed.')
          return
        }
        const data = await res.json()
        const list = Array.isArray(data) ? data : []
        setResults(list)
        setError(list.length === 0 ? 'No plants found. Try another search.' : '')
      } catch {
        setResults([])
        setError('Could not reach the server.')
      } finally {
        setSearching(false)
      }
    }, 300)
    return () => clearTimeout(t)
  }, [query, token, navigate])

  const handleSelectPlant = (plant) => {
    const name = plant.commonName ?? plant.common_name ?? 'Unknown'
    skipSearchRef.current = true
    setQuery(name)
    setSelectedPlant(plant)
    setSelectedId(plant.id)
  }

  const handleSearch = (e) => {
    e?.preventDefault()
    if ((query || '').trim().length < 2) return
    skipSearchRef.current = false
    // Trigger effect by touching query; effect will run and search
    setQuery((q) => q.trim())
  }

  useEffect(() => {
    if (!selectedId || !token) {
      setDetails(null)
      setSelectedPlant(null)
      return
    }
    setDetailsLoading(true)
    setDetails(null)
    fetch(`${API_BASE}/plants/species/${selectedId}`, {
      headers: { Authorization: `Bearer ${token}` },
    })
      .then((res) => {
        if (res.status === 401) {
          localStorage.removeItem('token')
          navigate('/login', { state: { message: 'Please log in again.' } })
          return null
        }
        if (!res.ok) return null
        return res.json()
      })
      .then((data) => {
        setDetails(data || null)
      })
      .catch(() => setDetails(null))
      .finally(() => setDetailsLoading(false))
  }, [selectedId, token, navigate])

  const displayName = (plant) => plant.commonName ?? plant.common_name ?? 'Unknown'
  const displayScientific = (plant) => {
    const sn = plant.scientificName ?? plant.scientific_name
    if (Array.isArray(sn) && sn.length) return sn.join(', ')
    return sn ?? ''
  }
  const imageUrl = (plant) => {
    const img = plant.defaultImage ?? plant.default_image
    if (!img) return null
    return img.thumbnail ?? img.smallUrl ?? img.small_url ?? img.regularUrl ?? img.regular_url ?? null
  }

  if (!token) return null

  return (
    <div className="plant-search-page">
      <header className="plant-search-header">
        <div className="plant-search-header-left">
          <img src={logo} alt="" className="plant-search-logo" />
          <div className="plant-search-brand">
            <h1 className="plant-search-app-name">Sproutly</h1>
            <p className="plant-search-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
        <Link to="/dashboard" className="plant-search-back">
          ← Back to Dashboard
        </Link>
      </header>
      <div className="plant-search-sep" />
      <main className="plant-search-main">
        <h1 className="plant-search-title">Plant search (Perenual)</h1>
        <p className="plant-search-subtitle">
          Search for a plant to see care info, watering, sunlight, and more.
        </p>

        <form onSubmit={handleSearch} className="plant-search-form">
          <input
            type="search"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            placeholder="Start typing to see suggestions (e.g. tomato, lavender)"
            className="plant-search-input"
            aria-label="Search for a plant"
            aria-autocomplete="list"
            aria-controls="plant-search-suggestions"
            aria-expanded={results.length > 0}
          />
          <button type="submit" className="plant-search-btn" disabled={searching}>
            {searching ? 'Searching…' : 'Search'}
          </button>
        </form>

        {error && (
          <p className="plant-search-error" role="alert">
            {error}
          </p>
        )}

        <div className="plant-search-layout">
          <aside className="plant-search-results" id="plant-search-suggestions">
            <h2 className="plant-search-results-title">Suggestions</h2>
            {results.length === 0 && !searching && (
              <p className="plant-search-empty">
                Start typing to see suggestions (e.g. tomato, lavender).
              </p>
            )}
            {searching && results.length === 0 && (
              <p className="plant-search-loading">Searching…</p>
            )}
            <ul className="plant-search-list">
              {results.map((plant) => {
                const id = plant.id
                const active = selectedId === id
                return (
                  <li key={id}>
                    <button
                      type="button"
                      className={`plant-search-result-item ${active ? 'plant-search-result-item-active' : ''}`}
                      onClick={() => handleSelectPlant(plant)}
                    >
                      {imageUrl(plant) ? (
                        <img
                          src={imageUrl(plant)}
                          alt=""
                          className="plant-search-result-thumb"
                        />
                      ) : (
                        <span className="plant-search-result-no-img">🌱</span>
                      )}
                      <div className="plant-search-result-text">
                        <span className="plant-search-result-name">{displayName(plant)}</span>
                        {displayScientific(plant) && (
                          <span className="plant-search-result-scientific">
                            {displayScientific(plant)}
                          </span>
                        )}
                      </div>
                    </button>
                  </li>
                )
              })}
            </ul>
          </aside>

          <section className="plant-search-detail" aria-label="Plant details">
            {detailsLoading && (
              <p className="plant-search-detail-loading">Loading details…</p>
            )}
            {!detailsLoading && details && (
              <div className="plant-search-detail-card">
                <div className="plant-search-detail-head">
                  {details.defaultImage?.thumbnail || details.default_image?.thumbnail ? (
                    <img
                      src={details.defaultImage?.regularUrl ?? details.default_image?.regular_url ?? details.defaultImage?.mediumUrl ?? details.default_image?.medium_url ?? details.defaultImage?.thumbnail ?? details.default_image?.thumbnail}
                      alt=""
                      className="plant-search-detail-image"
                    />
                  ) : (
                    <div className="plant-search-detail-image-placeholder">🌿</div>
                  )}
                  <div className="plant-search-detail-head-text">
                    <h2 className="plant-search-detail-name">
                      {details.commonName ?? details.common_name ?? 'Unknown'}
                    </h2>
                    {(details.scientificName ?? details.scientific_name)?.length > 0 && (
                      <p className="plant-search-detail-scientific">
                        {Array.isArray(details.scientificName ?? details.scientific_name)
                          ? (details.scientificName ?? details.scientific_name).join(', ')
                          : (details.scientificName ?? details.scientific_name)}
                      </p>
                    )}
                    {details.family && (
                      <p className="plant-search-detail-meta">Family: {details.family}</p>
                    )}
                  </div>
                </div>
                {details.description && (
                  <div className="plant-search-detail-block">
                    <h3>Description</h3>
                    <p>{details.description}</p>
                  </div>
                )}
                <div className="plant-search-detail-grid">
                  {details.cycle && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Cycle</span>
                      <span>{details.cycle}</span>
                    </div>
                  )}
                  {details.watering && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Watering</span>
                      <span>{details.watering}</span>
                    </div>
                  )}
                  {details.sunlight?.length > 0 && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Sunlight</span>
                      <span>{details.sunlight.join(', ')}</span>
                    </div>
                  )}
                  {details.careLevel && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Care level</span>
                      <span>{details.careLevel}</span>
                    </div>
                  )}
                  {details.maintenance && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Maintenance</span>
                      <span>{details.maintenance}</span>
                    </div>
                  )}
                  {details.growthRate && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Growth rate</span>
                      <span>{details.growthRate}</span>
                    </div>
                  )}
                  {details.indoor != null && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Indoor</span>
                      <span>{details.indoor ? 'Yes' : 'No'}</span>
                    </div>
                  )}
                  {details.hardiness && (details.hardiness.min || details.hardiness.max) && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Hardiness zone</span>
                      <span>{[details.hardiness.min, details.hardiness.max].filter(Boolean).join('–')}</span>
                    </div>
                  )}
                  {details.soil?.length > 0 && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Soil</span>
                      <span>{details.soil.join(', ')}</span>
                    </div>
                  )}
                  {details.floweringSeason && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Flowering season</span>
                      <span>{details.floweringSeason}</span>
                    </div>
                  )}
                  {details.poisonousToHumans != null && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Poisonous to humans</span>
                      <span>{details.poisonousToHumans ? 'Yes' : 'No'}</span>
                    </div>
                  )}
                  {details.poisonousToPets != null && (
                    <div className="plant-search-detail-item">
                      <span className="plant-search-detail-label">Poisonous to pets</span>
                      <span>{details.poisonousToPets ? 'Yes' : 'No'}</span>
                    </div>
                  )}
                </div>
                {details.pestSusceptibility?.length > 0 && (
                  <div className="plant-search-detail-block">
                    <h3>Pest susceptibility</h3>
                    <p>{details.pestSusceptibility.join(', ')}</p>
                  </div>
                )}
              </div>
            )}
            {!detailsLoading && !details && selectedId && selectedPlant && (
              <div className="plant-search-detail-fallback">
                <p className="plant-search-detail-empty">
                  Full care details are not available from Perenual for this species (API limit or ID). Here is what we have from search:
                </p>
                <div className="plant-search-detail-head">
                  {imageUrl(selectedPlant) ? (
                    <img src={imageUrl(selectedPlant)} alt="" className="plant-search-detail-image" />
                  ) : (
                    <div className="plant-search-detail-image-placeholder">🌿</div>
                  )}
                  <div className="plant-search-detail-head-text">
                    <h2 className="plant-search-detail-name">{displayName(selectedPlant)}</h2>
                    {displayScientific(selectedPlant) && (
                      <p className="plant-search-detail-scientific">{displayScientific(selectedPlant)}</p>
                    )}
                    <p className="plant-search-detail-meta">Perenual ID: {selectedPlant.id}</p>
                  </div>
                </div>
              </div>
            )}
            {!detailsLoading && !details && selectedId && !selectedPlant && (
              <p className="plant-search-detail-empty">Could not load details for this plant.</p>
            )}
            {!detailsLoading && !details && !selectedId && (
              <p className="plant-search-detail-empty">Select a plant from the results to see details.</p>
            )}
          </section>
        </div>
      </main>
    </div>
  )
}

export default PlantSearchPage
