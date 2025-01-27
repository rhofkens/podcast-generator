import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import './styles/globals.css'
import { AuthProvider } from './contexts/AuthContext'
import { TopNav } from './components/layout/TopNav'
import { LeftNav } from './components/layout/LeftNav'
import { PodcastListPage } from './pages/PodcastListPage'
import { SettingsPage } from './pages/SettingsPage'
import { PodcastWizard } from './components/wizard/PodcastWizard'
import { PodcastEditView } from './components/podcast/PodcastEditView'
import { Toaster } from './components/ui/toaster'

function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <div className="min-h-screen flex flex-col">
        <TopNav />
        <div className="flex-1 flex">
          <LeftNav />
          <main className="flex-1 bg-gray-50">
            <Routes>
              <Route path="/" element={<Navigate to="/podcasts" />} />
              <Route path="/podcasts" element={<PodcastListPage />} />
              <Route path="/podcasts/new" element={<PodcastWizard editMode={false} />} />
              <Route path="/podcasts/edit/:id" element={<PodcastEditView />} />
              <Route path="/settings" element={<SettingsPage />} />
            </Routes>
          </main>
        </div>
        </div>
        <Toaster />
      </AuthProvider>
    </BrowserRouter>
  )
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)

export default App
