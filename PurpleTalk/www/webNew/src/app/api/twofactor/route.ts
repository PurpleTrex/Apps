import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'
import { authenticator } from 'otplib'

export async function OPTIONS() { return NextResponse.json({}, { status: 200 }) }

export async function POST(req: NextRequest) {
  const body = await req.json().catch(()=>({}))
  const { action } = body || {}
  try {
    switch (action) {
      case 'status':
        return status(body)
      case 'initiate':
        return initiate(body)
      case 'enable':
        return enable(body)
      case 'disable':
        return disable2fa(body)
      default:
        return NextResponse.json({ success: false, message: 'Unknown action' }, { status: 400 })
    }
  } catch (e: any) {
    return NextResponse.json({ success: false, message: e.message || '2FA error' }, { status: 500 })
  }
}

async function status(input: any) {
  const { userId } = input || {}
  if (!userId) return NextResponse.json({ success: false, message: 'userId required' }, { status: 400 })
  const rec = db.prepare('SELECT enabled FROM two_factor WHERE user_id = ?').get(String(userId)) as { enabled?: number } | undefined
  return NextResponse.json({ success: true, enabled: !!(rec && rec.enabled) })
}

async function initiate(input: any) {
  const { userId } = input || {}
  if (!userId) return NextResponse.json({ success: false, message: 'userId required' }, { status: 400 })
  const secret = authenticator.generateSecret()
  const issuer = 'PurpleTalk'
  const label = `PurpleTalk:${userId}`
  const otpauth = authenticator.keyuri(label, issuer, secret)
  db.prepare('INSERT OR REPLACE INTO two_factor (user_id, secret, enabled) VALUES (?, ?, COALESCE((SELECT enabled FROM two_factor WHERE user_id = ?), 0))')
    .run(String(userId), secret, String(userId))
  return NextResponse.json({ success: true, secret, otpauth })
}

async function enable(input: any) {
  const { userId, code } = input || {}
  if (!userId || !code) return NextResponse.json({ success: false, message: 'userId and code required' }, { status: 400 })
  const rec = db.prepare('SELECT secret FROM two_factor WHERE user_id = ?').get(String(userId)) as { secret?: string } | undefined
  if (!rec?.secret) return NextResponse.json({ success: false, message: '2FA not initiated' }, { status: 400 })
  const ok = authenticator.check(String(code), rec.secret)
  if (!ok) return NextResponse.json({ success: false, message: 'Invalid code' }, { status: 400 })
  db.prepare('UPDATE two_factor SET enabled = 1 WHERE user_id = ?').run(String(userId))
  return NextResponse.json({ success: true })
}

async function disable2fa(input: any) {
  const { userId, code } = input || {}
  if (!userId || !code) return NextResponse.json({ success: false, message: 'userId and code required' }, { status: 400 })
  const rec = db.prepare('SELECT secret FROM two_factor WHERE user_id = ?').get(String(userId)) as { secret?: string } | undefined
  if (!rec?.secret) return NextResponse.json({ success: false, message: '2FA not enabled' }, { status: 400 })
  const ok = authenticator.check(String(code), rec.secret)
  if (!ok) return NextResponse.json({ success: false, message: 'Invalid code' }, { status: 400 })
  db.prepare('UPDATE two_factor SET enabled = 0 WHERE user_id = ?').run(String(userId))
  return NextResponse.json({ success: true })
}
