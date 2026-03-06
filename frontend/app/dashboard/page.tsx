"use client";

import { FormEvent, useEffect, useState } from "react";
import Link from "next/link";
import { Plus, Boxes } from "lucide-react";
import { AppHeader } from "@/components/AppHeader";
import { useAuth } from "@/components/AuthProvider";
import { api } from "@/lib/api";
import type { Workspace } from "@/lib/types";

export default function DashboardPage() {
  const { user, loading } = useAuth();
  const [workspaces, setWorkspaces] = useState<Workspace[]>([]);
  const [name, setName] = useState("");

  useEffect(() => {
    if (!loading && !user) window.location.href = "/login";
    if (user) api<Workspace[]>("/api/workspaces").then(setWorkspaces);
  }, [user, loading]);

  async function createWorkspace(event: FormEvent) {
    event.preventDefault();
    if (!name.trim()) return;
    const created = await api<Workspace>("/api/workspaces", {
      method: "POST",
      body: JSON.stringify({ name })
    });
    setWorkspaces((current) => [created, ...current]);
    setName("");
  }

  if (loading || !user) return null;

  return (
    <main className="shell">
      <AppHeader />
      <div className="container">
        <div className="page-head">
          <div>
            <p className="muted">Engineering workspaces</p>
            <h1>Your collaboration hub</h1>
          </div>

          <form onSubmit={createWorkspace} style={{ display: "flex", gap: 8 }}>
            <input
              aria-label="Workspace name"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="New workspace"
              style={{ border: "1px solid var(--line)", background: "var(--panel)", color: "white", borderRadius: 10, padding: "0 12px" }}
            />
            <button className="btn primary"><Plus size={16} /> Create</button>
          </form>
        </div>

        <section className="workspace-grid">
          {workspaces.map((workspace) => (
            <Link className="card workspace-card" key={workspace.id} href={`/workspace/${workspace.id}`}>
              <Boxes size={22} />
              <h3 style={{ marginTop: 28 }}>{workspace.name}</h3>
              <p>{workspace.slug}</p>
              <span className="role">{workspace.role}</span>
            </Link>
          ))}
        </section>
      </div>
    </main>
  );
}
