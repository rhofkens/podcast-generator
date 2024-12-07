import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { AudioPlayer } from '../AudioPlayer'
import type { Podcast } from '../../types/podcast'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { Button } from "@/components/ui/button"
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

interface PodcastCardProps {
  podcast: Podcast
}

function PodcastCard({ podcast }: PodcastCardProps) {
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

export function PodcastList() {
  const [podcasts, setPodcasts] = useState<Podcast[]>([])
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
    const consoleRef = useRef<HTMLDivElement>(null);
    const [generationState, setGenerationState] = useState<GenerationState>({
        status: '',
        progress: 0,
        message: null
    });
    const [error, setError] = useState<string | null>(null);
    const [consoleMessages, setConsoleMessages] = useState<string[]>([]);
    const [isCancelling, setIsCancelling] = useState(false);

    const handleCancel = async () => {
        setIsCancelling(true);
        try {
            await fetch(`/api/podcasts/${podcastId}/generate/cancel`, {
                method: 'POST'
            });
            onBack();
        } catch (error) {
            setError('Failed to cancel generation');
            setIsCancelling(false);
        }
    };

    const handleContinueInBackground = () => {
        onComplete();
    };

    const handleRegenerate = async () => {
        setConsoleMessages([]);
        setGenerationState({
            status: '',
            progress: 0,
            message: null
        });
        startGeneration();
    };

    const startGeneration = async () => {
        if (!podcastId) {
            setError('No podcast ID found');
            return null;
        }

        try {
            const response = await fetch(`/api/podcasts/${podcastId}/generate`, {
                method: 'POST'
            });

            if (!response.ok) {
                throw new Error('Failed to start podcast generation');
            }

            const ws = new PodcastGenerationWebSocket(podcastId);
            
            ws.onMessage((data) => {
                setGenerationState({
                    status: data.status,
                    progress: data.progress,
                    message: data.message,
                    audioUrl: data.audioUrl
                });
                
                setConsoleMessages(prev => [...prev, `[${data.status}] ${data.message}`]);
                
                if (data.status === 'COMPLETED') {
                    onComplete();
                } else if (data.status === 'ERROR') {
                    setError(data.message || 'An error occurred during generation');
                }
            });

            return ws;

        } catch (err) {
            setError(err instanceof Error ? err.message : 'Failed to start generation');
            return null;
        }
    };

    useEffect(() => {
        setConsoleMessages([]);
    }, []);

    useEffect(() => {
        if (consoleRef.current) {
            consoleRef.current.scrollTop = consoleRef.current.scrollHeight;
        }
    }, [consoleMessages]);

    useEffect(() => {
        let ws: PodcastGenerationWebSocket | null = null;

        const initializeGeneration = async () => {
            if (!podcastId) return;

            // Fetch initial status
            try {
                const response = await fetch(`/api/podcasts/${podcastId}`);
                if (response.ok) {
                    const podcast = await response.json();
                    if (podcast.generationStatus) {
                        setGenerationState({
                            status: podcast.generationStatus,
                            progress: podcast.generationProgress || 0,
                            message: podcast.generationMessage
                        });
                        setConsoleMessages([`[${podcast.generationStatus}] ${podcast.generationMessage}`]);
                    }
                }
            } catch (error) {
                console.error('Failed to fetch initial status:', error);
            }

            // Start generation and connect WebSocket
            ws = await startGeneration();
        };

        initializeGeneration();

        return () => {
            if (ws) {
                ws.close();
            }
        };
    }, [podcastId, onComplete]);

    return (
        <div className="p-6">
            <div className="max-w-2xl mx-auto">
                <h2 className="text-2xl font-bold mb-4">Generating Podcast</h2>
                
                <div className="mb-6">
                    <div 
                        ref={consoleRef}
                        className="bg-black text-green-400 font-mono p-4 rounded-lg h-64 overflow-y-auto"
                    >
                        {consoleMessages.map((message, index) => (
                            <div key={index} className="whitespace-pre-wrap">
                                {`> ${message}`}
                            </div>
                        ))}
                    </div>
                </div>

                {!error && (
                    <div className="relative h-8 bg-gray-200 rounded-full mb-6">
                        <div 
                            className="h-full bg-primary rounded-full transition-all duration-500"
                            style={{ width: `${generationState.progress}%` }}
                        />
                        <div className="absolute inset-0 flex items-center justify-center text-sm font-medium">
                            <span className={generationState.progress > 50 ? "text-white" : "text-black"}>
                                {`${Math.round(generationState.progress)}%`}
                            </span>
                        </div>
                    </div>
                )}

                {error && (
                    <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-6">
                        {error}
                    </div>
                )}

                {generationState.status === 'COMPLETED' && generationState.audioUrl && (
                    <div className="mb-6">
                        <AudioPlayer audioUrl={generationState.audioUrl} />
                    </div>
                )}

                <div className="flex justify-between">
                    <button
                        onClick={onBack}
                        className="px-4 py-2 border rounded hover:bg-gray-50"
                    >
                        Back
                    </button>

                    <div className="flex gap-4">
                        {generationState.status === 'COMPLETED' ? (
                            <button
                                onClick={handleRegenerate}
                                className="px-4 py-2 bg-primary text-white rounded hover:bg-primary/90"
                            >
                                Regenerate
                            </button>
                        ) : (
                            <>
                                <button
                                    onClick={handleCancel}
                                    disabled={isCancelling}
                                    className="px-4 py-2 bg-red-500 text-white rounded hover:bg-red-600 disabled:opacity-50"
                                >
                                    {isCancelling ? 'Cancelling...' : 'Cancel'}
                                </button>
                                <button
                                    onClick={handleContinueInBackground}
                                    className="px-4 py-2 bg-gray-500 text-white rounded hover:bg-gray-600"
                                >
                                    Continue in Background
                                </button>
                            </>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
}
