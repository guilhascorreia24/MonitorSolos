'use client';

import { useEffect, useState, use } from 'react';
import { useRouter } from 'next/navigation';
import { fetchDeviceByUuid, updateDevice, Device } from '../../../lib/api';
import { 
  ChevronLeft, 
  Save, 
  X, 
  Loader2, 
  Tag, 
  MapPin,
  AlertCircle
} from 'lucide-react';
import Link from 'next/link';

export default function EditDevicePage({ params }: { params: Promise<{ uuid: string }> }) {
  const router = useRouter();
  const { uuid } = use(params);
  
  const [device, setDevice] = useState<Device | null>(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [formData, setFormData] = useState({
    name: '',
    location: ''
  });

  useEffect(() => {
    async function load() {
      try {
        const data = await fetchDeviceByUuid(uuid);
        setDevice(data);
        setFormData({
          name: data.name,
          location: data.location || ''
        });
      } catch (err) {
        setError('Não foi possível carregar os dados do dispositivo.');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, [uuid]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setError(null);

    try {
      await updateDevice(uuid, formData);
      router.push(`/devices/${uuid}`);
      router.refresh();
    } catch (err) {
      setError('Erro ao guardar as alterações. Tente novamente.');
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="flex flex-col items-center justify-center py-32 space-y-4">
        <Loader2 className="h-12 w-12 text-purple-500 animate-spin" />
        <p className="text-slate-400 font-medium animate-pulse">A carregar dados do sensor...</p>
      </div>
    );
  }

  if (!device && !loading) {
    return (
      <div className="max-w-xl mx-auto py-20 text-center">
        <AlertCircle className="h-16 w-16 text-red-500 mx-auto mb-6" />
        <h1 className="text-3xl font-bold mb-4">Dispositivo inexistente</h1>
        <Link href="/devices" className="text-purple-400 hover:text-purple-300 font-bold">
          Voltar para a lista
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
      <header className="py-12">
        <Link href={`/devices/${uuid}`} className="text-slate-500 hover:text-slate-300 flex items-center gap-2 mb-6 transition-colors font-medium">
          <ChevronLeft className="h-4 w-4" /> Cancelar e Voltar
        </Link>
        <h1 className="text-5xl font-black tracking-tighter text-gradient mb-4">Editar Sensor</h1>
        <p className="text-slate-400 text-xl font-medium italic">"{device?.name}"</p>
      </header>

      <form onSubmit={handleSubmit} className="space-y-8">
        <div className="glass-card rounded-3xl p-10 space-y-10">
          {error && (
            <div className="bg-red-500/10 border border-red-500/20 p-4 rounded-2xl flex items-center gap-3 text-red-500 text-sm font-bold">
              <AlertCircle className="h-5 w-5" />
              {error}
            </div>
          )}

          {/* Device Name Field */}
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
              <Tag className="h-4 w-4" /> Nome do Dispositivo
            </label>
            <input
              type="text"
              required
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              className="w-full bg-slate-900/50 border border-slate-800 rounded-2xl px-6 py-4 text-lg font-bold focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
              placeholder="Ex: Sensor Norte 01"
            />
            <p className="text-slate-500 text-xs font-medium">Este nome será usado para identificar a telemetria no InfluxDB.</p>
          </div>

          {/* Location Field */}
          <div className="space-y-3">
            <label className="text-xs font-black uppercase tracking-widest text-slate-500 flex items-center gap-2">
              <MapPin className="h-4 w-4" /> Localização / Talhão
            </label>
            <input
              type="text"
              value={formData.location}
              onChange={(e) => setFormData({ ...formData, location: e.target.value })}
              className="w-full bg-slate-900/50 border border-slate-800 rounded-2xl px-6 py-4 text-lg font-bold focus:outline-none focus:ring-2 focus:ring-purple-500 focus:border-transparent transition-all"
              placeholder="Ex: Talhão A, Fileira 12"
            />
          </div>

          {/* Read Only Stats */}
          <div className="pt-6 border-t border-white/5 grid grid-cols-2 gap-6">
             <div>
                <div className="text-[10px] font-black uppercase tracking-widest text-slate-600 mb-1">UUID Sistema</div>
                <div className="text-xs font-mono text-slate-500 truncate">{uuid}</div>
             </div>
             <div>
                <div className="text-[10px] font-black uppercase tracking-widest text-slate-600 mb-1">Data Registo</div>
                <div className="text-xs text-slate-500">
                  {device ? new Date(device.createdAt).toLocaleDateString() : '-'}
                </div>
             </div>
          </div>
        </div>

        <div className="flex items-center justify-end gap-4">
          <Link 
            href={`/devices/${uuid}`}
            className="px-8 py-4 bg-slate-900 border border-slate-800 rounded-2xl font-bold text-slate-400 hover:bg-slate-800 transition-all flex items-center gap-2"
          >
            <X className="h-5 w-5" /> Descartar
          </Link>
          <button
            type="submit"
            disabled={saving}
            className="px-10 py-4 bg-purple-600 rounded-2xl font-black text-white hover:bg-purple-500 transition-all shadow-lg shadow-purple-600/30 flex items-center gap-2 disabled:opacity-50 disabled:scale-100 active:scale-95"
          >
            {saving ? (
              <Loader2 className="h-5 w-5 animate-spin" />
            ) : (
              <Save className="h-5 w-5" />
            )}
            Guardar Alterações
          </button>
        </div>
      </form>
    </div>
  );
}
