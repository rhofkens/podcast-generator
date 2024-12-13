import { ParticipantsStep } from '../../wizard/steps/ParticipantsStep'

interface ParticipantsTabProps {
  participants: any[]
  onChange: (participants: any[]) => void
}

export function ParticipantsTab({ participants, onChange }: ParticipantsTabProps) {
  return (
    <div className="bg-white rounded-lg shadow">
      <ParticipantsStep
        podcastId={null} // Will be handled by parent
        participants={participants}
        onChange={onChange}
        onNext={() => {}} // Not used in edit mode
        onBack={() => {}} // Not used in edit mode
        editMode={true}
        hideControls={true}
      />
    </div>
  )
}
