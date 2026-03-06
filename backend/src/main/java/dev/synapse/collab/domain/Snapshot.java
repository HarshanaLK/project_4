package dev.synapse.collab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snapshots")
public class Snapshot {
    @Id
    private UUID id;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(nullable = false, length = 140)
    private String label;

    @Lob
    @Column(name = "code_content", nullable = false, columnDefinition = "LONGTEXT")
    private String codeContent;

    @Lob
    @Column(name = "whiteboard_json", nullable = false, columnDefinition = "LONGTEXT")
    private String whiteboardJson;

    @Column(name = "document_version", nullable = false)
    private long documentVersion;

    @Column(name = "created_by", nullable = false)
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Snapshot() {}

    public Snapshot(UUID roomId, String label, RoomDocument document, UUID createdBy) {
        this.id = UUID.randomUUID();
        this.roomId = roomId;
        this.label = label;
        this.codeContent = document.getCodeContent();
        this.whiteboardJson = document.getWhiteboardJson();
        this.documentVersion = document.getVersion();
        this.createdBy = createdBy;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getRoomId() { return roomId; }
    public String getLabel() { return label; }
    public String getCodeContent() { return codeContent; }
    public String getWhiteboardJson() { return whiteboardJson; }
    public long getDocumentVersion() { return documentVersion; }
    public UUID getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
