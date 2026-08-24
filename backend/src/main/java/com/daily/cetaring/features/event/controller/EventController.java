package com.daily.cetaring.features.event.controller;

import com.daily.cetaring.features.event.dto.EventDtos;
import com.daily.cetaring.features.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Manage customer events and workspaces")
public class EventController {

    private final EventService eventService;

    @PostMapping
    @Operation(summary = "Create Event")
    public ResponseEntity<EventDtos.Response> createEvent(@Valid @RequestBody EventDtos.CreateEventRequest request) {
        EventDtos.Response resp = eventService.createEvent(request);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Event by ID")
    public ResponseEntity<EventDtos.Response> getEvent(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id));
    }

    @GetMapping
    @Operation(summary = "List my events")
    public ResponseEntity<List<EventDtos.Response>> listMyEvents() {
        return ResponseEntity.ok(eventService.listMyEvents());
    }

    @GetMapping("/{id}/workspace")
    @Operation(summary = "Get Event workspace")
    public ResponseEntity<EventDtos.WorkspaceResponse> getWorkspace(@PathVariable Long id) {
        return ResponseEntity.ok(eventService.getWorkspace(id));
    }
}
