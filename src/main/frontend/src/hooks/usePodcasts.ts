import { useState, useEffect } from 'react'
import { Podcast, PodcastsResponse } from '../types/podcast'

export function usePodcasts(page: number = 0, size: number = 5) {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<Error | null>(null)
  const [podcasts, setPodcasts] = useState<Podcast[]>([])
  const [totalPages, setTotalPages] = useState(0)

  useEffect(() => {
    const fetchPodcasts = async () => {
      try {
        setLoading(true)
        const response = await fetch(`/api/podcasts?page=${page}&size=${size}`)
        if (!response.ok) {
          throw new Error('Failed to fetch podcasts')
        }
        const data: PodcastsResponse = await response.json()
        setPodcasts(data.content)
        setTotalPages(data.totalPages)
        setError(null)
      } catch (err) {
        setError(err instanceof Error ? err : new Error('Unknown error occurred'))
      } finally {
        setLoading(false)
      }
    }

    fetchPodcasts()
  }, [page, size])

  return { podcasts, loading, error, totalPages }
}
