package dev.synapse.collab.api;

import dev.synapse.collab.domain.WorkspaceRole;
import jakarta.validation.constraints.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public final class ApiDtos {
    private ApiDtos() {}

    public record RegisterRequest(
            @Email @NotBlank String email,
            @Size(min = 2, max = 120) String displayName,
            @Size(min = 8, max = 100) String password) {}

    public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

    public record AuthResponse(String token, UserView user) {}

    public record UserView(UUID id, String email, String displayName) {}

    public record CreateWorkspaceRequest(@Size(min = 2, max = 120) String name) {}

    public record AddMemberRequest(@Email @NotBlank String email, @NotNull WorkspaceRole role) {}

    public record WorkspaceView(UUID id, String name, String slug, WorkspaceRole role, Instant createdAt) {}

    public record WorkspaceDetail(
            UUID id, String name, String slug, WorkspaceRole role, Instant createdAt,
            List<MemberView> members) {}

    public record MemberView(UUID userId, String email, String displayName, WorkspaceRole role) {}

    public record CreateRoomRequest(
            @Size(min = 2, max = 120) String name,
            @Pattern(regexp = "javascript|typescript|java|python|json|markdown|sql|html|css") String language) {}

    public record RoomView(UUID id, UUID workspaceId, String name, String language,
                           WorkspaceRole role, Instant createdAt) {}

    public record DocumentView(
            UUID roomId, String codeContent, String whiteboardJson, long version, Instant updatedAt) {}

    public record SaveDocumentRequest(
            @NotNull String codeContent,
            @NotNull String whiteboardJson,
            @PositiveOrZero long version) {}

    public record CreateSnapshotRequest(@Size(min = 1, max = 140) String label) {}

    public record SnapshotView(
            UUID id, String label, long documentVersion, UUID createdBy, Instant createdAt) {}

    public record AuditView(
            UUID id, UUID actorId, String action, String entityType, String entityId,
            String metadataJson, Instant createdAt) {}

    public record ErrorResponse(String code, String message, Instant timestamp) {}
}
