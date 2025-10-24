"use client"
import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'

export default function ForgotPage() {
  const [phone, setPhone] = useState('')
  const [sent, setSent] = useState(false)

  const submit = async (e: React.FormEvent) => {
    e.preventDefault()
    try {
      const res = await fetch('/api/password/initiate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ phoneNumber: phone }) })
      const data = await res.json()
      if (data.success) { toast.success('Reset link sent to your recovery email'); setSent(true) }
      else toast.error(data.message || 'Failed to send reset email')
    } catch {
      toast.error('Network error')
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-zinc-50 p-6">
      <form className="w-full max-w-md bg-white rounded-xl p-6 border" onSubmit={submit}>
        <h1 className="text-xl font-bold mb-2">Password Recovery</h1>
        <p className="text-sm text-zinc-600 mb-4">Enter your phone number and we'll email a reset link to your verified recovery email.</p>
        <div className="space-y-3">
          <div>
            <label className="text-sm font-medium">Phone Number</label>
            <Input value={phone} onChange={e=>setPhone(e.target.value)} placeholder="+1234567890" required />
          </div>
          <Button type="submit">{sent ? 'Resend Link' : 'Send Reset Link'}</Button>
        </div>
      </form>
    </div>
  )
}
