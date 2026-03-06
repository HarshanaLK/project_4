package dev.synapse.collab.repo;

import dev.synapse.collab.domain.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findAllByWorkspaceIdOrderByCreatedAtAsc(UUID workspaceId);
}
