import Link from 'next/link'
import { Button } from '@/components/ui/button'
import { Shield, Lock, MessageSquare, Smartphone, Sun, Moon } from 'lucide-react'

export default function Home() {
  return (
    <div className="min-h-screen bg-gradient-to-br from-indigo-500 to-purple-600 text-white">
      <header className="max-w-6xl mx-auto px-6 py-6 flex items-center justify-between">
        <div className="flex items-center gap-2 text-xl font-bold"><Shield className="w-6 h-6"/> PurpleTalk</div>
        <nav className="flex items-center gap-3">
          <Link href="/donate"><Button variant="secondary">Donate</Button></Link>
          <Link href="/login"><Button variant="secondary">Login</Button></Link>
          <Link href="/register"><Button className="bg-white text-indigo-600 hover:bg-white/90">Sign Up</Button></Link>
        </nav>
      </header>
      <main className="max-w-6xl mx-auto px-6 py-16">
        <div className="text-center mb-12">
          <h1 className="text-5xl font-extrabold mb-4">Secure Messaging. Your Privacy First.</h1>
          <p className="text-lg opacity-90">End-to-end encrypted communication hosted in Iceland. Your data, your control.</p>
          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link href="/downloads/PurpleTalk.apk"><Button className="bg-emerald-500 hover:bg-emerald-600"><Smartphone className="mr-2"/> Download Android</Button></Link>
            <Link href="/register"><Button variant="secondary" className="bg-white/15 hover:bg-white/25">Get Started Free</Button></Link>
            <Link href="/login"><Button variant="secondary" className="bg-white/15 hover:bg-white/25">Sign In</Button></Link>
          </div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            { icon: Lock, title: 'End-to-End Encryption', desc: 'Messages are encrypted on your device and only readable by you and recipients.' },
            { icon: MessageSquare, title: 'Cross-Platform', desc: 'Use PurpleTalk on web or any Matrix-compatible client.' },
            { icon: Shield, title: 'Privacy-First Hosting', desc: 'Hosted in Iceland with 1984.hosting for maximum privacy.' },
          ].map(({ icon: Icon, title, desc }) => (
            <div key={title} className="bg-white/10 backdrop-blur border border-white/20 rounded-xl p-6">
              <Icon className="w-10 h-10 mb-3"/>
              <h3 className="text-2xl font-semibold mb-2">{title}</h3>
              <p className="opacity-90">{desc}</p>
            </div>
          ))}
        </div>
      </main>
      <footer className="max-w-6xl mx-auto px-6 py-10 text-center opacity-90">
        <p>&copy; 2025 PurpleTalk. Privacy-first messaging powered by Matrix.</p>
        <p className="text-sm mt-2">Server: purpletalk.devit.dev | Hosted in Iceland with 1984.hosting</p>
      </footer>
    </div>
  )
}
