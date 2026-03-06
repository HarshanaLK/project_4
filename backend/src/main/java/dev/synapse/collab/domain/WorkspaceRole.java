package dev.synapse.collab.domain;

public enum WorkspaceRole {
    OWNER, ADMIN, EDITOR, VIEWER;

    public boolean canEdit() {
        return this != VIEWER;
    }

    public boolean canAdminister() {
        return this == OWNER || this == ADMIN;
    }
}
