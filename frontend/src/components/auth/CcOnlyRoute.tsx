import { Navigate } from 'react-router-dom';
import { getPostLoginPath } from '../../lib/auth/getPostLoginPath';
import { useAuth } from '../../lib/auth/useAuth';

interface CcOnlyRouteProps {
  children: React.ReactNode;
}

export function CcOnlyRoute({ children }: CcOnlyRouteProps) {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (session.role !== 'CC') {
    return <Navigate to={getPostLoginPath(session.role)} replace />;
  }

  return children;
}
