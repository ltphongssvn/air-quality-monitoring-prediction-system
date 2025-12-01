// air-quality-monitoring-prediction-system/frontend/src/components/Dashboard.tsx
import React, { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { RootState, AppDispatch } from '../store/store';
import { fetchAQIReadings } from '../store/aqiSlice';
import { AQICard } from './AQICard';
import { AQIChart } from './AQIChart';

export const Dashboard: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const { readings, loading, error } = useSelector((state: RootState) => state.aqi);

  useEffect(() => {
    dispatch(fetchAQIReadings());
  }, [dispatch]);

  if (loading) return <div>Loading AQI data...</div>;
  if (error) return <div style={{ color: 'red' }}>Error: {error}</div>;

  return (
    <div style={{ padding: 20 }}>
      <h1>Air Quality Dashboard</h1>
      
      <section>
        <h2>AQI Trend</h2>
        <AQIChart readings={readings} width={800} height={300} />
      </section>

      <section>
        <h2>Current Readings</h2>
        <div style={{ display: 'flex', flexWrap: 'wrap' }}>
          {readings.map((reading) => (
            <AQICard key={reading.id} reading={reading} />
          ))}
        </div>
      </section>
    </div>
  );
};
