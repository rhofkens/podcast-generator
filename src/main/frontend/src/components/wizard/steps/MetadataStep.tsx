import { useCallback, useState, useEffect } from 'react'
import { cn } from '../../../lib/utils'
import { useDropzone } from 'react-dropzone'
import { Button } from "@/components/ui/button"

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
  editMode?: boolean
  hideControls?: boolean
}

export function MetadataStep({ 
  data, 
  onChange, 
  onNext, 
  editMode = false,
  hideControls = false 
}: MetadataStepProps) {
  const [editedFields, setEditedFields] = useState<Set<string>>(new Set())

  useEffect(() => {
    // If in edit mode, mark ALL fields as edited immediately on mount
    if (editMode) {
      setEditedFields(new Set([
        'title',
        'description',
        'length',
        'contextDescription',
        'contextUrl'
      ]));
    }
  }, [editMode]); // Only run when editMode changes
  
  const handleInputChange = (field: string, value: any) => {
    if (!editMode) {
      setEditedFields(prev => new Set([...prev, field]))
    }
    onChange(field, value)
  }

  const handleFieldFocus = (field: string) => {
    if (!editMode && !editedFields.has(field)) {
      onChange(field, '')
      setEditedFields(prev => new Set([...prev, field]))
    }
  }
  const onDrop = useCallback((acceptedFiles: File[]) => {
    if (acceptedFiles[0]) {
      onChange('contextFile', acceptedFiles[0])
    }
  }, [onChange])

  const MAX_FILE_SIZE = 50 * 1024 * 1024; // 50MB in bytes

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    accept: {
      'application/pdf': ['.pdf'],
      'application/vnd.openxmlformats-officedocument.wordprocessingml.document': ['.docx'],
      'text/plain': ['.txt'],
      'application/vnd.ms-powerpoint': ['.ppt']
    },
    maxFiles: 1,
    maxSize: MAX_FILE_SIZE
  })

  const [isSubmitting, setIsSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [isLoadingSample, setIsLoadingSample] = useState(true)
  const [sampleError, setSampleError] = useState<string | null>(null)
  const [isExtractingUrl, setIsExtractingUrl] = useState(false)
  const [isExtractingDoc, setIsExtractingDoc] = useState(false)
  const [extractError, setExtractError] = useState<string | null>(null)

  const handleExtractDocument = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!data.contextFile) {
      return;
    }

    try {
      setIsExtractingDoc(true);
      setExtractError(null);

      const formData = new FormData();
      formData.append('file', data.contextFile);

      const response = await fetch('/api/contexts/extract-document', {
        method: 'POST',
        body: formData,
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.message || 'Failed to extract document content');
      }

      const extractedData = await response.json();
      console.log('Extracted data:', extractedData);

      // Update all relevant fields if they haven't been edited
      if (!editedFields.has('title')) {
        handleInputChange('title', extractedData.title || '');
        setEditedFields(prev => new Set([...prev, 'title']));
      }

      if (!editedFields.has('description')) {
        handleInputChange('description', extractedData.description || '');
        setEditedFields(prev => new Set([...prev, 'description']));
      }

      // Update the context description
      handleInputChange('contextDescription', extractedData.content || '');
      setEditedFields(prev => new Set([...prev, 'contextDescription']));

    } catch (error) {
      console.error('Error extracting document content:', error);
      setExtractError(error instanceof Error ? error.message : 'Failed to extract document content');
    } finally {
      setIsExtractingDoc(false);
    }
  };

  const handleExtractContext = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (!data.contextUrl?.trim()) {
      return
    }

    try {
      setIsExtractingUrl(true)
      setExtractError(null)

      const response = await fetch(`/api/contexts/scrape?url=${encodeURIComponent(data.contextUrl)}`)
      
      if (!response.ok) {
        const errorData = await response.json()
        throw new Error(errorData.message || 'Failed to extract context')
      }

      const scrapedData = await response.json()
      console.log('Scraped data:', scrapedData) // For debugging
      
      // Update both title and context if they haven't been edited
      if (!editedFields.has('title')) {
        handleInputChange('title', scrapedData.title || '')
        setEditedFields(prev => new Set([...prev, 'title']))
      }
      
      // Make sure we're using the content field from the DTO
      handleInputChange('contextDescription', scrapedData.content || '')
      setEditedFields(prev => new Set([...prev, 'contextDescription']))
      
    } catch (error) {
      console.error('Error extracting context:', error)
      setExtractError(error instanceof Error ? error.message : 'Failed to extract context')
    } finally {
      setIsExtractingUrl(false)
    }
  }

  useEffect(() => {
    // Only load sample data if we're not in edit mode
    if (!editMode) {
      loadSampleData()
    } else {
      setIsLoadingSample(false)
    }
  }, [editMode]) // Only depend on editMode

  const loadSampleData = async () => {
    // Don't load sample data if in edit mode
    if (editMode) {
      setIsLoadingSample(false);
      return;
    }

    try {
      setIsLoadingSample(true)
      setSampleError(null)
      
      const response = await fetch('/api/podcasts/sample')
      if (response.status === 405) {
        // Sample data loading is disabled, just set loading to false
        setIsLoadingSample(false);
        return;
      }
      
      if (!response.ok) {
        throw new Error('Failed to load sample data')
      }
      
      const sampleData = await response.json()
      
      onChange('title', sampleData.title)
      onChange('description', sampleData.description)
      onChange('length', sampleData.length)
      if (sampleData.context) {
        onChange('contextDescription', sampleData.context.descriptionText)
        onChange('contextUrl', sampleData.context.sourceUrl)
      }

      // Don't mark sample data as edited
      setEditedFields(new Set())
      
    } catch (error) {
      setSampleError(error instanceof Error ? error.message : 'Failed to load sample data')
    } finally {
      setIsLoadingSample(false)
    }
  }

  const handleNext = async () => {
    console.log('handleNext called', { data, editMode });
    try {
      setIsSubmitting(true);
      setError(null);
      
      // Get the existing podcastId from URL if in edit mode
      const currentPodcastId = window.location.pathname.split('/').pop();
      
      if (editMode && currentPodcastId) {
        // Update existing podcast
        console.log('Updating existing podcast:', currentPodcastId);
        
        const podcastResponse = await fetch(`/api/podcasts/${currentPodcastId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            title: data.title,
            description: data.description,
            length: data.length,
            status: 'DRAFT',
            userId: 'dev-user-123'
          }),
        });

        if (!podcastResponse.ok) {
          const errorData = await podcastResponse.json();
          throw new Error(errorData.message || 'Failed to update podcast');
        }

        // Update existing context
        const contextResponse = await fetch(`/api/contexts/podcast/${currentPodcastId}`, {
          method: 'PUT',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            descriptionText: data.contextDescription,
            sourceUrl: data.contextUrl || null,
            podcast: {
              id: parseInt(currentPodcastId)
            }
          }),
        });

        if (!contextResponse.ok) {
          const errorData = await contextResponse.json();
          throw new Error(errorData.message || 'Failed to update context');
        }

        // Store the podcast ID for later use
        localStorage.setItem('currentPodcastId', currentPodcastId);
        
      } else {
        // Create new podcast
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
        
        // Create the context and associate it with the podcast
        const contextData = {
          descriptionText: data.contextDescription,
          sourceUrl: data.contextUrl || null,
          podcast: {
            id: podcast.id
          }
        };

        console.log('Context data before fetch:', contextData);
        
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
      }
      
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
        <div className="mb-4">
          <h3 className="text-lg font-semibold">Settings</h3>
        </div>
        {isLoadingSample ? (
          <div className="p-6 flex justify-center items-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-primary"></div>
          </div>
        ) : sampleError ? (
          <div className="bg-red-50 text-red-500 p-4 rounded-lg mb-4">
            {sampleError}
          </div>
        ) : null}
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Title</label>
            <input
              type="text"
              value={data.title}
              onChange={(e) => handleInputChange('title', e.target.value)}
              className={cn(
                "w-full p-2 border rounded",
                (!editMode && !editedFields.has('title')) ? "italic text-gray-400" : "text-gray-900"
              )}
              onFocus={() => handleFieldFocus('title')}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Description</label>
            <input
              type="text"
              value={data.description}
              onChange={(e) => handleInputChange('description', e.target.value)}
              className={cn(
                "w-full p-2 border rounded",
                (!editMode && !editedFields.has('description')) ? "italic text-gray-400" : "text-gray-900"
              )}
              onFocus={() => handleFieldFocus('description')}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Length (minutes)</label>
            <input
              type="number"
              value={data.length}
              onChange={(e) => handleInputChange('length', parseInt(e.target.value))}
              className={cn(
                "w-full p-2 border rounded",
                (!editMode && !editedFields.has('length')) ? "italic text-gray-400" : "text-gray-900"
              )}
              min={1}
              onFocus={() => handleFieldFocus('length')}
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
              onChange={(e) => handleInputChange('contextDescription', e.target.value)}
              className={cn(
                "w-full p-2 border rounded",
                (!editMode && !editedFields.has('contextDescription')) ? "italic text-gray-400" : "text-gray-900"
              )}
              rows={4}
              onFocus={() => handleFieldFocus('contextDescription')}
            />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">URL (optional)</label>
            <div className="flex gap-2">
              <input
                type="url"
                value={data.contextUrl}
                onChange={(e) => handleInputChange('contextUrl', e.target.value)}
                className={cn(
                  "flex-1 p-2 border rounded",
                  (!editMode && !editedFields.has('contextUrl')) ? "italic text-gray-400" : "text-gray-900"
                )}
                onFocus={() => handleFieldFocus('contextUrl')}
              />
              <Button
                onClick={handleExtractContext}
                disabled={!data.contextUrl?.trim() || isExtractingUrl}
                className="whitespace-nowrap"
              >
                {isExtractingUrl ? (
                  <>
                    <span className="animate-spin mr-2">⭮</span>
                    Extracting...
                  </>
                ) : (
                  'Extract Context'
                )}
              </Button>
            </div>
            {extractError && (
              <p className="text-sm text-red-500 mt-1">
                {extractError}
              </p>
            )}
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">File Upload (optional)</label>
            <div className="space-y-2">
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
              {data.contextFile && (
                <Button
                  onClick={handleExtractDocument}
                  disabled={isExtractingDoc}
                  className="w-full"
                >
                  {isExtractingDoc ? (
                    <>
                      <span className="animate-spin mr-2">⭮</span>
                      Extracting...
                    </>
                  ) : (
                    'Extract Document Content'
                  )}
                </Button>
              )}
              {extractError && (
                <p className="text-sm text-red-500 mt-1">
                  {extractError}
                </p>
              )}
            </div>
          </div>
        </div>
      </section>

      {!hideControls && (
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
      )}
    </div>
  )
}
