"use client"
import { useEffect } from 'react'

const TIMEOUT_MIN = parseInt(process.env.NEXT_PUBLIC_INACTIVITY_TIMEOUT_MINUTES || process.env.INACTIVITY_TIMEOUT_MINUTES || '0', 10)

export default function IdleLogout() {
  useEffect(() => {
    if (!TIMEOUT_MIN || TIMEOUT_MIN <= 0) return
    const timeoutMs = TIMEOUT_MIN * 60 * 1000
    let last = Date.now()
    let timer: any

    const ping = () => {
      if (Date.now() - last >= timeoutMs) {
        // Auto-logout
        fetch('/api/auth', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'logout' }) })
          .finally(() => { window.location.href = '/login' })
      } else {
        timer = setTimeout(ping, 30_000)
      }
    }

    const mark = () => { last = Date.now() }
    const events = ['click','mousemove','keydown','scroll','touchstart','visibilitychange']
    events.forEach(e => window.addEventListener(e, mark, { passive: true }))
    timer = setTimeout(ping, 30_000)
    return () => {
      clearTimeout(timer)
      events.forEach(e => window.removeEventListener(e, mark))
    }
  }, [])
  return null
}
