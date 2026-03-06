import Link from "next/link";
import { Code2, ShieldCheck, RadioTower, History, PencilRuler, Users } from "lucide-react";

const features = [
  [RadioTower, "Real-time rooms", "Low-latency STOMP/WebSocket events for code, cursors, presence, chat and drawing."],
  [Code2, "Monaco workspace", "A VS Code-class editor surface with persistent room documents and language modes."],
  [PencilRuler, "Shared whiteboard", "Collaborative SVG canvas for diagrams, architecture sketches and interview sessions."],
  [ShieldCheck, "Resource-level RBAC", "OWNER, ADMIN, EDITOR and VIEWER permissions enforced by the Spring API."],
  [History, "Snapshots", "Version checkpoints let teams preserve and restore meaningful collaboration states."],
  [Users, "Presence + chat", "See who is in the room and keep technical discussion beside the code."]
] as const;

export default function Home() {
  return (
    <main className="shell">
      <header className="topbar">
        <Link className="brand" href="/"><span className="brand-dot" />Synapse Collab</Link>
        <div className="actions">
          <Link className="btn ghost" href="/login">Sign in</Link>
          <Link className="btn primary" href="/register">Create account</Link>
        </div>
      </header>

      <section className="container hero">
        <span className="badge">Spring Boot · Next.js · MySQL · WebSockets</span>
        <h1>Code together. Sketch systems. Stay in sync.</h1>
        <p>
          A full-stack collaboration platform for pair programming, system-design sessions
          and technical interviews, built around durable workspaces and real-time rooms.
        </p>
        <div className="actions">
          <Link className="btn primary" href="/register">Launch workspace</Link>
          <a className="btn" href="#architecture">See capabilities</a>
        </div>
      </section>

      <section id="architecture" className="container feature-grid" style={{ paddingBottom: 90 }}>
        {features.map(([Icon, title, text]) => (
          <article className="card" key={title}>
            <Icon size={22} />
            <h3 style={{ marginTop: 18 }}>{title}</h3>
            <p>{text}</p>
          </article>
        ))}
      </section>
    </main>
  );
}
