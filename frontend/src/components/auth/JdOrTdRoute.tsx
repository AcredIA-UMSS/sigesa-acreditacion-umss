import { Navigate } from 'react-router-dom';
import { getPostLoginPath } from '../../lib/auth/getPostLoginPath';
import { useAuth } from '../../lib/auth/useAuth';

interface JdOrTdRouteProps {
  children: React.ReactNode;
}

export function JdOrTdRoute({ children }: JdOrTdRouteProps) {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (session.role !== 'JD' && session.role !== 'TD') {
    return <Navigate to={getPostLoginPath(session.role)} replace />;
  }

  return children;
}
