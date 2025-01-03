import { MoreVertical, Trash2 } from 'lucide-react'
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
import { voicesApi } from '../../api/voicesApi'
import { useState } from 'react'
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from '../ui/alert-dialog'
import { useToast } from '../ui/use-toast'

interface VoiceGridRowProps {
  voice: Voice
  isStandardVoice: boolean
  onVoiceUpdated: () => void
}

export function VoiceGridRow({ voice, isStandardVoice, onVoiceUpdated }: VoiceGridRowProps) {
  const [isLoading, setIsLoading] = useState(false)
  const [showDeleteDialog, setShowDeleteDialog] = useState(false)
  const { toast } = useToast()

  const capitalizeFirstLetter = (str: string) => {
    return str.charAt(0).toUpperCase() + str.slice(1).toLowerCase()
  }

  const handleSetDefault = async () => {
    try {
      setIsLoading(true)
      await voicesApi.setDefaultVoice(voice.id, voice.gender)
      onVoiceUpdated()
      toast({
        variant: "success",
        title: "Success",
        description: `${voice.name} has been set as the default ${voice.gender.toLowerCase()} voice.`,
      })
    } catch (error) {
      console.error('Failed to set default voice:', error)
      toast({
        variant: "destructive",
        title: "Error",
        description: "Failed to set default voice. Please try again.",
      })
    } finally {
      setIsLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      setIsLoading(true)
      await voicesApi.deleteVoice(voice.id)
      onVoiceUpdated()
      toast({
        variant: "success",
        title: "Success",
        description: `${voice.name} has been deleted.`,
      })
    } catch (error) {
      console.error('Failed to delete voice:', error)
      toast({
        variant: "destructive",
        title: "Error",
        description: "Failed to delete voice. Please try again.",
      })
    } finally {
      setIsLoading(false)
      setShowDeleteDialog(false)
    }
  }

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
            <DropdownMenuItem 
              onClick={handleSetDefault}
              disabled={isLoading || voice.isDefault}
            >
              Set default {capitalizeFirstLetter(voice.gender)} voice
            </DropdownMenuItem>
            {!isStandardVoice && (
              <DropdownMenuItem 
                onClick={() => setShowDeleteDialog(true)}
                className="text-red-600"
                disabled={isLoading}
              >
                <Trash2 className="mr-2 h-4 w-4" />
                Delete
              </DropdownMenuItem>
            )}
          </DropdownMenuContent>
        </DropdownMenu>
      </td>
    </tr>

      <AlertDialog open={showDeleteDialog} onOpenChange={setShowDeleteDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>Are you absolutely sure?</AlertDialogTitle>
            <AlertDialogDescription>
              This will permanently delete the voice "{voice.name}". 
              This action cannot be undone.
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>Cancel</AlertDialogCancel>
            <AlertDialogAction 
              onClick={handleDelete}
              className="bg-red-600 hover:bg-red-700 focus:ring-red-600"
              disabled={isLoading}
            >
              {isLoading ? 'Deleting...' : 'Delete'}
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
