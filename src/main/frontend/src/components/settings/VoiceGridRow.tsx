import { MoreVertical } from 'lucide-react'
import { Voice } from '../../types/Voice'
import { Button } from '../ui/button'
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../ui/dropdown-menu'
import { MiniAudioPlayer } from './MiniAudioPlayer'
import { VoiceTags } from './VoiceTags'

interface VoiceGridRowProps {
  voice: Voice
  isStandardVoice: boolean
}

export function VoiceGridRow({ voice, isStandardVoice }: VoiceGridRowProps) {
  return (
    <tr>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm font-medium text-gray-900">{voice.name}</div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <VoiceTags tags={voice.tags} />
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm text-gray-900">{voice.gender}</div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm text-gray-900">
          {voice.isDefault ? '✓' : ''}
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <MiniAudioPlayer audioUrl={voice.audioPreviewPath} />
      </td>
      <td className="px-6 py-4 whitespace-nowrap text-right">
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button variant="ghost" className="h-8 w-8 p-0">
              <MoreVertical className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end">
            <DropdownMenuItem>
              Make default {voice.gender}
            </DropdownMenuItem>
            {!isStandardVoice && (
              <>
                <DropdownMenuItem>
                  Edit
                </DropdownMenuItem>
                <DropdownMenuItem className="text-red-600">
                  Delete
                </DropdownMenuItem>
              </>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </td>
    </tr>
  )
}
