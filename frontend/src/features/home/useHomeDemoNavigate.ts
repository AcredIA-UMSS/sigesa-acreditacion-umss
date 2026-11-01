import { useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { getPostLoginPath } from '../../lib/auth/getPostLoginPath';
import { useAuth } from '../../lib/auth/useAuth';

/**
 * No hay login simulado ni fakeRole en el frontend: la sesión real vive en AuthProvider.
 * Si ya hay sesión activa (p. ej. DEV con credenciales demo), entra al panel; si no, /login.
 */
export function useHomeDemoNavigate(): () => void {
  const navigate = useNavigate();
  const { isAuthenticated, session } = useAuth();

  return useCallback(() => {
    if (isAuthenticated && session) {
      navigate(getPostLoginPath(session.role));
      return;
    }
    navigate('/login');
  }, [isAuthenticated, navigate, session]);
}
