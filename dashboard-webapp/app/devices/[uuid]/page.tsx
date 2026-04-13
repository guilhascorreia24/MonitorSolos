import { fetchDeviceByUuid, fetchAnalyticsHistory } from "../../lib/api";
import { 
  ChevronLeft, 
  MapPin, 
  Cpu, 
  Settings, 
  Shield, 
  Calendar,
  Activity,
  Signal
} from 'lucide-react';
import Link from "next/link";
import SensorChart from "../../components/SensorChart";

export default async function DeviceDetailsPage({ params }: { params: Promise<{ uuid: string }> }) {
  const { uuid } = await params;
  
  const device = await fetchDeviceByUuid(uuid).catch(() => null);

  if (!device) {
    return (
      <div className="max-w-4xl mx-auto py-20 text-center">
        <h1 className="text-3xl font-bold mb-4">Dispositivo não encontrado</h1>
        <p className="text-slate-400 mb-8">O UUID especificado não corresponde a nenhum sensor registado.</p>
        <Link href="/devices" className="text-purple-400 hover:text-purple-300 flex items-center justify-center gap-2">
          <ChevronLeft className="h-4 w-4" /> Voltar para a lista
        </Link>
      </div>
    );
  }

  // Buscar histórico para o dispositivo (usando o nome conforme mapeamento corrigido)
  const humidityData = await fetchAnalyticsHistory(device.name, 'air_humidity', '-24h').catch(() => []);
  const temperatureData = await fetchAnalyticsHistory(device.name, 'air_temperature', '-24h').catch(() => []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
      <header className="py-10">
        <Link href="/devices" className="text-slate-500 hover:text-slate-300 flex items-center gap-2 mb-6 transition-colors font-medium">
          <ChevronLeft className="h-4 w-4" /> Voltar para Dispositivos
        </Link>
        <div className="flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center gap-3 mb-2">
              <span className="px-3 py-1 bg-purple-600/20 text-purple-400 text-xs font-black uppercase tracking-widest rounded-full">
                {device.status}
              </span>
              <h1 className="text-4xl font-black tracking-tighter text-gradient">{device.name}</h1>
            </div>
            <p className="text-slate-400 flex items-center gap-2 font-medium">
              <MapPin className="h-4 w-4" /> {device.location}
            </p>
          </div>
          <div className="flex gap-3">
          </div>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8 mb-12">
        {/* Technical Specs Card */}
        <div className="lg:col-span-1 glass-card rounded-3xl p-8 h-fit">
          <h2 className="text-xl font-bold mb-6 flex items-center gap-2">
            <Settings className="h-5 w-5 text-slate-500" />
            Detalhes Técnicos
          </h2>
          <div className="space-y-6 text-sm font-medium">
            <div className="flex justify-between items-center pb-4 border-b border-white/5">
              <div className="text-slate-500 flex items-center gap-2"><Cpu className="h-4 w-4" /> Hardware</div>
              <div className="text-slate-200">ESP32-WROOM</div>
            </div>
            <div className="flex justify-between items-center pb-4 border-b border-white/5">
              <div className="text-slate-500 flex items-center gap-2"><Shield className="h-4 w-4" /> Firmware</div>
              <div className="text-slate-200">{device.firmwareVersion || 'v1.0.4'}</div>
            </div>
            <div className="flex justify-between items-center pb-4 border-b border-white/5">
              <div className="text-slate-500 flex items-center gap-2"><Calendar className="h-4 w-4" /> Instalado</div>
              <div className="text-slate-200">{new Date(device.installationDate || device.createdAt).toLocaleDateString()}</div>
            </div>
            <div className="flex justify-between items-center pb-4 border-b border-white/5">
              <div className="text-slate-500 flex items-center gap-2"><Signal className="h-4 w-4" /> Sinal (RSSI)</div>
              <div className="text-emerald-500">-64 dBm</div>
            </div>
            <div className="flex flex-col pt-2">
              <div className="text-slate-500 mb-2">Identificador UUID</div>
              <div className="text-xs font-mono bg-slate-800/50 p-2 rounded-lg text-slate-400 break-all">
                {device.uuid}
              </div>
            </div>
          </div>
        </div>

        {/* Analytics on Details Page */}
        <div className="lg:col-span-2 space-y-8">
           <SensorChart 
              data={humidityData} 
              title="Histórico de Humidade" 
              field="humidity" 
              color="#3b82f6" 
           />
           <SensorChart 
              data={temperatureData} 
              title="Histórico de Temperatura" 
              field="temperature" 
              color="#f59e0b" 
           />
        </div>
      </div>
    </div>
  );
}
