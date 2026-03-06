package dev.synapse.collab.collab;

import dev.synapse.collab.repo.UserRepository;
import dev.synapse.collab.security.*;
import org.springframework.messaging.*;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {
    private final JwtService jwt;
    private final UserRepository users;

    public WebSocketAuthInterceptor(JwtService jwt, UserRepository users) {
        this.jwt = jwt;
        this.users = users;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String auth = accessor.getFirstNativeHeader("Authorization");
            if (auth == null || !auth.startsWith("Bearer ")) {
                throw new MessagingException("Missing WebSocket authorization.");
            }

            try {
                var userId = jwt.parseUserId(auth.substring(7));
                var user = users.findById(userId).orElseThrow();
                var principal = new UserPrincipal(user.getId(), user.getEmail(), user.getDisplayName());
                accessor.setUser(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
            } catch (Exception ex) {
                throw new MessagingException("Invalid WebSocket authorization.", ex);
            }
        }
        return message;
    }
}
