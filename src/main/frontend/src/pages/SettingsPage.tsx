import { useEffect, useState } from 'react'
import { AlertCircle } from 'lucide-react'
import { Breadcrumb } from '../components/layout/Breadcrumb'
import { VoicesGrid } from '../components/settings/VoicesGrid'
import { Voice } from '../types/Voice'
import { voicesApi } from '../api/voicesApi'
import { Alert, AlertDescription } from '../components/ui/alert'
import { useAuth } from '../contexts/AuthContext'

export function SettingsPage() {
  const [standardVoices, setStandardVoices] = useState<Voice[]>([])
  const [generatedVoices, setGeneratedVoices] = useState<Voice[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const { user, loading: authLoading } = useAuth()

  useEffect(() => {
    const fetchVoices = async () => {
      try {
        setIsLoading(true)
        setError(null)

        // Fetch standard voices
        const standardVoicesData = await voicesApi.getVoicesByType('STANDARD')
        setStandardVoices(standardVoicesData)

        // Only fetch generated voices if we have a user
        if (user?.id) {
          const generatedVoicesData = await voicesApi.getVoicesByUserIdAndType(
            user.id,
            'GENERATED'
          )
          setGeneratedVoices(generatedVoicesData)
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to load voices'
        setError(errorMessage)
        console.error('Error fetching voices:', err)
      } finally {
        setIsLoading(false)
      }
    }

    // Only fetch voices once auth is no longer loading
    if (!authLoading) {
      fetchVoices()
    }
  }, [user?.id, authLoading])

  // Show loading state while auth is loading
  if (authLoading) {
    return (
      <div className="flex items-center justify-center p-8">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
      </div>
    )
  }

  // Show error if user is not authenticated
  if (!user) {
    return (
      <Alert variant="destructive" className="m-6">
        <AlertCircle className="h-4 w-4" />
        <AlertDescription>
          You must be logged in to access voice settings
        </AlertDescription>
      </Alert>
    )
  }

  return (
    <>
      <Breadcrumb 
        items={[
          { label: 'Home', href: '/' },
          { label: 'Settings' }
        ]} 
      />
      <div className="p-6">
        <h2 className="text-2xl font-bold mb-6">Voice Settings</h2>

        {error && (
          <Alert variant="destructive" className="mb-6">
            <AlertCircle className="h-4 w-4" />
            <AlertDescription>{error}</AlertDescription>
          </Alert>
        )}

        {isLoading ? (
          <div className="flex items-center justify-center p-8">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
          </div>
        ) : (
          <>
            <VoicesGrid 
              title="Standard Voices" 
              voices={standardVoices} 
              isStandardVoices={true}
            />
            
            <VoicesGrid 
              title="Generated Voices" 
              voices={generatedVoices} 
              isStandardVoices={false}
            />

            {generatedVoices.length === 0 && (
              <div className="mt-4 text-sm text-gray-500">
                You haven't generated any custom voices yet.
              </div>
            )}
          </>
        )}
      </div>
    </>
  )
}
