"use client"
import { useEffect, useState } from 'react'
import { Card } from '@/components/ui/card'
import { Button } from '@/components/ui/button'

const amounts = [5, 10, 25, 50, 100]

export default function DonatePage() {
  const [amount, setAmount] = useState<number>(25)
  const [orderId, setOrderId] = useState<string>('')

  const createOrder = async () => {
    const res = await fetch('/api/donate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'createOrder', amount }) })
    const data = await res.json()
    if (data.success) setOrderId(data.orderId)
    else alert(data.message || 'Failed to create order')
  }

  const capture = async () => {
    if (!orderId) return
    const res = await fetch('/api/donate', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify({ action: 'captureOrder', orderId }) })
    const data = await res.json()
    if (data.success) alert('Thank you for your donation!')
    else alert(data.message || 'Payment failed')
  }

  const cryptoAddress = {
    BTC: process.env.CRYPTO_BTC || '',
    ETH: process.env.CRYPTO_ETH || '',
    SOL: process.env.CRYPTO_SOL || '',
    DOGE: process.env.CRYPTO_DOGE || '',
  }

  return (
    <div className="min-h-screen bg-zinc-50 p-6">
      <div className="max-w-3xl mx-auto space-y-6">
        <h1 className="text-2xl font-bold">Support PurpleTalk</h1>
        <Card className="p-6">
          <div className="flex gap-2 flex-wrap">
            {amounts.map(a => (
              <Button key={a} variant={a===amount?undefined:'secondary'} onClick={()=>setAmount(a)}>${'{'}a{'}'}</Button>
            ))}
          </div>
          <div className="mt-4 flex gap-2">
            <input type="number" className="border rounded px-3 py-2" value={amount} onChange={e=>setAmount(parseFloat(e.target.value))} />
            <Button onClick={createOrder}>Pay with PayPal</Button>
            {orderId && <Button variant="secondary" onClick={capture}>Capture</Button>}
          </div>
        </Card>
        <Card className="p-6">
          <h2 className="font-semibold mb-2">Crypto Donations</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            {Object.entries(cryptoAddress).map(([sym, addr]) => (
              <div key={sym} className="border rounded-lg p-4">
                <div className="font-medium">{sym}</div>
                {addr ? (
                  <div className="mt-2">
                    <img alt="QR" src={`/api/qr?value=${encodeURIComponent(String(addr))}`} className="w-40 h-40" />
                    <div className="text-xs break-all mt-2">{addr}</div>
                  </div>
                ) : (
                  <div className="text-sm text-zinc-500">Address not configured</div>
                )}
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  )
}
