'use client';

import { useEffect, useState } from 'react';
import { fetchAlerts, resolveAlert, Alert } from '../lib/api';
import { AlertTriangle, CheckCircle2, Search, Filter } from 'lucide-react';

export default function AlertsPage() {
  const [alerts, setAlerts] = useState<Alert[]>([]);
  const [loading, setLoading] = useState(true);

  const loadAlerts = async () => {
    try {
      const data = await fetchAlerts();
      setAlerts(data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAlerts();
  }, []);

  const handleResolve = async (id: number) => {
    try {
      await resolveAlert(id);
      setAlerts(prev => prev.map(a => 
        a.id === id ? { ...a, resolvedAt: new Date().toISOString() } : a
      ));
    } catch (error) {
      alert('Erro ao resolver alerta');
    }
  };

  const activeAlerts = alerts.filter(a => !a.resolvedAt);
  const historyAlerts = alerts.filter(a => a.resolvedAt);

  return (
    <div className="max-w-7xl mx-auto">
      <header className="mb-8 flex flex-col md:flex-row md:items-end justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-slate-50">Centro de Alertas</h1>
          <p className="text-slate-400">Monitorize e resolva riscos detetados pelo sistema.</p>
        </div>
        <div className="flex gap-2">
          <div className="relative">
            <Search className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-slate-500" />
            <input 
              type="text" 
              placeholder="Pesquisar alertas..." 
              className="pl-10 pr-4 py-2 bg-slate-900 border border-slate-800 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
          </div>
          <button className="p-2 bg-slate-900 border border-slate-800 rounded-lg hover:bg-slate-800 transition-colors">
            <Filter className="h-4 w-4 text-slate-400" />
          </button>
        </div>
      </header>

      <section className="mb-12">
        <h2 className="text-lg font-semibold mb-4 flex items-center">
          <AlertTriangle className="mr-2 h-5 w-5 text-amber-500" />
          Alertas Ativos
        </h2>
        {activeAlerts.length === 0 && !loading ? (
          <div className="bg-slate-900/50 p-8 text-center rounded-xl border border-dashed border-slate-700 text-slate-500">
            Excelente! Não há alertas por resolver.
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-4">
            {activeAlerts.map((alert) => (
              <div key={alert.id} className="bg-slate-900 border border-slate-800 rounded-xl p-6 flex flex-col sm:flex-row items-center justify-between gap-4">
                <div className="flex items-start space-x-4 flex-1">
                  <div className={`p-2 rounded-lg ${
                    alert.severity === 'HIGH' ? 'bg-red-500/10 text-red-500' : 'bg-amber-500/10 text-amber-500'
                  }`}>
                    <AlertTriangle className="h-6 w-6" />
                  </div>
                  <div>
                    <h3 className="font-bold text-slate-100">{alert.message}</h3>
                    <p className="text-slate-400 text-sm">Detectado em: {new Date(alert.triggeredAt).toLocaleString()}</p>
                  </div>
                </div>
                <button 
                  onClick={() => handleResolve(alert.id)}
                  className="px-6 py-2 bg-emerald-600 hover:bg-emerald-500 text-white font-medium rounded-lg transition-colors flex items-center"
                >
                  <CheckCircle2 className="mr-2 h-4 w-4" />
                  Resolver
                </button>
              </div>
            ))}
          </div>
        )}
      </section>

      <section>
        <h2 className="text-lg font-semibold mb-4 text-slate-400">Histórico de Alertas</h2>
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
          <table className="w-full text-left text-sm">
            <thead className="bg-slate-800/50 text-slate-300">
              <tr>
                <th className="px-6 py-3 font-medium">Severidade</th>
                <th className="px-6 py-3 font-medium">Mensagem</th>
                <th className="px-6 py-3 font-medium">Data</th>
                <th className="px-6 py-3 font-medium">Resolvido em</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {historyAlerts.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-8 text-center text-slate-500 italic">O histórico está vazio.</td>
                </tr>
              ) : (
                historyAlerts.slice(0, 10).map((alert) => (
                  <tr key={alert.id} className="hover:bg-slate-800/20 transition-colors">
                    <td className="px-6 py-4">
                      <span className="px-2 py-0.5 rounded text-xs bg-slate-800 text-slate-400">{alert.severity}</span>
                    </td>
                    <td className="px-6 py-4 text-slate-300 font-medium">{alert.message}</td>
                    <td className="px-6 py-4 text-slate-400">{new Date(alert.triggeredAt).toLocaleDateString()}</td>
                    <td className="px-6 py-4 text-slate-500">{new Date(alert.resolvedAt!).toLocaleDateString()}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>
    </div>
  );
}
