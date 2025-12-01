// air-quality-monitoring-prediction-system/frontend/src/store/store.ts
import { configureStore } from '@reduxjs/toolkit';
import aqiReducer from './aqiSlice';

export const store = configureStore({
  reducer: {
    aqi: aqiReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
