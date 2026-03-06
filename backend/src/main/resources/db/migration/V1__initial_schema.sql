CREATE TABLE users (
    id BINARY(16) PRIMARY KEY,
    email VARCHAR(190) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

CREATE TABLE workspaces (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    slug VARCHAR(140) NOT NULL UNIQUE,
    created_by BINARY(16) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_workspace_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE workspace_members (
    id BINARY(16) PRIMARY KEY,
    workspace_id BINARY(16) NOT NULL,
    user_id BINARY(16) NOT NULL,
    role VARCHAR(20) NOT NULL,
    joined_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_workspace_member UNIQUE (workspace_id, user_id),
    CONSTRAINT fk_member_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE rooms (
    id BINARY(16) PRIMARY KEY,
    workspace_id BINARY(16) NOT NULL,
    name VARCHAR(120) NOT NULL,
    language VARCHAR(40) NOT NULL,
    created_by BINARY(16) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_room_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_room_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE room_documents (
    id BINARY(16) PRIMARY KEY,
    room_id BINARY(16) NOT NULL UNIQUE,
    code_content LONGTEXT NOT NULL,
    whiteboard_json LONGTEXT NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    updated_by BINARY(16),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT fk_document_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_document_updater FOREIGN KEY (updated_by) REFERENCES users(id)
);

CREATE TABLE snapshots (
    id BINARY(16) PRIMARY KEY,
    room_id BINARY(16) NOT NULL,
    label VARCHAR(140) NOT NULL,
    code_content LONGTEXT NOT NULL,
    whiteboard_json LONGTEXT NOT NULL,
    document_version BIGINT NOT NULL,
    created_by BINARY(16) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_snapshot_room_created (room_id, created_at),
    CONSTRAINT fk_snapshot_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_snapshot_creator FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE chat_messages (
    id BINARY(16) PRIMARY KEY,
    room_id BINARY(16) NOT NULL,
    sender_id BINARY(16) NOT NULL,
    body VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_chat_room_created (room_id, created_at),
    CONSTRAINT fk_chat_room FOREIGN KEY (room_id) REFERENCES rooms(id) ON DELETE CASCADE,
    CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users(id)
);

CREATE TABLE audit_logs (
    id BINARY(16) PRIMARY KEY,
    workspace_id BINARY(16) NOT NULL,
    actor_id BINARY(16) NOT NULL,
    action VARCHAR(80) NOT NULL,
    entity_type VARCHAR(60) NOT NULL,
    entity_id VARCHAR(80),
    metadata_json TEXT NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    INDEX idx_audit_workspace_created (workspace_id, created_at),
    CONSTRAINT fk_audit_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces(id) ON DELETE CASCADE,
    CONSTRAINT fk_audit_actor FOREIGN KEY (actor_id) REFERENCES users(id)
);
