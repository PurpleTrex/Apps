"use client"
import { useEffect, useState } from 'react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { toast } from 'sonner'

export default function AccountPage() {
  const [session, setSession] = useState<any>(null)
  const [displayName, setDisplayName] = useState('')
  const [deviceId, setDeviceId] = useState('Unknown')
  const [recoveryEmail, setRecoveryEmail] = useState('')
  const [verifyToken, setVerifyToken] = useState('')
  const [twofaEnabled, setTwofaEnabled] = useState<boolean>(false)
  const [twofaSecret, setTwofaSecret] = useState<string>('')
  const [twofaUri, setTwofaUri] = useState<string>('')
  const [twofaCode, setTwofaCode] = useState<string>('')
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')

  useEffect(() => {
    fetch('/api/session').then(r=>r.json()).then(s => {
      if (!s.authenticated) location.href = '/login'
      setSession(s)
      setDeviceId(s.deviceId || 'Unknown')
      fetch('/api/matrix', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${s.accessToken}` }, body: JSON.stringify({ action: 'getProfile', userId: s.userId }) })
        .then(r=>r.json()).then(d => { if (d.success && d.displayName) setDisplayName(d.displayName) })
      // Load recovery email if any
      fetch('/api/recovery/status', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ userId: s.userId }) })
        .then(r=>r.json()).then(d => { if (d.success && d.email) setRecoveryEmail(d.email) })
      // Load 2FA status
      fetch('/api/twofactor', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'status', userId: s.userId }) })
        .then(r=>r.json()).then(d => { if (d.success) setTwofaEnabled(!!d.enabled) })
    })
  }, [])

  const save = async () => {
    const res = await fetch('/api/matrix', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${session.accessToken}` }, body: JSON.stringify({ action: 'updateProfile', displayName }) })
    const data = await res.json()
    if (data.success) toast.success('Profile updated')
    else toast.error(data.message || 'Failed to update profile')
  }

  const changePassword = async () => {
    if (newPassword.length < 8) return toast.error('New password must be at least 8 characters')
    const res = await fetch('/api/matrix', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${session.accessToken}` }, body: JSON.stringify({ action: 'changePassword', currentPassword, newPassword }) })
    const data = await res.json()
    if (data.success) { toast.success('Password changed. You may need to sign in again on other devices.'); setCurrentPassword(''); setNewPassword('') }
    else toast.error(data.message || 'Failed to change password')
  }

  const deleteAccount = async () => {
    if (!confirm('Type DELETE to confirm account deletion') || prompt('Type "DELETE" to confirm:') !== 'DELETE') return
    const res = await fetch('/api/matrix', { method: 'POST', headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${session.accessToken}` }, body: JSON.stringify({ action: 'deleteAccount' }) })
    const data = await res.json()
    if (data.success) { toast.success('Account deleted'); await fetch('/api/session', { method: 'DELETE' }); location.href = '/' }
    else toast.error(data.message || 'Failed to delete account')
  }

  const sendVerification = async () => {
    const res = await fetch('/api/recovery/initiate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ email: recoveryEmail }) })
    const data = await res.json()
    if (data.success) toast.success('Verification email sent')
    else toast.error(data.message || 'Failed to send email')
  }

  const confirmVerification = async () => {
    const res = await fetch('/api/recovery/confirm', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ token: verifyToken, userId: session.userId }) })
    const data = await res.json()
    if (data.success) toast.success('Recovery email verified')
    else toast.error(data.message || 'Verification failed')
  }

  const start2fa = async () => {
    const res = await fetch('/api/twofactor', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'initiate', userId: session.userId }) })
    const data = await res.json()
    if (data.success) { setTwofaSecret(data.secret); setTwofaUri(data.otpauth); toast.message('Scan the QR with your authenticator app') }
    else toast.error(data.message || 'Failed to initiate')
  }

  const enable2fa = async () => {
    const res = await fetch('/api/twofactor', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'enable', userId: session.userId, code: twofaCode }) })
    const data = await res.json()
    if (data.success) { setTwofaEnabled(true); setTwofaCode(''); toast.success('Two-factor enabled') }
    else toast.error(data.message || 'Invalid code')
  }

  const disable2fa = async () => {
    const code = prompt('Enter a current 2FA code to disable') || ''
    if (!code) return
    const res = await fetch('/api/twofactor', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'disable', userId: session.userId, code }) })
    const data = await res.json()
    if (data.success) { setTwofaEnabled(false); setTwofaSecret(''); setTwofaUri(''); toast.success('Two-factor disabled') }
    else toast.error(data.message || 'Failed to disable')
  }

  return (
    <div className="min-h-screen bg-zinc-50 p-6">
      <div className="max-w-2xl mx-auto">
        <h1 className="text-2xl font-bold mb-4">Account Settings</h1>
        <div className="space-y-4 bg-white p-6 rounded-xl border">
          <div>
            <label className="text-sm font-medium">Matrix ID</label>
            <Input value={session?.userId || ''} disabled />
          </div>
          <div>
            <label className="text-sm font-medium">Display Name</label>
            <Input value={displayName} onChange={e=>setDisplayName(e.target.value)} placeholder="Enter display name" />
          </div>
          <div>
            <label className="text-sm font-medium">Device ID</label>
            <Input value={deviceId} disabled />
          </div>
          <div className="flex gap-2">
            <Button onClick={save}>Update Profile</Button>
            <Button variant="destructive" onClick={deleteAccount}>Delete Account</Button>
          </div>
        </div>
        <div className="space-y-4 bg-white p-6 rounded-xl border mt-6">
          <h2 className="text-xl font-semibold">Password Recovery Email</h2>
          <div>
            <label className="text-sm font-medium">Recovery Email</label>
            <Input value={recoveryEmail} onChange={e=>setRecoveryEmail(e.target.value)} placeholder="you@example.com" />
          </div>
          <div className="flex gap-2">
            <Button onClick={sendVerification}>Send Verification</Button>
            <Input className="max-w-xs" placeholder="Enter code from email link or paste token" value={verifyToken} onChange={e=>setVerifyToken(e.target.value)} />
            <Button variant="secondary" onClick={confirmVerification}>Confirm</Button>
          </div>
        </div>
        <div className="space-y-4 bg-white p-6 rounded-xl border mt-6">
          <h2 className="text-xl font-semibold">Change Password</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <label className="text-sm font-medium">Current Password</label>
              <Input type="password" value={currentPassword} onChange={e=>setCurrentPassword(e.target.value)} />
            </div>
            <div>
              <label className="text-sm font-medium">New Password</label>
              <Input type="password" value={newPassword} onChange={e=>setNewPassword(e.target.value)} />
            </div>
          </div>
          <div>
            <Button onClick={changePassword}>Update Password</Button>
          </div>
        </div>
        <div className="space-y-4 bg-white p-6 rounded-xl border mt-6">
          <h2 className="text-xl font-semibold">Two-Factor Authentication (TOTP)</h2>
          {!twofaEnabled ? (
            <div className="space-y-3">
              <p className="text-sm text-zinc-600">Add an extra layer of security by requiring a one-time code from your authenticator app when signing in.</p>
              {twofaUri ? (
                <div className="space-y-3">
                  <div className="flex items-center gap-4">
                    <img src={`/api/qr?value=${encodeURIComponent(twofaUri)}`} alt="2FA QR" className="w-40 h-40 border rounded" />
                    <div className="text-sm">
                      <div className="font-medium">Secret</div>
                      <div className="font-mono break-all">{twofaSecret}</div>
                    </div>
                  </div>
                  <div className="flex items-end gap-2">
                    <div className="flex-1">
                      <label className="text-sm font-medium">Enter 6-digit code</label>
                      <Input value={twofaCode} onChange={e=>setTwofaCode(e.target.value)} placeholder="123456" />
                    </div>
                    <Button onClick={enable2fa}>Enable 2FA</Button>
                  </div>
                </div>
              ) : (
                <Button onClick={start2fa}>Set up 2FA</Button>
              )}
            </div>
          ) : (
            <div className="flex items-center justify-between">
              <div>
                <div className="font-medium">2FA is enabled</div>
                <div className="text-sm text-zinc-600">You will be prompted for a code when signing in.</div>
              </div>
              <Button variant="destructive" onClick={disable2fa}>Disable</Button>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
