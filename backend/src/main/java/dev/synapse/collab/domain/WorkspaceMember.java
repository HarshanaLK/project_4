package dev.synapse.collab.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "workspace_members",
       uniqueConstraints = @UniqueConstraint(name = "uk_workspace_member", columnNames = {"workspace_id", "user_id"}))
public class WorkspaceMember {
    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkspaceRole role;

    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;

    protected WorkspaceMember() {}

    public WorkspaceMember(UUID workspaceId, UUID userId, WorkspaceRole role) {
        this.id = UUID.randomUUID();
        this.workspaceId = workspaceId;
        this.userId = userId;
        this.role = role;
        this.joinedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public UUID getWorkspaceId() { return workspaceId; }
    public UUID getUserId() { return userId; }
    public WorkspaceRole getRole() { return role; }
    public Instant getJoinedAt() { return joinedAt; }
    public void setRole(WorkspaceRole role) { this.role = role; }
}
