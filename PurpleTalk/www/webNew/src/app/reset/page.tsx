"use client"
import { Suspense, useEffect, useState } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { Input } from '@/components/ui/input'
import { Button } from '@/components/ui/button'
import { toast } from 'sonner'

function ResetBody() {
  const params = useSearchParams()
  const router = useRouter()
  const [token, setToken] = useState('')
  const [userId, setUserId] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')

  useEffect(() => {
    const t = params.get('token') || ''
    const uid = params.get('uid') || ''
    setToken(t); setUserId(uid)
  }, [params])

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (password.length < 8) return toast.error('Password must be at least 8 characters')
    if (password !== confirm) return toast.error('Passwords do not match')
    try {
      const res = await fetch('/api/password/complete', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ token, userId, newPassword: password }) })
      const data = await res.json()
      if (data.success) { toast.success('Password reset. Please log in.'); router.push('/login') }
      else toast.error(data.message || 'Failed to reset password')
    } catch {
      toast.error('Network error')
    }
  }

  return (
    <form className="w-full max-w-md bg-white rounded-xl p-6 border" onSubmit={submit}>
      <h1 className="text-xl font-bold mb-2">Set a new password</h1>
      <div className="space-y-3">
        <Input type="password" placeholder="New password" value={password} onChange={e=>setPassword(e.target.value)} required />
        <Input type="password" placeholder="Confirm new password" value={confirm} onChange={e=>setConfirm(e.target.value)} required />
        <Button type="submit">Reset Password</Button>
      </div>
    </form>
  )
}

export default function ResetPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-zinc-50 p-6">
      <Suspense fallback={<div className="w-full max-w-md rounded-xl p-6 border bg-white">Loading reset form...</div>}>
        <ResetBody />
      </Suspense>
    </div>
  )
}
