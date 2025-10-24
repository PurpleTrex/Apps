import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'
import crypto from 'crypto'

export async function POST(req: NextRequest) {
  const body = await req.json().catch(()=>({}))
  const { token, userId } = body || {}
  if (!token || !userId) return NextResponse.json({ success: false, message: 'token and userId required' }, { status: 400 })
  const tokenHash = crypto.createHash('sha256').update(String(token)).digest('hex')
  const rec = db.prepare('SELECT email, verification_token_hash, verification_expires_at FROM recovery_emails WHERE user_id = ?').get(String(userId)) as any
  if (!rec) return NextResponse.json({ success: false, message: 'No recovery email setup' }, { status: 400 })
  if (rec.verification_token_hash !== tokenHash) return NextResponse.json({ success: false, message: 'Invalid token' }, { status: 400 })
  if (new Date(rec.verification_expires_at).getTime() < Date.now()) return NextResponse.json({ success: false, message: 'Token expired' }, { status: 400 })
  db.prepare('UPDATE recovery_emails SET verified = 1, verification_token_hash = NULL, verification_expires_at = NULL WHERE user_id = ?').run(String(userId))
  return NextResponse.json({ success: true })
}
