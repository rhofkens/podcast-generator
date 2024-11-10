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
                <div
                  className={`relative flex h-8 w-8 items-center justify-center rounded-full ${
                    index < currentStep
                      ? 'bg-primary text-primary-foreground'
                      : index === currentStep
                      ? 'border-2 border-primary'
                      : 'border-2 border-gray-300'
                  }`}
                >
                  <span className="text-sm">{index + 1}</span>
                </div>
                {index !== steps.length - 1 && (
                  <div className="absolute top-4 w-full h-0.5 -translate-y-1/2 bg-gray-300" />
                )}
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
