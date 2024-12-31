import { useState, useEffect } from 'react'
import { Loader2 } from 'lucide-react'
import { WaveformViewer } from './WaveformViewer'

interface AudioPlayerProps {
  podcastId: number
}

export function AudioPlayer({ podcastId }: AudioPlayerProps) {
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const validateAudio = async () => {
      try {
        const response = await fetch(`/api/podcasts/validate-audio/${podcastId}`)
        if (!response.ok) throw new Error('Failed to validate audio')
        
        const data = await response.json()
        if (data.valid && data.url) {
          setAudioUrl(data.url)
        } else {
          setError('Audio not available')
        }
      } catch (err) {
        setError('Failed to load audio')
        console.error('Error validating audio:', err)
      } finally {
        setIsLoading(false)
      }
    }

    validateAudio()
  }, [podcastId])

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-12">
        <Loader2 className="h-4 w-4 animate-spin" />
      </div>
    )
  }

  if (error || !audioUrl) {
    return (
      <div className="text-sm text-red-600 h-12 flex items-center justify-center">
        {error || 'Audio not available'}
      </div>
    )
  }

  return (
    <div className="space-y-2">
      <WaveformViewer url={audioUrl} />
    </div>
  )
}
