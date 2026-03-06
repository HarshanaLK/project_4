package dev.synapse.collab.repo;

import dev.synapse.collab.domain.RoomDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

import java.util.UUID;

public interface RoomDocumentRepository extends JpaRepository<RoomDocument, UUID> {
    Optional<RoomDocument> findByRoomId(UUID roomId);
}
