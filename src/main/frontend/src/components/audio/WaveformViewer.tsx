import { useEffect, useRef, useState } from 'react'
import WaveSurfer from 'wavesurfer.js'

interface WaveformViewerProps {
  url: string
  onReady?: () => void
  playing?: boolean
  onPlayPause?: (isPlaying: boolean) => void
}

export function WaveformViewer({ url, onReady, playing = false, onPlayPause }: WaveformViewerProps) {
  const containerRef = useRef<HTMLDivElement>(null)
  const wavesurferRef = useRef<WaveSurfer | null>(null)
  const [isReady, setIsReady] = useState(false)

  useEffect(() => {
    if (!containerRef.current) return

    const wavesurfer = WaveSurfer.create({
      container: containerRef.current,
      waveColor: '#4B5563', // Tailwind gray-600
      progressColor: '#6366F1', // Tailwind indigo-500
      cursorColor: '#4F46E5', // Tailwind indigo-600
      barWidth: 2,
      barGap: 1,
      barRadius: 3,
      height: 48,
      normalize: true,
      autoScroll: true,
    })

    wavesurfer.load(url)

    wavesurfer.on('ready', () => {
      setIsReady(true)
      onReady?.()
    })

    wavesurfer.on('play', () => onPlayPause?.(true))
    wavesurfer.on('pause', () => onPlayPause?.(false))

    wavesurferRef.current = wavesurfer

    return () => {
      wavesurfer.destroy()
    }
  }, [url])

  useEffect(() => {
    if (!wavesurferRef.current || !isReady) return

    if (playing) {
      wavesurferRef.current.play()
    } else {
      wavesurferRef.current.pause()
    }
  }, [playing, isReady])

  return (
    <div className="relative w-full">
      <div ref={containerRef} className="w-full" />
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
  )
}
