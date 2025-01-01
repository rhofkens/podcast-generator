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
  // Function to capitalize first letter
  const capitalizeFirstLetter = (str: string) => {
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase();
  };
  return (
    <tr>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm font-medium text-gray-900">{voice.name}</div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <VoiceTags tags={voice.tags} />
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm text-gray-900">
          {capitalizeFirstLetter(voice.gender)}
        </div>
      </td>
      <td className="px-6 py-4 whitespace-nowrap">
        <div className="text-sm text-gray-900">
          {voice.isDefault ? 'Yes' : 'No'}
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
              Set default {capitalizeFirstLetter(voice.gender)} voice
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
