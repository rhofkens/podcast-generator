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

  const generateTranscript = async () => {
    try {
      const participantsResponse = await fetch(`/api/participants/podcast/${podcastId}`);
      if (!participantsResponse.ok) {
        throw new Error('Failed to fetch participants');
      }
      const participantsList = await participantsResponse.json();

      const transcriptResponse = await fetch(`/api/podcasts/${podcastId}/generate-transcript`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          participants: participantsList
        })
      });

      if (!transcriptResponse.ok) {
        throw new Error('Failed to generate transcript');
      }

      const transcriptData = await transcriptResponse.json();
      onChange(transcriptData);
    } catch (error) {
      console.error('Error generating transcript:', error);
    }
  };

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
            onClick={generateTranscript}
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
