import { Voice } from '../../types/Voice'
import { VoiceGridRow } from './VoiceGridRow'

interface VoicesGridProps {
  title: string
  voices: Voice[]
  isStandardVoices?: boolean
  onVoiceUpdated: () => void
}

export function VoicesGrid({ title, voices, isStandardVoices = false, onVoiceUpdated }: VoicesGridProps) {
  return (
    <div className="mt-6">
      <h3 className="text-lg font-semibold mb-4">{title}</h3>
      <div className="bg-white shadow-sm rounded-lg overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Name
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tags
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Gender
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Default
              </th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Preview
              </th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">
                Actions
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {voices.map((voice) => (
              <VoiceGridRow 
                key={voice.id} 
                voice={voice} 
                isStandardVoice={isStandardVoices}
                onVoiceUpdated={onVoiceUpdated}
              />
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}
