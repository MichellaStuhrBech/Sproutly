import { Routes, Route } from 'react-router-dom'
import LandingPage from './components/LandingPage'
import LoginPage from './components/LoginPage'
import CreateAccountPage from './components/CreateAccountPage'
import DashboardPage from './components/DashboardPage'
import TodoPage from './components/TodoPage'
import SowingPage from './components/SowingPage'
import ChatPage from './components/ChatPage'
import AdminPage from './components/AdminPage'
import HelpPage from './components/HelpPage'
import PlantSearchPage from './components/PlantSearchPage'
import './App.css'

function App() {
  return (
    <Routes>
      <Route path="/" element={<LandingPage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/create-account" element={<CreateAccountPage />} />
      <Route path="/dashboard" element={<DashboardPage />} />
      <Route path="/todo" element={<TodoPage />} />
      <Route path="/sowing" element={<SowingPage />} />
      <Route path="/chat" element={<ChatPage />} />
      <Route path="/plant-search" element={<PlantSearchPage />} />
      <Route path="/admin" element={<AdminPage />} />
      <Route path="/help" element={<HelpPage />} />
    </Routes>
  )
}

export default App
