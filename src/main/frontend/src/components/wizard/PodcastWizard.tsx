import { useState, useEffect } from 'react'
import { useParams } from 'react-router-dom'
import { WizardStepBar } from './WizardStepBar'
import { MetadataStep } from './steps/MetadataStep'
import { ParticipantsStep } from './steps/ParticipantsStep'
import { TranscriptStep } from './steps/TranscriptStep'
import { PodcastStep } from './steps/PodcastStep'

const STEPS = [
  {
    title: 'Metadata',
    description: 'Basic information'
  },
  {
    title: 'Participants',
    description: 'Define speakers'
  },
  {
    title: 'Transcript',
    description: 'Review content'
  },
  {
    title: 'Podcast',
    description: 'Generate audio'
  }
]

interface Participant {
  id?: number
  name: string
  gender: string
  age: number
  role: string
  roleDescription: string
  voiceCharacteristics: string
  isNew?: boolean
}

interface Message {
  participantId: number
  content: string
  timing: number
}

interface PodcastWizardProps {
  editMode?: boolean;
  podcastId?: string;
}

export function PodcastWizard({ editMode = false }: PodcastWizardProps) {
  const { id } = useParams()
  const [currentStep, setCurrentStep] = useState(0)
  const [podcastId, setPodcastId] = useState<string | null>(
    id || localStorage.getItem('currentPodcastId')
  )
  const [isLoading, setIsLoading] = useState(editMode)
  const [initialData, setInitialData] = useState<{
    metadata: {
      title: string;
      description: string;
      length: number;
      contextDescription: string;
      contextUrl?: string;
      contextFile?: File;
    };
    participants: Participant[];
    messages: Message[];
  } | null>(null)

  useEffect(() => {
    if (editMode && id) {
      setPodcastId(id)
      localStorage.setItem('currentPodcastId', id)
      const loadPodcastData = async () => {
        setIsLoading(true)
        try {
          // Load podcast metadata
          const podcastResponse = await fetch(`/api/podcasts/${podcastId}`)
          const podcastData = await podcastResponse.json()

          // Load context
          const contextResponse = await fetch(`/api/contexts/podcast/${podcastId}`)
          const contextData = await contextResponse.json()

          // Load participants
          const participantsResponse = await fetch(`/api/participants/podcast/${podcastId}`)
          const participantsData = await participantsResponse.json()

          // Load transcript
          const transcriptResponse = await fetch(`/api/transcripts/podcast/${podcastId}`)
          const transcriptData = await transcriptResponse.json()

          setInitialData({
            metadata: {
              title: podcastData.title || '',
              description: podcastData.description || '',
              length: podcastData.length || 30,
              contextDescription: contextData?.descriptionText || '',
              contextUrl: contextData?.sourceUrl || undefined,
            },
            participants: participantsData || [],
            messages: transcriptData?.content?.messages || transcriptData?.messages || []
          })
        } catch (error) {
          console.error('Error loading podcast data:', error)
        } finally {
          setIsLoading(false)
        }
      }

      loadPodcastData()
    }

    return () => {
      if (!editMode) {
        localStorage.removeItem('currentPodcastId')
      }
    }
  }, [editMode, podcastId])

  const handleStepComplete = (step: number) => {
    console.log('handleStepComplete called:', { step, currentStep })
    if (step === 0) {
      const newPodcastId = localStorage.getItem('currentPodcastId')
      console.log('Setting new podcastId:', newPodcastId)
      setPodcastId(newPodcastId)
    }
    setCurrentStep(step + 1)
  }

  useEffect(() => {
    console.log('PodcastWizard step changed:', { currentStep })
  }, [currentStep])

  useEffect(() => {
    if (initialData) {
      setMetadata({
        title: initialData.metadata.title || '',
        description: initialData.metadata.description || '',
        length: initialData.metadata.length || 30,
        contextDescription: initialData.metadata.contextDescription || '',
        contextUrl: initialData.metadata.contextUrl,
        contextFile: undefined
      })
      setParticipants(initialData.participants)
      setMessages(initialData.messages)
    }
  }, [initialData])
  const [metadata, setMetadata] = useState({
    title: '',
    description: '',
    length: 30,
    contextDescription: '',
    contextUrl: undefined as string | undefined,
    contextFile: undefined as File | undefined
  })
  const [participants, setParticipants] = useState<Participant[]>([])
  const [messages, setMessages] = useState<Message[]>([])

  const handleMetadataChange = (field: string, value: any) => {
    setMetadata(prev => ({ ...prev, [field]: value }))
  }


  const renderStep = () => {
    if (isLoading) {
      return (
        <div className="flex justify-center items-center h-64">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      )
    }

    switch (currentStep) {
      case 0:
        return (
          <MetadataStep
            data={metadata}
            onChange={handleMetadataChange}
            onNext={() => handleStepComplete(0)}
          />
        )
      case 1:
        return (
          <ParticipantsStep
            podcastId={podcastId}
            participants={participants}
            onChange={setParticipants}
            onNext={() => setCurrentStep(2)}
            onBack={() => setCurrentStep(0)}
          />
        )
      case 2:
        console.log('Rendering TranscriptStep with:', {
          messages,
          participants: participants
            .filter((p): p is Participant & { id: number } => p.id !== undefined)
            .map(p => ({ 
              id: p.id,
              name: p.name 
            })),
          messagesLength: messages.length,
          participantsLength: participants.length
        })
        return (
          <TranscriptStep
            messages={messages}
            participants={participants
              .filter((p): p is Participant & { id: number } => p.id !== undefined)
              .map(p => ({ 
                id: p.id,
                name: p.name 
              }))}
            onChange={setMessages}
            onBack={() => setCurrentStep(1)}
            onNext={() => setCurrentStep(3)}
          />
        )
      case 3:
        return (
          <PodcastStep
            podcastId={podcastId}
            onBack={() => setCurrentStep(2)}
            onComplete={() => {
              window.location.href = '/podcasts'
            }}
          />
        )
      default:
        return null
    }
  }

  return (
    <div className="bg-gray-50 min-h-screen">
      <WizardStepBar currentStep={currentStep} steps={STEPS} />
      {renderStep()}
    </div>
  )
}
