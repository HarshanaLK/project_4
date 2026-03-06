package dev.synapse.collab.service;

import dev.synapse.collab.api.*;
import dev.synapse.collab.domain.*;
import dev.synapse.collab.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class RoomService {
    private final RoomRepository rooms;
    private final RoomDocumentRepository documents;
    private final SnapshotRepository snapshots;
    private final AccessService access;
    private final AuditService audit;

    public RoomService(RoomRepository rooms, RoomDocumentRepository documents, SnapshotRepository snapshots,
                       AccessService access, AuditService audit) {
        this.rooms = rooms;
        this.documents = documents;
        this.snapshots = snapshots;
        this.access = access;
        this.audit = audit;
    }

    public List<ApiDtos.RoomView> list(UUID workspaceId, UUID userId) {
        WorkspaceMember member = access.requireMember(workspaceId, userId);
        return rooms.findAllByWorkspaceIdOrderByCreatedAtAsc(workspaceId).stream()
                .map(room -> view(room, member.getRole())).toList();
    }

    @Transactional
    public ApiDtos.RoomView create(UUID workspaceId, UUID userId, ApiDtos.CreateRoomRequest request) {
        WorkspaceMember member = access.requireAdmin(workspaceId, userId);
        Room room = rooms.save(new Room(workspaceId, request.name().trim(), request.language(), userId));
        documents.save(new RoomDocument(room.getId(), starter(request.language())));
        audit.record(workspaceId, userId, "ROOM_CREATED", "ROOM", room.getId(),
                Map.of("name", room.getName(), "language", room.getLanguage()));
        return view(room, member.getRole());
    }

    public ApiDtos.RoomView get(UUID roomId, UUID userId) {
        Room room = access.requireRoomAccess(roomId, userId, false);
        WorkspaceMember member = access.requireMember(room.getWorkspaceId(), userId);
        return view(room, member.getRole());
    }

    public ApiDtos.DocumentView document(UUID roomId, UUID userId) {
        access.requireRoomAccess(roomId, userId, false);
        return documentView(findDocument(roomId));
    }

    @Transactional
    public ApiDtos.DocumentView save(UUID roomId, UUID userId, ApiDtos.SaveDocumentRequest request) {
        access.requireRoomAccess(roomId, userId, true);
        RoomDocument document = findDocument(roomId);

        if (document.getVersion() != request.version()) {
            throw new ApiException(HttpStatus.CONFLICT, "VERSION_CONFLICT",
                    "Document version is stale. Load the latest copy before saving.");
        }

        document.update(request.codeContent(), request.whiteboardJson(), userId);
        return documentView(documents.saveAndFlush(document));
    }

    @Transactional
    public ApiDtos.SnapshotView createSnapshot(UUID roomId, UUID userId, ApiDtos.CreateSnapshotRequest request) {
        Room room = access.requireRoomAccess(roomId, userId, true);
        RoomDocument document = findDocument(roomId);
        Snapshot snapshot = snapshots.save(new Snapshot(roomId, request.label().trim(), document, userId));
        audit.record(room.getWorkspaceId(), userId, "SNAPSHOT_CREATED", "SNAPSHOT", snapshot.getId(),
                Map.of("roomId", roomId.toString(), "label", snapshot.getLabel()));
        return snapshotView(snapshot);
    }

    public List<ApiDtos.SnapshotView> snapshots(UUID roomId, UUID userId) {
        access.requireRoomAccess(roomId, userId, false);
        return snapshots.findAllByRoomIdOrderByCreatedAtDesc(roomId).stream()
                .map(this::snapshotView).toList();
    }

    @Transactional
    public ApiDtos.DocumentView restore(UUID roomId, UUID snapshotId, UUID userId) {
        Room room = access.requireRoomAccess(roomId, userId, true);
        Snapshot snapshot = snapshots.findById(snapshotId)
                .filter(s -> s.getRoomId().equals(roomId))
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "SNAPSHOT_NOT_FOUND", "Snapshot not found."));

        RoomDocument document = findDocument(roomId);
        document.update(snapshot.getCodeContent(), snapshot.getWhiteboardJson(), userId);
        RoomDocument saved = documents.saveAndFlush(document);

        audit.record(room.getWorkspaceId(), userId, "SNAPSHOT_RESTORED", "SNAPSHOT", snapshotId,
                Map.of("roomId", roomId.toString()));

        return documentView(saved);
    }

    private RoomDocument findDocument(UUID roomId) {
        return documents.findByRoomId(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "DOCUMENT_NOT_FOUND", "Room document not found."));
    }

    private ApiDtos.RoomView view(Room room, WorkspaceRole role) {
        return new ApiDtos.RoomView(room.getId(), room.getWorkspaceId(), room.getName(), room.getLanguage(), role, room.getCreatedAt());
    }

    private ApiDtos.DocumentView documentView(RoomDocument d) {
        return new ApiDtos.DocumentView(d.getRoomId(), d.getCodeContent(), d.getWhiteboardJson(), d.getVersion(), d.getUpdatedAt());
    }

    private ApiDtos.SnapshotView snapshotView(Snapshot s) {
        return new ApiDtos.SnapshotView(s.getId(), s.getLabel(), s.getDocumentVersion(), s.getCreatedBy(), s.getCreatedAt());
    }

    private String starter(String language) {
        return switch (language) {
            case "java" -> "public class Main {\n    public static void main(String[] args) {\n        System.out.println(\"Hello, Synapse\");\n    }\n}\n";
            case "python" -> "def main():\n    print(\"Hello, Synapse\")\n\nif __name__ == \"__main__\":\n    main()\n";
            case "typescript" -> "type User = { id: string; name: string };\n\nconst hello = (user: User) => `Hello ${user.name}`;\n";
            case "json" -> "{\n  \"project\": \"Synapse Collab\"\n}\n";
            default -> "// Start collaborating here.\n";
        };
    }
}
