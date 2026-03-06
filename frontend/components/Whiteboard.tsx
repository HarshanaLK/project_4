"use client";

import { PointerEvent, useMemo, useRef, useState } from "react";
import { Pencil, Square, Trash2 } from "lucide-react";
import type { DrawElement } from "@/lib/types";

type Props = {
  elements: DrawElement[];
  readOnly?: boolean;
  onChange: (elements: DrawElement[]) => void;
};

export function Whiteboard({ elements, readOnly, onChange }: Props) {
  const [tool, setTool] = useState<"path" | "rect">("path");
  const draftRef = useRef<DrawElement | null>(null);
  const [, redraw] = useState(0);

  const renderElements = useMemo(() => elements, [elements]);

  function point(event: PointerEvent<SVGSVGElement>) {
    const box = event.currentTarget.getBoundingClientRect();
    return { x: event.clientX - box.left, y: event.clientY - box.top };
  }

  function down(event: PointerEvent<SVGSVGElement>) {
    if (readOnly) return;
    event.currentTarget.setPointerCapture(event.pointerId);
    const p = point(event);
    draftRef.current = tool === "path"
      ? { id: crypto.randomUUID(), type: "path", points: [p] }
      : { id: crypto.randomUUID(), type: "rect", x: p.x, y: p.y, width: 0, height: 0 };
    redraw((x) => x + 1);
  }

  function move(event: PointerEvent<SVGSVGElement>) {
    const draft = draftRef.current;
    if (!draft) return;
    const p = point(event);

    if (draft.type === "path") {
      draft.points = [...(draft.points ?? []), p];
    } else {
      draft.width = p.x - (draft.x ?? 0);
      draft.height = p.y - (draft.y ?? 0);
    }
    redraw((x) => x + 1);
  }

  function up() {
    if (!draftRef.current) return;
    onChange([...elements, draftRef.current]);
    draftRef.current = null;
  }

  const all = draftRef.current ? [...renderElements, draftRef.current] : renderElements;

  return (
    <div className="whiteboard">
      <div className="board-tools">
        <button className={`btn ${tool === "path" ? "primary" : ""}`} onClick={() => setTool("path")}><Pencil size={15} /></button>
        <button className={`btn ${tool === "rect" ? "primary" : ""}`} onClick={() => setTool("rect")}><Square size={15} /></button>
        {!readOnly && <button className="btn danger" onClick={() => onChange([])}><Trash2 size={15} /></button>}
      </div>

      <svg onPointerDown={down} onPointerMove={move} onPointerUp={up}>
        {all.map((item) => item.type === "path" ? (
          <polyline
            key={item.id}
            points={(item.points ?? []).map((p) => `${p.x},${p.y}`).join(" ")}
            fill="none"
            stroke="#bda8ff"
            strokeWidth="3"
            strokeLinecap="round"
            strokeLinejoin="round"
          />
        ) : (
          <rect
            key={item.id}
            x={Math.min(item.x ?? 0, (item.x ?? 0) + (item.width ?? 0))}
            y={Math.min(item.y ?? 0, (item.y ?? 0) + (item.height ?? 0))}
            width={Math.abs(item.width ?? 0)}
            height={Math.abs(item.height ?? 0)}
            fill="rgba(34,211,238,.08)"
            stroke="#22d3ee"
            strokeWidth="2"
            rx="8"
          />
        ))}
      </svg>
    </div>
  );
}
