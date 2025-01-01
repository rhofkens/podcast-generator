interface VoiceTagsProps {
  tags: string[]
}

export function VoiceTags({ tags }: VoiceTagsProps) {
  return (
    <div className="flex flex-wrap gap-1">
      {tags.map((tag) => (
        <span
          key={tag}
          className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-100 text-blue-800"
        >
          {tag}
        </span>
      ))}
    </div>
  )
}
