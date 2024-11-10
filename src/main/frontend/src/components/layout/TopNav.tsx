export function TopNav() {
  return (
    <header className="h-14 border-b flex items-center px-6 bg-white">
      <div className="flex-1" />
      <div className="flex items-center gap-4">
        <button className="flex items-center gap-2 p-2 rounded-full hover:bg-gray-100">
          <span className="w-8 h-8 rounded-full bg-gray-200" />
        </button>
      </div>
    </header>
  )
}
