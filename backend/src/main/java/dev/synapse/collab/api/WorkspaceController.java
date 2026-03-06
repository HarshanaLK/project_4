package dev.synapse.collab.api;

import dev.synapse.collab.repo.AuditLogRepository;
import dev.synapse.collab.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/workspaces")
public class WorkspaceController {
    private final WorkspaceService workspaces;
    private final RoomService rooms;
    private final CurrentUser current;
    private final AccessService access;
    private final AuditLogRepository audits;

    public WorkspaceController(WorkspaceService workspaces, RoomService rooms, CurrentUser current,
                               AccessService access, AuditLogRepository audits) {
        this.workspaces = workspaces;
        this.rooms = rooms;
        this.current = current;
        this.access = access;
        this.audits = audits;
    }

    @GetMapping
    public List<ApiDtos.WorkspaceView> list() {
        return workspaces.list(current.id());
    }

    @PostMapping
    public ApiDtos.WorkspaceView create(@Valid @RequestBody ApiDtos.CreateWorkspaceRequest request) {
        return workspaces.create(current.id(), request);
    }

    @GetMapping("/{workspaceId}")
    public ApiDtos.WorkspaceDetail detail(@PathVariable UUID workspaceId) {
        return workspaces.detail(workspaceId, current.id());
    }

    @PostMapping("/{workspaceId}/members")
    public ApiDtos.MemberView addMember(@PathVariable UUID workspaceId,
                                        @Valid @RequestBody ApiDtos.AddMemberRequest request) {
        return workspaces.addMember(workspaceId, current.id(), request);
    }

    @GetMapping("/{workspaceId}/rooms")
    public List<ApiDtos.RoomView> rooms(@PathVariable UUID workspaceId) {
        return rooms.list(workspaceId, current.id());
    }

    @PostMapping("/{workspaceId}/rooms")
    public ApiDtos.RoomView createRoom(@PathVariable UUID workspaceId,
                                       @Valid @RequestBody ApiDtos.CreateRoomRequest request) {
        return rooms.create(workspaceId, current.id(), request);
    }

    @GetMapping("/{workspaceId}/audit")
    public List<ApiDtos.AuditView> audit(@PathVariable UUID workspaceId) {
        access.requireAdmin(workspaceId, current.id());
        return audits.findTop100ByWorkspaceIdOrderByCreatedAtDesc(workspaceId).stream()
                .map(a -> new ApiDtos.AuditView(a.getId(), a.getActorId(), a.getAction(),
                        a.getEntityType(), a.getEntityId(), a.getMetadataJson(), a.getCreatedAt()))
                .toList();
    }
}
