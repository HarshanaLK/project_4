package dev.synapse.collab.service;

import dev.synapse.collab.domain.AuditLog;
import dev.synapse.collab.repo.AuditLogRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final AuditLogRepository logs;
    private final ObjectMapper mapper;

    public AuditService(
            AuditLogRepository logs,
            ObjectMapper mapper
    ) {
        this.logs = logs;
        this.mapper = mapper;
    }

    public void record(
            UUID workspaceId,
            UUID actorId,
            String action,
            String entityType,
            UUID entityId,
            Map<String, ?> metadata
    ) {
        try {
            logs.save(
                    new AuditLog(
                            workspaceId,
                            actorId,
                            action,
                            entityType,
                            entityId == null
                                    ? null
                                    : entityId.toString(),
                            mapper.writeValueAsString(metadata)
                    )
            );
        } catch (JacksonException e) {
            throw new IllegalStateException(
                    "Could not serialize audit metadata",
                    e
            );
        }
    }
}