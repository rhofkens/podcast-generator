import { TranscriptStep } from '../../wizard/steps/TranscriptStep'

interface TranscriptTabProps {
  transcript: any
  participants: any[]
  onChange: (transcript: any) => void
  podcastId: string
}

export function TranscriptTab({ transcript, participants, onChange, podcastId }: TranscriptTabProps) {
  return (
    <div className="bg-white rounded-lg shadow">
      <TranscriptStep
        podcastId={podcastId}
        messages={transcript?.content?.messages || transcript?.messages || []}
        participants={participants.map(p => ({ id: p.id, name: p.name }))}
        onChange={onChange}
        onNext={() => {}} // Not used in edit mode
        onBack={() => {}} // Not used in edit mode
        editMode={true}
      />
    </div>
  )
}
