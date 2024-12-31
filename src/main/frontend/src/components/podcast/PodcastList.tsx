import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { AudioPlayer } from '../audio/AudioPlayer'
import type { Podcast } from '../../types/podcast'

import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../ui/alert-dialog'
import { Button } from '../ui/button'
import { MoreVertical, Pencil, Trash2 } from 'lucide-react'

// Extended Podcast type for our needs
interface ExtendedPodcast extends Podcast {
  generationStatus?: string;
  audioUrl?: string;
  generationProgress?: number;
  hasAudio?: boolean;
}

function formatStatus(status: string) {
  return status
    ? status.charAt(0).toUpperCase() + 
      status.slice(1).toLowerCase().replace(/_/g, ' ')
    : 'Draft'
}

interface PodcastCardProps {
  podcast: ExtendedPodcast;
}

function PodcastCard({ podcast }: PodcastCardProps) {
  // Debug logging before render
  console.debug('Rendering podcast card:', {
    id: podcast.id,
    hasAudio: podcast.hasAudio,
    audioUrl: podcast.audioUrl,
    status: podcast.status,
    generationStatus: podcast.generationStatus
  });

  const navigate = useNavigate()
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)

  const handleDelete = async () => {
    try {
      const response = await fetch(`/api/podcasts/${podcast.id}`, {
        method: 'DELETE'
      })
      if (response.ok) {
        window.location.reload()
      } else {
        throw new Error('Failed to delete podcast')
      }
    } catch (error) {
      console.error('Error deleting podcast:', error)
    } finally {
      setShowDeleteDialog(false)
    }
  }

  const handleEdit = () => {
    navigate(`/podcasts/edit/${podcast.id}`)
  }

  return (
    <div className="bg-white rounded-lg shadow p-4">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <h3 className="text-lg font-semibold">{podcast.title}</h3>
          <p className="text-gray-600 mt-1">{podcast.description}</p>
          <div className="flex gap-4 mt-2 text-sm text-gray-500">
            <span>{Math.floor(podcast.length / 60)} minutes</span>
            <span>•</span>
            <span className={
              podcast.generationStatus?.toLowerCase() === 'error' 
                ? 'text-red-600' 
                : podcast.generationStatus?.toLowerCase() === 'completed'
                  ? 'text-green-600'
                  : 'text-gray-600'
            }>
              {formatStatus(podcast.generationStatus || '')}
            </span>
          </div>
          
          {(() => {
            console.debug('Rendering audio section:', {
              hasAudio: podcast.hasAudio,
              audioUrl: podcast.audioUrl,
              generationStatus: podcast.generationStatus
            });
            return null;
          })()}
          
          {podcast.hasAudio && podcast.audioUrl ? (
            <div className="mt-4 border-t pt-4">
              <div className="flex items-center gap-2 text-sm text-gray-500 mb-2">
                <span className="flex items-center">
                  <svg 
                    xmlns="http://www.w3.org/2000/svg" 
                    className="h-4 w-4 mr-1" 
                    viewBox="0 0 20 20" 
                    fill="currentColor"
                  >
                    <path 
                      fillRule="evenodd" 
                      d="M10 18a8 8 0 100-16 8 8 0 000 16zM9.555 7.168A1 1 0 008 8v4a1 1 0 001.555.832l3-2a1 1 0 000-1.664l-3-2z" 
                      clipRule="evenodd" 
                    />
                  </svg>
                  Generated Audio
                </span>
              </div>
              <AudioPlayer podcastId={podcast.id} />
            </div>
          ) : podcast.generationStatus?.toLowerCase() === 'processing' ? (
            <div className="mt-4 border-t pt-4">
              <div className="flex items-center gap-2 text-sm text-gray-500">
                <span className="flex items-center">
                  <svg 
                    className="animate-spin h-4 w-4 mr-1" 
                    xmlns="http://www.w3.org/2000/svg" 
                    fill="none" 
                    viewBox="0 0 24 24"
                  >
                    <circle 
                      className="opacity-25" 
                      cx="12" 
                      cy="12" 
                      r="10" 
                      stroke="currentColor" 
                      strokeWidth="4"
                    />
                    <path 
                      className="opacity-75" 
                      fill="currentColor" 
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  Generating Audio...
                </span>
              </div>
            </div>
          ) : null}
        </div>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-8 w-8 p-0">
              <MoreVertical className="h-4 w-4" />
              <span className="sr-only">Open menu</span>
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" className="w-[160px]">
            <DropdownMenuItem onClick={handleEdit} className="cursor-pointer">
              <Pencil className="mr-2 h-4 w-4" />
              <span>Edit</span>
            </DropdownMenuItem>
            <DropdownMenuItem 
              onClick={() => setShowDeleteDialog(true)} 
              className="text-red-600 cursor-pointer"
            >
              <Trash2 className="mr-2 h-4 w-4" />
              <span>Delete</span>
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the podcast "{podcast.title}" and all its associated files. 
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDelete}
              className="bg-red-600 hover:bg-red-700 focus:ring-red-600"
            >
              Delete
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}

export function PodcastList() {
  const [podcasts, setPodcasts] = useState<ExtendedPodcast[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchPodcasts = async () => {
      try {
        const response = await fetch('/api/podcasts')
        if (!response.ok) {
          throw new Error('Failed to fetch podcasts')
        }
        const data = await response.json()
        setPodcasts(data.content)
      } catch (err) {
        setError(err instanceof Error ? err.message : 'Failed to load podcasts')
      } finally {
        setLoading(false)
      }
    }

    fetchPodcasts()
  }, [])

  if (loading) {
    return (
      <div className="flex justify-center items-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-8">
        <div className="bg-red-50 text-red-500 p-4 rounded-lg">
          {error}
        </div>
      </div>
    )
  }

  if (podcasts.length === 0) {
    return (
      <div className="text-center p-8 text-gray-500">
        No podcasts found. Create your first podcast to get started!
      </div>
    )
  }

  return (
    <div className="space-y-4">
      {podcasts.map((podcast) => (
        <PodcastCard key={podcast.id} podcast={podcast} />
      ))}
    </div>
  )
}
