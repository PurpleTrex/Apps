"use client"
import { Suspense, useEffect, useState } from 'react'
import { useSearchParams, useRouter } from 'next/navigation'
import { Button } from '@/components/ui/button'

function VerifyBody() {
  const params = useSearchParams()
  const router = useRouter()
  const [status, setStatus] = useState<'pending'|'success'|'error'>('pending')
  const [message, setMessage] = useState('Verifying...')

  useEffect(() => {
    const token = params.get('token')
    const uid = params.get('uid')
    if (!token || !uid) { setStatus('error'); setMessage('Missing token or user ID.'); return }
    ;(async () => {
      try {
        const res = await fetch('/api/recovery/confirm', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ token, userId: uid }) })
        const data = await res.json()
        if (data.success) { setStatus('success'); setMessage('Recovery email verified. You can now use password reset.'); }
        else { setStatus('error'); setMessage(data.message || 'Verification failed') }
      } catch {
        setStatus('error'); setMessage('Network error')
      }
    })()
  }, [params])

  return (
    <div className="w-full max-w-md rounded-xl p-6 border mx-auto" style={{ background: status==='success' ? '#ecfdf5' : status==='error' ? '#fef2f2' : 'white', borderColor: status==='success' ? '#a7f3d0' : status==='error' ? '#fecaca' : undefined }}>
      <h1 className="text-xl font-bold mb-2">Email Verification</h1>
      <p className="mb-4">{message}</p>
      <Button onClick={()=>router.push('/account')}>Back to Account</Button>
    </div>
  )
}

export default function VerifyEmailPage() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-zinc-50 p-6">
      <Suspense fallback={<div className="w-full max-w-md rounded-xl p-6 border bg-white">Verifying...</div>}>
        <VerifyBody />
      </Suspense>
    </div>
  )
}
