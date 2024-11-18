import { useState, useEffect } from 'react'

interface Participant {
  id?: number
  name: string
  gender: string
  age: number
  role: string
  roleDescription: string
  voiceCharacteristics: string
  isNew?: boolean
}

interface ParticipantsStepProps {
  podcastId: string | null
  participants: Participant[]
  onChange: (participants: Participant[]) => void
  onNext: () => void
  onBack: () => void
}

export function ParticipantsStep({ podcastId, participants, onChange, onNext, onBack }: ParticipantsStepProps) {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (podcastId) {
      loadParticipants();
    }
  }, [podcastId]);

  const loadParticipants = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await fetch(`/api/participants/podcast/${podcastId}`);
      if (!response.ok) {
        throw new Error('Failed to load participants');
      }
      const data = await response.json();
      onChange(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load participants');
    } finally {
      setIsLoading(false);
    }
  };
  const addParticipant = () => {
    // Only add to local state, no API call yet
    const newParticipant = {
      name: '',
      gender: '',
      age: 30,
      role: '',
      roleDescription: '',
      voiceCharacteristics: '',
      isNew: true // Flag to track unsaved participants
    };

    onChange([...participants, newParticipant]);
  };

  const updateParticipant = async (index: number, field: keyof Participant, value: any) => {
    try {
      setError(null);
      const participant = participants[index];
      const updated = { ...participant, [field]: value };
      
      if (participant.isNew) {
        // Just update local state for new participants
        const newParticipants = [...participants];
        newParticipants[index] = updated;
        onChange(newParticipants);
        return;
      }

      // Only make API call for existing participants
      if (participant.id) {
        const response = await fetch(`/api/participants/${participant.id}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            ...updated,
            podcast: {
              id: parseInt(podcastId!)
            }
          })
        });

        if (!response.ok) {
          throw new Error('Failed to update participant');
        }

        const updatedParticipant = await response.json();
        const newParticipants = [...participants];
        newParticipants[index] = updatedParticipant;
        onChange(newParticipants);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to update participant');
    }
  };

  const saveNewParticipants = async () => {
    try {
      setError(null);
      const newParticipants = participants.filter(p => p.isNew);
      
      // Validate all new participants before saving
      const invalidParticipants = newParticipants.filter(
        p => !p.name || !p.gender || !p.role || !p.roleDescription
      );
      
      if (invalidParticipants.length > 0) {
        throw new Error('Please fill out all required fields for all participants');
      }

      // Save all new participants
      const savedParticipants = await Promise.all(
        newParticipants.map(participant =>
          fetch('/api/participants', {
            method: 'POST',
            headers: {
              'Content-Type': 'application/json',
            },
            body: JSON.stringify({
              ...participant,
              podcast: {
                id: parseInt(podcastId!)
              }
            })
          }).then(res => {
            if (!res.ok) throw new Error('Failed to save participant');
            return res.json();
          })
        )
      );

      // Update state with saved participants
      const updatedParticipants = participants.map(p => 
        p.isNew ? savedParticipants.shift() : p
      );
      onChange(updatedParticipants);
      
      return true;
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save participants');
      return false;
    }
  };

  const removeParticipant = async (index: number) => {
    try {
      setError(null);
      const participant = participants[index];

      if (participant.id) {
        const response = await fetch(`/api/participants/${participant.id}`, {
          method: 'DELETE'
        });

        if (!response.ok) {
          throw new Error('Failed to delete participant');
        }
      }

      onChange(participants.filter((_, i) => i !== index));
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to delete participant');
    }
  };

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

      {error && (
        <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
          {error}
        </div>
      )}

      {isLoading ? (
        <div className="flex justify-center py-4">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
        </div>
      ) : (
        <div className="flex justify-between mt-6">
          <button
            onClick={onBack}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            Back
          </button>
          <button
            onClick={async () => {
              if (participants.some(p => p.isNew)) {
                const saved = await saveNewParticipants();
                if (!saved) return;
              }
              onNext();
            }}
            disabled={!isValid || isLoading}
            className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
    </div>
  )
}
