import { useEffect, useRef, useState } from 'react'
import WaveSurfer from 'wavesurfer.js'
import { Play, Pause, RotateCcw, Volume2, VolumeX } from 'lucide-react'
import { Button } from '../ui/button'
import { Slider } from '../ui/slider'

interface WaveformViewerProps {
  url: string
  onReady?: () => void
}

export function WaveformViewer({ url, onReady }: WaveformViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const wavesurferRef = useRef<WaveSurfer | null>(null)
  const [isReady, setIsReady] = useState(false)
  const [isPlaying, setIsPlaying] = useState(false)
  const [currentTime, setCurrentTime] = useState('0:00')
  const [duration, setDuration] = useState('0:00')
  const [volume, setVolume] = useState(1)
  const [isMuted, setIsMuted] = useState(false)

  const formatTime = (seconds: number): string => {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = Math.floor(seconds % 60)
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }

  useEffect(() => {
    if (!containerRef.current) return

    const wavesurfer = WaveSurfer.create({
      container: containerRef.current,
      waveColor: '#9CA3AF',
      progressColor: '#6366F1',
      cursorColor: '#4F46E5',
      barWidth: 2,
      barGap: 1,
      barRadius: 3,
      height: 64,
      normalize: true,
      autoScroll: false,  // Changed to prevent scrolling
      interact: true,
      fillParent: true,
      minPxPerSec: 1,      // Reduced to prevent excessive width
    })

    // Ensure container has a fixed width
    if (containerRef.current.parentElement) {
      containerRef.current.parentElement.style.width = '100%'
      containerRef.current.style.width = '100%'
    }

    wavesurfer.load(url)

    wavesurfer.on('ready', () => {
      setIsReady(true)
      setDuration(formatTime(wavesurfer.getDuration()))
      onReady?.()
    })

    wavesurfer.on('audioprocess', () => {
      setCurrentTime(formatTime(wavesurfer.getCurrentTime()))
    })

    wavesurfer.on('play', () => setIsPlaying(true))
    wavesurfer.on('pause', () => setIsPlaying(false))

    wavesurferRef.current = wavesurfer

    return () => {
      wavesurfer.destroy()
    }
  }, [url])


  const handlePlayPause = () => {
    if (!wavesurferRef.current) return
    if (isPlaying) {
      wavesurferRef.current.pause()
    } else {
      wavesurferRef.current.play()
    }
  }

  const handleRestart = () => {
    if (!wavesurferRef.current) return
    wavesurferRef.current.seekTo(0)
    setCurrentTime('0:00')
  }

  const handleVolumeChange = (value: number[]) => {
    if (!wavesurferRef.current) return
    const newVolume = value[0]
    setVolume(newVolume)
    wavesurferRef.current.setVolume(newVolume)
    setIsMuted(newVolume === 0)
  }

  const toggleMute = () => {
    if (!wavesurferRef.current) return
    if (isMuted) {
      wavesurferRef.current.setVolume(volume)
      setIsMuted(false)
    } else {
      wavesurferRef.current.setVolume(0)
      setIsMuted(true)
    }
  }

  return (
    <div className="space-y-4 p-4 bg-white rounded-lg shadow-sm border">
      <div className="relative w-full bg-gray-50 rounded-md p-4">
        <div className="w-full overflow-hidden">
          <div ref={containerRef} className="w-full" />
        </div>
        {!isReady && (
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="animate-pulse flex space-x-4">
              <div className="h-2 bg-gray-200 rounded w-12"></div>
              <div className="h-2 bg-gray-200 rounded w-12"></div>
              <div className="h-2 bg-gray-200 rounded w-12"></div>
            </div>
          </div>
        )}
      </div>

      <div className="flex items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <Button
            variant="ghost"
            size="icon"
            onClick={handlePlayPause}
            disabled={!isReady}
            className="h-10 w-10"
          >
            {isPlaying ? (
              <Pause className="h-5 w-5" />
            ) : (
              <Play className="h-5 w-5" />
            )}
          </Button>
          <Button
            variant="ghost"
            size="icon"
            onClick={handleRestart}
            disabled={!isReady}
            className="h-10 w-10"
          >
            <RotateCcw className="h-4 w-4" />
          </Button>
          <span className="text-sm font-medium text-gray-500 min-w-[100px]">
            {currentTime} / {duration}
          </span>
        </div>

        <div className="flex items-center gap-2 w-32">
          <Button
            variant="ghost"
            size="icon"
            onClick={toggleMute}
            disabled={!isReady}
            className="h-8 w-8"
          >
            {isMuted ? (
              <VolumeX className="h-4 w-4" />
            ) : (
              <Volume2 className="h-4 w-4" />
            )}
          </Button>
          <Slider
            defaultValue={[1]}
            max={1}
            step={0.1}
            value={[isMuted ? 0 : volume]}
            onValueChange={handleVolumeChange}
            className="w-20"
          />
        </div>
      </div>
    </div>
  )
}
