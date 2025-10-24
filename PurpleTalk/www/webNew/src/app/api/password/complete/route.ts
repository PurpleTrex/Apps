import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'
import crypto from 'crypto'

const ADMIN_TOKEN = process.env.SYNAPSE_ADMIN_TOKEN || ''
const MATRIX_SERVER = process.env.MATRIX_SERVER || ''

export async function POST(req: NextRequest) {
  const body = await req.json().catch(()=>({}))
  const { token, userId, newPassword } = body || {}
  if (!token || !userId || !newPassword) return NextResponse.json({ success: false, message: 'token, userId, newPassword required' }, { status: 400 })
  if (String(newPassword).length < 8) return NextResponse.json({ success: false, message: 'Password must be at least 8 characters' }, { status: 400 })

  const rec = db.prepare('SELECT token_hash, expires_at, consumed_at FROM password_resets WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(String(userId)) as any
  if (!rec) return NextResponse.json({ success: false, message: 'No reset requested' }, { status: 400 })
  if (rec.consumed_at) return NextResponse.json({ success: false, message: 'Token already used' }, { status: 400 })
  const tokenHash = crypto.createHash('sha256').update(String(token)).digest('hex')
  if (tokenHash !== rec.token_hash) return NextResponse.json({ success: false, message: 'Invalid token' }, { status: 400 })
  if (new Date(rec.expires_at).getTime() < Date.now()) return NextResponse.json({ success: false, message: 'Token expired' }, { status: 400 })

  // Call Synapse Admin API to set password
  if (!ADMIN_TOKEN) return NextResponse.json({ success: false, message: 'Admin token not configured' }, { status: 500 })
  const url = `${MATRIX_SERVER}/_synapse/admin/v2/users/${encodeURIComponent(String(userId))}/password`
  const resp = await fetch(url, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${ADMIN_TOKEN}` },
    body: JSON.stringify({ new_password: String(newPassword), logout_devices: true })
  })
  if (!resp.ok) {
    const text = await resp.text()
    return NextResponse.json({ success: false, message: 'Failed to set password', details: text }, { status: 500 })
  }

  db.prepare('UPDATE password_resets SET consumed_at = datetime("now") WHERE user_id = ? AND token_hash = ?').run(String(userId), tokenHash)
  return NextResponse.json({ success: true })
}
