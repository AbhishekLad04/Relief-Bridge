import { createContext, useState, useContext, useEffect } from 'react';

const AuthContext = createContext();
const STORAGE_KEY = 'reliefbridge_user';

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    try {
      return JSON.parse(localStorage.getItem(STORAGE_KEY));
    } catch {
      return null;
    }
  });

  const loginUser = (userData, token) => {
    setUser(userData);
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(userData));
      if (token) {
        localStorage.setItem('jwtToken', token);
      }
    } catch {
      /* storage unavailable, keep going */
    }
  };

  const logout = () => {
    setUser(null);
    try {
      localStorage.removeItem(STORAGE_KEY);
      localStorage.removeItem('jwtToken');
    } catch {
      /* ignore */
    }
  };

  return (
    <AuthContext.Provider value={{ user, login: loginUser, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  return useContext(AuthContext);
}
