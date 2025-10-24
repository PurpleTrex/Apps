import crypto from 'crypto'

export const MATRIX_SERVER = process.env.MATRIX_SERVER as string
export const MATRIX_DOMAIN = process.env.MATRIX_DOMAIN as string

export function phoneToMatrixUsername(phoneNumber: string) {
  const phoneDigits = (phoneNumber || '').replace(/[^0-9+]/g, '')
  const salt = `PurpleTalk2025_${MATRIX_DOMAIN}`
  const hash = crypto.createHash('sha256').update(salt + phoneDigits).digest('hex')
  return 'tel_' + hash.substring(0, 16)
}

export function phoneToMatrixId(phoneNumber: string) {
  const username = phoneToMatrixUsername(phoneNumber)
  return `@${username}:${MATRIX_DOMAIN}`
}

export async function matrixRequest<T = any>(endpoint: string, method: string = 'GET', data?: any, accessToken?: string) {
  const url = `${MATRIX_SERVER}${endpoint}`
  const headers: Record<string, string> = { 'Content-Type': 'application/json' }
  if (accessToken) headers['Authorization'] = `Bearer ${accessToken}`

  const res = await fetch(url, {
    method,
    headers,
    body: data && (method === 'POST' || method === 'PUT') ? JSON.stringify(data) : undefined,
  })

  const text = await res.text()
  try {
    return JSON.parse(text)
  } catch {
    return text as any
  }
}
