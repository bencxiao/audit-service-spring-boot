package com.example.auditservice.controller;

import com.example.auditservice.model.AuditEvent;
import com.example.auditservice.repository.AuditEventRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/audit-events")
public class AuditEventController {

    private final AuditEventRepository auditEventRepository;

    public AuditEventController(AuditEventRepository auditEventRepository) {
        this.auditEventRepository = auditEventRepository;
    }

    // 🔍 Get all audit events
    @GetMapping
    public List<AuditEvent> getAllEvents() {
        return auditEventRepository.findAll();
    }

    // 🔍 Get a specific audit event by ID
    @GetMapping("/{id}")
    public Optional<AuditEvent> getEventById(@PathVariable String id) {
        return auditEventRepository.findById(id);
    }

    @GetMapping("/by-entity/{entityType}")
    public List<AuditEvent> getEventsByEntityType(@PathVariable String entityType,
                                                  Authentication authentication) {
        List<String> allowedEntities = (List<String>) authentication.getDetails();

        if (!allowedEntities.contains(entityType)) {
            throw new AccessDeniedException("Access denied to entity type: " + entityType);
        }

        return auditEventRepository.findByEntityType(entityType);
    }
}
