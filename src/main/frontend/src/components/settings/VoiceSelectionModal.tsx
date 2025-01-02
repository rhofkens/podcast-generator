import { Voice } from '../../types/Voice'
import { MiniAudioPlayer } from './MiniAudioPlayer'
import { Info } from 'lucide-react'
import {
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '../ui/tooltip'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog'
import { Button } from '../ui/button'

interface VoiceSelectionModalProps {
  isOpen: boolean
  onClose: () => void
  onSelect: (voice: Voice) => void
  voices: Voice[]
  selectedVoiceId?: number
}

export function VoiceSelectionModal({
  isOpen,
  onClose,
  onSelect,
  voices,
  selectedVoiceId
}: VoiceSelectionModalProps) {
  return (
    <Dialog open={isOpen} onOpenChange={onClose}>
      <DialogContent className="max-w-3xl">
        <DialogHeader>
          <DialogTitle>Select Voice</DialogTitle>
        </DialogHeader>
        <div className="max-h-[60vh] overflow-y-auto">
          <table className="min-w-full">
            <thead>
              <tr className="border-b">
                <th className="px-4 py-2 text-left">Name</th>
                <th className="px-4 py-2 text-left">Tags</th>
                <th className="px-4 py-2 text-left">Gender</th>
                <th className="px-4 py-2 text-left">Preview</th>
                <th className="px-4 py-2 text-left">Select</th>
              </tr>
            </thead>
            <tbody>
              {voices.map((voice) => (
                <tr
                  key={voice.id}
                  className={`hover:bg-gray-50 ${
                    selectedVoiceId === voice.id ? 'bg-blue-50' : ''
                  }`}
                >
                  <td className="px-4 py-2">{voice.name}</td>
                  <td className="px-4 py-2">
                    <TooltipProvider>
                      <Tooltip>
                        <TooltipTrigger>
                          <Info className="h-4 w-4 text-gray-500" />
                        </TooltipTrigger>
                        <TooltipContent>
                          <div className="space-x-1">
                            {voice.tags.map((tag) => (
                              <span
                                key={tag}
                                className="inline-block px-2 py-1 text-xs bg-gray-100 rounded"
                              >
                                {tag}
                              </span>
                            ))}
                          </div>
                        </TooltipContent>
                      </Tooltip>
                    </TooltipProvider>
                  </td>
                  <td className="px-4 py-2 capitalize">{voice.gender}</td>
                  <td className="px-4 py-2">
                    <MiniAudioPlayer audioUrl={voice.audioPreviewPath} />
                  </td>
                  <td className="px-4 py-2">
                    <Button
                      variant="ghost"
                      onClick={() => onSelect(voice)}
                      className={selectedVoiceId === voice.id ? 'bg-blue-100' : ''}
                    >
                      {selectedVoiceId === voice.id ? 'Selected' : 'Select'}
                    </Button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={onClose}>
            Cancel
          </Button>
          <Button 
            onClick={() => {
              const selectedVoice = voices.find(v => v.id === selectedVoiceId)
              if (selectedVoice) {
                onSelect(selectedVoice)
                onClose()
              }
            }}
            disabled={!selectedVoiceId}
          >
            Use selected voice
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
