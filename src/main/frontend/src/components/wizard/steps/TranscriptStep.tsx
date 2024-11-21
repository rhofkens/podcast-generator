import { useState, useEffect, useRef } from 'react'
import { formatTime } from '../../../utils/timeFormat'

interface Message {
  participantId: number
  content: string
  timing: number
}

interface TranscriptStepProps {
  messages: Message[]
  participants: Array<{ id: number; name: string }>
  onChange: (messages: Message[]) => void
  onBack: () => void
  onSubmit: () => void
}

export function TranscriptStep({ messages, participants, onChange, onBack, onSubmit }: TranscriptStepProps) {
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editMode, setEditMode] = useState(false)
  const chatContainerRef = useRef<HTMLDivElement>(null)

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight
    }
  }, [messages])

  const getParticipantName = (participantId: number) => {
    return participants.find(p => p.id === participantId)?.name || 'Unknown'
  }

  useEffect(() => {
    console.log('TranscriptStep mounted/updated:', {
      messagesLength: messages.length,
      participantsLength: participants.length,
      shouldGenerate: messages.length === 0 && participants.length >= 2
    })
    
    // Only generate if no messages exist yet and we have participants
    if (messages.length === 0 && participants.length >= 2) {
      console.log('Starting automatic transcript generation')
      generateTranscript()
    }
  }, [messages.length, participants.length, onChange]) // Include onChange as it's used in generateTranscript

  const generateTranscript = async () => {
    console.log('Generating transcript...')
    try {
      setIsGenerating(true)
      setError(null)

      const podcastId = localStorage.getItem('currentPodcastId')
      console.log('Retrieved podcastId:', podcastId)
      if (!podcastId) {
        throw new Error('No podcast ID found')
      }

      // Get participants
      const participantsResponse = await fetch(`/api/participants/podcast/${podcastId}`)
      if (!participantsResponse.ok) {
        throw new Error('Failed to fetch participants')
      }
      const participantsList = await participantsResponse.json()

      // Generate transcript
      const transcriptResponse = await fetch(`/api/podcasts/${podcastId}/generate-transcript`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          participants: participantsList
        })
      })

      if (!transcriptResponse.ok) {
        throw new Error('Failed to generate transcript')
      }

      const transcriptData = await transcriptResponse.json()
      
      // Convert the transcript data to messages format
      const newMessages = transcriptData.transcript.map((entry: any) => ({
        participantId: participants.find(p => p.name === entry.speakerName)?.id || participants[0].id,
        content: entry.text,
        timing: entry.timeOffset
      }))

      onChange(newMessages)

    } catch (err) {
      console.error('Error generating transcript:', err)
      setError(err instanceof Error ? err.message : 'Failed to generate transcript')
    } finally {
      setIsGenerating(false)
    }
  }

  const updateMessage = (index: number, field: keyof Message, value: any) => {
    const updated = [...messages]
    updated[index] = { ...updated[index], [field]: value }
    onChange(updated)
  }

  if (isGenerating) {
    return (
      <div className="p-6 flex flex-col items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-4"></div>
        <p>Generating transcript...</p>
      </div>
    )
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
          {error}
        </div>
        <div className="flex justify-between">
          <button
            onClick={onBack}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            Back
          </button>
          <button
            onClick={generateTranscript}
            className="bg-primary text-primary-foreground px-4 py-2 rounded"
          >
            Retry Generation
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="p-6 flex flex-col h-[calc(100vh-200px)]">
      <div className="flex justify-between mb-4">
        <button
          onClick={() => setEditMode(!editMode)}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          {editMode ? 'View Mode' : 'Edit Mode'}
        </button>
        <div className="space-x-2">
          <button
            onClick={generateTranscript}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Regenerate
          </button>
          <button
            onClick={onSubmit}
            className="px-4 py-2 text-sm font-medium text-white bg-primary rounded-md hover:bg-primary/90"
          >
            Generate Podcast
          </button>
        </div>
      </div>

      {editMode ? (
        <div className="flex-1 overflow-y-auto bg-white rounded-lg border p-4 space-y-4">
          {messages.map((message, index) => (
            <div key={index} className="flex gap-4">
              <div className="w-48">
                <select
                  value={message.participantId}
                  onChange={(e) => updateMessage(index, 'participantId', parseInt(e.target.value))}
                  className="w-full p-2 border rounded"
                >
                  {participants.map((p) => (
                    <option key={p.id} value={p.id}>
                      {p.name}
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex-1">
                <textarea
                  value={message.content}
                  onChange={(e) => updateMessage(index, 'content', e.target.value)}
                  className="w-full p-2 border rounded"
                  rows={2}
                />
              </div>
              <div className="w-24">
                <input
                  type="number"
                  value={message.timing}
                  onChange={(e) => updateMessage(index, 'timing', parseInt(e.target.value))}
                  className="w-full p-2 border rounded"
                  min={0}
                  step={1}
                />
              </div>
            </div>
          ))}
        </div>
      ) : (
        <div 
          ref={chatContainerRef}
          className="flex-1 overflow-y-auto bg-gray-100 rounded-lg p-4 space-y-4"
        >
          {messages.map((message, index) => (
            <div key={index} className="flex flex-col max-w-[80%]">
              <div className="bg-white rounded-lg shadow-sm p-4">
                <div className="flex justify-between items-baseline mb-2">
                  <span className="font-medium text-primary">
                    {getParticipantName(message.participantId)}
                  </span>
                  <span className="text-xs text-gray-500">
                    {formatTime(message.timing)}
                  </span>
                </div>
                <p className="text-gray-800 whitespace-pre-wrap">{message.content}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      <div className="flex justify-between mt-4">
        <button
          onClick={onBack}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Back
        </button>
      </div>
    </div>
  )
}
