import { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import {
  LayoutDashboard,
  Network,
  BarChart,
  History,
  HelpCircle,
  LogOut,
  ChevronLeft,
  ChevronDown,
  Menu,
  Users,
  List,
  Plus,
  Layers,
  FileUp,
} from 'lucide-react';
import { getRoleLabel } from '../../lib/auth/roleLabels';
import { useAuth } from '../../lib/auth/useAuth';

type SidebarNavKey =
  | 'dashboard'
  | 'processes'
  | 'evidence'
  | 'users'
  | 'templates'
  | 'reports'
  | 'history'
  | 'help';


interface SidebarProps {
  activeNav?: SidebarNavKey;
}

export const Sidebar = ({ activeNav = 'processes' }: SidebarProps) => {
  const [isExpanded, setIsExpanded] = useState(true);
  const location = useLocation();
  const { session, logout } = useAuth();
  const navigate = useNavigate();

  const isExternalEvaluator = session?.role === 'EE';
  const isProcessRoute = location.pathname.startsWith('/procesos');
  const [processesOpen, setProcessesOpen] = useState(isProcessRoute);

  useEffect(() => {
    if (isProcessRoute) {
      setProcessesOpen(true);
    }
  }, [isProcessRoute]);

  const initials = session?.role ?? 'U';
  const roleLabel = session ? getRoleLabel(session.role) : 'Usuario';
  const panelSubtitle = isExternalEvaluator ? 'REVISIÓN DOCUMENTAL' : 'PANEL ADMINISTRATIVO';
  const isJd = session?.role === 'JD';

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const toggleProcesses = () => {
    if (!isExpanded) {
      setIsExpanded(true);
      setProcessesOpen(true);
      return;
    }
    setProcessesOpen((prev) => !prev);
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
          <div>
            <button
              type="button"
              onClick={toggleProcesses}
              className={`flex w-full items-center rounded-lg p-3 transition-colors ${
                isExpanded ? 'justify-between' : 'justify-center'
              } ${
                activeNav === 'processes' || isProcessRoute
                  ? 'border-l-4 border-secondary bg-primary-800 text-body'
                  : 'text-primary-200 hover:bg-primary-800 hover:text-body'
              }`}
              title={!isExpanded ? 'Gestión procesos' : undefined}
              aria-expanded={processesOpen}
            >
              <div className="flex items-center gap-3">
                <div className="min-w-[20px]">
                  <Network size={20} />
                </div>
                {isExpanded && (
                  <span className="overflow-hidden whitespace-nowrap text-left text-label-md">
                    GESTIÓN PROCESOS
                  </span>
                )}
              </div>
              {isExpanded && (
                <ChevronDown
                  size={16}
                  className={`transition-transform ${processesOpen ? 'rotate-180' : ''}`}
                />
              )}
            </button>

            {isExpanded && processesOpen && (
              <ul className="mt-1 ml-5 space-y-1 border-l border-primary-700 pl-4">
                <SubNavItem
                  icon={<List size={16} />}
                  label="Ver procesos"
                  to="/procesos"
                  active={
                    location.pathname === '/procesos' ||
                    /^\/procesos\/[0-9a-f-]{36}(\/estructura)?$/i.test(location.pathname)
                  }
                />
                {isJd && (
                  <SubNavItem
                    icon={<Plus size={16} />}
                    label="Nuevo proceso"
                    to="/procesos/nuevo"
                    active={location.pathname === '/procesos/nuevo'}
                  />
                )}
              </ul>
            )}

            {!isExpanded && isProcessRoute && (
              <ul className="mt-1 space-y-1">
                <SubNavItem
                  icon={<List size={16} />}
                  label="Ver procesos"
                  to="/procesos"
                  active={location.pathname === '/procesos'}
                  compact
                />
                {isJd && (
                  <SubNavItem
                    icon={<Plus size={16} />}
                    label="Nuevo"
                    to="/procesos/nuevo"
                    active={location.pathname === '/procesos/nuevo'}
                    compact
                  />
                )}
              </ul>
            )}
          </div>
        )}

        {isJd && (
          <>
            <NavItem
              icon={<Layers size={20} />}
              label="PLANTILLAS"
              isExpanded={isExpanded}
              active={activeNav === 'templates'}
              to="/admin/plantillas"
            />
            <NavItem
              icon={<Users size={20} />}
              label="GESTIÓN USUARIOS"
              isExpanded={isExpanded}
              active={activeNav === 'users'}
              to="/admin/users"
            />
          </>
        )}

        {session?.role === 'CC' && (
          <NavItem
            icon={<FileUp size={20} />}
            label="CARGAR EVIDENCIA"
            isExpanded={isExpanded}
            active={activeNav === 'evidence'}
            to="/evidencias/cargar"
          />
        )}

        {!isExternalEvaluator && (
          <>
            {isJd ? (
              <NavItem
                icon={<BarChart size={20} />}
                label="REPORTES"
                isExpanded={isExpanded}
                active={activeNav === 'reports'}
                to="/reportes/ejecutivo"
              />
            ) : (
              <NavItem
                icon={<BarChart size={20} />}
                label="REPORTES"
                isExpanded={isExpanded}
                hasDropdown
                active={activeNav === 'reports'}
              />
            )}
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

const SubNavItem = ({
  icon,
  label,
  to,
  active,
  compact = false,
}: {
  icon: React.ReactNode;
  label: string;
  to: string;
  active?: boolean;
  compact?: boolean;
}) => (
  <li>
    <Link
      to={to}
      className={`flex items-center gap-2 rounded-lg px-3 py-2 text-label-md transition-colors ${
        compact ? 'justify-center' : ''
      } ${
        active
          ? 'bg-primary-700 font-medium text-body'
          : 'text-primary-300 hover:bg-primary-800 hover:text-body'
      }`}
      title={compact ? label : undefined}
    >
      {icon}
      {!compact && <span>{label}</span>}
    </Link>
  </li>
);

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
