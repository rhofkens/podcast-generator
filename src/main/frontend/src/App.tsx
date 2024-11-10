import React from 'react'
import ReactDOM from 'react-dom/client'
import './styles/globals.css'
import { TopNav } from './components/layout/TopNav'
import { LeftNav } from './components/layout/LeftNav'
import { Breadcrumb } from './components/layout/Breadcrumb'

function App() {
  return (
    <div className="min-h-screen flex flex-col">
      <TopNav />
      <div className="flex-1 flex">
        <LeftNav />
        <main className="flex-1 bg-gray-50">
          <Breadcrumb 
            items={[
              { label: 'Home', href: '/' },
              { label: 'Podcasts' }
            ]} 
          />
          <div className="p-6">
            <h2 className="text-2xl font-bold mb-6">Your Podcasts</h2>
            {/* Podcast list will go here */}
            <div className="bg-white rounded-lg shadow p-4">
              <p className="text-gray-600">No podcasts yet. Create your first podcast to get started.</p>
            </div>
          </div>
        </main>
      </div>
    </div>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)

export default App
