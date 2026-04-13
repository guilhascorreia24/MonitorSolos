const isServer = typeof window === 'undefined';

const INTERNAL_API_URL = process.env.INTERNAL_API_URL || 
  (process.env.NODE_ENV === 'development' ? 'http://localhost:8000/api' : 'http://api-gateway:8000/api');

const EXTERNAL_API_URL = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8000/api';

export const API_BASE_URL = isServer ? INTERNAL_API_URL : EXTERNAL_API_URL;

export interface Device {
  id: number;
  uuid: string;
  name: string;
  location: string;
  status: string;
  installationDate: string;
  firmwareVersion: string;
  createdAt: string;
  updatedAt: string;
}

export interface Alert {
  id: number;
  deviceId: number;
  deviceName?: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  message: string;
  triggeredAt: string;
  resolvedAt: string | null;
  createdAt: string;
}

export interface DataPoint {
  timestamp: string;
  value: number;
}

export async function fetchDevices(): Promise<Device[]> {
  const response = await fetch(`${API_BASE_URL}/devices`, { cache: 'no-store' });
  if (!response.ok) throw new Error('Failed to fetch devices');
  return response.json();
}

export async function fetchDeviceByUuid(uuid: string): Promise<Device> {
  const response = await fetch(`${API_BASE_URL}/devices/${uuid}`, { cache: 'no-store' });
  if (!response.ok) throw new Error('Failed to fetch device details');
  return response.json();
}

export async function updateDevice(uuid: string, data: Partial<Device>): Promise<Device> {
  const response = await fetch(`${API_BASE_URL}/devices/${uuid}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
  if (!response.ok) throw new Error('Failed to update device');
  return response.json();
}

export async function fetchAlerts(): Promise<Alert[]> {
  const response = await fetch(`${API_BASE_URL}/alerts`, { cache: 'no-store' });
  if (!response.ok) throw new Error('Failed to fetch alerts');
  return response.json();
}

export async function resolveAlert(id: number): Promise<void> {
  const response = await fetch(`${API_BASE_URL}/alerts/${id}/resolve`, {
    method: 'PATCH',
  });
  if (!response.ok) throw new Error('Failed to resolve alert');
}

export async function fetchAnalyticsHistory(
  deviceId: string, 
  field: string = 'air_humidity', 
  range: string = '-24h'
): Promise<DataPoint[]> {
  const response = await fetch(`${API_BASE_URL}/analytics/history/${deviceId}?field=${field}&range=${range}`, { cache: 'no-store' });
  if (!response.ok) throw new Error('Failed to fetch analytics history');
  return response.json();
}
