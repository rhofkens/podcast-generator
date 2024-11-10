import { useState } from 'react'
import { WizardStepBar } from './WizardStepBar'
import { MetadataStep } from './steps/MetadataStep'
import { ParticipantsStep } from './steps/ParticipantsStep'
import { TranscriptStep } from './steps/TranscriptStep'

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
}

interface Message {
  participantId: number
  content: string
  timing: number
}

export function PodcastWizard() {
  const [currentStep, setCurrentStep] = useState(0)
  const [metadata, setMetadata] = useState({
    title: '',
    description: '',
    length: 30,
    contextDescription: '',
    contextUrl: '',
    contextFile: undefined as File | undefined
  })
  const [participants, setParticipants] = useState<Participant[]>([])
  const [messages, setMessages] = useState<Message[]>([])

  const handleMetadataChange = (field: string, value: any) => {
    setMetadata(prev => ({ ...prev, [field]: value }))
  }

  const handleSubmit = async () => {
    // TODO: Implement API calls to create podcast
    console.log('Submitting podcast:', { metadata, participants, messages })
  }

  const renderStep = () => {
    switch (currentStep) {
      case 0:
        return (
          <MetadataStep
            data={metadata}
            onChange={handleMetadataChange}
            onNext={() => setCurrentStep(1)}
          />
        )
      case 1:
        return (
          <ParticipantsStep
            participants={participants}
            onChange={setParticipants}
            onNext={() => setCurrentStep(2)}
            onBack={() => setCurrentStep(0)}
          />
        )
      case 2:
        return (
          <TranscriptStep
            messages={messages}
            participants={participants.map((p, i) => ({ id: i, name: p.name }))}
            onChange={setMessages}
            onBack={() => setCurrentStep(1)}
            onSubmit={handleSubmit}
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
