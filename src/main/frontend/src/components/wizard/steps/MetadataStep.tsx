import { useCallback } from 'react'
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

  const isValid = data.title && data.description && data.contextDescription

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

      <div className="flex justify-end">
        <button
          onClick={onNext}
          disabled={!isValid}
          className="bg-primary text-primary-foreground px-4 py-2 rounded disabled:opacity-50"
        >
          Next
        </button>
      </div>
    </div>
  )
}