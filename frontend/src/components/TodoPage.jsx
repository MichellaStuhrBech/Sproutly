import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './TodoPage.css'

const API_BASE = '/api'

function TodoPage() {
  const navigate = useNavigate()
  const [tasks, setTasks] = useState([])
  const [newTaskText, setNewTaskText] = useState('')
  const [loading, setLoading] = useState(true)
  const [adding, setAdding] = useState(false)
  const [error, setError] = useState('')

  const token = localStorage.getItem('token')

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in to view your todo list.' } })
      return
    }
    loadTasks()
  }, [token, navigate])

  const loadTasks = async () => {
    if (!token) return
    setError('')
    try {
      const res = await fetch(`${API_BASE}/tasks`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }
      if (!res.ok) throw new Error('Failed to load tasks')
      const data = await res.json()
      setTasks(
        (data || []).map((t) => ({
          id: String(t.id),
          text: t.title,
          notes: t.notes || '',
          completed: false,
        }))
      )
    } catch (err) {
      setError('Could not load tasks.')
    } finally {
      setLoading(false)
    }
  }

  const completedCount = tasks.filter((t) => t.completed).length
  const totalCount = tasks.length

  const handleAdd = async (e) => {
    e.preventDefault()
    const text = newTaskText.trim()
    if (!text || !token) return

    setAdding(true)
    setError('')
    try {
      const res = await fetch(`${API_BASE}/tasks`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ title: text, notes: '' }),
      })

      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }

      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setError(data.msg || 'Failed to add task')
        return
      }

      const created = await res.json()
      setTasks((prev) => [
        ...prev,
        {
          id: String(created.id),
          text: created.title,
          notes: created.notes || '',
          completed: false,
        },
      ])
      setNewTaskText('')
    } catch (err) {
      setError('Could not add task. Is the backend running?')
    } finally {
      setAdding(false)
    }
  }

  const handleToggle = (id) => {
    setTasks((prev) =>
      prev.map((task) => (task.id === id ? { ...task, completed: !task.completed } : task))
    )
  }

  const handleDelete = (id) => {
    setTasks((prev) => prev.filter((task) => task.id !== id))
  }

  if (!token) return null

  return (
    <div className="todo-page">
      <header className="todo-header">
        <div className="todo-header-left">
          <img src={logo} alt="" className="todo-header-logo" />
          <div className="todo-header-brand">
            <h1 className="todo-header-app-name">Sproutly</h1>
            <p className="todo-header-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
      </header>
      <div className="todo-header-sep" />
      <nav className="todo-nav">
        <Link to="/dashboard" className="todo-back">
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="todo-main">
        <div className="todo-card">
          <h1 className="todo-title">Garden To Do List</h1>
          <p className="todo-subtitle">
            Manage your garden tasks and activities
          </p>

          {error && (
            <div className="todo-error" role="alert">
              {error}
            </div>
          )}

          <form className="todo-add-form" onSubmit={handleAdd}>
            <input
              type="text"
              className="todo-add-input"
              placeholder="Add a new task..."
              value={newTaskText}
              onChange={(e) => setNewTaskText(e.target.value)}
              aria-label="New task"
              disabled={adding}
            />
            <button type="submit" className="todo-add-btn" disabled={adding}>
              <span className="todo-add-icon">+</span> {adding ? 'Adding…' : 'Add'}
            </button>
          </form>

          {loading ? (
            <p className="todo-loading">Loading tasks…</p>
          ) : (
            <>
              <ul className="todo-list">
                {tasks.map((task) => (
                  <li
                    key={task.id}
                    className={`todo-item ${task.completed ? 'todo-item--completed' : ''}`}
                  >
                    <label className="todo-item-label">
                      <input
                        type="checkbox"
                        className="todo-checkbox"
                        checked={task.completed}
                        onChange={() => handleToggle(task.id)}
                        aria-label={task.text}
                      />
                      <span className="todo-item-text">{task.text}</span>
                    </label>
                    <button
                      type="button"
                      className="todo-delete-btn"
                      onClick={() => handleDelete(task.id)}
                      aria-label={`Delete ${task.text}`}
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
                  </li>
                ))}
              </ul>

              <p className="todo-summary">
                {completedCount} of {totalCount} tasks completed
              </p>
            </>
          )}
        </div>
      </main>
    </div>
  )
}

export default TodoPage
