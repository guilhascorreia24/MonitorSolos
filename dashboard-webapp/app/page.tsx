import { fetchDevices, fetchAlerts, fetchAnalyticsHistory } from "./lib/api";
import {
  Cpu,
  AlertTriangle,
  CheckCircle2,
  Activity,
  Droplets,
  Thermometer,
  ExternalLink,
  ChevronRight
} from 'lucide-react';
import SensorChart from "./components/SensorChart";
import RiskCard from "./components/RiskCard";

export default async function DashboardPage() {
  const devices = await fetchDevices().catch(() => []);
  const alerts = await fetchAlerts().catch(() => []);
  const activeAlerts = alerts.filter(a => !a.resolvedAt);

  // Determinar o nível de risco atual baseado nos alertas ativos
  let currentRisk: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL" = "LOW";
  if (activeAlerts.some(a => a.severity === "CRITICAL")) currentRisk = "CRITICAL";
  else if (activeAlerts.some(a => a.severity === "HIGH")) currentRisk = "HIGH";
  else if (activeAlerts.some(a => a.severity === "MEDIUM")) currentRisk = "MEDIUM";

  // Selecionar o primeiro dispositivo para os gráficos de demonstração
  const mainDevice = devices.length > 0 ? devices[0] : null;

  let humidityData: any[] = [];
  let temperatureData: any[] = [];

  if (mainDevice) {
    humidityData = await fetchAnalyticsHistory(mainDevice.name, 'air_humidity', '-24h').catch(() => []);
    temperatureData = await fetchAnalyticsHistory(mainDevice.name, 'air_temperature', '-24h').catch(() => []);
  }

  const stats = [
    { name: 'Devices', value: devices.length, icon: Cpu, color: 'text-blue-400' },
    { name: 'Alertas', value: activeAlerts.length, icon: AlertTriangle, color: 'text-amber-400' },
    { name: 'Uptime', value: '99.9%', icon: CheckCircle2, color: 'text-emerald-400' },
    { name: 'Sensores', value: '4/4', icon: Droplets, color: 'text-cyan-400' },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 pb-20">
      {/* Premium Header */}
      <header className="py-12 flex flex-col md:flex-row md:items-end justify-between gap-6">
        <div>
          <h1 className="text-5xl font-black tracking-tighter text-gradient mb-4">
            Smart Vineyard
          </h1>
          <p className="text-slate-400 text-xl max-w-2xl font-medium">
            Sistema inteligente de monitorização e prevenção de pragas.
          </p>
        </div>
        <div className="flex items-center gap-3 bg-slate-900/50 p-2 rounded-2xl border border-slate-800">
           <div className="w-3 h-3 rounded-full bg-emerald-500 animate-pulse shadow-[0_0_10px_rgba(16,185,129,0.5)]" />
           <span className="text-sm font-bold text-slate-300 pr-4 uppercase tracking-widest">Live System</span>
        </div>
      </header>

      {/* Primary Action: Fungal Risk Card */}
      <div className="mb-12">
        <RiskCard level={currentRisk} />
      </div>

      {/* Grid Quick Stats */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-6 mb-12">
        {stats.map((stat) => (
          <div key={stat.name} className="glass-card p-6 rounded-3xl transition-all hover:scale-[1.02] cursor-default">
            <div className="flex items-center gap-4 mb-4">
              <div className={`p-3 rounded-2xl bg-slate-800/50 ${stat.color}`}>
                <stat.icon className="h-6 w-6" />
              </div>
              <div>
                <p className="text-xs font-black text-slate-500 uppercase tracking-widest leading-none mb-1">{stat.name}</p>
                <p className="text-2xl font-bold">{stat.value}</p>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* Analytics Visualization */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-12">
        {mainDevice ? (
          <>
            <SensorChart
              data={humidityData}
              title="Humidade do Ar"
              field="humidity"
              color="#3b82f6"
            />
            <SensorChart
              data={temperatureData}
              title="Temperatura Ambiente"
              field="temperature"
              color="#f59e0b"
            />
          </>
        ) : (
          <div className="lg:col-span-2 glass-card rounded-3xl p-20 text-center flex flex-col items-center justify-center">
            <Activity className="h-12 w-12 text-slate-700 mb-4 animate-bounce" />
            <p className="text-slate-500 text-lg font-medium">Aguardando telemetria de dispositivos...</p>
          </div>
        )}
      </div>

      {/* Footer Sections */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
        {/* Recent Alerts */}
        <section className="lg:col-span-3 glass-card rounded-3xl overflow-hidden">
          <div className="px-8 py-6 border-b border-white/5 flex justify-between items-center bg-white/5">
            <h2 className="font-bold text-xl tracking-tight flex items-center gap-3">
              <AlertTriangle className="h-5 w-5 text-amber-500" />
              Alertas Recentes
            </h2>
            <a href="/alerts" className="text-sm font-bold text-purple-400 hover:text-purple-300 flex items-center gap-1 transition-all">
              Histórico Completo <ChevronRight className="h-4 w-4" />
            </a>
          </div>
          <div className="divide-y divide-white/5">
            {activeAlerts.length === 0 ? (
              <div className="p-16 text-center text-slate-500 font-medium italic">Sem alertas críticos no momento.</div>
            ) : (
              activeAlerts.slice(0, 5).map((alert) => (
                <div key={alert.id} className="p-6 px-8 flex items-center justify-between hover:bg-white/5 transition-all group">
                  <div className="flex items-center space-x-5">
                    <div className={`p-3 rounded-xl ${
                        alert.severity === 'CRITICAL' ? 'bg-red-500/20 text-red-500' :
                        alert.severity === 'HIGH' ? 'bg-amber-500/20 text-amber-500' :
                        'bg-blue-500/20 text-blue-500'
                    }`}>
                      <AlertTriangle className="h-5 w-5" />
                    </div>
                    <div>
                      <h3 className="font-bold text-slate-100 group-hover:text-white transition-colors">{alert.message}</h3>
                      <p className="text-sm text-slate-500 font-medium">
                        {mainDevice?.name || 'Device'} • {new Date(alert.triggeredAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                  <ChevronRight className="h-5 w-5 text-slate-700 group-hover:text-slate-400 group-hover:translate-x-1 transition-all" />
                </div>
              ))
            )}
          </div>
        </section>

        {/* Grafana CTA */}
        <section className="lg:col-span-2 glass-card rounded-3xl p-10 flex flex-col items-center justify-center text-center relative group overflow-hidden">
          <div className="absolute inset-0 bg-gradient-to-br from-purple-600/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500" />
          <div className="relative z-10">
            <div className="w-20 h-20 bg-slate-800 rounded-3xl flex items-center justify-center mb-6 mx-auto group-hover:scale-110 group-hover:rotate-3 transition-all duration-500">
              <Activity className="h-10 w-10 text-purple-500" />
            </div>
            <h2 className="font-bold text-2xl mb-4 text-gradient">Análise Profunda</h2>
            <p className="text-slate-400 mb-8 max-w-xs font-medium mx-auto italic">
              "Visualize mapas de calor e persistência histórica do solo no painel avançado."
            </p>
            <a
              href="http://localhost:3000/d/vineyard_main"
              target="_blank"
              className="inline-flex items-center gap-2 px-10 py-4 bg-purple-600 hover:bg-purple-500 text-white rounded-2xl transition-all font-black shadow-lg shadow-purple-600/20 active:scale-95 group"
            >
              Abrir Grafana <ExternalLink className="h-4 w-4 group-hover:translate-x-1 group-hover:-translate-y-1 transition-transform" />
            </a>
          </div>
        </section>
      </div>
    </div>
  );
}
