import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './GardenBedsPage.css'

const API_BASE = '/api'

function GardenBedsPage() {
  const navigate = useNavigate()
  const [beds, setBeds] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [adding, setAdding] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [formName, setFormName] = useState('')
  const [formContents, setFormContents] = useState('')

  const token = localStorage.getItem('token')

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in to view your garden beds.' } })
      return
    }
    loadBeds()
  }, [token, navigate])

  const loadBeds = async () => {
    if (!token) return
    setError('')
    try {
      const res = await fetch(`${API_BASE}/garden-beds`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('roles')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) throw new Error('Failed to load garden beds')
      const data = await res.json()
      setBeds(Array.isArray(data) ? data : [])
    } catch (err) {
      setError('Could not load garden beds.')
    } finally {
      setLoading(false)
    }
  }

  const startAdd = () => {
    setEditingId(null)
    setFormName('')
    setFormContents('')
    setAdding(true)
  }

  const startEdit = (bed) => {
    setAdding(false)
    setEditingId(bed.id)
    setFormName(bed.name || '')
    setFormContents(bed.contents || '')
  }

  const cancelForm = () => {
    setAdding(false)
    setEditingId(null)
    setFormName('')
    setFormContents('')
  }

  const handleSaveNew = async (e) => {
    e.preventDefault()
    if (!token) return
    setError('')
    try {
      const res = await fetch(`${API_BASE}/garden-beds`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ name: formName.trim(), contents: formContents.trim() }),
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('roles')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setError(data.msg || 'Failed to add garden bed')
        return
      }
      const created = await res.json()
      setBeds((prev) => [...prev, created])
      cancelForm()
    } catch (err) {
      setError('Could not add garden bed.')
    }
  }

  const handleSaveEdit = async (e) => {
    e.preventDefault()
    if (!token || editingId == null) return
    setError('')
    try {
      const res = await fetch(`${API_BASE}/garden-beds/${editingId}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ name: formName.trim(), contents: formContents.trim() }),
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('roles')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setError(data.msg || 'Failed to update garden bed')
        return
      }
      const updated = await res.json()
      setBeds((prev) => prev.map((b) => (b.id === editingId ? updated : b)))
      cancelForm()
    } catch (err) {
      setError('Could not update garden bed.')
    }
  }

  const handleDelete = async (id) => {
    if (!token || !window.confirm('Remove this garden bed?')) return
    try {
      const res = await fetch(`${API_BASE}/garden-beds/${id}`, {
        method: 'DELETE',
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        localStorage.removeItem('roles')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (res.ok || res.status === 204) {
        setBeds((prev) => prev.filter((b) => b.id !== id))
        if (editingId === id) cancelForm()
      }
    } catch (err) {
      setError('Could not delete garden bed.')
    }
  }

  if (!token) return null

  return (
    <div className="garden-beds-page">
      <header className="garden-beds-header">
        <div className="garden-beds-header-left">
          <img src={logo} alt="" className="garden-beds-header-logo" />
          <div className="garden-beds-header-brand">
            <h1 className="garden-beds-header-app-name">Sproutly</h1>
            <p className="garden-beds-header-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
      </header>
      <div className="garden-beds-header-sep" />
      <nav className="garden-beds-nav">
        <Link to="/dashboard" className="garden-beds-back">
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="garden-beds-main">
        <h1 className="garden-beds-title">Garden Beds</h1>
        <p className="garden-beds-subtitle">
          Add a bed (square) and write what you plan to grow in it, e.g. peas, tomatoes and cauliflower.
        </p>

        {error && (
          <div className="garden-beds-error" role="alert">
            {error}
          </div>
        )}

        {adding && (
          <form className="garden-beds-form garden-beds-form-new" onSubmit={handleSaveNew}>
            <h3 className="garden-beds-form-title">New garden bed</h3>
            <label className="garden-beds-label">
              Name <span className="garden-beds-optional">(optional)</span>
            </label>
            <input
              type="text"
              className="garden-beds-input"
              placeholder="e.g. North bed, Bed 1"
              value={formName}
              onChange={(e) => setFormName(e.target.value)}
              aria-label="Bed name"
            />
            <label className="garden-beds-label">What’s in this bed?</label>
            <textarea
              className="garden-beds-textarea"
              placeholder="e.g. peas, tomatoes and cauliflower"
              value={formContents}
              onChange={(e) => setFormContents(e.target.value)}
              rows={3}
              aria-label="Contents"
            />
            <div className="garden-beds-form-actions">
              <button type="button" className="garden-beds-btn garden-beds-btn-secondary" onClick={cancelForm}>
                Cancel
              </button>
              <button type="submit" className="garden-beds-btn garden-beds-btn-primary">
                Add bed
              </button>
            </div>
          </form>
        )}

        {!adding && (
          <button type="button" className="garden-beds-add-btn" onClick={startAdd} aria-label="Add garden bed">
            <span className="garden-beds-add-icon">+</span> Add garden bed
          </button>
        )}

        {loading ? (
          <p className="garden-beds-loading">Loading garden beds…</p>
        ) : (
          <div className="garden-beds-grid">
            {beds.map((bed) => (
              <div key={bed.id} className="garden-beds-card-wrapper">
                {editingId === bed.id ? (
                  <form
                    className="garden-beds-form garden-beds-form-edit"
                    onSubmit={handleSaveEdit}
                  >
                    <input
                      type="text"
                      className="garden-beds-input garden-beds-input-inline"
                      placeholder="Bed name (optional)"
                      value={formName}
                      onChange={(e) => setFormName(e.target.value)}
                      aria-label="Bed name"
                    />
                    <textarea
                      className="garden-beds-textarea garden-beds-textarea-inline"
                      placeholder="e.g. peas, tomatoes and cauliflower"
                      value={formContents}
                      onChange={(e) => setFormContents(e.target.value)}
                      rows={4}
                      aria-label="Contents"
                    />
                    <div className="garden-beds-form-actions">
                      <button
                        type="button"
                        className="garden-beds-btn garden-beds-btn-danger"
                        onClick={() => handleDelete(bed.id)}
                      >
                        Delete
                      </button>
                      <button type="button" className="garden-beds-btn garden-beds-btn-secondary" onClick={cancelForm}>
                        Cancel
                      </button>
                      <button type="submit" className="garden-beds-btn garden-beds-btn-primary">
                        Save
                      </button>
                    </div>
                  </form>
                ) : (
                  <div
                    className="garden-beds-card"
                    onClick={() => startEdit(bed)}
                    onKeyDown={(e) => e.key === 'Enter' && startEdit(bed)}
                    role="button"
                    tabIndex={0}
                    aria-label={`Edit ${bed.name || 'Garden bed'} ${bed.contents || '(empty)'}`}
                  >
                    <h3 className="garden-beds-card-title">
                      {bed.name && bed.name.trim() ? bed.name.trim() : 'Garden bed'}
                    </h3>
                    <p className="garden-beds-card-contents">
                      {bed.contents && bed.contents.trim() ? bed.contents.trim() : 'Tap to add what you’re growing…'}
                    </p>
                  </div>
                )}
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  )
}

export default GardenBedsPage
