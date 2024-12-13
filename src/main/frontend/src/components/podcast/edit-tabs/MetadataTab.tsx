import { MetadataStep } from '../../wizard/steps/MetadataStep'

interface MetadataTabProps {
  data: any
  onChange: (data: any) => void
}

export function MetadataTab({ data, onChange }: MetadataTabProps) {
  return (
    <div className="bg-white rounded-lg shadow">
      <MetadataStep
        data={data}
        onChange={(field, value) => {
          onChange({
            ...data,
            [field]: value
          })
        }}
        onNext={() => {}} // Not used in edit mode
        editMode={true}
        hideControls={true}
      />
    </div>
  )
}
