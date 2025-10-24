import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'

const PAYPAL_MODE = (process.env.PAYPAL_MODE || 'sandbox').toLowerCase()
const PAYPAL_CLIENT_ID = process.env.PAYPAL_CLIENT_ID || ''
const PAYPAL_SECRET = process.env.PAYPAL_SECRET || ''

async function getPayPalAccessToken() {
  const auth = Buffer.from(`${PAYPAL_CLIENT_ID}:${PAYPAL_SECRET}`).toString('base64')
  const res = await fetch(`https://api.${PAYPAL_MODE === 'live' ? '' : 'sandbox.'}paypal.com/v1/oauth2/token`, {
    method: 'POST',
    headers: {
      'Authorization': `Basic ${auth}`,
      'Content-Type': 'application/x-www-form-urlencoded',
    },
    body: 'grant_type=client_credentials',
  })
  if (!res.ok) throw new Error('Failed to obtain PayPal token')
  return res.json() as Promise<{ access_token: string }>
}

export async function OPTIONS() { return NextResponse.json({}, { status: 200 }) }

export async function POST(req: NextRequest) {
  const input = await req.json().catch(() => ({}))
  const { action } = input
  try {
    switch (action) {
      case 'createOrder':
        return await createOrder(input)
      case 'captureOrder':
        return await captureOrder(input)
      case 'logCrypto':
        return await logCrypto(input)
      default:
        return NextResponse.json({ success: false, message: 'Unknown action' }, { status: 400 })
    }
  } catch (e: any) {
    return NextResponse.json({ success: false, message: e.message || 'Donation error' }, { status: 500 })
  }
}

async function createOrder(input: any) {
  const token = await getPayPalAccessToken()
  const res = await fetch(`https://api.${PAYPAL_MODE === 'live' ? '' : 'sandbox.'}paypal.com/v2/checkout/orders`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token.access_token}` },
    body: JSON.stringify({
      intent: 'CAPTURE',
      purchase_units: [{ amount: { currency_code: 'USD', value: String(input.amount || '5.00') } }],
    }),
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data?.message || 'Failed to create PayPal order')
  return NextResponse.json({ success: true, orderId: data.id, data })
}

async function captureOrder(input: any) {
  const { orderId } = input
  if (!orderId) return NextResponse.json({ success: false, message: 'orderId required' }, { status: 400 })
  const token = await getPayPalAccessToken()
  const res = await fetch(`https://api.${PAYPAL_MODE === 'live' ? '' : 'sandbox.'}paypal.com/v2/checkout/orders/${orderId}/capture`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', 'Authorization': `Bearer ${token.access_token}` },
  })
  const data = await res.json()
  if (!res.ok) throw new Error(data?.message || 'Failed to capture PayPal order')
  const amount = data?.purchase_units?.[0]?.payments?.captures?.[0]?.amount?.value
  const payer_email = data?.payer?.email_address
  db.prepare('INSERT INTO donations (type, amount, currency, transaction_id, payer_email, meta) VALUES (?, ?, ?, ?, ?, ?)')
    .run('paypal', amount ? parseFloat(amount) : null, 'USD', data.id, payer_email || null, JSON.stringify(data))
  return NextResponse.json({ success: true, data })
}

async function logCrypto(input: any) {
  const { type, amount, txId, address } = input
  db.prepare('INSERT INTO donations (type, amount, currency, transaction_id, meta) VALUES (?, ?, ?, ?, ?)')
    .run(type || 'crypto', amount ? parseFloat(String(amount)) : null, 'CRYPTO', txId || null, JSON.stringify({ address }))
  return NextResponse.json({ success: true })
}
