import { useState } from 'react'
import { usePodcasts } from '../../hooks/usePodcasts'
import { Podcast } from '../../types/podcast'

function Pagination({ currentPage, totalPages, onPageChange }: { 
  currentPage: number
  totalPages: number
  onPageChange: (page: number) => void 
}) {
  if (totalPages <= 1) return null;

  return (
    <div className="flex justify-center gap-2 mt-4">
      {[...Array(totalPages)].map((_, i) => (
        <button
          key={i}
          onClick={() => onPageChange(i)}
          className={`px-3 py-1 rounded ${
            currentPage === i
              ? 'bg-primary text-primary-foreground'
              : 'bg-white hover:bg-gray-50'
          }`}
        >
          {i + 1}
        </button>
      ))}
    </div>
  );
}

export function PodcastList() {
  const [currentPage, setCurrentPage] = useState(0)
  const { podcasts, loading, error, totalPages } = usePodcasts(currentPage)

  if (loading) {
    return (
      <div className="animate-pulse">
        {[...Array(5)].map((_, i) => (
          <div key={i} className="bg-gray-100 h-24 mb-4 rounded-lg"></div>
        ))}
      </div>
    )
  }

  if (error) {
    return (
      <div className="bg-red-50 text-red-500 p-4 rounded-lg">
        Failed to load podcasts: {error.message}
      </div>
    )
  }

  if (podcasts.length === 0) {
    return (
      <div className="bg-white rounded-lg shadow">
        <div className="p-4">
          <p className="text-gray-600">No podcasts yet. Create your first podcast to get started.</p>
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {podcasts.map((podcast) => (
        <PodcastCard key={podcast.id} podcast={podcast} />
      ))}
      
      <Pagination 
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </div>
  )
}

function PodcastCard({ podcast }: { podcast: Podcast }) {
  return (
    <div className="bg-white rounded-lg shadow p-4">
      <div className="flex items-center justify-between">
        <div>
          <h3 className="text-lg font-semibold">{podcast.title}</h3>
          <p className="text-gray-600 mt-1">{podcast.description}</p>
          <div className="flex gap-4 mt-2 text-sm text-gray-500">
            <span>{Math.floor(podcast.length / 60)} minutes</span>
            <span>•</span>
            <span>Status: {podcast.status}</span>
          </div>
        </div>
        <div className="flex items-center gap-2">
          {podcast.status === 'COMPLETED' && (
            <button className="p-2 rounded-full hover:bg-gray-100">
              <span className="sr-only">Play</span>
              ▶️
            </button>
          )}
          <button className="p-2 rounded-full hover:bg-gray-100">
            <span className="sr-only">Menu</span>
            ⋮
          </button>
        </div>
      </div>
    </div>
  )
}
