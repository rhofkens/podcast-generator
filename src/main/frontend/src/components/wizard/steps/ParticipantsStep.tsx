import { useState, useEffect } from 'react'
import { cn } from '../../../lib/utils'
import { VoiceSelectionModal } from '../../settings/VoiceSelectionModal'
import { Voice } from '../../../types/Voice'
import { useAuth } from '../../../contexts/AuthContext'

interface Participant {
  id?: number;
  name: string;
  gender: string;
  age: number;
  role: string;
  roleDescription: string;
  voiceCharacteristics: string;
  selectedVoice?: Voice;
  voicePreviewId?: string;
  voicePreviewUrl?: string;
  syntheticVoiceId?: string;
  isNew?: boolean;
  isDirty?: boolean;
  isGeneratingVoice?: boolean;
}

interface ParticipantsStepProps {
  podcastId: string | null;
  participants: Participant[];
  onChange: (participants: Participant[]) => void;
  onNext: () => void;
  onBack: () => void;
  editMode?: boolean;
  hideControls?: boolean;
}

export function ParticipantsStep({ 
  podcastId, 
  participants, 
  onChange, 
  onNext, 
  onBack,
  editMode = false,
  hideControls = false
}: ParticipantsStepProps) {
  const { user } = useAuth();
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [editedFields, setEditedFields] = useState<Set<string>>(new Set());
  const [isVoiceModalOpen, setIsVoiceModalOpen] = useState(false);
  const [activeParticipantIndex, setActiveParticipantIndex] = useState<number | null>(null);
  const [availableVoices, setAvailableVoices] = useState<Voice[]>([]);

  const getDefaultVoice = async (gender: string, userId?: string) => {
    try {
      console.log(`Fetching default voice for gender: ${gender}, userId: ${userId}`);
      
      if (userId) {
        console.log('Attempting to fetch user-specific default voice');
        const userDefaultResponse = await fetch(`/api/voices/user/${userId}/default/${gender}`);
        const userDefaultVoices = await userDefaultResponse.json();
        if (userDefaultVoices && userDefaultVoices.length > 0) {
          console.log('Found user-specific default voice:', userDefaultVoices[0]);
          return userDefaultVoices[0];
        }
      }
      
      console.log('Fetching system default voice');
      const systemDefaultResponse = await fetch(`/api/voices/default/${gender}`);
      const systemDefaultVoices = await systemDefaultResponse.json();
      if (systemDefaultVoices && systemDefaultVoices.length > 0) {
        console.log('Found system default voice:', systemDefaultVoices[0]);
        return systemDefaultVoices[0];
      }
      
      console.log('No default voice found');
      return null;
    } catch (error) {
      console.error('Error fetching default voice:', error);
      return null;
    }
  };

  useEffect(() => {
    // If in edit mode, mark ALL fields as edited immediately on mount
    if (editMode) {
      const allFields = new Set<string>();
      participants.forEach((_, index) => {
        [
          'name',
          'gender', 
          'age',
          'role',
          'roleDescription',
          'voiceCharacteristics'
        ].forEach(field => {
          allFields.add(`${index}-${field}`);
        });
      });
      setEditedFields(allFields);
    }
  }, [editMode, participants.length]); // Only depend on editMode and participants length

  useEffect(() => {
    console.log('Participants state changed:', participants);
  }, [participants]);

  useEffect(() => {
    const loadDefaultVoicesForParticipants = async () => {
      const updatedParticipants = await Promise.all(
        participants.map(async (participant) => {
          // Only load default voice if no voice is selected and gender is set
          if (!participant.selectedVoice && participant.gender) {
            const defaultVoice = await getDefaultVoice(participant.gender, user?.id);
            if (defaultVoice) {
              return {
                ...participant,
                selectedVoice: defaultVoice,
                voicePreviewUrl: defaultVoice.audioPreviewPath,
                syntheticVoiceId: defaultVoice.externalVoiceId
              };
            }
          }
          return participant;
        })
      );

      // Only update if there were changes
      if (JSON.stringify(updatedParticipants) !== JSON.stringify(participants)) {
        onChange(updatedParticipants);
      }
    };

    loadDefaultVoicesForParticipants();
  }, [participants.map(p => p.gender).join(','), user?.id]); // Dependency on gender changes and user ID

  useEffect(() => {
    if (podcastId) {
      if (!editMode && participants.length === 0) {
        loadSampleParticipants();
      } else if (editMode) {
        loadParticipants();
      }
    }
  }, [podcastId, editMode]);

  useEffect(() => {
    const loadVoices = async () => {
      try {
        const response = await fetch('/api/voices')
        if (!response.ok) throw new Error('Failed to load voices')
        const data = await response.json()
        setAvailableVoices(data)
      } catch (error) {
        console.error('Error loading voices:', error)
      }
    }
    loadVoices()
  }, [])

  const loadSampleParticipants = async () => {
    // Don't load sample data if in edit mode
    if (editMode) {
      setIsLoading(false);
      return;
    }

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
      // Don't mark sample data as edited
      setEditedFields(new Set());
      
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load sample participants');
    } finally {
      setIsLoading(false);
    }
  };

  const handleFieldFocus = (index: number, field: keyof Participant) => {
    if (!editMode && !isFieldEdited(index, field)) {
      // Clear the sample value when focusing for the first time in create mode
      updateParticipant(index, field, '');
      setEditedFields(prev => new Set([...prev, `${index}-${field}`]));
    }
  };

  const isFieldEdited = (index: number, field: keyof Participant) => {
    return editedFields.has(`${index}-${field}`);
  };

  const saveAndGenerateVoicePreview = async (participant: Participant, index: number) => {
    console.log('Starting voice preview generation for:', participant.name);
    
    // Create a new array with the updated loading state
    const updatedParticipants = participants.map((p, i) => 
        i === index ? { ...p, isGeneratingVoice: true } : p
    );
    onChange(updatedParticipants);

    try {
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
            participant = savedParticipant;
        }

        // Generate voice preview
        console.log('Generating voice preview for participant:', participant.id);
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
        console.log('Voice preview generated:', updatedParticipant);

        // Create final updated state with all necessary voice-related fields
        const finalParticipants = participants.map((p, i) => 
            i === index 
                ? { 
                    ...p,
                    ...updatedParticipant,
                    selectedVoice: {
                        id: updatedParticipant.voiceId,
                        name: `${updatedParticipant.name} - generated`,
                        externalVoiceId: updatedParticipant.syntheticVoiceId,
                        audioPreviewPath: updatedParticipant.voicePreviewUrl,
                        gender: updatedParticipant.gender.toLowerCase(),
                        voiceType: 'GENERATED',
                        tags: [],
                        isDefault: false
                    },
                    voicePreviewUrl: updatedParticipant.voicePreviewUrl,
                    syntheticVoiceId: updatedParticipant.syntheticVoiceId,
                    voicePreviewId: updatedParticipant.voicePreviewId,
                    isGeneratingVoice: false,
                    isNew: false,
                    isDirty: true
                } 
                : p
        );

        console.log('Updating participants state with:', finalParticipants[index]);
        onChange(finalParticipants);

    } catch (error) {
        console.error('Error in voice preview generation:', error);
        
        // Reset loading state while preserving other participant data
        const errorParticipants = participants.map((p, i) => 
            i === index ? { ...p, isGeneratingVoice: false } : p
        );
        onChange(errorParticipants);
        
        // Show error to user
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
    const newParticipant = {
      name: '',
      gender: '',
      age: 30,
      role: '',
      roleDescription: '',
      voiceCharacteristics: '',
      isNew: true
    };

    onChange([...participants, newParticipant]);
  };

  const updateParticipant = async (index: number, field: keyof Participant, value: any) => {
    // Only clear value on first edit if not in edit mode
    if (!editMode && !isFieldEdited(index, field)) {
      value = '';
    }
    
    // Just update local state
    const newParticipants = [...participants];
    newParticipants[index] = { 
      ...newParticipants[index], 
      [field]: value,
      isDirty: true // Add a flag to track changes
    };

    // If gender is being updated, try to set default voice
    if (field === 'gender' && value) {
      const defaultVoice = await getDefaultVoice(value, user?.id);
      if (defaultVoice) {
        newParticipants[index] = {
          ...newParticipants[index],
          selectedVoice: defaultVoice,
          voicePreviewUrl: defaultVoice.audioPreviewPath,
          syntheticVoiceId: defaultVoice.externalVoiceId
        };
      }
    }

    onChange(newParticipants);
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
    <>
      <div className="p-6">
        <div className="space-y-6 mb-6">
        {participants.map((participant, index) => (
          <div key={participant.id || `new-${index}`} className="bg-white p-4 rounded-lg border">
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

              <div className="col-span-2 grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium mb-1">Voice Selection</label>
                  <div className="flex items-center space-x-2 p-2 border rounded bg-gray-50">
                    <span className="text-sm">
                      {participant.selectedVoice ? participant.selectedVoice.name : 
                       participant.voicePreviewId ? `${participant.name} - generated` : 
                       'No voice selected'}
                    </span>
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium mb-1">Voice Preview</label>
                  {(participant.selectedVoice?.audioPreviewPath || participant.voicePreviewUrl) ? (
                    <audio 
                      controls 
                      className="w-full h-8"
                      key={participant.selectedVoice?.audioPreviewPath || participant.voicePreviewUrl}
                    >
                      <source 
                        src={participant.selectedVoice?.audioPreviewPath || participant.voicePreviewUrl} 
                        type="audio/mpeg" 
                      />
                      Your browser does not support the audio element.
                    </audio>
                  ) : (
                    <div className="flex items-center space-x-2 p-2 border rounded bg-gray-50 text-gray-500">
                      <span className="text-sm">No preview available</span>
                    </div>
                  )}
                </div>
              </div>

              <div className="col-span-2 mt-4">
                <div className="flex items-center justify-between space-x-4">
                  <button
                    onClick={() => {
                      setActiveParticipantIndex(index)
                      setIsVoiceModalOpen(true)
                    }}
                    className="px-4 py-2 rounded-md text-sm font-medium bg-secondary text-secondary-foreground hover:bg-secondary/90"
                  >
                    Select voice from library
                  </button>
                  <button
                    onClick={() => saveAndGenerateVoicePreview(participant, index)}
                    disabled={!!participant.isGeneratingVoice}
                    className={cn(
                      "px-4 py-2 rounded-md text-sm font-medium transition-colors",
                      !!participant.isGeneratingVoice
                        ? "bg-gray-200 cursor-not-allowed"
                        : "bg-primary text-primary-foreground hover:bg-primary/90"
                    )}
                  >
                    {!!participant.isGeneratingVoice ? (
                      <span className="flex items-center space-x-2">
                        <svg 
                          className="animate-spin h-5 w-5" 
                          xmlns="http://www.w3.org/2000/svg" 
                          fill="none" 
                          viewBox="0 0 24 24"
                        >
                          <circle 
                            className="opacity-25" 
                            cx="12" 
                            cy="12" 
                            r="10" 
                            stroke="currentColor" 
                            strokeWidth="4"
                          ></circle>
                          <path 
                            className="opacity-75" 
                            fill="currentColor" 
                            d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                          ></path>
                        </svg>
                        <span>Generating...</span>
                      </span>
                    ) : (
                      "Generate new voice"
                    )}
                  </button>
                </div>
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
      ) : !hideControls && (
        <div className="flex justify-between mt-6">
          <button
            onClick={onBack}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            Back
          </button>
          <button
            onClick={async () => {
              try {
                setError(null);
                  
                // Get all participants that need saving (new or modified)
                const participantsToSave = participants.filter(p => p.isNew || p.isDirty);
                  
                // Save all modified participants and collect the results
                const savedParticipants = await Promise.all(participantsToSave.map(async (participant) => {
                  const method = participant.isNew ? 'POST' : 'PUT';
                  const url = participant.isNew 
                    ? '/api/participants'
                    : `/api/participants/${participant.id}`;
                      
                  const response = await fetch(url, {
                    method,
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
                    throw new Error(`Failed to ${participant.isNew ? 'create' : 'update'} participant`);
                  }
                    
                  return await response.json();
                }));

                // Update the participants array with the saved data
                const updatedParticipants = participants.map(p => {
                  const savedParticipant = savedParticipants.find(sp => 
                    (p.id && sp.id === p.id) || (!p.id && sp.name === p.name)
                  );
                  return savedParticipant || p;
                });

                // Update state with saved participants
                onChange(updatedParticipants);

                // If all saves successful, proceed to next step
                onNext();
              } catch (err) {
                setError(err instanceof Error ? err.message : 'Failed to save participants');
              }
            }}
            disabled={!isValid || isLoading}
            className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
          >
            Next
          </button>
        </div>
      )}
      </div>
      <VoiceSelectionModal
        isOpen={isVoiceModalOpen}
        onClose={() => setIsVoiceModalOpen(false)}
        onSelect={(voice) => {
          if (activeParticipantIndex !== null) {
            const updatedParticipants = [...participants];
            updatedParticipants[activeParticipantIndex] = {
              ...updatedParticipants[activeParticipantIndex],
              selectedVoice: voice,
              voicePreviewUrl: voice.audioPreviewPath,
              syntheticVoiceId: voice.externalVoiceId,
              voicePreviewId: undefined
            };
            onChange(updatedParticipants);
            setIsVoiceModalOpen(false);
          }
        }}
        voices={availableVoices.filter(voice => {
          const participantGender = participants[activeParticipantIndex || 0]?.gender
          return participantGender ? voice.gender === participantGender : false
        })}
      />
    </>
  )
}
