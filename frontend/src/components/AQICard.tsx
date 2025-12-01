// air-quality-monitoring-prediction-system/frontend/src/components/AQICard.tsx
import React from 'react';
import { AQIReading } from '../store/aqiSlice';

interface AQICardProps {
  reading: AQIReading;
}

const getAQIColor = (aqi: number): string => {
  if (aqi <= 50) return '#00e400';
  if (aqi <= 100) return '#ffff00';
  if (aqi <= 150) return '#ff7e00';
  if (aqi <= 200) return '#ff0000';
  if (aqi <= 300) return '#8f3f97';
  return '#7e0023';
};

const getAQICategory = (aqi: number): string => {
  if (aqi <= 50) return 'Good';
  if (aqi <= 100) return 'Moderate';
  if (aqi <= 150) return 'Unhealthy for Sensitive';
  if (aqi <= 200) return 'Unhealthy';
  if (aqi <= 300) return 'Very Unhealthy';
  return 'Hazardous';
};

export const AQICard: React.FC<AQICardProps> = ({ reading }) => {
  const color = getAQIColor(reading.aqi);
  const category = getAQICategory(reading.aqi);

  return (
    <div style={{ border: `3px solid ${color}`, borderRadius: 8, padding: 16, margin: 8 }}>
      <h3>{reading.location.city || reading.sensorId}</h3>
      <div style={{ fontSize: 48, fontWeight: 'bold', color }}>{reading.aqi}</div>
      <div style={{ color }}>{category}</div>
      <div style={{ marginTop: 8, fontSize: 12 }}>
        <div>PM2.5: {reading.pm25} μg/m³</div>
        <div>PM10: {reading.pm10} μg/m³</div>
        <div>O₃: {reading.o3} ppm</div>
      </div>
    </div>
  );
};
