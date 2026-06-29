import { useState } from 'react';
import { 
  LayoutDashboard, 
  Network, 
  BarChart, 
  History, 
  HelpCircle, 
  LogOut,
  ChevronLeft,
  Menu
} from 'lucide-react';

export const Sidebar = () => {
  const [isExpanded, setIsExpanded] = useState(true);

  return (
    <aside 
      className={`flex flex-col h-screen bg-primary-900 text-body border-r border-primary-800 transition-all duration-300 ease-in-out relative ${
        isExpanded ? 'w-72' : 'w-20'
      }`}
    >
      {/* Botón de Colapsar/Expandir */}
      <button 
        onClick={() => setIsExpanded(!isExpanded)}
        className="absolute -right-3 top-6 bg-secondary text-body rounded-full p-1 border-2 border-primary-900 z-10 hover:bg-secondary-600 transition-colors"
      >
        {isExpanded ? <ChevronLeft size={16} /> : <Menu size={16} />}
      </button>

      {/* Logo Header */}
      <div className={`flex items-center gap-3 p-6 border-b border-primary-800 h-[88px] ${!isExpanded && 'justify-center'}`}>
        <div className="w-10 h-10 min-w-[40px] bg-body rounded-full flex items-center justify-center">
          <img src="/umss-logo.svg" alt="UMSS" className="w-8 h-8" />
        </div>
        {isExpanded && (
          <div className="overflow-hidden whitespace-nowrap">
            <h1 className="text-heading-sm font-bold leading-tight">UMSS DUEA</h1>
            <p className="text-label-md text-primary-200">PANEL ADMINISTRATIVO</p>
          </div>
        )}
      </div>

      {/* Navegación */}
      <nav className="flex-1 py-6 px-4 space-y-2 overflow-y-auto overflow-x-hidden">
        <NavItem icon={<LayoutDashboard size={20} />} label="PANEL DE CONTROL" isExpanded={isExpanded} hasDropdown />
        
        {/* Item Activo */}
        <div className="bg-primary-800 rounded-lg border-l-4 border-secondary relative">
          <NavItem icon={<Network size={20} />} label="GESTIÓN PROCESOS" isExpanded={isExpanded} hasDropdown active />
        </div>
        
        <NavItem icon={<BarChart size={20} />} label="REPORTES" isExpanded={isExpanded} hasDropdown />
        <NavItem icon={<History size={20} />} label="HISTORIAL" isExpanded={isExpanded} />
        <NavItem icon={<HelpCircle size={20} />} label="AYUDA" isExpanded={isExpanded} />
      </nav>

      {/* Perfil de Usuario */}
      <div className={`p-4 border-t border-primary-800 bg-primary-900 flex items-center h-[88px] ${isExpanded ? 'justify-between' : 'justify-center'}`}>
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="w-10 h-10 min-w-[40px] bg-warning text-primary-900 flex items-center justify-center rounded-full text-heading-sm">
              MA
            </div>
            <div className="absolute bottom-0 right-0 w-3 h-3 bg-secondary rounded-full border-2 border-primary-900"></div>
          </div>
          {isExpanded && (
            <div className="overflow-hidden whitespace-nowrap">
              <p className="text-body-md font-bold">María Antezana</p>
              <p className="text-label-md text-primary-300">TECNICO DUEA</p>
            </div>
          )}
        </div>
        {isExpanded && (
          <button className="text-primary-300 hover:text-body transition-colors">
            <LogOut size={20} />
          </button>
        )}
      </div>
    </aside>
  );
};

const NavItem = ({ 
  icon, 
  label, 
  hasDropdown, 
  active, 
  isExpanded 
}: { 
  icon: React.ReactNode, 
  label: string, 
  hasDropdown?: boolean, 
  active?: boolean,
  isExpanded: boolean 
}) => (
  <button 
    className={`w-full flex items-center p-3 rounded-lg transition-colors ${
      active ? 'text-body' : 'text-primary-200 hover:bg-primary-800 hover:text-body'
    } ${isExpanded ? 'justify-between' : 'justify-center'}`}
    title={!isExpanded ? label : undefined}
  >
    <div className="flex items-center gap-3">
      <div className="min-w-[20px]">{icon}</div>
      {isExpanded && <span className="text-label-md whitespace-nowrap overflow-hidden text-left">{label}</span>}
    </div>
    {(hasDropdown && isExpanded) && <span className="text-label-md min-w-[12px]">▼</span>}
  </button>
);