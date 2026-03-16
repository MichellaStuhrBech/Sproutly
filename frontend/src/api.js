/**
 * Base URL for API requests. In dev, use relative /api (Vite proxies to backend).
 * For production, set VITE_API_URL at build time to the backend origin (e.g. https://api.example.com).
 */
export const API_BASE = (import.meta.env.VITE_API_URL || '').replace(/\/$/, '') || '/api'
