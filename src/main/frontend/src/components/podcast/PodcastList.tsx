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

import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { AudioPlayer } from '../AudioPlayer'
import { 
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger
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

function getStatusIcon(status: string) {
  switch (status.toLowerCase()) {
    case 'completed':
      return '✅'
    case 'error':
      return '❌'
    case 'queued':
      return '⏳'
    case 'generating_voices':
      return '🎤'
    case 'generating_segments':
      return '🔄'
    case 'stitching':
      return '🔗'
    case 'cancelled':
      return '⏹️'
    default:
      return '📝'
  }
}

function formatStatus(status: string) {
  return status.charAt(0).toUpperCase() + 
         status.slice(1).toLowerCase().replace(/_/g, ' ')
}

function PodcastCard({ podcast }: { podcast: Podcast }) {
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
    localStorage.setItem('currentPodcastId', podcast.id.toString())
    navigate('/podcasts/edit')
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
            <span className="flex items-center gap-1">
              {getStatusIcon(podcast.generationStatus)}
              <span className={
                podcast.generationStatus.toLowerCase() === 'error' 
                  ? 'text-red-600' 
                  : podcast.generationStatus.toLowerCase() === 'completed'
                    ? 'text-green-600'
                    : ''
              }>
                {formatStatus(podcast.generationStatus)}
              </span>
            </span>
          </div>
          
          {podcast.generationStatus.toLowerCase() === 'completed' && podcast.audioUrl && (
            <div className="mt-4">
              <AudioPlayer audioUrl={podcast.audioUrl} />
            </div>
          )}
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
