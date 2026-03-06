package dev.synapse.collab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "room_documents")
public class RoomDocument {
    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false, unique = true)
    private UUID roomId;

    @Lob
    @Column(name = "code_content", nullable = false, columnDefinition = "LONGTEXT")
    private String codeContent;

    @Lob
    @Column(name = "whiteboard_json", nullable = false, columnDefinition = "LONGTEXT")
    private String whiteboardJson;

    @Version
    @Column(nullable = false)
    private long version;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected RoomDocument() {}

    public RoomDocument(UUID roomId, String codeContent) {
        this.id = UUID.randomUUID();
        this.roomId = roomId;
        this.codeContent = codeContent;
        this.whiteboardJson = "[]";
        this.version = 0;
        this.updatedAt = Instant.now();
    }

    public void update(String codeContent, String whiteboardJson, UUID updatedBy) {
        this.codeContent = codeContent;
        this.whiteboardJson = whiteboardJson;
        this.updatedBy = updatedBy;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRoomId() { return roomId; }
    public String getCodeContent() { return codeContent; }
    public String getWhiteboardJson() { return whiteboardJson; }
    public long getVersion() { return version; }
    public UUID getUpdatedBy() { return updatedBy; }
    public Instant getUpdatedAt() { return updatedAt; }
}
