package dev.synapse.collab.repo;

import dev.synapse.collab.domain.Snapshot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

import java.util.UUID;

public interface SnapshotRepository extends JpaRepository<Snapshot, UUID> {
    List<Snapshot> findAllByRoomIdOrderByCreatedAtDesc(UUID roomId);
}
