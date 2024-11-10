import { Breadcrumb } from '../components/layout/Breadcrumb'
import { PodcastList } from '../components/podcast/PodcastList'

export function PodcastListPage() {
  return (
    <>
      <Breadcrumb 
        items={[
          { label: 'Home', href: '/' },
          { label: 'Podcasts' }
        ]} 
      />
      <div className="p-6">
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-2xl font-bold">Your Podcasts</h2>
          <button className="bg-primary text-primary-foreground px-4 py-2 rounded-lg hover:bg-primary/90">
            New Podcast
          </button>
        </div>
        <PodcastList />
      </div>
    </>
  )
}
