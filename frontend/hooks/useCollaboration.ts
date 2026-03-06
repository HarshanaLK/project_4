"use client";

import { Client, IMessage } from "@stomp/stompjs";
import { useCallback, useEffect, useRef, useState } from "react";
import type { CollabEvent } from "@/lib/types";

const WS_URL = process.env.NEXT_PUBLIC_WS_URL ?? "ws://localhost:8080/ws";

export function useCollaboration(roomId: string, onEvent: (event: CollabEvent) => void) {
  const clientRef = useRef<Client | null>(null);
  const onEventRef = useRef(onEvent);
  const [connected, setConnected] = useState(false);

  useEffect(() => { onEventRef.current = onEvent; }, [onEvent]);

  useEffect(() => {
    const token = localStorage.getItem("synapse_token");
    if (!token) return;

    const client = new Client({
      brokerURL: WS_URL,
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 1500,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    });

    client.onConnect = () => {
      setConnected(true);
      client.subscribe(`/topic/rooms/${roomId}/events`, (message: IMessage) => {
        onEventRef.current(JSON.parse(message.body) as CollabEvent);
      });
      client.publish({
        destination: `/app/rooms/${roomId}/presence`,
        body: JSON.stringify({ action: "join" })
      });
    };

    client.onWebSocketClose = () => setConnected(false);
    client.onStompError = () => setConnected(false);
    client.activate();
    clientRef.current = client;

    return () => {
      if (client.connected) {
        client.publish({
          destination: `/app/rooms/${roomId}/presence`,
          body: JSON.stringify({ action: "leave" })
        });
      }
      client.deactivate();
      clientRef.current = null;
    };
  }, [roomId]);

  const send = useCallback((channel: string, payload: Record<string, unknown>) => {
    if (!clientRef.current?.connected) return;
    clientRef.current.publish({
      destination: `/app/rooms/${roomId}/${channel}`,
      body: JSON.stringify(payload)
    });
  }, [roomId]);

  return { connected, send };
}
