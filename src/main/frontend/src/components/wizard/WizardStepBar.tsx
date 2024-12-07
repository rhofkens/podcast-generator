interface WizardStepBarProps {
  currentStep: number
  steps: Array<{
    title: string
    description: string
  }>
}

export function WizardStepBar({ currentStep, steps }: WizardStepBarProps) {
  return (
    <div className="py-4 px-6 border-b">
      <nav aria-label="Progress">
        <ol role="list" className="flex items-center">
          {steps.map((step, index) => (
            <li key={step.title} className={`relative ${index !== steps.length - 1 ? 'pr-8 sm:pr-20' : ''}`}>
              <div className="flex items-center">
                {/* Add connecting line first with lower z-index */}
                {index !== steps.length - 1 && (
                  <div 
                    className="absolute top-4 h-0.5 bg-gray-300" 
                    style={{ 
                      left: '2rem',
                      right: '0',
                      zIndex: 0 
                    }} 
                  />
                )}
                {/* Number circle with higher z-index */}
                <div
                  className={`relative flex h-8 w-8 items-center justify-center rounded-full ${
                    index < currentStep
                      ? 'bg-primary text-primary-foreground'
                      : index === currentStep
                      ? 'border-2 border-primary text-primary'
                      : 'border-2 border-gray-300 text-gray-500'
                  }`}
                  style={{ 
                    zIndex: 1
                  }}
                >
                  <span className="relative z-10 text-sm">{index + 1}</span>
                </div>
              </div>
              <div className="mt-2">
                <span className="text-sm font-medium">{step.title}</span>
                <p className="text-xs text-gray-500">{step.description}</p>
              </div>
            </li>
          ))}
        </ol>
      </nav>
    </div>
  )
}
