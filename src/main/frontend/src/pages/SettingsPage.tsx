import { useEffect, useState } from 'react'
import { Breadcrumb } from '../components/layout/Breadcrumb'
import { VoicesGrid } from '../components/settings/VoicesGrid'
import { Voice } from '../types/Voice'

export function SettingsPage() {
  const [standardVoices, setStandardVoices] = useState<Voice[]>([])
  const [generatedVoices, setGeneratedVoices] = useState<Voice[]>([])

  useEffect(() => {
    // TODO: Replace with actual API calls
    // For now using mock data
    const fetchVoices = async () => {
      // Mock data - replace with actual API calls later
      setStandardVoices([
        // Add some mock standard voices
      ])
      setGeneratedVoices([
        // Add some mock generated voices
      ])
    }

    fetchVoices()
  }, [])

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
      </div>
    </>
  )
}
