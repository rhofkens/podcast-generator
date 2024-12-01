import { useState, useEffect } from 'react'
import { cn } from '../../../lib/utils'

interface Participant {
  id?: number;
  name: string;
  gender: string;
  age: number;
  role: string;
  roleDescription: string;
  voiceCharacteristics: string;
  voicePreviewId?: string;
  voicePreviewUrl?: string;
  syntheticVoiceId?: string;
  isNew?: boolean;
  isGeneratingVoice?: boolean;
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
  const [editedFields, setEditedFields] = useState<Set<string>>(new Set());

  useEffect(() => {
    if (podcastId) {
      if (participants.length === 0) {
        loadSampleParticipants();
      } else {
        loadParticipants();
      }
    }
  }, [podcastId]);

  const loadSampleParticipants = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response = await fetch(`/api/podcasts/${podcastId}/sample-participants`);
      if (!response.ok) {
        throw new Error('Failed to load sample participants');
      }
      
      const data = await response.json();
      const sampleParticipants = data.participants.map((p: any) => ({
        name: p.name,
        gender: p.gender.toLowerCase(),
        age: p.age,
        role: p.role,
        roleDescription: p.roleDescription,
        voiceCharacteristics: p.voiceCharacteristics,
        isNew: true
      }));
      
      onChange(sampleParticipants);
      
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sample participants');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFieldFocus = (index: number, field: keyof Participant) => {
    setEditedFields(prev => new Set([...prev, `${index}-${field}`]));
  };

  const isFieldEdited = (index: number, field: keyof Participant) => {
    return editedFields.has(`${index}-${field}`);
  };

  const saveAndGenerateVoicePreview = async (participant: Participant, index: number) => {
    console.log('saveAndGenerateVoicePreview called:', { participant, index });
    
    // Create a copy of participants array to modify
    const updatedParticipants = [...participants];
    
    try {
      // Set loading state
      updatedParticipants[index] = { ...updatedParticipants[index], isGeneratingVoice: true };
      onChange(updatedParticipants);

      // If participant is new, save it first
      if (!participant.id) {
        console.log('Saving new participant first...');
        const response = await fetch('/api/participants', {
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
        });

        if (!response.ok) {
          throw new Error('Failed to save participant');
        }

        const savedParticipant = await response.json();
        console.log('Participant saved:', savedParticipant);
        
        // Update the local participant with saved data
        updatedParticipants[index] = { ...savedParticipant, isGeneratingVoice: true };
        onChange(updatedParticipants);
        
        participant = savedParticipant;
      }

      console.log('Generating voice preview for participant:', participant);

      const response = await fetch(`/api/participants/${participant.id}/generate-voice-preview`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        }
      });

      if (!response.ok) {
        throw new Error(`Failed to generate voice preview: ${response.statusText}`);
      }

      const updatedParticipant = await response.json();
      console.log('Received voice preview response:', updatedParticipant);
      
      // Update the participant with the preview data and clear loading state
      updatedParticipants[index] = {
        ...updatedParticipant,
        isGeneratingVoice: false
      };
      onChange(updatedParticipants);

    } catch (error) {
      console.error('Error in saveAndGenerateVoicePreview:', error);
      // Always clear loading state on error
      updatedParticipants[index] = { 
        ...updatedParticipants[index], 
        isGeneratingVoice: false 
      };
      onChange(updatedParticipants);
      setError(error instanceof Error ? error.message : 'Failed to generate voice preview');
    }
  };

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
      
      // When field is focused for the first time, clear the sample value
      if (!isFieldEdited(index, field)) {
        value = '';
      }
      
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
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'name') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'name')}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Gender</label>
                <select
                  value={participant.gender}
                  onChange={(e) => updateParticipant(index, 'gender', e.target.value)}
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'gender') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'gender')}
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
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'age') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'age')}
                  min={0}
                />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">Role</label>
                <input
                  type="text"
                  value={participant.role}
                  onChange={(e) => updateParticipant(index, 'role', e.target.value)}
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'role') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'role')}
                />
              </div>
              <div className="col-span-2">
                <label className="block text-sm font-medium mb-1">Role Description</label>
                <textarea
                  value={participant.roleDescription}
                  onChange={(e) => updateParticipant(index, 'roleDescription', e.target.value)}
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'roleDescription') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'roleDescription')}
                  rows={2}
                />
              </div>
              <div className="col-span-2">
                <label className="block text-sm font-medium mb-1">Voice Characteristics</label>
                <textarea
                  value={participant.voiceCharacteristics}
                  onChange={(e) => updateParticipant(index, 'voiceCharacteristics', e.target.value)}
                  className={cn(
                    "w-full p-2 border rounded",
                    !isFieldEdited(index, 'voiceCharacteristics') && "italic text-gray-400"
                  )}
                  onFocus={() => handleFieldFocus(index, 'voiceCharacteristics')}
                  rows={2}
                />
              </div>

              <div className="col-span-2 mt-4">
                <div className="flex items-center justify-between">
                  <button
                    onClick={() => saveAndGenerateVoicePreview(participant, index)}
                    disabled={participant.isGeneratingVoice}
                    className={cn(
                      "px-4 py-2 rounded-md text-sm font-medium",
                      participant.isGeneratingVoice
                        ? "bg-gray-200 cursor-not-allowed"
                        : "bg-primary text-primary-foreground hover:bg-primary/90"
                    )}
                  >
                    {participant.isGeneratingVoice ? (
                      <span className="flex items-center">
                        <svg className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        Generating...
                      </span>
                    ) : (
                      "Generate Voice Preview"
                    )}
                  </button>
                </div>
                
                {participant.voicePreviewUrl && (
                  <div className="mt-4">
                    <h4 className="text-sm font-medium mb-2">Voice Preview</h4>
                    <audio controls className="w-full">
                      <source src={participant.voicePreviewUrl} type="audio/mpeg" />
                      Your browser does not support the audio element.
                    </audio>
                  </div>
                )}
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
