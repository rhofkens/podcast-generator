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
  const updateMessage = (index: number, field: keyof Message, value: any) => {
    const updated = [...messages]
    updated[index] = { ...updated[index], [field]: value }
    onChange(updated)
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
        <button
          onClick={onSubmit}
          className="bg-primary text-primary-foreground px-4 py-2 rounded"
        >
          Generate Podcast
        </button>
      </div>
    </div>
  )
}
