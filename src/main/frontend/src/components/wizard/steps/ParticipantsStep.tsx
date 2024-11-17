interface Participant {
  id?: number
  name: string
  gender: string
  age: number
  role: string
  roleDescription: string
  voiceCharacteristics: string
}

interface ParticipantsStepProps {
  podcastId: string | null
  participants: Participant[]
  onChange: (participants: Participant[]) => void
  onNext: () => void
  onBack: () => void
}

export function ParticipantsStep({ participants, onChange, onNext, onBack }: ParticipantsStepProps) {
  const addParticipant = () => {
    onChange([
      ...participants,
      {
        name: '',
        gender: '',
        age: 30,
        role: '',
        roleDescription: '',
        voiceCharacteristics: ''
      }
    ])
  }

  const updateParticipant = (index: number, field: keyof Participant, value: any) => {
    const updated = [...participants]
    updated[index] = { ...updated[index], [field]: value }
    onChange(updated)
  }

  const removeParticipant = (index: number) => {
    onChange(participants.filter((_, i) => i !== index))
  }

  const isValid = participants.length >= 2 && 
    participants.every(p => p.name && p.gender && p.role && p.roleDescription)

  return (
    <div className="p-6">
      <div className="space-y-6 mb-6">
        {participants.map((participant, index) => (
          <div key={index} className="bg-white p-4 rounded-lg border">
            <div className="flex justify-between items-start mb-4">
              <h4 className="text-lg font-semibold">Participant {index + 1}</h4>
              <button
                onClick={() => removeParticipant(index)}
                className="text-red-500 hover:text-red-700"
              >
                Remove
              </button>
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">Name</label>
                <input
                  type="text"
                  value={participant.name}
                  onChange={(e) => updateParticipant(index, 'name', e.target.value)}
                  className="w-full p-2 border rounded"
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Gender</label>
                <select
                  value={participant.gender}
                  onChange={(e) => updateParticipant(index, 'gender', e.target.value)}
                  className="w-full p-2 border rounded"
                >
                  <option value="">Select gender</option>
                  <option value="male">Male</option>
                  <option value="female">Female</option>
                  <option value="other">Other</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Age</label>
                <input
                  type="number"
                  value={participant.age}
                  onChange={(e) => updateParticipant(index, 'age', parseInt(e.target.value))}
                  className="w-full p-2 border rounded"
                  min={0}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Role</label>
                <input
                  type="text"
                  value={participant.role}
                  onChange={(e) => updateParticipant(index, 'role', e.target.value)}
                  className="w-full p-2 border rounded"
                />
              </div>
              <div className="col-span-2">
                <label className="block text-sm font-medium mb-1">Role Description</label>
                <textarea
                  value={participant.roleDescription}
                  onChange={(e) => updateParticipant(index, 'roleDescription', e.target.value)}
                  className="w-full p-2 border rounded"
                  rows={2}
                />
              </div>
              <div className="col-span-2">
                <label className="block text-sm font-medium mb-1">Voice Characteristics</label>
                <textarea
                  value={participant.voiceCharacteristics}
                  onChange={(e) => updateParticipant(index, 'voiceCharacteristics', e.target.value)}
                  className="w-full p-2 border rounded"
                  rows={2}
                />
              </div>
            </div>
          </div>
        ))}
      </div>

      <button
        onClick={addParticipant}
        className="w-full p-4 border-2 border-dashed rounded-lg text-gray-500 hover:text-gray-700 hover:border-gray-400"
      >
        Add Participant
      </button>

      <div className="flex justify-between mt-6">
        <button
          onClick={onBack}
          className="px-4 py-2 border rounded hover:bg-gray-50"
        >
          Back
        </button>
        <button
          onClick={onNext}
          disabled={!isValid}
          className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  )
}
