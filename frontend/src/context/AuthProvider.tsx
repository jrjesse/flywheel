"use client";

import { createContext, useCallback, useContext, useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { apiFetch } from "@/lib/api";
import {
  clearAuth,
  getStoredUser,
  getToken,
  parseJwtPayload,
  setAuth,
} from "@/lib/auth-storage";
import type { AuthResponse, AuthUser, UserRole } from "@/lib/types";

interface AuthContextValue {
  user: AuthUser | null;
  loading: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => void;
  refreshUser: () => void;
}

const AuthContext = createContext<AuthContextValue | null>(null);

function userFromAuthResponse(res: AuthResponse): AuthUser {
  return {
    userId: res.userId,
    tenantId: res.tenantId,
    email: res.email,
    displayName: res.displayName,
    roles: [res.role],
  };
}

function userFromToken(token: string): AuthUser | null {
  const payload = parseJwtPayload(token);
  if (!payload) return null;
  const roles = (payload.roles as string[] | undefined) ?? [];
  return {
    userId: String(payload.sub ?? payload.userId ?? ""),
    tenantId: String(payload.tenantId ?? ""),
    email: String(payload.email ?? ""),
    displayName: String(payload.displayName ?? payload.name ?? "Usuário"),
    roles: roles as UserRole[],
  };
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const router = useRouter();
  const [user, setUser] = useState<AuthUser | null>(null);
  const [loading, setLoading] = useState(true);

  const refreshUser = useCallback(() => {
    const stored = getStoredUser();
    const token = getToken();
    if (stored) {
      setUser(stored);
    } else if (token) {
      setUser(userFromToken(token));
    } else {
      setUser(null);
    }
  }, []);

  useEffect(() => {
    refreshUser();
    setLoading(false);
  }, [refreshUser]);

  const login = async (email: string, password: string) => {
    const res = await apiFetch<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password }),
    });
    const authUser = userFromAuthResponse(res);
    setAuth(res.token, authUser);
    setUser(authUser);
    router.push("/");
  };

  const logout = () => {
    clearAuth();
    setUser(null);
    router.push("/login");
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, refreshUser }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
