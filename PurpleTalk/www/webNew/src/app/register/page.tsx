"use client"
import { useState } from 'react'
import { useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'
import Link from 'next/link'

export default function RegisterPage() {
  const [phone, setPhone] = useState('')
  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [loading, setLoading] = useState(false)
  const router = useRouter()

  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault()
    if (password.length < 8) return toast.error('Password must be at least 8 characters')
    if (password !== confirm) return toast.error('Passwords do not match')
    setLoading(true)
    try {
      const res = await fetch('/api/auth', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action: 'register', phoneNumber: phone, password }),
      })
      const data = await res.json()
      if (data.success) {
        toast.success('Account created!')
        router.push('/dashboard')
      } else {
        toast.error(data.message || 'Registration failed')
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
        <h1 className="text-2xl font-bold mb-2">Create Account</h1>
        <p className="text-zinc-600 mb-6">Join the secure messaging network</p>
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-medium">Phone Number</label>
            <Input type="tel" value={phone} onChange={e=>setPhone(e.target.value)} placeholder="+1234567890" required />
          </div>
          <div>
            <label className="text-sm font-medium">Password</label>
            <Input type="password" value={password} onChange={e=>setPassword(e.target.value)} placeholder="Password" required />
            <p className="text-xs text-zinc-500 mt-1">Minimum 8 characters</p>
          </div>
          <div>
            <label className="text-sm font-medium">Confirm Password</label>
            <Input type="password" value={confirm} onChange={e=>setConfirm(e.target.value)} placeholder="Confirm password" required />
          </div>
          <Button className="w-full" disabled={loading}>
            {loading ? 'Creating...' : 'Create Account'}
          </Button>
        </form>
        <div className="text-sm text-zinc-600 mt-4 text-right">
          <Link href="/login" className="font-medium">Already have an account? Sign in</Link>
        </div>
      </div>
    </div>
  )
}
