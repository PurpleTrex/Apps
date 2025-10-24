import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'
import crypto from 'crypto'
import { Resend } from 'resend'
import { phoneToMatrixId } from '@/lib/matrix'

const resend = new Resend(process.env.RESEND_API_KEY)
const BASE_URL = process.env.NEXT_PUBLIC_MATRIX_BASE || ''

export async function POST(req: NextRequest) {
  const body = await req.json().catch(()=>({}))
  const phoneNumber = String(body.phoneNumber||'')
  if (!phoneNumber) return NextResponse.json({ success: false, message: 'phoneNumber required' }, { status: 400 })

  const userId = phoneToMatrixId(phoneNumber)
  const rec = db.prepare('SELECT email, verified FROM recovery_emails WHERE user_id = ?').get(userId) as { email?: string; verified?: number } | undefined
  if (!rec?.email || !rec?.verified) return NextResponse.json({ success: false, message: 'No verified recovery email on file' }, { status: 400 })

  const token = crypto.randomBytes(32).toString('hex')
  const tokenHash = crypto.createHash('sha256').update(token).digest('hex')
  const expires = new Date(Date.now()+1000*60*30).toISOString()
  db.prepare('INSERT INTO password_resets (user_id, token_hash, expires_at, ip, ua) VALUES (?, ?, ?, ?, ?)')
    .run(userId, tokenHash, expires, req.headers.get('x-forwarded-for')||req.headers.get('x-real-ip')||'', req.headers.get('user-agent')||'')

  const link = `${BASE_URL}/reset?token=${token}&uid=${encodeURIComponent(userId)}`

  if (!process.env.RESEND_API_KEY) return NextResponse.json({ success: false, message: 'Email service not configured' }, { status: 500 })
  await resend.emails.send({
    from: process.env.SMTP_FROM || 'noreply@purpletalk.devit.dev',
    to: rec.email!,
    subject: 'PurpleTalk password reset',
    html: `<p>Click to reset your password:</p><p><a href="${link}">Reset Password</a></p><p>This link expires in 30 minutes.</p>`
  })

  return NextResponse.json({ success: true })
}
