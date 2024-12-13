import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Tabs, TabsList, TabsTrigger, TabsContent } from '../ui/tabs'

interface PodcastMetadata {
  title: string
  description: string
  length: number
  contextDescription: string
  contextUrl?: string
}

interface Participant {
  id: number
  name: string
  gender: string
  age: number
  role: string
  roleDescription: string
  voiceCharacteristics: string
  voicePreviewUrl?: string
}

interface TranscriptMessage {
  participantId: number
  content: string
  timing: number
}

interface Transcript {
  content: {
    messages: TranscriptMessage[]
  }
}
import { Button } from '../ui/button'
import { MetadataTab } from './edit-tabs/MetadataTab'
import { ParticipantsTab } from './edit-tabs/ParticipantsTab'
import { TranscriptTab } from './edit-tabs/TranscriptTab'
import { GenerateTab } from './edit-tabs/GenerateTab'
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

export function PodcastEditView() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [activeTab, setActiveTab] = useState('metadata')
  const [hasUnsavedChanges, setHasUnsavedChanges] = useState(false)
  const [showSaveDialog, setShowSaveDialog] = useState(false)
  const [isLoading, setIsLoading] = useState(false)

  // Track changes in each tab
  const [metadata, setMetadata] = useState<PodcastMetadata | null>(null)
  const [participants, setParticipants] = useState<Participant[]>([])
  const [transcript, setTranscript] = useState<Transcript | null>(null)

  useEffect(() => {
    loadPodcastData()
  }, [id])

  const loadPodcastData = async () => {
    setIsLoading(true)
    try {
      // Load all podcast data using the same endpoints as the wizard
      const [podcastRes, contextRes, participantsRes, transcriptRes] = await Promise.all([
        fetch(`/api/podcasts/${id}`),
        fetch(`/api/contexts/podcast/${id}`),
        fetch(`/api/participants/podcast/${id}`),
        fetch(`/api/transcripts/podcast/${id}`)
      ])

      const [podcast, context, participants, transcript] = await Promise.all([
        podcastRes.json(),
        contextRes.json(),
        participantsRes.json(),
        transcriptRes.json()
      ])

      console.log('PodcastEditView loaded transcript data:', transcript);

      setMetadata({
        title: podcast.title,
        description: podcast.description,
        length: podcast.length,
        contextDescription: context?.descriptionText,
        contextUrl: context?.sourceUrl
      })
      setParticipants(participants)
    
      // Ensure transcript is in the correct format before setting state
      const formattedTranscript = Array.isArray(transcript) ? transcript[0] : transcript;
      setTranscript(formattedTranscript)
      
    } catch (error) {
      console.error('Error loading podcast data:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleSave = async () => {
    if (!metadata || !id) return

    setIsLoading(true)
    try {
      // Save all changes using PUT endpoints
      await Promise.all([
        // Save podcast metadata
        fetch(`/api/podcasts/${id}`, {
          method: 'PUT',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({
            title: metadata.title,
            description: metadata.description,
            length: metadata.length,
            userId: 'dev-user-123',
            status: 'DRAFT'
          })
        }),
      
        // For context, first get the context ID then update it
        (async () => {
          // Get the context for this podcast
          const contextResponse = await fetch(`/api/contexts/podcast/${id}`);
          if (!contextResponse.ok) {
            throw new Error('Failed to fetch context');
          }
          const existingContext = await contextResponse.json();
        
          // Update the context using its ID
          const updateResponse = await fetch(`/api/contexts/${existingContext.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              id: existingContext.id,
              descriptionText: metadata.contextDescription,
              sourceUrl: metadata.contextUrl,
              podcast: { id: parseInt(id) }
            })
          });
        
          if (!updateResponse.ok) {
            throw new Error('Failed to update context');
          }
        
          return updateResponse.json();
        })(),
      
        // Save participants
        ...participants.map(participant => 
          fetch(`/api/participants/${participant.id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              ...participant,
              podcast: { id: parseInt(id) }
            })
          })
        ),
        // Save transcript
        transcript && (async () => {
          // First get the transcript ID
          const transcriptResponse = await fetch(`/api/transcripts/podcast/${id}`);
          if (!transcriptResponse.ok) {
            throw new Error('Failed to fetch transcript');
          }
          const transcripts = await transcriptResponse.json();
          if (!transcripts || !transcripts[0]?.id) {
            throw new Error('No transcript ID found');
          }

          // Use the direct PUT endpoint with the transcript ID
          return fetch(`/api/transcripts/${transcripts[0].id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
              id: transcripts[0].id,
              podcast: { id: parseInt(id) },
              content: transcript.content
            })
          });
        })()
      ].filter(Boolean)) // Filter out null values from Promise.all

      setHasUnsavedChanges(false)
    } catch (error) {
      console.error('Error saving podcast:', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleGenerate = () => {
    if (hasUnsavedChanges) {
      setShowSaveDialog(true)
    } else {
      setActiveTab('generate')
    }
  }

  return (
    <div className="p-6">
      <Tabs value={activeTab} onValueChange={setActiveTab}>
        <TabsList className="mb-4">
          <TabsTrigger value="metadata">Metadata</TabsTrigger>
          <TabsTrigger value="participants">Participants</TabsTrigger>
          <TabsTrigger value="transcript">Transcript</TabsTrigger>
          <TabsTrigger 
            value="generate"
            className={activeTab === 'generate' ? 'bg-primary text-primary-foreground' : ''}
          >
            Generate Podcast
          </TabsTrigger>
        </TabsList>

        <TabsContent value="metadata">
          {metadata && (
            <MetadataTab 
              data={metadata} 
              onChange={(data: PodcastMetadata) => {
                setMetadata(data)
                setHasUnsavedChanges(true)
              }} 
            />
          )}
        </TabsContent>

        <TabsContent value="participants">
          <ParticipantsTab 
            participants={participants}
            onChange={(data: Participant[]) => {
              setParticipants(data)
              setHasUnsavedChanges(true)
            }}
          />
        </TabsContent>

        <TabsContent value="transcript">
          {transcript && (
            <TranscriptTab 
              transcript={transcript}
              participants={participants}
              onChange={(data: Transcript) => {
                setTranscript(data)
                setHasUnsavedChanges(true)
              }}
              podcastId={id!}
            />
          )}
        </TabsContent>

        <TabsContent value="generate">
          <GenerateTab 
            podcastId={id!}
            onBack={() => setActiveTab('transcript')}
          />
        </TabsContent>
      </Tabs>

      <div className="flex justify-between mt-6">
        <Button
          variant="outline"
          onClick={() => navigate('/podcasts')}
        >
          Cancel
        </Button>

        <div className="space-x-2">
          <Button
            variant="outline"
            onClick={handleSave}
            disabled={!hasUnsavedChanges || isLoading}
          >
            {isLoading ? 'Saving...' : 'Save'}
          </Button>

          <Button
            onClick={handleGenerate}
            disabled={isLoading}
            className="bg-primary text-primary-foreground"
          >
            Generate Podcast
          </Button>
        </div>
      </div>

      <AlertDialog open={showSaveDialog} onOpenChange={setShowSaveDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Unsaved Changes</AlertDialogTitle>
            <AlertDialogDescription>
              You have unsaved changes. Would you like to save them before generating the podcast?
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction
              onClick={async () => {
                await handleSave()
                setActiveTab('generate')
              }}
            >
              Save & Generate
            </AlertDialogAction>
            <AlertDialogAction
              onClick={() => {
                setHasUnsavedChanges(false)
                setActiveTab('generate')
              }}
            >
              Discard & Generate
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </div>
  )
}
