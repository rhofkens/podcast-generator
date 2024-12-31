import { useState, useEffect } from 'react'
import { Play, Pause, Loader2 } from 'lucide-react'
import { Button } from '../ui/button'
import { WaveformViewer } from './WaveformViewer'

interface AudioPlayerProps {
  podcastId: number
}

export function AudioPlayer({ podcastId }: AudioPlayerProps) {
  const [audioUrl, setAudioUrl] = useState<string | null>(null)
  const [isPlaying, setIsPlaying] = useState(false)
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
      <div className="flex items-center gap-2">
        <Button
          variant="ghost"
          size="sm"
          className="w-8 h-8 p-0"
          onClick={() => setIsPlaying(!isPlaying)}
        >
          {isPlaying ? (
            <Pause className="h-4 w-4" />
          ) : (
            <Play className="h-4 w-4" />
          )}
          <span className="sr-only">
            {isPlaying ? 'Pause' : 'Play'}
          </span>
        </Button>
      </div>
      
      <WaveformViewer 
        url={audioUrl}
        playing={isPlaying}
        onPlayPause={setIsPlaying}
      />
    </div>
  )
}
