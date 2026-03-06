# API Surface

## Auth
- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

## Workspaces
- `GET /api/workspaces`
- `POST /api/workspaces`
- `GET /api/workspaces/{workspaceId}`
- `POST /api/workspaces/{workspaceId}/members`

## Rooms
- `GET /api/workspaces/{workspaceId}/rooms`
- `POST /api/workspaces/{workspaceId}/rooms`
- `GET /api/rooms/{roomId}`
- `GET /api/rooms/{roomId}/document`
- `PUT /api/rooms/{roomId}/document`

## Snapshots
- `GET /api/rooms/{roomId}/snapshots`
- `POST /api/rooms/{roomId}/snapshots`
- `POST /api/rooms/{roomId}/snapshots/{snapshotId}/restore`

## Audit
- `GET /api/workspaces/{workspaceId}/audit`

## WebSocket

Endpoint: `/ws`

Client sends:
- `/app/rooms/{roomId}/presence`
- `/app/rooms/{roomId}/code`
- `/app/rooms/{roomId}/cursor`
- `/app/rooms/{roomId}/whiteboard`
- `/app/rooms/{roomId}/chat`

Client subscribes:
- `/topic/rooms/{roomId}/events`
