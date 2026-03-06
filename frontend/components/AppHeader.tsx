"use client";

import Link from "next/link";
import { LogOut } from "lucide-react";
import { useAuth } from "./AuthProvider";

export function AppHeader() {
  const { user, logout } = useAuth();
  return (
    <header className="topbar">
      <Link className="brand" href="/dashboard"><span className="brand-dot" />Synapse Collab</Link>
      <div className="actions" style={{ alignItems: "center" }}>
        <span className="muted" style={{ fontSize: 13 }}>{user?.displayName}</span>
        <button className="btn ghost" onClick={logout} title="Sign out"><LogOut size={16} /></button>
      </div>
    </header>
  );
}
