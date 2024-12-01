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
import { useState, useEffect, useCallback } from 'react'
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
  const [error, setError] = useState<string | null>(null)
  const [status, setStatus] = useState<string>('')
  const [progress, setProgress] = useState(0)
  const [messages, setMessages] = useState<string[]>([])
  const [webSocket, setWebSocket] = useState<WebSocket | null>(null)
  const [reconnectAttempts, setReconnectAttempts] = useState(0)
  const MAX_RECONNECT_ATTEMPTS = 3

  const connectWebSocket = useCallback(() => {
    if (!podcastId || reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) return

    const ws = new WebSocket(`ws://${window.location.host}/ws/podcast-generation/${podcastId}`)
    
    ws.onopen = () => {
      console.log('WebSocket connected')
      setReconnectAttempts(0) // Reset attempts on successful connection
    }

    ws.onmessage = (event) => {
      try {
        const data: GenerationStatus = JSON.parse(event.data)
        setStatus(data.status)
        setProgress(data.progress)
        setMessages(prev => [...prev, data.message])

        if (data.status === 'COMPLETED') {
          ws.close()
        }
      } catch (err) {
        console.error('Error parsing WebSocket message:', err)
      }
    }

    ws.onerror = (error) => {
      console.error('WebSocket error:', error)
      setError('Failed to connect to generation status updates')
    }

    ws.onclose = () => {
      console.log('WebSocket closed')
      // Only attempt reconnect if we're still generating
      if (isGenerating && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
        setReconnectAttempts(prev => prev + 1)
        setTimeout(() => connectWebSocket(), 2000 * (reconnectAttempts + 1)) // Exponential backoff
      }
    }

    setWebSocket(ws)
  }, [podcastId, reconnectAttempts, isGenerating])

  useEffect(() => {
    connectWebSocket()

    return () => {
      if (webSocket) {
        webSocket.close()
      }
    }
  }, [connectWebSocket])

  const startGeneration = async () => {
    if (!podcastId) {
      setError('No podcast ID provided')
      return
    }

    try {
      setIsGenerating(true)
      setError(null)
      setMessages([])
      setProgress(0)
      
      const response = await fetch(`/api/podcasts/${podcastId}/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        }
      })

      if (!response.ok) {
        const errorData = await response.json().catch(() => null)
        throw new Error(errorData?.message || 'Failed to start podcast generation')
      }

      // Reconnect WebSocket after starting generation
      if (!webSocket || webSocket.readyState !== WebSocket.OPEN) {
        connectWebSocket()
      }

    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start generation')
      setIsGenerating(false)
    }
  }

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ERROR':
        return 'bg-red-50 text-red-700'
      case 'COMPLETED':
        return 'bg-green-50 text-green-700'
      case 'GENERATING_VOICES':
        return 'bg-blue-50 text-blue-700'
      case 'GENERATING_SEGMENTS':
        return 'bg-purple-50 text-purple-700'
      case 'STITCHING':
        return 'bg-yellow-50 text-yellow-700'
      default:
        return 'bg-gray-50 text-gray-700'
    }
  }

  return (
    <div className="p-6 space-y-6">
      <div className="bg-white p-6 rounded-lg border">
        <h3 className="text-lg font-semibold mb-4">Generate Podcast</h3>
        
        {/* Progress bar */}
        <div className="mb-6">
          <div className="flex justify-between text-sm mb-1">
            <span>Progress</span>
            <span>{progress}%</span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2.5">
            <div
              className={cn(
                "h-2.5 rounded-full transition-all duration-500",
                status === 'ERROR' ? 'bg-red-500' : 'bg-primary'
              )}
              style={{ width: `${progress}%` }}
            ></div>
          </div>
        </div>

        {/* Status display */}
        <div className="mb-6">
          <div className="text-sm font-medium mb-2">Status</div>
          <div className={cn(
            "px-3 py-2 rounded",
            getStatusColor(status)
          )}>
            {status || 'Ready to generate'}
          </div>
        </div>

        {/* Console-like log */}
        <div className="mb-6">
          <div className="text-sm font-medium mb-2">Generation Log</div>
          <div className="bg-gray-900 text-gray-100 p-4 rounded font-mono text-sm h-48 overflow-y-auto">
            {messages.length === 0 ? (
              <div className="text-gray-500">No messages yet...</div>
            ) : (
              messages.map((msg, i) => (
                <div key={i} className="mb-1">
                  <span className="text-gray-400">[{new Date().toLocaleTimeString()}]</span> {msg}
                </div>
              ))
            )}
          </div>
        </div>

        {error && (
          <div className="mb-6 p-4 bg-red-50 text-red-700 rounded">
            {error}
          </div>
        )}

        {reconnectAttempts > 0 && reconnectAttempts < MAX_RECONNECT_ATTEMPTS && (
          <div className="mb-6 p-4 bg-yellow-50 text-yellow-700 rounded">
            Connection lost. Attempting to reconnect... (Attempt {reconnectAttempts} of {MAX_RECONNECT_ATTEMPTS})
          </div>
        )}

        <div className="flex justify-between">
          <button
            onClick={onBack}
            className="px-4 py-2 border rounded hover:bg-gray-50"
            disabled={isGenerating}
          >
            Back
          </button>

          {status === 'COMPLETED' ? (
            <button
              onClick={onComplete}
              className="bg-primary text-primary-foreground px-4 py-2 rounded hover:bg-primary/90"
            >
              View Podcast
            </button>
          ) : (
            <button
              onClick={startGeneration}
              disabled={isGenerating}
              className={cn(
                "px-4 py-2 rounded text-white",
                isGenerating ? 
                  "bg-gray-400 cursor-not-allowed" : 
                  "bg-primary hover:bg-primary/90"
              )}
            >
              {isGenerating ? 'Generating...' : 'Generate Podcast'}
            </button>
          )}
        </div>
      </div>
    </div>
  )
}
