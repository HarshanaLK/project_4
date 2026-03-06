package dev.synapse.collab.service;

import dev.synapse.collab.api.*;
import dev.synapse.collab.domain.*;
import dev.synapse.collab.repo.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.*;

@Service
public class WorkspaceService {
    private final WorkspaceRepository workspaces;
    private final WorkspaceMemberRepository members;
    private final UserRepository users;
    private final AccessService access;
    private final AuditService audit;

    public WorkspaceService(WorkspaceRepository workspaces, WorkspaceMemberRepository members,
                            UserRepository users, AccessService access, AuditService audit) {
        this.workspaces = workspaces;
        this.members = members;
        this.users = users;
        this.access = access;
        this.audit = audit;
    }

    public List<ApiDtos.WorkspaceView> list(UUID userId) {
        return members.findAllByUserId(userId).stream()
                .map(member -> {
                    Workspace w = workspaces.findById(member.getWorkspaceId()).orElseThrow();
                    return new ApiDtos.WorkspaceView(w.getId(), w.getName(), w.getSlug(), member.getRole(), w.getCreatedAt());
                }).toList();
    }

    @Transactional
    public ApiDtos.WorkspaceView create(UUID userId, ApiDtos.CreateWorkspaceRequest request) {
        String slug = slugify(request.name()) + "-" + UUID.randomUUID().toString().substring(0, 6);
        Workspace workspace = workspaces.save(new Workspace(request.name().trim(), slug, userId));
        members.save(new WorkspaceMember(workspace.getId(), userId, WorkspaceRole.OWNER));
        audit.record(workspace.getId(), userId, "WORKSPACE_CREATED", "WORKSPACE", workspace.getId(),
                Map.of("name", workspace.getName()));
        return new ApiDtos.WorkspaceView(workspace.getId(), workspace.getName(), workspace.getSlug(),
                WorkspaceRole.OWNER, workspace.getCreatedAt());
    }

    public ApiDtos.WorkspaceDetail detail(UUID workspaceId, UUID userId) {
        WorkspaceMember me = access.requireMember(workspaceId, userId);
        Workspace w = workspaces.findById(workspaceId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "WORKSPACE_NOT_FOUND", "Workspace not found."));

        List<ApiDtos.MemberView> memberViews = members.findAllByWorkspaceId(workspaceId).stream()
                .map(member -> {
                    User user = users.findById(member.getUserId()).orElseThrow();
                    return new ApiDtos.MemberView(user.getId(), user.getEmail(), user.getDisplayName(), member.getRole());
                }).toList();

        return new ApiDtos.WorkspaceDetail(w.getId(), w.getName(), w.getSlug(), me.getRole(), w.getCreatedAt(), memberViews);
    }

    @Transactional
    public ApiDtos.MemberView addMember(UUID workspaceId, UUID actorId, ApiDtos.AddMemberRequest request) {
        access.requireAdmin(workspaceId, actorId);
        User user = users.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND",
                        "The user must register before being added."));

        WorkspaceMember member = members.findByWorkspaceIdAndUserId(workspaceId, user.getId())
                .map(existing -> {
                    existing.setRole(request.role());
                    return existing;
                })
                .orElseGet(() -> new WorkspaceMember(workspaceId, user.getId(), request.role()));

        members.save(member);
        audit.record(workspaceId, actorId, "MEMBER_UPSERTED", "USER", user.getId(),
                Map.of("role", request.role().name(), "email", user.getEmail()));

        return new ApiDtos.MemberView(user.getId(), user.getEmail(), user.getDisplayName(), member.getRole());
    }

    private String slugify(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        return normalized.isBlank() ? "workspace" : normalized;
    }
}
