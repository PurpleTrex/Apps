import { NextRequest, NextResponse } from 'next/server'
import { jwtVerify } from 'jose'

const SESSION_COOKIE = process.env.SESSION_COOKIE_NAME || 'purpletalk_session'
const JWT_SECRET = (process.env.JWT_SECRET || 'change_me_to_a_long_random_secret')
const JWT_KEY = new TextEncoder().encode(JWT_SECRET)

export async function GET(req: NextRequest) {
  const cookieHeader = req.headers.get('cookie') || ''
  const match = cookieHeader.match(new RegExp(`${SESSION_COOKIE}=([^;]+)`))
  if (!match) return NextResponse.json({ authenticated: false })
  try {
    const token = match[1]
    const { payload } = await jwtVerify(token, JWT_KEY)
    return NextResponse.json({ authenticated: true, accessToken: payload.accessToken, userId: payload.userId, deviceId: payload.deviceId })
  } catch (e) {
    return NextResponse.json({ authenticated: false })
  }
}

export async function DELETE() {
  const resp = NextResponse.json({ success: true })
  resp.headers.set('Set-Cookie', `${SESSION_COOKIE}=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0`)
  return resp
}
