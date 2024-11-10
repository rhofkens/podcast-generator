import { Link, useLocation } from 'react-router-dom'

export function LeftNav() {
  const location = useLocation()

  return (
    <nav className="w-64 border-r bg-white h-full">
      <div className="p-4">
        <h1 className="text-xl font-bold">Podcast Generator</h1>
      </div>
      <div className="space-y-1 p-2">
        <Link 
          to="/" 
          className={`flex items-center gap-2 p-2 rounded hover:bg-gray-100 ${
            location.pathname === '/' ? 'bg-gray-100' : ''
          }`}
        >
          <span>Podcasts</span>
        </Link>
        <Link 
          to="/settings" 
          className={`flex items-center gap-2 p-2 rounded hover:bg-gray-100 ${
            location.pathname === '/settings' ? 'bg-gray-100' : ''
          }`}
        >
          <span>Settings</span>
        </Link>
      </div>
    </nav>
  )
}
