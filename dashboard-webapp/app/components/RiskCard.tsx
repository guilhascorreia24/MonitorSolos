"use client";

import { Droplets, Info, AlertOctagon, AlertTriangle, ShieldCheck } from 'lucide-react';
import { clsx } from "clsx";

interface RiskCardProps {
  level: "LOW" | "MEDIUM" | "HIGH" | "CRITICAL";
}

export default function RiskCard({ level }: RiskCardProps) {
  const config = {
    LOW: {
      color: "text-emerald-500",
      bg: "bg-emerald-500/10",
      border: "border-emerald-500/20",
      icon: ShieldCheck,
      title: "Risco Baixo",
      desc: "Condições ideais. Nenhuma ameaça de fungos detectada.",
      glow: ""
    },
    MEDIUM: {
      color: "text-blue-400",
      bg: "bg-blue-400/10",
      border: "border-blue-400/20",
      icon: Info,
      title: "Risco Moderado",
      desc: "Humidade a subir. Recomenda-se monitorização atenta.",
      glow: ""
    },
    HIGH: {
      color: "text-amber-500",
      bg: "bg-amber-500/10",
      border: "border-amber-500/20",
      icon: AlertTriangle,
      title: "Risco Alto",
      desc: "Condições críticas atingidas. Risco elevado de infecção.",
      glow: ""
    },
    CRITICAL: {
      color: "text-red-500",
      bg: "bg-red-500/10",
      border: "border-red-500/20",
      icon: AlertOctagon,
      title: "Crítico / Emergência",
      desc: "Exposição prolongada a condições de fungos. Ação imediata necessária.",
      glow: "risk-glow-critical"
    }
  };

  const current = config[level];
  const Icon = current.icon;

  return (
    <div className={clsx(
      "glass-card p-8 rounded-3xl relative overflow-hidden transition-all duration-500",
      current.border,
      current.glow
    )}>
      <div className="flex flex-col md:flex-row items-center gap-8 relative z-10">
        <div className={clsx(
          "w-20 h-20 rounded-2xl flex items-center justify-center transition-colors duration-500",
          current.bg,
          current.color
        )}>
          <Icon className="w-10 h-10" />
        </div>
        
        <div className="flex-1 text-center md:text-left">
          <div className="flex items-center justify-center md:justify-start gap-3 mb-2">
            <h2 className="text-2xl font-bold tracking-tight">{current.title}</h2>
            <div className={clsx("px-3 py-1 rounded-full text-xs font-bold uppercase tracking-widest", current.bg, current.color)}>
              Nível {level}
            </div>
          </div>
          <p className="text-slate-400 text-lg leading-relaxed max-w-xl">
            {current.desc}
          </p>
        </div>

        <div className="flex gap-4">
           <div className="text-center">
              <div className="text-xs text-slate-500 font-bold uppercase mb-1">Impacto</div>
              <div className={clsx("text-xl font-mono font-bold", current.color)}>
                {level === "CRITICAL" ? "100%" : level === "HIGH" ? "75%" : level === "MEDIUM" ? "30%" : "0%"}
              </div>
           </div>
        </div>
      </div>

      {/* Decorative pulse */}
      {level !== "LOW" && (
        <div className={clsx(
          "absolute -right-20 -top-20 w-64 h-64 rounded-full blur-3xl opacity-20 animate-pulse",
          current.bg
        )} />
      )}
    </div>
  );
}
