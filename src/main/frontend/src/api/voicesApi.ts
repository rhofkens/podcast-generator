import axios from 'axios'
import { Voice } from '../types/Voice'

const API_BASE_URL = '/api/voices'

export const voicesApi = {
  async getAllVoices(): Promise<Voice[]> {
    const response = await axios.get(API_BASE_URL)
    return response.data
  },

  async getVoicesByType(voiceType: Voice['voiceType']): Promise<Voice[]> {
    const response = await axios.get(`${API_BASE_URL}/type/${voiceType}`)
    return response.data
  },

  async getVoicesByUserId(userId: string): Promise<Voice[]> {
    const response = await axios.get(`${API_BASE_URL}/user/${userId}`)
    return response.data
  },

  async getVoicesByUserIdAndType(userId: string, voiceType: Voice['voiceType']): Promise<Voice[]> {
    const response = await axios.get(`${API_BASE_URL}/user/${userId}/type/${voiceType}`)
    return response.data
  },

  async setDefaultVoice(voiceId: number, gender: Voice['gender']): Promise<Voice> {
    const response = await axios.put(`${API_BASE_URL}/${voiceId}/default/${gender}`)
    return response.data
  }
}
