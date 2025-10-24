import { NextRequest, NextResponse } from 'next/server'
import db from '@/lib/db'

export async function POST(req: NextRequest) {
  const body = await req.json().catch(()=>({}))
  const userId = String(body.userId||'')
  if (!userId) return NextResponse.json({ success: false, message: 'userId required' }, { status: 400 })
  const rec = db.prepare('SELECT email, verified FROM recovery_emails WHERE user_id = ?').get(userId) as any
  if (!rec) return NextResponse.json({ success: true, email: '' })
  return NextResponse.json({ success: true, email: rec.email, verified: !!rec.verified })
}
