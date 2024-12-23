import { TranscriptStep } from '../../wizard/steps/TranscriptStep'
import { Button } from '../../ui/button'
import { useState } from 'react'

interface TranscriptTabProps {
  transcript: any
  participants: any[]
  onChange: (transcript: any) => void
  podcastId: string
}

export function TranscriptTab({ transcript, participants, onChange, podcastId }: TranscriptTabProps) {
  const [isGenerating, setIsGenerating] = useState(false)

  // More strict check for valid transcript content
  const hasTranscript = Boolean(
    transcript && 
    !(Array.isArray(transcript) && transcript.length === 0) && // Check for empty array
    (
      (Array.isArray(transcript) && transcript[0]?.content?.messages?.length > 0) ||
      transcript?.content?.messages?.length > 0 ||
      transcript?.messages?.length > 0
    )
  )

  console.log('Current transcript:', transcript); // Debug log
  console.log('Has transcript:', hasTranscript); // Debug log

  const handleGenerateTranscript = async () => {
    setIsGenerating(true)
    try {
      const response = await fetch(`/api/podcasts/${podcastId}/generate-transcript`, {
        method: 'POST'
      })
      
      if (!response.ok) {
        throw new Error('Failed to generate transcript')
      }

      const newTranscript = await response.json()
      onChange(newTranscript)
    } catch (error) {
      console.error('Error generating transcript:', error)
      // You might want to add error handling UI here
    } finally {
      setIsGenerating(false)
    }
  }

  if (!hasTranscript) {
    return (
      <div className="flex flex-col items-center justify-center min-h-[200px] p-8 bg-white rounded-lg shadow">
        <p className="mb-4 text-gray-600 text-center">
          No transcript available. Generate one to get started.
        </p>
        <Button 
          onClick={handleGenerateTranscript}
          disabled={isGenerating}
          className="bg-primary text-primary-foreground hover:bg-primary/90"
        >
          {isGenerating ? 'Generating...' : 'Generate Transcript'}
        </Button>
      </div>
    )
  }

  // Transform the transcript data into the expected format
  const messages = Array.isArray(transcript) 
    ? transcript[0]?.content?.messages || transcript[0]?.messages || []
    : transcript?.content?.messages || transcript?.messages || []
  
  // Ensure participants are in the correct format
  const formattedParticipants = participants.map(p => ({ 
    id: p.id, 
    name: p.name 
  }))

  return (
    <div className="bg-white rounded-lg shadow">
      <TranscriptStep
        podcastId={podcastId}
        messages={messages}
        participants={formattedParticipants}
        onChange={(updatedMessages) => {
          const updatedTranscript = Array.isArray(transcript)
            ? [{
                ...transcript[0],
                content: {
                  messages: updatedMessages
                }
              }]
            : {
                ...transcript,
                content: {
                  messages: updatedMessages
                }
              }
          onChange(updatedTranscript)
        }}
        onNext={() => {}} // Not used in edit mode
        onBack={() => {}} // Not used in edit mode
        editMode={false}
        hideNavigation={true}
      />
    </div>
  )
}
