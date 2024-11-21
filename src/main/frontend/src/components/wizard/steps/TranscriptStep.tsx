import { useState, useEffect } from 'react'

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

  useEffect(() => {
    // Only generate if no messages exist yet
    if (messages.length === 0) {
      generateTranscript()
    }
  }, []) // Empty dependency array means this runs once on mount

  const generateTranscript = async () => {
    try {
      setIsGenerating(true)
      setError(null)

      const podcastId = localStorage.getItem('currentPodcastId')
      if (!podcastId) {
        throw new Error('No podcast ID found')
      }

      // Get podcast details first
      const podcastResponse = await fetch(`/api/podcasts/${podcastId}`)
      if (!podcastResponse.ok) {
        throw new Error('Failed to fetch podcast details')
      }
      const podcast = await podcastResponse.json()

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
          podcastId: parseInt(podcastId),
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
    <div className="p-6">
      <div className="bg-white rounded-lg border p-4 mb-6">
        <div className="space-y-4">
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
      </div>

      <div className="flex justify-between">
        <button
          onClick={onBack}
          className="px-4 py-2 border rounded hover:bg-gray-50"
        >
          Back
        </button>
        <div className="space-x-2">
          <button
            onClick={generateTranscript}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            Regenerate
          </button>
          <button
            onClick={onSubmit}
            className="bg-primary text-primary-foreground px-4 py-2 rounded"
          >
            Generate Podcast
          </button>
        </div>
      </div>
    </div>
  )
}
