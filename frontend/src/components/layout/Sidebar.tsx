import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Network,
  BarChart,
  History,
  HelpCircle,
  LogOut,
  ChevronLeft,
  Menu,
  Users,
} from 'lucide-react';
import { getRoleLabel } from '../../lib/auth/roleLabels';
import { useAuth } from '../../lib/auth/useAuth';

type SidebarNavKey = 'dashboard' | 'processes' | 'users' | 'reports' | 'history' | 'help';

interface SidebarProps {
  activeNav?: SidebarNavKey;
}

export const Sidebar = ({ activeNav = 'processes' }: SidebarProps) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const { session, logout } = useAuth();
  const navigate = useNavigate();

  const isExternalEvaluator = session?.role === 'EE';
  const initials = session?.role ?? 'U';
  const roleLabel = session ? getRoleLabel(session.role) : 'Usuario';
  const panelSubtitle = isExternalEvaluator ? 'REVISIÓN DOCUMENTAL' : 'PANEL ADMINISTRATIVO';

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  return (
    <aside
      className={`relative flex h-screen flex-col border-r border-primary-800 bg-primary-900 text-body transition-all duration-300 ease-in-out ${
        isExpanded ? 'w-72' : 'w-20'
      }`}
    >
      <button
        type="button"
        onClick={() => setIsExpanded(!isExpanded)}
        className="absolute -right-3 top-6 z-10 rounded-full border-2 border-primary-900 bg-secondary p-1 text-body transition-colors hover:bg-secondary-600"
      >
        {isExpanded ? <ChevronLeft size={16} /> : <Menu size={16} />}
      </button>

      <div
        className={`flex h-[88px] items-center gap-3 border-b border-primary-800 p-6 ${
          !isExpanded && 'justify-center'
        }`}
      >
        <div className="flex h-10 w-10 min-w-[40px] items-center justify-center rounded-full bg-body">
          <img src="/umss-logo.svg" alt="UMSS" className="h-8 w-8" />
        </div>
        {isExpanded && (
          <div className="overflow-hidden whitespace-nowrap">
            <h1 className="text-heading-sm font-bold leading-tight">UMSS DUEA</h1>
            <p className="text-label-md text-primary-200">{panelSubtitle}</p>
          </div>
        )}
      </div>

      <nav className="flex-1 space-y-2 overflow-x-hidden overflow-y-auto px-4 py-6">
        <NavItem
          icon={<LayoutDashboard size={20} />}
          label="PANEL DE CONTROL"
          isExpanded={isExpanded}
          hasDropdown
          active={activeNav === 'dashboard'}
          to="/dashboard"
        />

        {!isExternalEvaluator && (
          <NavItem
            icon={<Network size={20} />}
            label="GESTIÓN PROCESOS"
            isExpanded={isExpanded}
            hasDropdown
            active={activeNav === 'processes'}
            to="/procesos/nuevo"
          />
        )}

        {session?.role === 'JD' && (
          <NavItem
            icon={<Users size={20} />}
            label="GESTIÓN USUARIOS"
            isExpanded={isExpanded}
            active={activeNav === 'users'}
            to="/admin/users"
          />
        )}

        {!isExternalEvaluator && (
          <>
            <NavItem
              icon={<BarChart size={20} />}
              label="REPORTES"
              isExpanded={isExpanded}
              hasDropdown
              active={activeNav === 'reports'}
            />
            <NavItem
              icon={<History size={20} />}
              label="HISTORIAL"
              isExpanded={isExpanded}
              active={activeNav === 'history'}
            />
          </>
        )}

        <NavItem
          icon={<HelpCircle size={20} />}
          label="AYUDA"
          isExpanded={isExpanded}
          active={activeNav === 'help'}
          to="/ayuda"
        />
      </nav>

      <div
        className={`flex h-[88px] items-center border-t border-primary-800 bg-primary-900 p-4 ${
          isExpanded ? 'justify-between' : 'justify-center'
        }`}
      >
        <div className="flex items-center gap-3">
          <div className="relative">
            <div className="flex h-10 w-10 min-w-[40px] items-center justify-center rounded-full bg-warning text-primary-900 text-heading-sm">
              {initials}
            </div>
            <div className="absolute bottom-0 right-0 h-3 w-3 rounded-full border-2 border-primary-900 bg-secondary" />
          </div>
          {isExpanded && (
            <div className="overflow-hidden whitespace-nowrap">
              <p className="text-body-md font-bold">{roleLabel}</p>
              <p className="text-label-md text-primary-300">SIGESA</p>
            </div>
          )}
        </div>
        {isExpanded && (
          <button
            type="button"
            onClick={handleLogout}
            className="text-primary-300 transition-colors hover:text-body"
            aria-label="Cerrar sesión"
          >
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
  isExpanded,
  to,
}: {
  icon: React.ReactNode;
  label: string;
  hasDropdown?: boolean;
  active?: boolean;
  isExpanded: boolean;
  to?: string;
}) => {
  const content = (
    <>
      <div className="flex items-center gap-3">
        <div className="min-w-[20px]">{icon}</div>
        {isExpanded && (
          <span className="overflow-hidden whitespace-nowrap text-left text-label-md">{label}</span>
        )}
      </div>
      {hasDropdown && isExpanded && <span className="min-w-[12px] text-label-md">▼</span>}
    </>
  );

  const className = `flex w-full items-center rounded-lg p-3 transition-colors ${
    isExpanded ? 'justify-between' : 'justify-center'
  } ${active ? 'border-l-4 border-secondary bg-primary-800 text-body' : 'text-primary-200 hover:bg-primary-800 hover:text-body'}`;

  if (to) {
    return (
      <Link to={to} className={className} title={!isExpanded ? label : undefined}>
        {content}
      </Link>
    );
  }

  return (
    <button type="button" className={className} title={!isExpanded ? label : undefined}>
      {content}
    </button>
  );
};
