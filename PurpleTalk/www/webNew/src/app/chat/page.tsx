"use client"
import { useEffect, useRef, useState } from 'react'
import * as sdk from 'matrix-js-sdk'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'

export default function ChatPage() {
  const [session, setSession] = useState<any>(null)
  const [client, setClient] = useState<any>(null)
  const [rooms, setRooms] = useState<any[]>([])
  const [currentRoom, setCurrentRoom] = useState<any>(null)
  const [message, setMessage] = useState('')
  const messagesRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    fetch('/api/session').then(r=>r.json()).then(async s => {
      if (!s.authenticated) { location.href = '/login'; return }
      setSession(s)
      const c = sdk.createClient({ baseUrl: process.env.NEXT_PUBLIC_MATRIX_BASE || process.env.MATRIX_SERVER || 'https://purpletalk.devit.dev', accessToken: s.accessToken, userId: s.userId })
      setClient(c)
      // matrix-js-sdk emits 'Room.timeline' at runtime; cast to any to satisfy TS
      ;(c as any).on('Room.timeline', (event: any, room: any, toStartOfTimeline: boolean) => {
        if (currentRoom && room.roomId === currentRoom.roomId && !toStartOfTimeline && event.getType() === 'm.room.message') {
          setRooms(prev => [...prev]) // trigger rerender
          scrollToBottom()
        }
      })
      await c.startClient()
      setRooms(c.getRooms().sort((a: any, b: any) => b.getLastActiveTimestamp() - a.getLastActiveTimestamp()))
    })
  }, [])

  const selectRoom = (room: any) => {
    setCurrentRoom(room)
    setTimeout(scrollToBottom, 50)
  }

  const send = async () => {
    if (!currentRoom || !message.trim()) return
    await client.sendMessage(currentRoom.roomId, { msgtype: 'm.text', body: message.trim() })
    setMessage('')
    scrollToBottom()
  }

  const scrollToBottom = () => {
    messagesRef.current && (messagesRef.current.scrollTop = messagesRef.current.scrollHeight)
  }

  return (
    <div className="h-screen grid grid-cols-12">
      <aside className="col-span-4 border-r p-4 overflow-y-auto">
        <div className="flex items-center justify-between mb-3">
          <h2 className="font-bold">Conversations</h2>
          <Button onClick={()=>location.href='/dashboard'} variant="secondary">Back</Button>
        </div>
        <div className="space-y-2">
          {rooms.map(room => (
            <div key={room.roomId} className={`p-3 rounded-lg border cursor-pointer ${currentRoom?.roomId===room.roomId?'bg-zinc-100':''}`} onClick={()=>selectRoom(room)}>
              <div className="font-medium">{room.name || 'Unknown'}</div>
              <div className="text-xs text-zinc-500">{room.getJoinedMembers().length} members</div>
            </div>
          ))}
        </div>
      </aside>
      <section className="col-span-8 flex flex-col">
        <div className="border-b p-4 font-medium">{currentRoom?.name || 'Select a conversation'}</div>
        <div ref={messagesRef} className="flex-1 p-4 overflow-y-auto space-y-2 bg-zinc-50">
          {currentRoom?.getLiveTimeline().getEvents().filter((e:any)=>e.getType()==='m.room.message').map((e:any)=> {
            const isMe = e.getSender() === session?.userId
            const c = e.getContent()
            return (
              <div key={e.getId()} className={`max-w-[80%] p-3 rounded-lg ${isMe?'ml-auto bg-indigo-600 text-white':'bg-white border'}`}>
                {c.msgtype === 'm.text' ? c.body : '[Unsupported message]'}
                <div className="text-[10px] opacity-70 mt-1">{new Date(e.getTs()).toLocaleTimeString()}</div>
              </div>
            )
          })}
        </div>
        {currentRoom && (
          <div className="p-3 border-t flex gap-2">
            <Input placeholder="Type a message" value={message} onChange={e=>setMessage(e.target.value)} onKeyDown={e=>{ if(e.key==='Enter' && !e.shiftKey){ e.preventDefault(); send() } }} />
            <Button onClick={send}>Send</Button>
          </div>
        )}
      </section>
    </div>
  )
}
