import { Navigate } from 'react-router-dom';
import { getPostLoginPath, useAuth } from '../../lib/auth/AuthContext';

interface JdOnlyRouteProps {
  children: React.ReactNode;
}

export function JdOnlyRoute({ children }: JdOnlyRouteProps) {
  const { session } = useAuth();

  if (!session) {
    return <Navigate to="/login" replace />;
  }

  if (session.role !== 'JD') {
    return <Navigate to={getPostLoginPath(session.role)} replace />;
  }

  return children;
}
