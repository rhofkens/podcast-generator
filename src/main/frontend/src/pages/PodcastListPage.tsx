import { Breadcrumb } from '../components/layout/Breadcrumb'

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
        <div className="bg-white rounded-lg shadow">
          <div className="p-4">
            <p className="text-gray-600">No podcasts yet. Create your first podcast to get started.</p>
          </div>
        </div>
      </div>
    </>
  )
}
