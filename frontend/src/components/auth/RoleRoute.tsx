import { Navigate } from 'react-router-dom';
import { getPostLoginPath } from '../../lib/auth/getPostLoginPath';
import { useAuth } from '../../lib/auth/useAuth';
import type { BackendRoleCode } from '../../lib/auth/roleLabels';

interface RoleRouteProps {
  allowed: BackendRoleCode[];
  children: React.ReactNode;
}

export function RoleRoute({ allowed, children }: RoleRouteProps) {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (!allowed.includes(session.role)) {
    return <Navigate to={getPostLoginPath(session.role)} replace />;
  }

  return children;
}
