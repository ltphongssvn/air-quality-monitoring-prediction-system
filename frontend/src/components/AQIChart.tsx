// air-quality-monitoring-prediction-system/frontend/src/components/AQIChart.tsx
import React, { useEffect, useRef } from 'react';
import * as d3 from 'd3';
import { AQIReading } from '../store/aqiSlice';

interface AQIChartProps {
  readings: AQIReading[];
  width?: number;
  height?: number;
}

export const AQIChart: React.FC<AQIChartProps> = ({ 
  readings, 
  width = 600, 
  height = 300 
}) => {
  const svgRef = useRef<SVGSVGElement>(null);

  useEffect(() => {
    if (!svgRef.current || readings.length === 0) return;

    const svg = d3.select(svgRef.current);
    svg.selectAll('*').remove();

    const margin = { top: 20, right: 30, bottom: 40, left: 50 };
    const innerWidth = width - margin.left - margin.right;
    const innerHeight = height - margin.top - margin.bottom;

    const g = svg.append('g')
      .attr('transform', `translate(${margin.left},${margin.top})`);

    const xScale = d3.scaleTime()
      .domain(d3.extent(readings, d => new Date(d.timestamp)) as [Date, Date])
      .range([0, innerWidth]);

    const yScale = d3.scaleLinear()
      .domain([0, d3.max(readings, d => d.aqi) || 100])
      .range([innerHeight, 0]);

    const line = d3.line<AQIReading>()
      .x(d => xScale(new Date(d.timestamp)))
      .y(d => yScale(d.aqi))
      .curve(d3.curveMonotoneX);

    // X axis
    g.append('g')
      .attr('transform', `translate(0,${innerHeight})`)
      .call(d3.axisBottom(xScale).ticks(5));

    // Y axis
    g.append('g')
      .call(d3.axisLeft(yScale));

    // Line path
    g.append('path')
      .datum(readings)
      .attr('fill', 'none')
      .attr('stroke', '#2196f3')
      .attr('stroke-width', 2)
      .attr('d', line);

    // Data points
    g.selectAll('circle')
      .data(readings)
      .enter()
      .append('circle')
      .attr('cx', d => xScale(new Date(d.timestamp)))
      .attr('cy', d => yScale(d.aqi))
      .attr('r', 4)
      .attr('fill', '#2196f3');

  }, [readings, width, height]);

  return <svg ref={svgRef} width={width} height={height} />;
};
