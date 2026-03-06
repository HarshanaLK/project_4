package dev.synapse.collab.collab;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;

@Component
public class PresenceRegistry {
    private final ConcurrentMap<UUID, ConcurrentMap<UUID, String>> rooms = new ConcurrentHashMap<>();

    public void join(UUID roomId, UUID userId, String displayName) {
        rooms.computeIfAbsent(roomId, ignored -> new ConcurrentHashMap<>()).put(userId, displayName);
    }

    public void leave(UUID roomId, UUID userId) {
        var room = rooms.get(roomId);
        if (room == null) return;
        room.remove(userId);
        if (room.isEmpty()) rooms.remove(roomId);
    }

    public Set<UUID> leaveEverywhere(UUID userId) {
        Set<UUID> affected = new HashSet<>();
        rooms.forEach((roomId, roomUsers) -> {
            if (roomUsers.remove(userId) != null) affected.add(roomId);
            if (roomUsers.isEmpty()) rooms.remove(roomId, roomUsers);
        });
        return affected;
    }

    public Map<UUID, String> users(UUID roomId) {
        return Map.copyOf(rooms.getOrDefault(roomId, new ConcurrentHashMap<>()));
    }
}
