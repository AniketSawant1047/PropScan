import { Link } from 'react-router-dom';
import { Bookmark, Home, Search, Sparkles, User, Menu, X, LogOut } from 'lucide-react';
import { useState } from 'react';
import { useStore } from '../../store';

export default function Navbar() {
  const [mobileOpen, setMobileOpen] = useState(false);
  const { user, logout } = useStore();

  return (
    <header className="sticky top-0 z-50 bg-white/95 backdrop-blur-sm border-b border-gray-100 shadow-sm">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center group">
            <img src="/FV.png" alt="PropScan Icon" className="w-22 h-22 object-contain group-hover:scale-105 transition-transform drop-shadow-sm" />
            <img src="/propscan-logo-full.png" alt="PropScan Text" className="mt-4 h-40 object-contain mix-blend-multiply hidden sm:block" />
          </Link>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center gap-1">
            <Link to="/" className="btn-ghost text-sm">
              <Home size={16} /> Buy
            </Link>
            <Link to="/" className="btn-ghost text-sm">
              <Search size={16} /> Rent
            </Link>
            <Link to="/results" className="btn-ghost text-sm">
              <Search size={16} /> Compare
            </Link>
            <Link to="/results" className="btn-ghost text-sm">
              <Sparkles size={16} /> AI Picks
            </Link>
            <Link to="/saved" className="btn-ghost text-sm">
              <Bookmark size={16} /> Saved
            </Link>
          </nav>

          {/* Desktop Actions */}
          <div className="hidden md:flex items-center gap-3">
            {user ? (
              <>
                <span className="text-sm font-medium text-gray-700">Hi, {user.name}</span>
                <button onClick={logout} className="btn-ghost text-sm">
                  Logout
                </button>
              </>
            ) : (
              <>
                <Link to="/login" className="btn-ghost text-sm">
                  <User size={16} /> Sign In
                </Link>
                <Link to="/register" className="btn-primary text-sm py-2 px-4">
                  Create Account
                </Link>
              </>
            )}
          </div>

          {/* Mobile menu toggle */}
          <button
            className="md:hidden p-2 rounded-lg hover:bg-gray-100"
            onClick={() => setMobileOpen(!mobileOpen)}
          >
            {mobileOpen ? <X size={20} /> : <Menu size={20} />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {mobileOpen && (
        <div className="md:hidden border-t border-gray-100 bg-white animate-fade-in">
          <div className="px-4 py-3 space-y-1">
            <Link to="/" onClick={() => setMobileOpen(false)} className="flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-50 text-gray-700">
              <Home size={16} /> Buy
            </Link>
            <Link to="/results" onClick={() => setMobileOpen(false)} className="flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-50 text-gray-700">
              <Search size={16} /> Search Results
            </Link>
            <Link to="/saved" onClick={() => setMobileOpen(false)} className="flex items-center gap-2 px-3 py-2.5 rounded-lg hover:bg-gray-50 text-gray-700">
              <Bookmark size={16} /> Saved Properties
            </Link>
          </div>
          <div className="px-4 pb-4 flex gap-3">
            {user ? (
              <button onClick={() => { logout(); setMobileOpen(false); }} className="flex-1 btn-secondary text-sm py-2.5 flex items-center justify-center gap-2">
                <LogOut size={16} /> Logout
              </button>
            ) : (
              <>
                <Link to="/login" onClick={() => setMobileOpen(false)} className="flex-1 btn-secondary flex justify-center items-center text-sm py-2.5">Sign In</Link>
                <Link to="/register" onClick={() => setMobileOpen(false)} className="flex-1 btn-primary flex justify-center items-center text-sm py-2.5">Create Account</Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
}
