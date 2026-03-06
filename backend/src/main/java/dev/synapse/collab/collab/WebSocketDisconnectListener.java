package dev.synapse.collab.collab;

import dev.synapse.collab.security.UserPrincipal;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class WebSocketDisconnectListener {
    private final PresenceRegistry presence;
    private final SimpMessagingTemplate messaging;

    public WebSocketDisconnectListener(PresenceRegistry presence, SimpMessagingTemplate messaging) {
        this.presence = presence;
        this.messaging = messaging;
    }

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        if (!(event.getUser() instanceof UsernamePasswordAuthenticationToken auth) ||
                !(auth.getPrincipal() instanceof UserPrincipal user)) {
            return;
        }

        presence.leaveEverywhere(user.id()).forEach(roomId -> {
            Map<String, Object> users = new LinkedHashMap<>();
            presence.users(roomId).forEach((id, name) -> users.put(id.toString(), name));
            CollabEvent update = CollabEvent.of(roomId, "PRESENCE", user.id(), user.displayName(), Map.of("users", users));
            messaging.convertAndSend("/topic/rooms/" + roomId + "/events", update);
        });
    }
}
