"use client"
import { useEffect, useState } from 'react'
import { useRouter } from 'next/navigation'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function DashboardPage() {
  const [session, setSession] = useState<any>(null)
  const [stats, setStats] = useState({ roomCount: 0, deviceCount: 1, unread: 0 })
  const router = useRouter()

  useEffect(() => {
    fetch('/api/session').then(r=>r.json()).then(s => {
      if (!s.authenticated) router.replace('/login')
      setSession(s)
      fetch('/api/matrix', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.accessToken}` }, body: JSON.stringify({ action: 'getUserInfo' }) })
        .then(r=>r.json()).then(d => d.success && setStats(prev => ({ ...prev, roomCount: d.roomCount })))
    })
  }, [router])

  const logout = async () => {
    await fetch('/api/auth', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'logout' }) })
    router.replace('/login')
  }

  return (
    <div className="min-h-screen bg-zinc-50 p-6">
      <header className="max-w-6xl mx-auto flex items-center justify-between py-4">
        <div className="text-xl font-bold">🔒 PurpleTalk</div>
        <div className="flex items-center gap-2">
          <Link href="/chat"><Button variant="secondary">Messages</Button></Link>
          <Link href="/account"><Button variant="secondary">Account</Button></Link>
          <Button variant="destructive" onClick={logout}>Logout</Button>
        </div>
      </header>
      <main className="max-w-6xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-4">
        <Card className="p-6"><div className="text-sm text-zinc-600">Connected Devices</div><div className="text-3xl font-bold">{stats.deviceCount}</div></Card>
        <Card className="p-6"><div className="text-sm text-zinc-600">Active Conversations</div><div className="text-3xl font-bold">{stats.roomCount}</div></Card>
        <Card className="p-6"><div className="text-sm text-zinc-600">Unread Messages</div><div className="text-3xl font-bold">{stats.unread}</div></Card>
      </main>
    </div>
  )
}
