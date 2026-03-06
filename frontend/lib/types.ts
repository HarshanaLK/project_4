export type User = { id: string; email: string; displayName: string };
export type Role = "OWNER" | "ADMIN" | "EDITOR" | "VIEWER";

export type Workspace = {
  id: string;
  name: string;
  slug: string;
  role: Role;
  createdAt: string;
};

export type Room = {
  id: string;
  workspaceId: string;
  name: string;
  language: string;
  role: Role;
  createdAt: string;
};

export type RoomDocument = {
  roomId: string;
  codeContent: string;
  whiteboardJson: string;
  version: number;
  updatedAt: string;
};

export type Snapshot = {
  id: string;
  label: string;
  documentVersion: number;
  createdBy: string;
  createdAt: string;
};

export type CollabEvent = {
  eventId: string;
  roomId: string;
  type: "PRESENCE" | "CODE_CHANGE" | "CURSOR" | "WHITEBOARD" | "CHAT";
  userId: string;
  displayName: string;
  payload: Record<string, unknown>;
  timestamp: string;
};

export type DrawElement = {
  id: string;
  type: "path" | "rect";
  points?: { x: number; y: number }[];
  x?: number;
  y?: number;
  width?: number;
  height?: number;
};
