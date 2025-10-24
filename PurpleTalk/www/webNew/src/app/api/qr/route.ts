import { NextRequest, NextResponse } from 'next/server'
import QRCode from 'qrcode'

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url)
  const value = searchParams.get('value')
  if (!value) return NextResponse.json({ error: 'value required' }, { status: 400 })
  const png = await QRCode.toBuffer(value, { scale: 6 })
  const ab = png.buffer.slice(png.byteOffset, png.byteOffset + png.byteLength)
  return new NextResponse(ab as ArrayBuffer, { headers: { 'Content-Type': 'image/png', 'Cache-Control': 'public, max-age=31536000, immutable' } })
}
