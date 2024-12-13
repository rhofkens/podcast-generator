import { TranscriptStep } from '../../wizard/steps/TranscriptStep'

interface TranscriptTabProps {
  transcript: any
  participants: any[]
  onChange: (transcript: any) => void
  podcastId: string
}

export function TranscriptTab({ transcript, participants, onChange, podcastId }: TranscriptTabProps) {
  // Add logging to debug the incoming data
  console.log('TranscriptTab received:', {
    transcript,
    participants,
    podcastId,
    messages: transcript?.content?.messages || transcript?.messages || []
  });

  // Transform the transcript data into the expected format
  const messages = transcript?.content?.messages || transcript?.messages || [];
  
  // Ensure participants are in the correct format
  const formattedParticipants = participants.map(p => ({ 
    id: p.id, 
    name: p.name 
  }));

  console.log('TranscriptTab formatted data:', {
    messages,
    formattedParticipants
  });

  return (
    <div className="bg-white rounded-lg shadow">
      <TranscriptStep
        podcastId={podcastId}
        messages={messages}
        participants={formattedParticipants}
        onChange={(updatedMessages) => {
          // Transform the data back to the expected format before calling onChange
          const updatedTranscript = {
            ...transcript,
            content: {
              messages: updatedMessages
            }
          };
          onChange(updatedTranscript);
        }}
        onNext={() => {}} // Not used in edit mode
        onBack={() => {}} // Not used in edit mode
        editMode={true}
      />
    </div>
  )
}
