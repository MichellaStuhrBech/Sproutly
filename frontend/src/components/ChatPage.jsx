import { useState, useEffect, useRef } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import logo from '../logo/logo.png'
import './ChatPage.css'

const API_BASE = '/api'

function ChatPage() {
  const navigate = useNavigate()
  const token = localStorage.getItem('token')
  const [messages, setMessages] = useState([])
  const [input, setInput] = useState('')
  const [sending, setSending] = useState(false)
  const [error, setError] = useState('')
  const messagesEndRef = useRef(null)

  useEffect(() => {
    if (!token) {
      navigate('/login', { state: { message: 'Please log in to use the plant chat.' } })
      return
    }
  }, [token, navigate])

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  const handleSend = async (e) => {
    e.preventDefault()
    const text = input.trim()
    if (!text || !token || sending) return

    const userMessage = { role: 'user', content: text }
    setMessages((prev) => [...prev, userMessage])
    setInput('')
    setError('')
    setSending(true)

    try {
      const res = await fetch(`${API_BASE}/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ message: text }),
      })

      if (res.status === 401) {
        localStorage.removeItem('token')
        localStorage.removeItem('email')
        navigate('/login', { state: { message: 'Please log in again.' } })
        return
      }

      if (!res.ok) {
        const data = await res.json().catch(() => ({}))
        setError(data.msg || 'Failed to get a reply.')
        setMessages((prev) => prev.filter((m) => m !== userMessage))
        return
      }

      const data = await res.json()
      const reply = data.reply ?? 'No reply received.'
      setMessages((prev) => [...prev, { role: 'assistant', content: reply }])
    } catch (err) {
      setError('Could not reach the chat. Is the backend running?')
      setMessages((prev) => prev.filter((m) => m !== userMessage))
    } finally {
      setSending(false)
    }
  }

  if (!token) return null

  return (
    <div className="chat-page">
      <header className="chat-header">
        <div className="chat-header-left">
          <img src={logo} alt="" className="chat-header-logo" />
          <div className="chat-header-brand">
            <h1 className="chat-header-app-name">Sproutly</h1>
            <p className="chat-header-tagline">PLAN. GROW. HARVEST.</p>
          </div>
        </div>
      </header>
      <div className="chat-header-sep" />
      <nav className="chat-nav">
        <Link to="/dashboard" className="chat-back">
          ← Back to Dashboard
        </Link>
      </nav>

      <main className="chat-main">
        <div className="chat-card">
          <h1 className="chat-title">Plant Chat</h1>
          <p className="chat-subtitle">
            Ask questions about plants and gardening. Answers use data from Trefle and Perenual.
          </p>

          {error && (
            <div className="chat-error" role="alert">
              {error}
            </div>
          )}

          <div className="chat-messages" aria-live="polite">
            {messages.length === 0 && (
              <p className="chat-placeholder">
                Type a question below, e.g. “When should I sow tomatoes?” or “What is Monstera?”
              </p>
            )}
            {messages.map((msg, i) => (
              <div
                key={i}
                className={`chat-bubble chat-bubble--${msg.role}`}
                aria-label={msg.role === 'user' ? 'Your message' : 'Assistant reply'}
              >
                <span className="chat-bubble-role">
                  {msg.role === 'user' ? 'You' : 'Sproutly'}
                </span>
                <div className="chat-bubble-content">{msg.content}</div>
              </div>
            ))}
            {sending && (
              <div className="chat-bubble chat-bubble--assistant chat-bubble--typing">
                <span className="chat-bubble-role">Sproutly</span>
                <div className="chat-bubble-content">
                  <span className="chat-dots">
                    <span>.</span><span>.</span><span>.</span>
                  </span>
                </div>
              </div>
            )}
            <div ref={messagesEndRef} />
          </div>

          <form className="chat-form" onSubmit={handleSend}>
            <input
              type="text"
              className="chat-input"
              placeholder="Ask about plants or gardening..."
              value={input}
              onChange={(e) => setInput(e.target.value)}
              aria-label="Your message"
              disabled={sending}
            />
            <button type="submit" className="chat-send-btn" disabled={sending || !input.trim()}>
              {sending ? 'Sending…' : 'Send'}
            </button>
          </form>
        </div>
      </main>
    </div>
  )
}

export default ChatPage
