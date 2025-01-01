export interface Voice {
  id: number
  name: string
  tags: string[]
  externalVoiceId: string
  voiceType: 'STANDARD' | 'GENERATED'
  userId?: string
  gender: 'male' | 'female'
  isDefault: boolean
  audioPreviewPath: string
}
