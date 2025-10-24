import { NextRequest, NextResponse } from 'next/server'
import fs from 'fs'
import path from 'path'

export async function OPTIONS() { return NextResponse.json({}, { status: 200 }) }

export async function POST(req: NextRequest) {
  const data = await req.json().catch(() => ({}))
  const email = String(data.email || '').trim().toLowerCase()
  if (!email || !/^\S+@\S+\.\S+$/.test(email)) {
    return NextResponse.json({ success: false, message: 'Invalid email address' }, { status: 400 })
  }
  const file = path.join(process.cwd(), 'data', 'email-signups.txt')
  if (!fs.existsSync(path.dirname(file))) fs.mkdirSync(path.dirname(file), { recursive: true })
  if (fs.existsSync(file)) {
    const existing = fs.readFileSync(file, 'utf-8')
    if (existing.includes(email)) {
      return NextResponse.json({ success: false, message: 'Email already registered' }, { status: 200 })
    }
  }
  const entry = `${email} | ${new Date().toISOString()}\n`
  fs.appendFileSync(file, entry, { encoding: 'utf-8' })
  return NextResponse.json({ success: true, message: "Thanks! We'll notify you when PurpleTalk launches." })
}
