package dev.synapse.collab.collab;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record CollabEvent(
        UUID eventId,
        UUID roomId,
        String type,
        UUID userId,
        String displayName,
        Map<String, Object> payload,
        Instant timestamp
) {
    public static CollabEvent of(UUID roomId, String type, UUID userId, String displayName, Map<String, Object> payload) {
        return new CollabEvent(UUID.randomUUID(), roomId, type, userId, displayName, payload, Instant.now());
    }
}
