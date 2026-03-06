"use client";

import { createContext, useContext, useEffect, useState } from "react";
import { api } from "@/lib/api";
import type { User } from "@/lib/types";

type AuthContextValue = {
  user: User | null;
  loading: boolean;
  setSession: (token: string, user: User) => void;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const [user, setUser] = useState<User | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    if (!localStorage.getItem("synapse_token")) {
      setLoading(false);
      return;
    }

    api<User>("/api/auth/me")
      .then(setUser)
      .catch(() => localStorage.removeItem("synapse_token"))
      .finally(() => setLoading(false));
  }, []);

  const setSession = (token: string, nextUser: User) => {
    localStorage.setItem("synapse_token", token);
    setUser(nextUser);
  };

  const logout = () => {
    localStorage.removeItem("synapse_token");
    setUser(null);
    window.location.href = "/";
  };

  return (
    <AuthContext.Provider value={{ user, loading, setSession, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used within AuthProvider");
  return value;
}
