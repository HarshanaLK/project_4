"use client";

import { use, useCallback, useEffect, useRef, useState } from "react";
import Link from "next/link";
import { Save, GitCommitHorizontal, Wifi, History } from "lucide-react";
import { api, ApiError } from "@/lib/api";
import type { CollabEvent, DrawElement, Room, RoomDocument, Snapshot } from "@/lib/types";
import { CodeEditor } from "@/components/CodeEditor";
import { Whiteboard } from "@/components/Whiteboard";
import { RoomSidebar } from "@/components/RoomSidebar";
import { useAuth } from "@/components/AuthProvider";
import { useCollaboration } from "@/hooks/useCollaboration";

type Message = { id: string; userId: string; name: string; body: string };

export default function RoomPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params);
  const { user } = useAuth();
  const [room, setRoom] = useState<Room | null>(null);
  const [document, setDocument] = useState<RoomDocument | null>(null);
  const [code, setCode] = useState("");
  const [board, setBoard] = useState<DrawElement[]>([]);
  const [tab, setTab] = useState<"code" | "whiteboard">("code");
  const [users, setUsers] = useState<Record<string, string>>({});
  const [messages, setMessages] = useState<Message[]>([]);
  const [cursors, setCursors] = useState<Record<string, { line: number; column: number }>>({});
  const [snapshots, setSnapshots] = useState<Snapshot[]>([]);
  const [historyOpen, setHistoryOpen] = useState(false);
  const [saveState, setSaveState] = useState("Saved");
  const applyingRemote = useRef(false);
  const persistenceTimer = useRef<ReturnType<typeof setTimeout> | null>(null);

  useEffect(() => {
    Promise.all([
      api<Room>(`/api/rooms/${id}`),
      api<RoomDocument>(`/api/rooms/${id}/document`)
    ]).then(([r, d]) => {
      setRoom(r);
      setDocument(d);
      setCode(d.codeContent);
      try { setBoard(JSON.parse(d.whiteboardJson)); } catch { setBoard([]); }
    });
  }, [id]);

  const onEvent = useCallback((event: CollabEvent) => {
    if (event.userId === user?.id) return;

    if (event.type === "CODE_CHANGE" && typeof event.payload.code === "string") {
      applyingRemote.current = true;
      setCode(event.payload.code);
      queueMicrotask(() => { applyingRemote.current = false; });
    }

    if (event.type === "WHITEBOARD" && Array.isArray(event.payload.elements)) {
      setBoard(event.payload.elements as DrawElement[]);
    }

    if (event.type === "PRESENCE" && event.payload.users && typeof event.payload.users === "object") {
      setUsers(event.payload.users as Record<string, string>);
    }

    if (event.type === "CURSOR" && typeof event.payload.line === "number" && typeof event.payload.column === "number") {
      setCursors((current) => ({ ...current, [event.userId]: { line: event.payload.line as number, column: event.payload.column as number } }));
    }

    if (event.type === "CHAT" && typeof event.payload.body === "string") {
      setMessages((current) => [...current, {
        id: String(event.payload.id ?? event.eventId),
        userId: event.userId,
        name: event.displayName,
        body: String(event.payload.body)
      }]);
    }
  }, [user?.id]);

  const { connected, send } = useCollaboration(id, onEvent);

  async function persist(nextCode = code, nextBoard = board) {
    if (!document) return;
    setSaveState("Saving…");
    try {
      const saved = await api<RoomDocument>(`/api/rooms/${id}/document`, {
        method: "PUT",
        body: JSON.stringify({
          codeContent: nextCode,
          whiteboardJson: JSON.stringify(nextBoard),
          version: document.version
        })
      });
      setDocument(saved);
      setSaveState("Saved");
    } catch (error) {
      if (error instanceof ApiError && error.status === 409) {
        const latest = await api<RoomDocument>(`/api/rooms/${id}/document`);
        setDocument(latest);
        setCode(latest.codeContent);
        try { setBoard(JSON.parse(latest.whiteboardJson)); } catch { setBoard([]); }
        setSaveState("Conflict resolved with latest server copy");
      } else {
        setSaveState("Save failed");
      }
    }
  }

  function schedulePersist(nextCode: string, nextBoard: DrawElement[]) {
    if (persistenceTimer.current) clearTimeout(persistenceTimer.current);
    persistenceTimer.current = setTimeout(() => persist(nextCode, nextBoard), 1200);
  }

  function changeCode(next: string) {
    setCode(next);
    if (!applyingRemote.current) send("code", { code: next });
    schedulePersist(next, board);
  }

  function changeBoard(next: DrawElement[]) {
    setBoard(next);
    send("whiteboard", { elements: next });
    schedulePersist(code, next);
  }

  async function loadHistory() {
    const items = await api<Snapshot[]>(`/api/rooms/${id}/snapshots`);
    setSnapshots(items);
    setHistoryOpen(true);
  }

  async function restore(snapshotId: string) {
    const restored = await api<RoomDocument>(`/api/rooms/${id}/snapshots/${snapshotId}/restore`, { method: "POST" });
    setDocument(restored);
    setCode(restored.codeContent);
    try { setBoard(JSON.parse(restored.whiteboardJson)); } catch { setBoard([]); }
    send("code", { code: restored.codeContent });
    send("whiteboard", { elements: JSON.parse(restored.whiteboardJson || "[]") });
    setHistoryOpen(false);
    setSaveState("Snapshot restored");
  }

  async function snapshot() {
    if (room?.role === "VIEWER") return;
    const label = window.prompt("Snapshot label", `Checkpoint ${new Date().toLocaleTimeString()}`);
    if (!label) return;
    await persist();
    await api<Snapshot>(`/api/rooms/${id}/snapshots`, {
      method: "POST",
      body: JSON.stringify({ label })
    });
    setSaveState("Snapshot created");
  }

  function sendChat(body: string) {
    send("chat", { body });
    if (user) {
      setMessages((current) => [...current, {
        id: crypto.randomUUID(),
        userId: user.id,
        name: user.displayName,
        body
      }]);
    }
  }

  if (!room || !document) return null;
  const readOnly = room.role === "VIEWER";

  return (
    <main className="shell">
      <header className="topbar">
        <div>
          <Link className="brand" href={`/workspace/${room.workspaceId}`}><span className="brand-dot" />{room.name}</Link>
          <span className="muted" style={{ fontSize: 12, marginLeft: 22 }}>{room.language}</span>
        </div>
        <div className="toolbar">
          <span className="muted" style={{ fontSize: 12 }}><Wifi size={13} style={{ verticalAlign: -2 }} /> {saveState}</span>
          <button className="btn" onClick={loadHistory}><History size={15} /> History</button>
          {!readOnly && <button className="btn" onClick={() => persist()}><Save size={15} /> Save</button>}
          {!readOnly && <button className="btn primary" onClick={snapshot}><GitCommitHorizontal size={15} /> Snapshot</button>}
        </div>
      </header>

      <div className="editor-shell">
        <section className="main-workspace">
          <div className="tabbar">
            <button className={`tab ${tab === "code" ? "active" : ""}`} onClick={() => setTab("code")}>Code</button>
            <button className={`tab ${tab === "whiteboard" ? "active" : ""}`} onClick={() => setTab("whiteboard")}>Whiteboard</button>
          </div>

          <div className="editor-stage">
            {tab === "code" ? (
              <CodeEditor
                value={code}
                language={room.language}
                readOnly={readOnly}
                onChange={changeCode}
                onCursor={(line, column) => send("cursor", { line, column })}
              />
            ) : (
              <Whiteboard elements={board} readOnly={readOnly} onChange={changeBoard} />
            )}
          </div>
        </section>

        <RoomSidebar connected={connected} users={users} cursors={cursors} messages={messages} onSend={sendChat} />
      </div>

      {historyOpen && (
        <div style={{ position: "fixed", inset: 0, background: "rgba(0,0,0,.58)", zIndex: 50, display: "grid", placeItems: "center", padding: 20 }} onClick={() => setHistoryOpen(false)}>
          <div className="card" style={{ width: "min(620px, 100%)", maxHeight: "70vh", overflow: "auto" }} onClick={(e) => e.stopPropagation()}>
            <div style={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
              <div><p className="muted">Version checkpoints</p><h2 style={{ marginTop: 0 }}>Room history</h2></div>
              <button className="btn" onClick={() => setHistoryOpen(false)}>Close</button>
            </div>
            {snapshots.length === 0 && <p className="muted">No snapshots yet.</p>}
            {snapshots.map((item) => (
              <div key={item.id} style={{ display: "flex", justifyContent: "space-between", alignItems: "center", gap: 14, padding: "12px 0", borderTop: "1px solid var(--line)" }}>
                <div><b>{item.label}</b><div className="muted" style={{ fontSize: 12 }}>Version {item.documentVersion} · {new Date(item.createdAt).toLocaleString()}</div></div>
                {!readOnly && <button className="btn" onClick={() => restore(item.id)}>Restore</button>}
              </div>
            ))}
          </div>
        </div>
      )}
    </main>
  );
}
