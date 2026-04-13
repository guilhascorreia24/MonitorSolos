'use client';

import Link from 'next/link';
import { usePathname } from 'next/navigation';
import { 
  LayoutDashboard, 
  Cpu, 
  AlertTriangle, 
  Settings, 
  Grape 
} from 'lucide-react';
import { clsx, type ClassValue } from 'clsx';
import { twMerge } from 'tailwind-merge';

function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

const navItems = [
  { name: 'Dashboard', href: '/', icon: LayoutDashboard },
  { name: 'Dispositivos', href: '/devices', icon: Cpu },
  { name: 'Alertas', href: '/alerts', icon: AlertTriangle },
];

export default function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="flex h-screen w-64 flex-col bg-slate-900 text-white border-r border-slate-800">
      <div className="flex h-16 items-center px-6 border-b border-slate-800">
        <Grape className="h-8 w-8 text-purple-500 mr-2" />
        <span className="text-xl font-bold tracking-tight">Smart Vineyard</span>
      </div>
      
      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "group flex items-center px-3 py-2 text-sm font-medium rounded-lg transition-colors",
                isActive 
                  ? "bg-purple-600/10 text-purple-400" 
                  : "text-slate-400 hover:bg-slate-800 hover:text-white"
              )}
            >
              <item.icon className={cn(
                "mr-3 h-5 w-5",
                isActive ? "text-purple-400" : "text-slate-500 group-hover:text-white"
              )} />
              {item.name}
            </Link>
          );
        })}
      </nav>

      <div className="p-4 border-t border-slate-800">
        <div className="flex items-center text-sm text-slate-400 hover:text-white cursor-pointer px-3 py-2 rounded-lg hover:bg-slate-800 transition-colors">
          <Settings className="mr-3 h-5 w-5" />
          Configurações
        </div>
      </div>
    </div>
  );
}
