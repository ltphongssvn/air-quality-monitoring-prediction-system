// air-quality-monitoring-prediction-system/frontend/src/store/aqiSlice.ts
import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { aqiService } from '../services/aqiService';

export interface AQIReading {
  id: string;
  sensorId: string;
  location: { latitude: number; longitude: number; city?: string; country?: string };
  aqi: number;
  pm25: number;
  pm10: number;
  o3: number;
  no2: number;
  co: number;
  timestamp: string;
}

interface AQIState {
  readings: AQIReading[];
  loading: boolean;
  error: string | null;
}

const initialState: AQIState = {
  readings: [],
  loading: false,
  error: null,
};

export const fetchAQIReadings = createAsyncThunk('aqi/fetchAll', async () => {
  return await aqiService.getAll();
});

const aqiSlice = createSlice({
  name: 'aqi',
  initialState,
  reducers: {
    addReading: (state, action: PayloadAction<AQIReading>) => {
      state.readings.unshift(action.payload);
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(fetchAQIReadings.pending, (state) => {
        state.loading = true;
        state.error = null;
      })
      .addCase(fetchAQIReadings.fulfilled, (state, action) => {
        state.loading = false;
        state.readings = action.payload;
      })
      .addCase(fetchAQIReadings.rejected, (state, action) => {
        state.loading = false;
        state.error = action.error.message || 'Failed to fetch AQI data';
      });
  },
});

export const { addReading, clearError } = aqiSlice.actions;
export default aqiSlice.reducer;
