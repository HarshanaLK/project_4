# Architecture Notes

## Boundary choices

The system separates durable business operations from ephemeral collaboration traffic.

**REST**
- authentication
- workspaces and memberships
- room creation
- room document load/save
- snapshots
- audit log

**WebSocket/STOMP**
- code changes
- remote cursor updates
- whiteboard operations
- presence
- room chat

This gives the codebase a clear consistency model: WebSocket messages optimize collaboration latency; REST owns durable state.

## Consistency model

The durable document has a monotonically increasing `version`.

`PUT /api/rooms/{roomId}/document` requires the client's `version`.
The service updates only when the supplied version equals the current version. A mismatch returns HTTP 409 with the server copy. This prevents blind overwrites from stale tabs.

Live WebSocket code events are intentionally not treated as the source of truth. The browser broadcasts changes immediately and persists a debounced version through REST.

## Horizontal scaling path

The provided implementation is intentionally runnable without infrastructure beyond MySQL.

For multi-node production:
1. Move `PresenceRegistry` to Redis.
2. Use Redis Pub/Sub or a broker relay for STOMP.
3. Make collaboration events idempotent with event IDs.
4. Use CRDT updates instead of whole-text messages for code.
5. Add sticky sessions only if a chosen broker topology requires them.

## Authorization

Global authentication is JWT based.
Authorization is resource based:

```text
OWNER  -> full workspace administration
ADMIN  -> member + room administration
EDITOR -> edit/code/draw/chat
VIEWER -> read-only room access
```

Backend authorization is authoritative. UI role checks are convenience only.

## Data model

```text
users
  |
workspace_members >--- workspaces
                         |
                         +--- rooms --- room_documents
                         |       |
                         |       +--- snapshots
                         |       +--- chat_messages
                         |
                         +--- audit_logs
```

## Interview discussion

Good questions to be ready for:
- Why WebSockets instead of polling?
- Why persist via REST rather than every WebSocket keystroke?
- What happens with two users editing the same line?
- How would you migrate to CRDT?
- How does horizontal scaling change presence?
- Why is authorization checked on the server?
- What does optimistic versioning protect against?
