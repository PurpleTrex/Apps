import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'
import crypto from 'crypto'
import { Resend } from 'resend'

const resend = new Resend(process.env.RESEND_API_KEY)
const BASE_URL = process.env.NEXT_PUBLIC_MATRIX_BASE || ''

export async function POST(req: NextRequest) {
  const authHeader = req.headers.get('cookie') || ''
  const match = authHeader.match(new RegExp(`${process.env.SESSION_COOKIE_NAME||'purpletalk_session'}=([^;]+)`))
  if (!match) return NextResponse.json({ success: false, message: 'Not authenticated' }, { status: 401 })
  let session: any
  try { session = JSON.parse(decodeURIComponent(match[1])) } catch { return NextResponse.json({ success: false, message: 'Invalid session' }, { status: 401 }) }

  const body = await req.json().catch(()=>({}))
  const email = String(body.email||'').trim().toLowerCase()
  if (!/^\S+@\S+\.\S+$/.test(email)) return NextResponse.json({ success: false, message: 'Invalid email' }, { status: 400 })

  const token = crypto.randomBytes(32).toString('hex')
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex')
  const expires = new Date(Date.now()+1000*60*30).toISOString()

  db.prepare(`INSERT OR REPLACE INTO recovery_emails (user_id, email, verified, verification_token_hash, verification_expires_at) VALUES (?, ?, ?, ?, ? )`)
    .run(session.userId, email, 0, tokenHash, expires)

  const link = `${BASE_URL}/account/verify-email?token=${token}&uid=${encodeURIComponent(session.userId)}`

  if (!process.env.RESEND_API_KEY) return NextResponse.json({ success: false, message: 'Email service not configured' }, { status: 500 })
  await resend.emails.send({
    from: process.env.SMTP_FROM || 'noreply@purpletalk.devit.dev',
    to: email,
    subject: 'Verify your PurpleTalk recovery email',
    html: `<p>Confirm this email for password recovery.</p><p><a href="${link}">Verify Email</a></p><p>This link expires in 30 minutes.</p>`
  })

  return NextResponse.json({ success: true })
}
