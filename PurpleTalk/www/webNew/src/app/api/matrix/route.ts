import { NextRequest, NextResponse } from 'next/server'
import { matrixRequest } from '@/lib/matrix'

export async function OPTIONS() { return NextResponse.json({}, { status: 200 }) }

export async function POST(req: NextRequest) {
  const body = await req.json().catch(() => ({}))
  const action = body.action as string
  const authHeader = req.headers.get('authorization') || ''
  const accessToken = authHeader.replace(/^Bearer\s+/i, '')

  if (!action) return NextResponse.json({ success: false, message: 'Invalid request' }, { status: 400 })

  try {
    switch (action) {
      case 'getUserInfo':
        return await getUserInfo(accessToken)
      case 'getProfile':
        return await getProfile(accessToken, body)
      case 'updateProfile':
        return await updateProfile(accessToken, body)
      case 'changePassword':
        return await changePassword(accessToken, body)
      case 'deleteAccount':
        return await deleteAccount(accessToken)
      case 'getRooms':
        return await getRooms(accessToken)
      case 'sendMessage':
        return await sendMessage(accessToken, body)
      default:
        return NextResponse.json({ success: false, message: 'Unknown action' }, { status: 400 })
    }
  } catch (e: any) {
    return NextResponse.json({ success: false, message: e.message || 'Server error' }, { status: 500 })
  }
}

async function getUserInfo(token?: string) {
  if (!token) return NextResponse.json({ success: false, message: 'Authentication required' }, { status: 401 })
  const roomsResponse = await matrixRequest('/_matrix/client/v3/joined_rooms', 'GET', null, token)
  const roomCount = roomsResponse?.joined_rooms ? roomsResponse.joined_rooms.length : 0
  return NextResponse.json({ success: true, message: 'User info retrieved', roomCount, deviceCount: 1 })
}

async function getProfile(token?: string, input?: any) {
  if (!token || !input?.userId) return NextResponse.json({ success: false, message: 'Authentication and userId required' }, { status: 401 })
  const profile = await matrixRequest(`/_matrix/client/v3/profile/${encodeURIComponent(input.userId)}`, 'GET', null, token)
  if (profile?.displayname !== undefined) {
    return NextResponse.json({ success: true, message: 'Profile retrieved', displayName: profile.displayname, avatarUrl: profile.avatar_url ?? null })
  }
  return NextResponse.json({ success: true, message: 'Profile retrieved', displayName: null, avatarUrl: null })
}

async function updateProfile(token?: string, input?: any) {
  if (!token || !input?.displayName) return NextResponse.json({ success: false, message: 'Authentication and displayName required' }, { status: 401 })
  const whoami = await matrixRequest('/_matrix/client/v3/account/whoami', 'GET', null, token)
  if (!whoami?.user_id) return NextResponse.json({ success: false, message: 'Failed to get user ID' }, { status: 400 })
  const res = await matrixRequest(`/_matrix/client/v3/profile/${encodeURIComponent(whoami.user_id)}/displayname`, 'PUT', { displayname: input.displayName }, token)
  if (res !== null) return NextResponse.json({ success: true, message: 'Profile updated successfully' })
  return NextResponse.json({ success: false, message: 'Failed to update profile' }, { status: 400 })
}

async function changePassword(token?: string, input?: any) {
  if (!token || !input?.currentPassword || !input?.newPassword) return NextResponse.json({ success: false, message: 'Authentication, current password and new password required' }, { status: 401 })
  if (String(input.newPassword).length < 8) return NextResponse.json({ success: false, message: 'New password must be at least 8 characters' }, { status: 400 })
  const requestBody = { new_password: input.newPassword, auth: { type: 'm.login.password', password: input.currentPassword } }
  const res = await matrixRequest('/_matrix/client/v3/account/password', 'POST', requestBody, token)
  if (res !== null && !res?.errcode) return NextResponse.json({ success: true, message: 'Password changed successfully' })
  const msg = res?.errcode === 'M_FORBIDDEN' ? 'Current password is incorrect' : (res?.error || 'Failed to change password')
  return NextResponse.json({ success: false, message: msg }, { status: 400 })
}

async function deleteAccount(token?: string) {
  if (!token) return NextResponse.json({ success: false, message: 'Authentication required' }, { status: 401 })
  const res = await matrixRequest('/_matrix/client/v3/account/deactivate', 'POST', {}, token)
  if (res !== null && !res?.errcode) return NextResponse.json({ success: true, message: 'Account deleted successfully' })
  return NextResponse.json({ success: false, message: res?.error || 'Failed to delete account' }, { status: 400 })
}

async function getRooms(token?: string) {
  if (!token) return NextResponse.json({ success: false, message: 'Authentication required' }, { status: 401 })
  const res = await matrixRequest('/_matrix/client/v3/joined_rooms', 'GET', null, token)
  if (res?.joined_rooms) return NextResponse.json({ success: true, message: 'Rooms retrieved', rooms: res.joined_rooms })
  return NextResponse.json({ success: false, message: 'Failed to get rooms' }, { status: 400 })
}

async function sendMessage(token: string | undefined, input?: any) {
  if (!token || !input?.roomId || !input?.message) return NextResponse.json({ success: false, message: 'Authentication, roomId and message required' }, { status: 401 })
  const txnId = `web_${Date.now()}_${Math.random().toString(16).slice(2)}`
  const messageBody = { msgtype: 'm.text', body: input.message }
  const endpoint = `/_matrix/client/v3/rooms/${encodeURIComponent(input.roomId)}/send/m.room.message/${encodeURIComponent(txnId)}`
  const res = await matrixRequest(endpoint, 'PUT', messageBody, token)
  if (res?.event_id) return NextResponse.json({ success: true, message: 'Message sent', eventId: res.event_id })
  return NextResponse.json({ success: false, message: 'Failed to send message' }, { status: 400 })
}
