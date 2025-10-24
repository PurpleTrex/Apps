import { NextRequest, NextResponse } from 'next/server'
import { MATRIX_DOMAIN, matrixRequest, phoneToMatrixId, phoneToMatrixUsername } from '@/lib/matrix'
import db from '@/lib/db'
import { totp } from 'otplib'
import { SignJWT } from 'jose'

const SESSION_COOKIE = process.env.SESSION_COOKIE_NAME || 'purpletalk_session'
const SESSION_MAX_AGE = parseInt(process.env.SESSION_MAX_AGE || '2592000', 10) // 30d
const JWT_SECRET = (process.env.JWT_SECRET || 'change_me_to_a_long_random_secret')
const JWT_KEY = new TextEncoder().encode(JWT_SECRET)

export async function OPTIONS() {
  return NextResponse.json({}, { status: 200 })
}

export async function POST(req: NextRequest) {
  const body = await req.json().catch(() => ({}))
  const action = body.action as string

  try {
    switch (action) {
      case 'ping':
        return await ping()
      case 'register':
        return await register(body)
      case 'login':
        return await login(body)
      case 'enable2fa':
        return await enable2fa(body)
      case 'verify2fa':
        return await verify2fa(body)
      case 'logout':
        return await logout()
      default:
        return NextResponse.json({ success: false, message: 'Unknown action' }, { status: 400 })
    }
  } catch (e: any) {
    return NextResponse.json({ success: false, message: e.message || 'Server error' }, { status: 500 })
  }
}

async function ping() {
  const versions = await matrixRequest('/_matrix/client/versions', 'GET')
  if (versions?.versions) {
    return NextResponse.json({ success: true, message: 'Server connected', versions: versions.versions })
  }
  return NextResponse.json({ success: false, message: 'Server unreachable' }, { status: 503 })
}

async function register(input: any) {
  const { phoneNumber, password } = input || {}
  if (!phoneNumber || !password || String(password).length < 8) {
    return NextResponse.json({ success: false, message: 'Phone and password required (min 8 chars)' }, { status: 400 })
  }
  const username = phoneToMatrixUsername(phoneNumber)
  const requestBody = {
    username,
    password,
    initial_device_display_name: 'PurpleTalk Web',
    auth: { type: 'm.login.dummy' },
  }
  const res = await matrixRequest('/_matrix/client/v3/register', 'POST', requestBody)
  if (res?.access_token) {
    const matrixId = res.user_id
    db.prepare(`INSERT OR REPLACE INTO user_mappings (phone_number, matrix_username, matrix_id, last_login) VALUES (?, ?, ?, datetime('now'))`).run(
      String(phoneNumber).replace(/[^0-9+]/g, ''),
      username,
      matrixId,
    )
    // Set cookie session
    const cookie = await setSession(res.access_token, res.user_id, res.device_id)
    const response = NextResponse.json({ success: true, message: 'Registration successful', userId: res.user_id, deviceId: res.device_id })
    response.headers.set('Set-Cookie', cookie)
    return response
  }
  const msg = res?.error || 'Registration failed'
  return NextResponse.json({ success: false, message: msg }, { status: 400 })
}

async function login(input: any) {
  const { phoneNumber, password, totpCode } = input || {}
  if (!phoneNumber || !password) {
    return NextResponse.json({ success: false, message: 'Phone and password required' }, { status: 400 })
  }
  // 2FA check if enabled
  const matrixId = phoneToMatrixId(phoneNumber)
  const twofa = db.prepare('SELECT secret, enabled FROM two_factor WHERE user_id = ?').get(matrixId) as { secret?: string; enabled?: number } | undefined
  if (twofa && twofa.enabled) {
    if (!totpCode) return NextResponse.json({ success: false, message: '2FA code required', require2fa: true }, { status: 401 })
    const valid = totp.check(String(totpCode), twofa.secret as string)
    if (!valid) return NextResponse.json({ success: false, message: 'Invalid 2FA code', require2fa: true }, { status: 401 })
  }

  const requestBody = {
    type: 'm.login.password',
    identifier: { type: 'm.id.user', user: matrixId },
    password,
    initial_device_display_name: 'PurpleTalk Web',
  }
  const res = await matrixRequest('/_matrix/client/v3/login', 'POST', requestBody)
  if (res?.access_token) {
    db.prepare(`INSERT OR REPLACE INTO user_mappings (phone_number, matrix_username, matrix_id, last_login) VALUES (?, ?, ?, datetime('now'))`).run(
      String(phoneNumber).replace(/[^0-9+]/g, ''),
      phoneToMatrixUsername(phoneNumber),
      res.user_id,
    )
    const cookie = await setSession(res.access_token, res.user_id, res.device_id)
    const response = NextResponse.json({ success: true, message: 'Login successful', userId: res.user_id, deviceId: res.device_id })
    response.headers.set('Set-Cookie', cookie)
    return response
  }
  const msg = res?.error || 'Invalid credentials'
  return NextResponse.json({ success: false, message: msg }, { status: 401 })
}

async function enable2fa(input: any) {
  const { userId, secret, enabled } = input || {}
  if (!userId || !secret) return NextResponse.json({ success: false, message: 'userId and secret required' }, { status: 400 })
  db.prepare('INSERT OR REPLACE INTO two_factor (user_id, secret, enabled) VALUES (?, ?, ?)').run(userId, secret, enabled ? 1 : 0)
  return NextResponse.json({ success: true, message: '2FA settings saved' })
}

async function verify2fa(input: any) {
  const { userId, code } = input || {}
  if (!userId || !code) return NextResponse.json({ success: false, message: 'userId and code required' }, { status: 400 })
  const rec = db.prepare('SELECT secret FROM two_factor WHERE user_id = ?').get(userId) as { secret?: string } | undefined
  if (!rec) return NextResponse.json({ success: false, message: '2FA not enabled' }, { status: 400 })
  const valid = totp.check(String(code), rec.secret as string)
  return NextResponse.json({ success: valid, message: valid ? 'Valid code' : 'Invalid code' }, { status: valid ? 200 : 400 })
}

async function logout() {
  const resp = NextResponse.json({ success: true, message: 'Logged out' })
  resp.headers.set('Set-Cookie', `${SESSION_COOKIE}=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0`)
  return resp
}

async function setSession(accessToken: string, userId: string, deviceId?: string) {
  const now = Math.floor(Date.now() / 1000)
  const token = await new SignJWT({ accessToken, userId, deviceId })
    .setProtectedHeader({ alg: 'HS256', typ: 'JWT' })
    .setIssuedAt(now)
    .setExpirationTime(now + SESSION_MAX_AGE)
    .setSubject(userId)
    .sign(JWT_KEY)
  return `${SESSION_COOKIE}=${token}; Path=/; HttpOnly; SameSite=Lax; Max-Age=${SESSION_MAX_AGE}`
}
