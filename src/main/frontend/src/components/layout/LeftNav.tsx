export function LeftNav() {
  return (
    <nav className="w-64 border-r bg-white h-full">
      <div className="p-4">
        <h1 className="text-xl font-bold">Podcast Generator</h1>
      </div>
      <div className="space-y-1 p-2">
        <a href="/" className="flex items-center gap-2 p-2 rounded hover:bg-gray-100">
          <span>Podcasts</span>
        </a>
        <a href="/settings" className="flex items-center gap-2 p-2 rounded hover:bg-gray-100">
          <span>Settings</span>
        </a>
      </div>
    </nav>
  )
}
