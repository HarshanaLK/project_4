package dev.synapse.collab.collab;

import dev.synapse.collab.domain.ChatMessage;
import dev.synapse.collab.repo.ChatMessageRepository;
import dev.synapse.collab.security.UserPrincipal;
import dev.synapse.collab.service.AccessService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.*;

@Controller
public class CollabController {
    private final SimpMessagingTemplate messaging;
    private final AccessService access;
    private final PresenceRegistry presence;
    private final ChatMessageRepository chats;

    public CollabController(SimpMessagingTemplate messaging, AccessService access,
                            PresenceRegistry presence, ChatMessageRepository chats) {
        this.messaging = messaging;
        this.access = access;
        this.presence = presence;
        this.chats = chats;
    }

    @MessageMapping("/rooms/{roomId}/presence")
    public void presence(@DestinationVariable UUID roomId, @Payload Map<String, Object> payload, Principal principal) {
        UserPrincipal user = user(principal);
        access.requireRoomAccess(roomId, user.id(), false);

        String action = String.valueOf(payload.getOrDefault("action", "join"));
        if ("leave".equalsIgnoreCase(action)) {
            presence.leave(roomId, user.id());
        } else {
            presence.join(roomId, user.id(), user.displayName());
        }

        Map<String, Object> users = new LinkedHashMap<>();
        presence.users(roomId).forEach((id, name) -> users.put(id.toString(), name));
        publish(CollabEvent.of(roomId, "PRESENCE", user.id(), user.displayName(), Map.of("users", users)));
    }

    @MessageMapping("/rooms/{roomId}/code")
    public void code(@DestinationVariable UUID roomId, @Payload Map<String, Object> payload, Principal principal) {
        UserPrincipal user = user(principal);
        access.requireRoomAccess(roomId, user.id(), true);
        publish(CollabEvent.of(roomId, "CODE_CHANGE", user.id(), user.displayName(), payload));
    }

    @MessageMapping("/rooms/{roomId}/cursor")
    public void cursor(@DestinationVariable UUID roomId, @Payload Map<String, Object> payload, Principal principal) {
        UserPrincipal user = user(principal);
        access.requireRoomAccess(roomId, user.id(), false);
        publish(CollabEvent.of(roomId, "CURSOR", user.id(), user.displayName(), payload));
    }

    @MessageMapping("/rooms/{roomId}/whiteboard")
    public void whiteboard(@DestinationVariable UUID roomId, @Payload Map<String, Object> payload, Principal principal) {
        UserPrincipal user = user(principal);
        access.requireRoomAccess(roomId, user.id(), true);
        publish(CollabEvent.of(roomId, "WHITEBOARD", user.id(), user.displayName(), payload));
    }

    @MessageMapping("/rooms/{roomId}/chat")
    public void chat(@DestinationVariable UUID roomId, @Payload Map<String, Object> payload, Principal principal) {
        UserPrincipal user = user(principal);
        access.requireRoomAccess(roomId, user.id(), false);

        String body = String.valueOf(payload.getOrDefault("body", "")).trim();
        if (body.isBlank() || body.length() > 1000) return;

        ChatMessage saved = chats.save(new ChatMessage(roomId, user.id(), body));
        publish(CollabEvent.of(roomId, "CHAT", user.id(), user.displayName(),
                Map.of("id", saved.getId().toString(), "body", body)));
    }

    private void publish(CollabEvent event) {
        messaging.convertAndSend("/topic/rooms/" + event.roomId() + "/events", event);
    }

    private UserPrincipal user(Principal principal) {
        if (principal instanceof UsernamePasswordAuthenticationToken auth &&
                auth.getPrincipal() instanceof UserPrincipal user) {
            return user;
        }
        throw new IllegalStateException("Authenticated WebSocket principal required.");
    }
}
