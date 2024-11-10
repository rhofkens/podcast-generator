export interface Podcast {
  id: number
  title: string
  description: string
  length: number
  status: 'DRAFT' | 'PROCESSING' | 'COMPLETED' | 'ERROR'
  createdAt: string
  updatedAt: string
  userId: string
}

export interface PaginatedResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface PodcastsResponse extends PaginatedResponse<Podcast> {}
