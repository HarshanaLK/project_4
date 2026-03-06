package dev.synapse.collab.api;

import dev.synapse.collab.service.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService rooms;
    private final CurrentUser current;

    public RoomController(RoomService rooms, CurrentUser current) {
        this.rooms = rooms;
        this.current = current;
    }

    @GetMapping("/{roomId}")
    public ApiDtos.RoomView room(@PathVariable UUID roomId) {
        return rooms.get(roomId, current.id());
    }

    @GetMapping("/{roomId}/document")
    public ApiDtos.DocumentView document(@PathVariable UUID roomId) {
        return rooms.document(roomId, current.id());
    }

    @PutMapping("/{roomId}/document")
    public ApiDtos.DocumentView save(@PathVariable UUID roomId,
                                     @Valid @RequestBody ApiDtos.SaveDocumentRequest request) {
        return rooms.save(roomId, current.id(), request);
    }

    @GetMapping("/{roomId}/snapshots")
    public List<ApiDtos.SnapshotView> snapshots(@PathVariable UUID roomId) {
        return rooms.snapshots(roomId, current.id());
    }

    @PostMapping("/{roomId}/snapshots")
    public ApiDtos.SnapshotView snapshot(@PathVariable UUID roomId,
                                         @Valid @RequestBody ApiDtos.CreateSnapshotRequest request) {
        return rooms.createSnapshot(roomId, current.id(), request);
    }

    @PostMapping("/{roomId}/snapshots/{snapshotId}/restore")
    public ApiDtos.DocumentView restore(@PathVariable UUID roomId, @PathVariable UUID snapshotId) {
        return rooms.restore(roomId, snapshotId, current.id());
    }
}
