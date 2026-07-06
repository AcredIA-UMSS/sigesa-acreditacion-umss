import { createContext } from 'react';
import type { LoginResponse } from '../../api/model';
import type { AuthSession } from './types';

export interface AuthContextValue {
  session: AuthSession | null;
  isAuthenticated: boolean;
  login: (response: LoginResponse) => void;
  logout: () => void;
}

export const AuthContext = createContext<AuthContextValue | null>(null);
