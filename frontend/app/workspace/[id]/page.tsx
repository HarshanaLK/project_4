"use client";

import { FormEvent, use, useEffect, useState } from "react";
import Link from "next/link";
import { AppHeader } from "@/components/AppHeader";
import { api } from "@/lib/api";
import type { Room, Workspace, Role } from "@/lib/types";

type Detail = Workspace & { members: { userId: string; email: string; displayName: string; role: Role }[] };

export default function WorkspacePage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const [workspace, setWorkspace] = useState<Detail | null>(null);
  const [rooms, setRooms] = useState<Room[]>([]);
  const [name, setName] = useState("");
  const [language, setLanguage] = useState("typescript");
  const [memberEmail, setMemberEmail] = useState("");
  const [memberRole, setMemberRole] = useState<Role>("EDITOR");

  async function refresh() {
    const [w, r] = await Promise.all([
      api<Detail>(`/api/workspaces/${id}`),
      api<Room[]>(`/api/workspaces/${id}/rooms`)
    ]);
    setWorkspace(w);
    setRooms(r);
  }

  useEffect(() => { refresh(); }, [id]);

  async function createRoom(event: FormEvent) {
    event.preventDefault();
    const room = await api<Room>(`/api/workspaces/${id}/rooms`, {
      method: "POST",
      body: JSON.stringify({ name, language })
    });
    setRooms((current) => [...current, room]);
    setName("");
  }

  async function addMember(event: FormEvent) {
    event.preventDefault();
    await api(`/api/workspaces/${id}/members`, {
      method: "POST",
      body: JSON.stringify({ email: memberEmail, role: memberRole })
    });
    setMemberEmail("");
    await refresh();
  }

  if (!workspace) return null;
  const canAdmin = workspace.role === "OWNER" || workspace.role === "ADMIN";

  return (
    <main className="shell">
      <AppHeader />
      <div className="container">
        <div className="page-head">
          <div>
            <Link className="muted" href="/dashboard">← Workspaces</Link>
            <h1 style={{ marginTop: 8 }}>{workspace.name}</h1>
          </div>
          <span className="role">{workspace.role}</span>
        </div>

        {canAdmin && (
          <form className="card" onSubmit={createRoom} style={{ display: "grid", gridTemplateColumns: "1fr 180px auto", gap: 10, marginBottom: 20 }}>
            <input required minLength={2} value={name} onChange={(e) => setName(e.target.value)} placeholder="Room name, e.g. Checkout refactor"
              style={{ border: "1px solid var(--line)", background: "#0c1017", color: "white", borderRadius: 10, padding: "0 12px" }} />
            <select value={language} onChange={(e) => setLanguage(e.target.value)}
              style={{ border: "1px solid var(--line)", background: "#0c1017", color: "white", borderRadius: 10, padding: "0 10px" }}>
              {["typescript","javascript","java","python","json","markdown","sql","html","css"].map((value) => <option key={value}>{value}</option>)}
            </select>
            <button className="btn primary">Create room</button>
          </form>
        )}

        <section className="workspace-grid">
          {rooms.map((room) => (
            <Link key={room.id} className="card workspace-card" href={`/room/${room.id}`}>
              <span className="badge">{room.language}</span>
              <h3 style={{ marginTop: 20 }}>{room.name}</h3>
              <p>Real-time code + whiteboard session</p>
            </Link>
          ))}
        </section>

        <section style={{ margin: "34px 0 70px" }}>
          <div className="page-head" style={{ paddingBottom: 14 }}>
            <div><p className="muted">Access control</p><h1 style={{ fontSize: 26 }}>Workspace members</h1></div>
          </div>
          {canAdmin && (
            <form className="card" onSubmit={addMember} style={{ display: "grid", gridTemplateColumns: "1fr 170px auto", gap: 10, marginBottom: 14 }}>
              <input type="email" required value={memberEmail} onChange={(e) => setMemberEmail(e.target.value)} placeholder="Registered user email"
                style={{ border: "1px solid var(--line)", background: "#0c1017", color: "white", borderRadius: 10, padding: "0 12px" }} />
              <select value={memberRole} onChange={(e) => setMemberRole(e.target.value as Role)}
                style={{ border: "1px solid var(--line)", background: "#0c1017", color: "white", borderRadius: 10, padding: "0 10px" }}>
                {(["ADMIN", "EDITOR", "VIEWER"] as Role[]).map((role) => <option key={role}>{role}</option>)}
              </select>
              <button className="btn">Add / update</button>
            </form>
          )}
          <div className="card">
            {workspace.members.map((member) => (
              <div key={member.userId} style={{ display: "flex", justifyContent: "space-between", gap: 12, padding: "10px 0", borderBottom: "1px solid var(--line)" }}>
                <div><b>{member.displayName}</b><div className="muted" style={{ fontSize: 13 }}>{member.email}</div></div>
                <span className="role">{member.role}</span>
              </div>
            ))}
          </div>
        </section>
      </div>
    </main>
  );
}
