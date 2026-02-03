import { useState } from 'react'
import { Link } from 'react-router-dom'
import logo from '../logo/logo.png'
import './TodoPage.css'

const initialTasks = [
  { id: '1', text: 'Water the tomato plants', completed: false },
  { id: '2', text: 'Weed the vegetable garden', completed: false },
  { id: '3', text: 'Prune the rose bushes', completed: true },
]

function TodoPage() {
  const [tasks, setTasks] = useState(initialTasks)
  const [newTaskText, setNewTaskText] = useState('')

  const completedCount = tasks.filter((t) => t.completed).length
  const totalCount = tasks.length

  const handleAdd = (e) => {
    e.preventDefault()
    const text = newTaskText.trim()
    if (!text) return
    setTasks((prev) => [
      ...prev,
      { id: String(Date.now()), text, completed: false },
    ])
    setNewTaskText('')
  }

  const handleToggle = (id) => {
    setTasks((prev) =>
      prev.map((task) => (task.id === id ? { ...task, completed: !task.completed } : task))
    )
  }

  const handleDelete = (id) => {
    setTasks((prev) => prev.filter((task) => task.id !== id))
  }

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

          <form className="todo-add-form" onSubmit={handleAdd}>
            <input
              type="text"
              className="todo-add-input"
              placeholder="Add a new task..."
              value={newTaskText}
              onChange={(e) => setNewTaskText(e.target.value)}
              aria-label="New task"
            />
            <button type="submit" className="todo-add-btn">
              <span className="todo-add-icon">+</span> Add
            </button>
          </form>

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
        </div>
      </main>
    </div>
  )
}

export default TodoPage
