"use client"
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'
import Link from 'next/link'

export default function LoginPage() {
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [totp, setTotp] = useState('')
  const [loading, setLoading] = useState(false)
  const router = useRouter()

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    setLoading(true)
    try {
      const res = await fetch('/api/auth', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'login', phoneNumber: phone, password, totpCode: totp || undefined }),
      })
      const data = await res.json()
      if (data.success) {
        toast.success('Login successful')
        router.push('/dashboard')
      } else if (data.require2fa) {
        toast.error('2FA code required or invalid')
      } else {
        toast.error(data.message || 'Login failed')
      }
    } catch (e: any) {
      toast.error('Network error')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-gradient-to-br from-indigo-500 to-purple-600 p-6">
      <div className="w-full max-w-md bg-white rounded-2xl p-6 shadow-lg">
        <h1 className="text-2xl font-bold mb-2">Welcome Back</h1>
        <p className="text-zinc-600 mb-6">Sign in to your account</p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Phone Number</label>
            <Input type="tel" value={phone} onChange={e=>setPhone(e.target.value)} placeholder="+1234567890" required />
          </div>
          <div>
            <label className="text-sm font-medium">Password</label>
            <Input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" required />
          </div>
          <div>
            <label className="text-sm font-medium">2FA Code (if enabled)</label>
            <Input type="text" value={totp} onChange={e=>setTotp(e.target.value)} placeholder="123456" />
          </div>
          <Button className="w-full" disabled={loading}>
            {loading ? 'Signing in...' : 'Sign In'}
          </Button>
        </form>
        <div className="text-sm text-zinc-600 mt-4 flex justify-between">
          <Link href="/forgot">Forgot password?</Link>
          <Link href="/register" className="font-medium">Create account</Link>
        </div>
      </div>
    </div>
  )
}
