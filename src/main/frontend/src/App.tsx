import React from 'react'
import ReactDOM from 'react-dom/client'
import './styles/globals.css'

function App() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-background">
      <div className="p-6 bg-card rounded-lg shadow-lg">
        <h1 className="text-4xl font-bold text-primary">Hello Podcast Gen</h1>
        <p className="mt-2 text-muted-foreground">Welcome to your podcast generator</p>
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
