// src/components/layout/Footer.jsx
import { Heart } from 'lucide-react'
import { Link } from 'react-router-dom'

export default function Footer() {
  return (
    <footer className="bg-slate-900 text-slate-400">
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-1 gap-8 md:grid-cols-4">
          {/* Brand */}
          <div className="col-span-1 md:col-span-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="h-8 w-8 rounded-lg bg-gradient-to-br from-cyan-500 to-cyan-700 flex items-center justify-center">
                <span className="text-sm font-black text-white">RB</span>
              </div>
              <span className="text-lg font-bold text-white">
                Relief<span className="text-orange-400">Bridge</span>
              </span>
            </div>
            <p className="text-sm leading-relaxed">
              AI-powered disaster relief coordination platform connecting donors, NGOs, and volunteers for maximum impact.
            </p>
          </div>

          {/* Quick links */}
          <div>
            <h4 className="mb-4 text-sm font-semibold uppercase tracking-wider text-white">Platform</h4>
            <ul className="space-y-2 text-sm">
              {[
                ['Disasters', '/disasters'],
                ['Campaigns', '/campaigns'],
                ['Register', '/register'],
                ['Login', '/login'],
              ].map(([label, to]) => (
                <li key={to}>
                  <Link to={to} className="hover:text-cyan-400 transition">{label}</Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Support */}
          <div>
            <h4 className="mb-4 text-sm font-semibold uppercase tracking-wider text-white">Support</h4>
            <ul className="space-y-2 text-sm">
              {['Help Center', 'Transparency', 'Privacy Policy', 'Terms of Service'].map(item => (
                <li key={item}>
                  <a href="#" className="hover:text-cyan-400 transition">{item}</a>
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="mb-4 text-sm font-semibold uppercase tracking-wider text-white">Contact</h4>
            <ul className="space-y-2 text-sm">
              <li>support@reliefbridge.org</li>
              <li>+91 98765 43210</li>
              <li>New Delhi, India</li>
            </ul>
          </div>
        </div>

        <div className="mt-10 border-t border-slate-800 pt-6 flex flex-col sm:flex-row items-center justify-between gap-3">
          <p className="text-xs">© {new Date().getFullYear()} ReliefBridge. All rights reserved.</p>
          <p className="flex items-center gap-1 text-xs">
            Made with <Heart className="h-3.5 w-3.5 text-red-400 fill-red-400" /> for disaster relief
          </p>
        </div>
      </div>
    </footer>
  )
}
