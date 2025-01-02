import { Voice } from '../../types/Voice'
import { MiniAudioPlayer } from './MiniAudioPlayer'
import { Volume2 } from 'lucide-react'
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from '../ui/dialog'
import { Button } from '../ui/button'
import { 
  Tooltip,
  TooltipContent,
  TooltipProvider,
  TooltipTrigger,
} from '../ui/tooltip'

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
                <th className="px-4 py-2 text-center">
                  <TooltipProvider>
                    <Tooltip>
                      <TooltipTrigger>
                        <Volume2 className="h-4 w-4" />
                      </TooltipTrigger>
                      <TooltipContent>
                        <p>Listen to voice</p>
                      </TooltipContent>
                    </Tooltip>
                  </TooltipProvider>
                </th>
              </tr>
            </thead>
            <tbody>
              {voices.map((voice) => (
                <tr
                  key={voice.id}
                  onClick={() => onSelect(voice)}
                  className={`hover:bg-gray-50 cursor-pointer ${
                    selectedVoiceId === voice.id ? 'bg-blue-50' : ''
                  }`}
                >
                  <td className="px-4 py-2">{voice.name}</td>
                  <td className="px-4 py-2">
                    <div className="flex flex-wrap gap-1">
                      {voice.tags.map((tag) => (
                        <span
                          key={tag}
                          className="inline-block px-2 py-1 text-xs bg-gray-100 rounded"
                        >
                          {tag}
                        </span>
                      ))}
                    </div>
                  </td>
                  <td className="px-4 py-2 text-center">
                    <div onClick={(e) => e.stopPropagation()}>
                      <MiniAudioPlayer audioUrl={voice.audioPreviewPath} />
                    </div>
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
            Select voice
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  )
}
