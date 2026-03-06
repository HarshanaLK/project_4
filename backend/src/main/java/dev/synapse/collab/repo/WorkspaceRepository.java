package dev.synapse.collab.repo;

import dev.synapse.collab.domain.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WorkspaceRepository extends JpaRepository<Workspace, UUID> {}
