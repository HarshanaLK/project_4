"use client";

import { FormEvent, useState } from "react";

type Message = { id: string; userId: string; name: string; body: string };

export function RoomSidebar({
  connected,
  users,
  messages,
  cursors,
  onSend
}: {
  connected: boolean;
  users: Record<string, string>;
  messages: Message[];
  cursors: Record<string, { line: number; column: number }>;
  onSend: (body: string) => void;
}) {
  const [body, setBody] = useState("");

  function submit(event: FormEvent) {
    event.preventDefault();
    if (!body.trim()) return;
    onSend(body.trim());
    setBody("");
  }

  return (
    <aside className="side-panel">
      <section className="side-section">
        <h3>Connection</h3>
        <span className="badge">{connected ? "● Live" : "○ Reconnecting"}</span>
      </section>

      <section className="side-section">
        <h3>In this room</h3>
        <div className="presence">
          {Object.entries(users).map(([id, name]) => (
            <span className="person" key={id}>
              {name}{cursors[id] ? ` · ${cursors[id].line}:${cursors[id].column}` : ""}
            </span>
          ))}
        </div>
      </section>

      <section className="chat">
        <div className="chat-log">
          {messages.map((message) => (
            <div className="chat-msg" key={message.id}>
              <b>{message.name}</b>
              <span>{message.body}</span>
            </div>
          ))}
        </div>
        <form className="chat-form" onSubmit={submit}>
          <input value={body} onChange={(e) => setBody(e.target.value)} placeholder="Message the room" />
          <button className="btn">Send</button>
        </form>
      </section>
    </aside>
  );
}
