export interface Podcast {
  id: number
  title: string
  description: string
  length: number
  status: string
  createdAt: string
  updatedAt: string
  userId: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PodcastsResponse extends PageResponse<Podcast> {}

export type PodcastStatus = 'DRAFT' | 'PROCESSING' | 'COMPLETED' | 'ERROR'
