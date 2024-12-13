import { TranscriptStep } from '../../wizard/steps/TranscriptStep'
import { useState } from 'react'
import { Button } from '../../ui/button'

interface TranscriptTabProps {
  transcript: any
  participants: any[]
  onChange: (transcript: any) => void
  podcastId: string
}

export function TranscriptTab({ transcript, participants, onChange, podcastId }: TranscriptTabProps) {
  const [localEditMode, setLocalEditMode] = useState(false);

  // Transform the transcript data into the expected format
  const messages = Array.isArray(transcript) 
    ? transcript[0]?.content?.messages || transcript[0]?.messages || []
    : transcript?.content?.messages || transcript?.messages || [];
  
  // Ensure participants are in the correct format
  const formattedParticipants = participants.map(p => ({ 
    id: p.id, 
    name: p.name 
  }));

  return (
    <div className="bg-white rounded-lg shadow">
      <div className="flex justify-between p-4">
        <div className="flex gap-2">
          {!localEditMode && (
            <Button
              variant="outline"
              onClick={() => setLocalEditMode(true)}
            >
              Edit
            </Button>
          )}
          <Button
            variant="outline"
            onClick={() => {
              // Trigger transcript regeneration
              // This will be handled by the TranscriptStep component
              const transcriptStep = document.querySelector('button[aria-label="Regenerate"]');
              if (transcriptStep) {
                (transcriptStep as HTMLButtonElement).click();
              }
            }}
          >
            Regenerate
          </Button>
        </div>
      </div>

      <TranscriptStep
        podcastId={podcastId}
        messages={messages}
        participants={formattedParticipants}
        onChange={(updatedMessages) => {
          // Transform the data back to the expected format before calling onChange
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
              };
          onChange(updatedTranscript);
        }}
        onNext={() => {}} // Not used in edit mode
        onBack={() => {}} // Not used in edit mode
        editMode={localEditMode}
      />
    </div>
  )
}
