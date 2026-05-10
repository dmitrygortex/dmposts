import { createContext, ReactNode, useContext, useEffect, useMemo, useState } from 'react';
import { authApi } from '../../services/api';
import { User } from '../../shared/types/domain';

interface AuthContextValue {
  user: User | null;
  loading: boolean;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  register: (email: string, password: string, fullName: string) => Promise<void>;
  logout: () => void;
  reloadUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  const reloadUser = async () => {
    const token = localStorage.getItem('accessToken');
    if (!token) {
      setUser(null);
      setLoading(false);
      return;
    }
    try {
      setUser(await authApi.me());
    } catch {
      localStorage.removeItem('accessToken');
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    void reloadUser();
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    loading,
    isAuthenticated: Boolean(user),
    login: async (email, password) => {
      const response = await authApi.login({ email, password });
      localStorage.setItem('accessToken', response.accessToken);
      setUser(response.user);
    },
    register: async (email, password, fullName) => {
      const response = await authApi.register({ email, password, fullName });
      localStorage.setItem('accessToken', response.accessToken);
      setUser(response.user);
    },
    logout: () => {
      localStorage.removeItem('accessToken');
      setUser(null);
    },
    reloadUser
  }), [user, loading]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used within AuthProvider');
  }
  return context;
}
