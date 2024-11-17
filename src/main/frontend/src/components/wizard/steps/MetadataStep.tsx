import { useCallback, useState } from 'react'
import { useDropzone } from 'react-dropzone'

interface MetadataStepProps {
  data: {
    title: string
    description: string
    length: number
    contextDescription: string
    contextUrl?: string
    contextFile?: File
  }
  onChange: (field: string, value: any) => void
  onNext: () => void
}

export function MetadataStep({ data, onChange, onNext }: MetadataStepProps) {
  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles[0]) {
      onChange('contextFile', acceptedFiles[0])
    }
  }, [onChange])

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
      'application/vnd.ms-powerpoint': ['.ppt']
    },
    maxFiles: 1
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const handleNext = async () => {
    console.log('handleNext called', { data });
    try {
      setIsSubmitting(true);
      setError(null);
      
      // Step 1: Create the podcast with basic metadata
      const podcastData = {
        title: data.title,
        description: data.description,
        length: data.length,
        status: 'DRAFT',
        userId: 'dev-user-123'
      };

      console.log('Creating podcast with data:', podcastData);
      
      const podcastResponse = await fetch('/api/podcasts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(podcastData),
      });

      if (!podcastResponse.ok) {
        const errorData = await podcastResponse.json();
        throw new Error(errorData.message || 'Failed to create podcast');
      }

      const podcast = await podcastResponse.json();
      console.log('Created podcast:', podcast);
      
      // Step 2: Create the context and associate it with the podcast
      const contextData = {
        descriptionText: data.contextDescription,
        sourceUrl: data.contextUrl || null,
        podcast: {
          id: podcast.id
        }
      };

      // Verify the structure before sending
      console.log('Context data before fetch:', contextData);
      console.log('Stringified context data:', JSON.stringify(contextData));
      
      const contextResponse = await fetch('/api/contexts', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(contextData),
      });

      if (!contextResponse.ok) {
        const errorData = await contextResponse.json();
        throw new Error(errorData.message || 'Failed to create context');
      }

      const context = await contextResponse.json();
      console.log('Created context:', context);

      // Store the podcast ID for later use
      localStorage.setItem('currentPodcastId', podcast.id.toString());
      
      onNext();
    } catch (error) {
      console.error('Error creating podcast:', error)
      setError(error instanceof Error ? error.message : 'Failed to create podcast')
    } finally {
      setIsSubmitting(false)
    }
  }

  const isValid = data.title && data.description && data.contextDescription
  console.log('Form validity:', { isValid, data });

  return (
    <div className="p-6 space-y-8">
      <section>
        <h3 className="text-lg font-semibold mb-4">Settings</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Title</label>
            <input
              type="text"
              value={data.title}
              onChange={(e) => onChange('title', e.target.value)}
              className="w-full p-2 border rounded"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              value={data.description}
              onChange={(e) => onChange('description', e.target.value)}
              className="w-full p-2 border rounded"
              rows={3}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Length (minutes)</label>
            <input
              type="number"
              value={data.length}
              onChange={(e) => onChange('length', parseInt(e.target.value))}
              className="w-full p-2 border rounded"
              min={1}
            />
          </div>
        </div>
      </section>

      <section>
        <h3 className="text-lg font-semibold mb-4">Context</h3>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <textarea
              value={data.contextDescription}
              onChange={(e) => onChange('contextDescription', e.target.value)}
              className="w-full p-2 border rounded"
              rows={4}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">URL (optional)</label>
            <input
              type="url"
              value={data.contextUrl}
              onChange={(e) => onChange('contextUrl', e.target.value)}
              className="w-full p-2 border rounded"
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">File Upload (optional)</label>
            <div
              {...getRootProps()}
              className={`border-2 border-dashed rounded-lg p-6 text-center cursor-pointer
                ${isDragActive ? 'border-primary bg-primary/5' : 'border-gray-300'}`}
            >
              <input {...getInputProps()} />
              {data.contextFile ? (
                <p>Selected file: {data.contextFile.name}</p>
              ) : (
                <p>Drop a file here, or click to select</p>
              )}
              <p className="text-sm text-gray-500 mt-2">
                Supported formats: PDF, DOCX, TXT, PPT
              </p>
            </div>
          </div>
        </div>
      </section>

      <div className="flex flex-col gap-4">
        {error && (
          <div className="bg-red-50 text-red-500 p-4 rounded-lg">
            {error}
          </div>
        )}
        <div className="flex justify-end">
          <button
            onClick={handleNext}
            disabled={!isValid || isSubmitting}
            className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
          >
            {isSubmitting ? 'Creating...' : 'Next'}
          </button>
        </div>
      </div>
    </div>
  )
}
