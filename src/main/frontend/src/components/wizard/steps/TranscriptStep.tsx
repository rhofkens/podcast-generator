import { useState, useEffect, useRef } from 'react'
import { formatTime } from '../../../utils/timeFormat'
import { cn } from '../../../lib/utils'
import { motion, AnimatePresence, useReducedMotion } from 'framer-motion'

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
  onNext: () => void
}

export function TranscriptStep({ messages, participants, onChange, onBack, onNext }: TranscriptStepProps) {
  const [isGenerating, setIsGenerating] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editMode, setEditMode] = useState(false)
  const chatContainerRef = useRef<HTMLDivElement>(null)
  const shouldReduceMotion = useReducedMotion()

  useEffect(() => {
    if (chatContainerRef.current) {
      chatContainerRef.current.scrollTop = chatContainerRef.current.scrollHeight
    }
  }, [messages])

  const getParticipantName = (participantId: number) => {
    return participants.find(p => p.id === participantId)?.name || 'Unknown'
  }

  const getMessagePosition = (participantId: number) => {
    const participantIndex = participants.findIndex(p => p.id === participantId)
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
    const participantIndex = participants.findIndex(p => p.id === participantId)
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
    console.log('TranscriptStep mounted/updated:', {
      messagesLength: messages.length,
      participantsLength: participants.length,
      shouldGenerate: messages.length === 0 && participants.length >= 2
    })
    
    // Only generate if no messages exist yet and we have participants
    if (messages.length === 0 && participants.length >= 2) {
      console.log('Starting automatic transcript generation')
      generateTranscript()
    }
  }, [messages.length, participants.length, onChange]) // Include onChange as it's used in generateTranscript

  const generateTranscript = async () => {
    console.log('Generating transcript...')
    try {
      setIsGenerating(true)
      setError(null)

      const podcastId = localStorage.getItem('currentPodcastId')
      console.log('Retrieved podcastId:', podcastId)
      if (!podcastId) {
        throw new Error('No podcast ID found')
      }

      // Get participants
      const participantsResponse = await fetch(`/api/participants/podcast/${podcastId}`)
      if (!participantsResponse.ok) {
        throw new Error('Failed to fetch participants')
      }
      const participantsList = await participantsResponse.json()

      // Generate transcript
      const transcriptResponse = await fetch(`/api/podcasts/${podcastId}/generate-transcript`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          participants: participantsList
        })
      })

      if (!transcriptResponse.ok) {
        throw new Error('Failed to generate transcript')
      }

      const transcriptData = await transcriptResponse.json()
      
      // Convert the transcript data to messages format
      const newMessages = transcriptData.transcript.map((entry: any) => ({
        participantId: participants.find(p => p.name === entry.speakerName)?.id || participants[0].id,
        content: entry.text,
        timing: entry.timeOffset
      }))

      onChange(newMessages)

    } catch (err) {
      console.error('Error generating transcript:', err)
      setError(err instanceof Error ? err.message : 'Failed to generate transcript')
    } finally {
      setIsGenerating(false)
    }
  }

  const updateMessage = (index: number, field: keyof Message, value: any) => {
    const updated = [...messages]
    updated[index] = { ...updated[index], [field]: value }
    onChange(updated)
  }

  if (isGenerating) {
    return (
      <div className="p-6 flex flex-col items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary mb-4"></div>
        <p>Generating transcript...</p>
      </div>
    )
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
      <div className="flex justify-between mb-4">
        <button
          onClick={() => setEditMode(!editMode)}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          {editMode ? 'View Mode' : 'Edit Mode'}
        </button>
        <button
          onClick={generateTranscript}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
        >
          Regenerate
        </button>
      </div>

      {editMode ? (
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
          className="flex-1 overflow-y-auto bg-gradient-to-b from-gray-50 to-gray-100 rounded-lg p-4 space-y-6 shadow-inner"
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ duration: 0.3 }}
        >
          <AnimatePresence>
            {messages.map((message, index) => {
              const position = getMessagePosition(message.participantId)
              
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
                        {getParticipantName(message.participantId)}
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
              const podcastId = localStorage.getItem('currentPodcastId')
              if (!podcastId) {
                throw new Error('No podcast ID found')
              }

              const response = await fetch(`/api/podcasts/${podcastId}/transcript`, {
                method: 'POST',
                headers: {
                  'Content-Type': 'application/json'
                },
                body: JSON.stringify({ messages })
              })

              if (!response.ok) {
                throw new Error('Failed to save transcript')
              }

              onNext()  // Use onNext from props
            } catch (err) {
              setError(err instanceof Error ? err.message : 'Failed to save transcript')
            }
          }}
          disabled={messages.length === 0 || isGenerating}
          className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
        >
          Next
        </button>
      </motion.div>
    </div>
  )
}
