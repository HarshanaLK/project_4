package dev.synapse.collab.service;

import dev.synapse.collab.api.ApiException;
import dev.synapse.collab.domain.*;
import dev.synapse.collab.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessService {
    private final WorkspaceMemberRepository members;
    private final RoomRepository rooms;

    public AccessService(WorkspaceMemberRepository members, RoomRepository rooms) {
        this.members = members;
        this.rooms = rooms;
    }

    public WorkspaceMember requireMember(UUID workspaceId, UUID userId) {
        return members.findByWorkspaceIdAndUserId(workspaceId, userId)
                .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "WORKSPACE_ACCESS_DENIED",
                        "You are not a member of this workspace."));
    }

    public WorkspaceMember requireAdmin(UUID workspaceId, UUID userId) {
        WorkspaceMember member = requireMember(workspaceId, userId);
        if (!member.getRole().canAdminister()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED", "Workspace administration permission required.");
        }
        return member;
    }

    public Room requireRoomAccess(UUID roomId, UUID userId, boolean edit) {
        Room room = rooms.findById(roomId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "ROOM_NOT_FOUND", "Room not found."));
        WorkspaceMember member = requireMember(room.getWorkspaceId(), userId);
        if (edit && !member.getRole().canEdit()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "READ_ONLY", "This workspace role is read-only.");
        }
        return room;
    }
}
