import { useState, useEffect, useRef } from 'react'
import { formatTime } from '../../../utils/timeFormat'
import { cn } from '../../../lib/utils'
import { motion, AnimatePresence, useReducedMotion } from 'framer-motion'

interface ParticipantData {
  id: number
  name: string
  role: string
  roleDescription: string
  voiceCharacteristics: string
}

interface TranscriptData {
  transcript: Array<{
    participantId?: number
    speakerName: string
    text: string
    timeOffset: number
  }>
}

interface Message {
  participantId: number
  content: string
  timing: number
}

interface TranscriptStepProps {
  podcastId: string | null;
  messages: Message[];
  participants: Array<{ id: number; name: string }>;
  onChange: (messages: Message[]) => void;
  onBack: () => void;
  onNext: () => void;
  editMode?: boolean;
  hideControls?: boolean;
  hideNavigation?: boolean;
}

export function TranscriptStep({
                                 podcastId,
                                 messages,
                                 participants,
                                 onChange,
                                 onBack,
                                 onNext,
                                 editMode: propEditMode = false,
                                 hideControls = false,
                                 hideNavigation = false
                               }: TranscriptStepProps) {
  const [localEditMode, setEditMode] = useState(propEditMode);
  console.log('TranscriptStep initial render:', {
    podcastId,
    messages,
    participants,
    messagesLength: messages?.length,
    participantsLength: participants?.length,
    editMode: localEditMode
  });

  if (!podcastId) {
    return (
      <div className="p-6">
        <div className="bg-red-50 text-red-500 p-4 rounded-lg">
          No podcast ID found. Please start from the beginning.
        </div>
      </div>
    );
  }

  if (!participants || participants.length === 0) {
    return (
      <div className="p-6">
        <div className="bg-yellow-50 text-yellow-700 p-4 rounded-lg">
          No participants found. Please add participants first.
        </div>
        <button
          onClick={onBack}
          className="mt-4 px-4 py-2 border rounded hover:bg-gray-50"
        >
          Back
        </button>
      </div>
    );
  }
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const chatContainerRef = useRef<HTMLDivElement>(null)
  const shouldReduceMotion = useReducedMotion()

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight
    }
  }, [messages])

  const getParticipantName = (participantId: number) => {
    console.log('Looking up participant name for ID:', participantId, 'in participants:', participants)
    const participant = participants.find(p => p.id === participantId)
    if (!participant) {
      console.warn(`No participant found for ID: ${participantId}`, {
        searchId: participantId,
        availableParticipants: participants
      })
      return 'Unknown'
    }
    return participant.name
  }

  const getMessagePosition = (participantId: number) => {
    // Get the actual participant
    const participant = participants.find(p => p.id === participantId)
    if (!participant) {
      console.warn(`No participant found for ID: ${participantId}`, {
        searchId: participantId,
        availableParticipants: participants
      })
      return 'left'
    }

    // Get the index of this participant in the original participants array
    const participantIndex = participants.indexOf(participant)
    return participantIndex % 2 === 0 ? 'left' : 'right'
  }


  const messageVariants = {
    hidden: (position: string) => ({
      opacity: 0,
      x: shouldReduceMotion ? 0 : (position === 'left' ? -50 : 50),
      y: shouldReduceMotion ? 0 : 20,
      scale: 0.9,
    }),
    visible: {
      opacity: 1,
      x: 0,
      y: 0,
      scale: 1,
      transition: {
        type: "spring",
        stiffness: 500,
        damping: 30,
        mass: 1,
      }
    },
    hover: {
      scale: 1.02,
      y: -5,
      transition: {
        type: "spring",
        stiffness: 400,
        damping: 10
      }
    },
    tap: {
      scale: 0.98,
      transition: {
        type: "spring",
        stiffness: 400,
        damping: 10
      }
    }
  }


  const getMessageGradient = (participantId: number) => {
    // Get the actual participant
    const participant = participants.find(p => p.id === participantId)
    if (!participant) {
      console.warn(`No participant found for ID: ${participantId}`, {
        searchId: participantId,
        availableParticipants: participants
      })
      return 'bg-gradient-to-br from-gray-50 to-gray-100 border-gray-200' // default gradient
    }

    // Get the index of this participant in the original participants array
    const participantIndex = participants.indexOf(participant)

    const gradients = [
      'bg-gradient-to-br from-blue-50 to-blue-100 border-blue-200',
      'bg-gradient-to-br from-green-50 to-green-100 border-green-200',
      'bg-gradient-to-br from-purple-50 to-purple-100 border-purple-200',
      'bg-gradient-to-br from-yellow-50 to-yellow-100 border-yellow-200',
      'bg-gradient-to-br from-pink-50 to-pink-100 border-pink-200'
    ]

    return gradients[participantIndex % gradients.length]
  }

  useEffect(() => {
    console.log('TranscriptStep useEffect triggered:', {
      messages,
      participants,
      messagesLength: messages?.length,
      participantsLength: participants?.length,
      shouldGenerate: messages?.length === 0 && participants?.length >= 2,
      editMode: localEditMode
    });

    const loadExistingTranscript = async () => {
      if (!podcastId) {
        console.log('No podcastId available for loading transcript');
        return;
      }
      
      setIsGenerating(true);
      try {
        const response = await fetch(`/api/transcripts/podcast/${podcastId}`);
        if (!response.ok) {
          throw new Error('Failed to load transcript');
        }
        const data = await response.json();
        console.log('Loaded transcript data:', data);
        
        if (data.content?.messages) {
          onChange(data.content.messages);
        } else if (data.messages) {
          onChange(data.messages);
        }
      } catch (error) {
        console.error('Error loading transcript:', error);
        setError(error instanceof Error ? error.message : 'Failed to load transcript');
      } finally {
        setIsGenerating(false);
      }
    };

    // In edit mode, try to load existing transcript first
    if (localEditMode && (!messages || messages.length === 0)) {
      loadExistingTranscript();
    }
    // In create mode, generate new transcript if we have participants but no messages
    else if (!localEditMode && (!messages || messages.length === 0) && participants?.length >= 2) {
      console.log('Starting automatic transcript generation');
      generateTranscript();
    }
  }, [messages?.length, participants?.length, localEditMode, podcastId]);

  const generateTranscript = async () => {
    console.log('Generating transcript...', { podcastId });
    try {
      setIsGenerating(true);
      setError(null);

      if (!podcastId) {
        throw new Error('No podcast ID found');
      }

      // Get participants
      const participantsResponse = await fetch(`/api/participants/podcast/${podcastId}`);
      if (!participantsResponse.ok) {
        throw new Error('Failed to fetch participants');
      }
      const participantsList = await participantsResponse.json() as ParticipantData[];

      console.log('Sending participants for transcript generation:', participantsList);

      // Generate transcript
      const transcriptResponse = await fetch(`/api/podcasts/${podcastId}/generate-transcript`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          participants: participantsList.map((p: ParticipantData) => ({
            id: p.id,
            name: p.name,
            role: p.role,
            roleDescription: p.roleDescription,
            voiceCharacteristics: p.voiceCharacteristics
          }))
        })
      });

      if (!transcriptResponse.ok) {
        const errorData = await transcriptResponse.json();
        throw new Error(errorData.message || 'Failed to generate transcript');
      }

      const transcriptData = await transcriptResponse.json() as TranscriptData;
      console.log('Received transcript data:', transcriptData);

      if (!transcriptData || !transcriptData.transcript) {
        throw new Error('Invalid transcript data received');
      }

      // Helper function to ensure valid participantId
      const getValidParticipantId = (entry: typeof transcriptData.transcript[0], participants: ParticipantData[]): number => {
        if (entry.participantId) {
          return entry.participantId;
        }
        
        const participant = participants.find(p => 
          p.name.toLowerCase() === entry.speakerName.toLowerCase()
        );
        
        if (!participant) {
          console.warn(`No participant found for speaker: ${entry.speakerName}, using first participant as fallback`);
          return participants[0].id;
        }
        
        return participant.id;
      };

      // Convert the transcript data to messages format with proper typing
      const newMessages: Message[] = transcriptData.transcript.map(entry => ({
        participantId: getValidParticipantId(entry, participantsList),
        content: entry.text,
        timing: entry.timeOffset
      }));

      console.log('Generated messages:', newMessages);
      onChange(newMessages);

    } catch (err) {
      console.error('Error generating transcript:', err);
      setError(err instanceof Error ? err.message : 'Failed to generate transcript');
    } finally {
      setIsGenerating(false);
    }
  };

  const updateMessage = (index: number, field: keyof Message, value: any) => {
    const updated = [...messages]
    updated[index] = { ...updated[index], [field]: value }
    onChange(updated)
  }

  if (isGenerating) {
    return (
      <div className="p-6 flex flex-col items-center justify-center h-[calc(100vh-200px)]">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary mb-4"></div>
        <p className="text-gray-600">Generating transcript...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="p-6">
        <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
          {error}
        </div>
        <div className="flex justify-between">
          <button
            onClick={onBack}
            className="px-4 py-2 border rounded hover:bg-gray-50"
          >
            Back
          </button>
          <button
            onClick={generateTranscript}
            className="bg-primary text-primary-foreground px-4 py-2 rounded"
          >
            Retry Generation
          </button>
        </div>
      </div>
    );
  }

  if (error) {
    return (
        <div className="p-6">
          <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
            {error}
          </div>
          <div className="flex justify-between">
            <button
                onClick={onBack}
                className="px-4 py-2 border rounded hover:bg-gray-50"
            >
              Back
            </button>
            <button
                onClick={generateTranscript}
                className="bg-primary text-primary-foreground px-4 py-2 rounded"
            >
              Retry Generation
            </button>
          </div>
        </div>
    )
  }

  return (
      <div className="p-6 flex flex-col h-[calc(100vh-200px)]">
        {!hideControls && (
          <div className="flex justify-between mb-4">
            <div className="flex gap-2">
              {!localEditMode && (
                  <button
                      onClick={() => setEditMode(true)}
                      className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
                  >
                      Edit
                  </button>
              )}
              <button
                  onClick={generateTranscript}
                  className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
              >
                  Regenerate
              </button>
            </div>
          </div>
        )}

        {localEditMode ? (
            <motion.div
                className="flex-1 overflow-y-auto bg-white rounded-lg border p-4 space-y-4 shadow-sm"
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.2 }}
            >
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
            </motion.div>
        ) : (
            <motion.div
                ref={chatContainerRef}
                className="flex-1 overflow-y-auto bg-gradient-to-b from-gray-50 to-gray-100 rounded-lg p-4 space-y-6 shadow-inner min-h-[400px]"
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.3 }}
            >
              <AnimatePresence>
                {messages.map((message, index) => {
                  const position = getMessagePosition(message.participantId)
                  const speakerName = getParticipantName(message.participantId)

                  return (
                      <motion.div
                          key={index}
                          className={cn(
                              "flex flex-col max-w-[80%]",
                              position === 'right' ? 'ml-auto' : ''
                          )}
                          custom={position}
                          initial="hidden"
                          animate="visible"
                          variants={messageVariants}
                          whileHover="hover"
                          whileTap="tap"
                          layout
                      >
                        <motion.div
                            className={cn(
                                "rounded-lg border p-4",
                                getMessageGradient(message.participantId),
                                position === 'left' ? 'rounded-tl-none' : 'rounded-tr-none'
                            )}
                        >
                          <div className="flex justify-between items-baseline mb-2">
                            <motion.span
                                className="font-medium text-primary"
                                whileHover={{ scale: 1.05 }}
                            >
                              {speakerName}
                            </motion.span>
                            <span className="text-xs text-gray-500">
                        {formatTime(message.timing)}
                      </span>
                          </div>
                          <p className="text-gray-800 whitespace-pre-wrap leading-relaxed">
                            {message.content}
                          </p>
                        </motion.div>

                        <motion.div
                            className={cn(
                                "text-xs text-gray-400 mt-1",
                                position === 'right' ? 'text-right' : 'text-left'
                            )}
                            initial={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 0.2 }}
                        >
                          {new Date(message.timing * 1000).toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </motion.div>
                      </motion.div>
                  )
                })}
              </AnimatePresence>

              {isGenerating && (
                  <motion.div
                      className="flex items-center space-x-2 p-3 bg-gray-200 rounded-lg max-w-[100px]"
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                  >
                    <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce" />
                    <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce delay-100" />
                    <span className="w-2 h-2 bg-gray-500 rounded-full animate-bounce delay-200" />
                  </motion.div>
              )}
            </motion.div>
        )}

        {!hideNavigation && (
          <motion.div
              className="flex justify-between mt-4"
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.3 }}
          >
          <button
              onClick={onBack}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Back
          </button>
          <button
              onClick={async () => {
                try {
                  if (!podcastId) {
                    throw new Error('No podcast ID found')
                  }

                  // First check if a transcript already exists
                  const checkResponse = await fetch(`/api/transcripts/podcast/${podcastId}`);
                  if (!checkResponse.ok) {
                    throw new Error('Failed to check existing transcript');
                  }
                  
                  const existingTranscripts = await checkResponse.json();
                  const transcriptId = Array.isArray(existingTranscripts) ? 
                    existingTranscripts[0]?.id : 
                    existingTranscripts?.id;

                  if (!transcriptId) {
                    throw new Error('No transcript ID found');
                  }

                  // Use the existing PUT /{id} endpoint
                  const response = await fetch(`/api/transcripts/${transcriptId}`, {
                    method: 'PUT',
                    headers: {
                      'Content-Type': 'application/json'
                    },
                    body: JSON.stringify({
                      id: transcriptId,
                      podcast: {
                        id: parseInt(podcastId)
                      },
                      content: {
                        messages: messages.map(msg => ({
                          participantId: msg.participantId,
                          content: msg.content,
                          timing: msg.timing
                        }))
                      }
                    })
                  });

                  if (!response.ok) {
                    throw new Error('Failed to update transcript')
                  }

                  // After successful save, ensure the podcastId is still in localStorage
                  localStorage.setItem('currentPodcastId', podcastId);

                  onNext();
                } catch (err) {
                  setError(err instanceof Error ? err.message : 'Failed to save transcript')
                }
              }}
              disabled={messages.length === 0 || isGenerating}
              className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
          >
            {localEditMode ? 'Save Changes' : 'Next'}
          </button>
          </motion.div>
        )}
      </div>
  )
}
