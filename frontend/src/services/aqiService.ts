// air-quality-monitoring-prediction-system/frontend/src/services/aqiService.ts
import axios from 'axios';
import { AQIReading } from '../store/aqiSlice';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:9000';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
});

export const aqiService = {
  getAll: async (): Promise<AQIReading[]> => {
    const response = await api.get('/api/v1/aqi');
    return response.data;
  },

  getById: async (id: string): Promise<AQIReading> => {
    const response = await api.get(`/api/v1/aqi/${id}`);
    return response.data;
  },

  create: async (reading: Omit<AQIReading, 'id'>): Promise<AQIReading> => {
    const response = await api.post('/api/v1/aqi', reading);
    return response.data;
  },

  getPredictions: async (location?: string) => {
    const url = location ? `/api/v1/predictions/${location}` : '/api/v1/predictions';
    const response = await api.get(url);
    return response.data;
  },
};
