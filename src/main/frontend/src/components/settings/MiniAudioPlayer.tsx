import { Play, Square } from 'lucide-react'
import { useState } from 'react'
import { Button } from '../ui/button'

interface MiniAudioPlayerProps {
  audioUrl: string
}

export function MiniAudioPlayer({ audioUrl }: MiniAudioPlayerProps) {
  const [isPlaying, setIsPlaying] = useState(false)
  const [audio] = useState(new Audio(audioUrl))

  const togglePlay = () => {
    if (isPlaying) {
      audio.pause()
      audio.currentTime = 0
    } else {
      audio.play()
    }
    setIsPlaying(!isPlaying)
  }

  // Handle audio ending
  audio.onended = () => setIsPlaying(false)

  return (
    <Button
      variant="ghost"
      size="sm"
      onClick={togglePlay}
      className="h-8 w-8 p-0"
    >
      {isPlaying ? (
        <Square className="h-4 w-4" />
      ) : (
        <Play className="h-4 w-4" />
      )}
    </Button>
  )
}
