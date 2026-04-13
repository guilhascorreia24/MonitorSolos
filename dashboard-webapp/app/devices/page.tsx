import { fetchDevices } from "../lib/api";
import { Cpu, MapPin, Calendar, Activity } from 'lucide-react';
import Link from "next/link";

export default async function DevicesPage() {
  const devices = await fetchDevices().catch(() => []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
      <header className="py-12">
        <h1 className="text-5xl font-black tracking-tighter text-gradient mb-4">Dispositivos</h1>
        <p className="text-slate-400 text-xl font-medium">Gestão e inventário de sensores IoT instalados na vinha.</p>
      </header>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
        {devices.length === 0 ? (
          <div className="col-span-full glass-card p-20 text-center rounded-3xl">
            <Activity className="h-12 w-12 text-slate-700 mx-auto mb-4" />
            <p className="text-slate-400 text-lg font-medium">Nenhum dispositivo encontrado.</p>
          </div>
        ) : (
          devices.map((device) => (
            <div key={device.id} className="glass-card rounded-3xl overflow-hidden hover:border-slate-500 transition-all group">
              <div className="p-8">
                <div className="flex justify-between items-start mb-6">
                  <div className="p-4 bg-purple-600/10 rounded-2xl group-hover:bg-purple-600/20 transition-colors">
                    <Cpu className="h-8 w-8 text-purple-500" />
                  </div>
                  <span className={`px-3 py-1 text-xs font-black rounded-full uppercase tracking-widest ${
                    device.status === 'ONLINE' ? 'bg-emerald-500/10 text-emerald-500' : 'bg-slate-500/10 text-slate-400'
                  }`}>
                    {device.status}
                  </span>
                </div>
                
                <h2 className="text-2xl font-bold text-slate-100 mb-4 group-hover:text-purple-400 transition-colors">{device.name}</h2>
                <div className="space-y-4">
                  <div className="flex items-center text-slate-400 font-medium">
                    <MapPin className="h-5 w-5 mr-3 text-slate-600" />
                    {device.location || 'Sem localização definida'}
                  </div>
                  <div className="flex items-center text-slate-400 font-medium">
                    <Calendar className="h-5 w-5 mr-3 text-slate-600" />
                    Instalado em: {new Date(device.installationDate || device.createdAt).toLocaleDateString()}
                  </div>
                </div>
              </div>
              <div className="px-8 py-6 bg-white/5 border-t border-white/5 flex gap-3">
                <Link 
                  href={`/devices/${device.uuid}`}
                  className="flex-1 text-center text-sm font-bold px-4 py-3 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl transition-all active:scale-95"
                >
                  Detalhes
                </Link>
                <Link 
                  href={`/devices/${device.uuid}/edit`}
                  className="flex-1 text-center text-sm font-bold px-4 py-3 border border-slate-700 hover:border-slate-500 text-slate-400 rounded-xl transition-all active:scale-95"
                >
                  Editar
                </Link>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
}
