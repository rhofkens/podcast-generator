import { useState, useEffect } from 'react'
import { cn } from '../../../lib/utils'

interface GenerationStatus {
  status: string
  progress: number
  message: string
}

interface PodcastStepProps {
  podcastId: string | null
  onBack: () => void
  onComplete: () => void
}

export function PodcastStep({ podcastId, onBack, onComplete }: PodcastStepProps) {
  const [isGenerating, setIsGenerating] = useState(false)
  const [status, setStatus] = useState<GenerationStatus | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [logs, setLogs] = useState<string[]>([])
  const [socket, setSocket] = useState<WebSocket | null>(null)

  useEffect(() => {
    // Connect to WebSocket when component mounts
    const ws = new WebSocket(`ws://${window.location.host}/api/ws/podcast-generation/${podcastId}`)
    
    ws.onmessage = (event) => {
      const data = JSON.parse(event.data)
      setStatus(data)
      setLogs(prev => [...prev, data.message])
    }

    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      setError('Connection error occurred')
    }

    setSocket(ws)

    return () => {
      ws.close()
    }
  }, [podcastId])

  const startGeneration = async () => {
    try {
      setIsGenerating(true)
      setError(null)
      
      const response = await fetch(`/api/podcasts/${podcastId}/generate`, {
        method: 'POST'
      })

      if (!response.ok) {
        throw new Error('Failed to start podcast generation')
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start generation')
      setIsGenerating(false)
    }
  }

  const renderGenerationConsole = () => {
    return (
      <div className="bg-gray-900 text-gray-100 p-4 rounded-lg h-64 overflow-y-auto font-mono text-sm">
        {logs.map((log, index) => (
          <div key={index} className="py-1">
            <span className="text-gray-500">[{new Date().toISOString()}]</span>{' '}
            {log}
          </div>
        ))}
      </div>
    )
  }

  const renderProgressBar = () => {
    if (!status) return null

    return (
      <div className="w-full bg-gray-200 rounded-full h-2.5 mb-4">
        <div 
          className="bg-primary h-2.5 rounded-full transition-all duration-500"
          style={{ width: `${status.progress}%` }}
        />
      </div>
    )
  }

  return (
    <div className="p-6 space-y-6">
      <div className="space-y-4">
        <h2 className="text-2xl font-bold">Generate Podcast</h2>
        <p className="text-gray-600">
          Generate the final podcast audio file. This process will:
          <ul className="list-disc pl-5 mt-2 space-y-1">
            <li>Generate synthetic voices for all participants</li>
            <li>Create audio segments for each part of the transcript</li>
            <li>Combine all segments into the final podcast</li>
          </ul>
        </p>
      </div>

      {error && (
        <div className="bg-red-50 text-red-500 p-4 rounded-lg">
          {error}
        </div>
      )}

      {status && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <span className="font-medium">{status.status}</span>
            <span>{status.progress}%</span>
          </div>
          {renderProgressBar()}
        </div>
      )}

      {renderGenerationConsole()}

      <div className="flex justify-between mt-6">
        <button
          onClick={onBack}
          className="px-4 py-2 border rounded hover:bg-gray-50"
          disabled={isGenerating}
        >
          Back
        </button>

        {!isGenerating ? (
          <button
            onClick={startGeneration}
            className="bg-primary text-primary-foreground px-4 py-2 rounded hover:bg-primary/90"
          >
            Start Generation
          </button>
        ) : (
          <button
            onClick={onComplete}
            className={cn(
              "px-4 py-2 rounded",
              status?.status === 'COMPLETED'
                ? "bg-green-500 text-white hover:bg-green-600"
                : "bg-gray-300 text-gray-600 cursor-not-allowed"
            )}
            disabled={status?.status !== 'COMPLETED'}
          >
            Complete
          </button>
        )}
      </div>
    </div>
  )
}
