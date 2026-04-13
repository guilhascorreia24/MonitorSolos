'use client';

import React from 'react';
import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  ResponsiveContainer,
} from 'recharts';
import { DataPoint } from '../lib/api';

interface SensorChartProps {
  data: DataPoint[];
  title: string;
  field: string;
  color: string;
}

export default function SensorChart({ data, title, field, color }: SensorChartProps) {
  // Formatar dados para o gráfico
  const chartData = data.map(point => ({
    time: new Date(point.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
    value: parseFloat(point.value.toFixed(2)),
  }));

  return (
    <div className="glass-card p-8 rounded-3xl h-[450px] flex flex-col transition-all hover:border-slate-600 group">
      <div className="flex items-center justify-between mb-8">
        <h3 className="text-xl font-bold tracking-tight text-gradient">{title}</h3>
        <div className="bg-slate-800/50 px-3 py-1 rounded-lg text-xs font-mono text-slate-400 group-hover:text-slate-200 transition-colors">
          LIVE
        </div>
      </div>
      <div className="flex-1 w-full">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart
            data={chartData}
            margin={{ top: 10, right: 10, left: -20, bottom: 0 }}
          >
            <defs>
              <linearGradient id={`color${field}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={color} stopOpacity={0.6} />
                <stop offset="95%" stopColor={color} stopOpacity={0} />
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} opacity={0.5} />
            <XAxis 
              dataKey="time" 
              stroke="#64748b" 
              fontSize={11} 
              tickLine={false}
              axisLine={false}
              minTickGap={40}
              dy={10}
            />
            <YAxis 
              stroke="#64748b" 
              fontSize={11} 
              tickLine={false}
              axisLine={false}
              tickFormatter={(value) => `${value}`}
            />
            <Tooltip
              cursor={{ stroke: '#334155', strokeWidth: 1 }}
              contentStyle={{ 
                backgroundColor: 'rgba(15, 23, 42, 0.9)',
                backdropFilter: 'blur(8px)',
                border: '1px solid rgba(255, 255, 255, 0.1)',
                borderRadius: '12px',
                boxShadow: '0 10px 15px -3px rgba(0, 0, 0, 0.1)'
              }}
              itemStyle={{ color: color, fontWeight: 'bold' }}
              labelStyle={{ color: '#94a3b8', marginBottom: '4px', fontSize: '12px' }}
            />
            <Area
              type="monotone"
              dataKey="value"
              stroke={color}
              strokeWidth={3}
              fillOpacity={1}
              fill={`url(#color${field})`}
              animationDuration={2000}
              activeDot={{ r: 6, stroke: '#fff', strokeWidth: 2, fill: color }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
