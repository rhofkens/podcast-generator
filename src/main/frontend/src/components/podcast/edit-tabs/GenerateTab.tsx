import { PodcastStep } from '../../wizard/steps/PodcastStep'

interface GenerateTabProps {
  podcastId: string
  onBack: () => void
}

export function GenerateTab({ podcastId, onBack }: GenerateTabProps) {
  return (
    <div className="bg-white rounded-lg shadow">
      <PodcastStep
        podcastId={podcastId}
        onBack={onBack}
        onComplete={() => {
          window.location.href = '/podcasts'
        }}
      />
    </div>
  )
}
