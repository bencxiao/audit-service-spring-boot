package com.example.auditservice.model;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "audit_events")
public class AuditEvent {

    @Id
    @Column(name = "event_id", nullable = false)
    private String eventId;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "event_type")
    private String eventType;

    @Column(name = "entity_type")
    private String entityType;

    @Column(name = "entity_id")
    private String entityId;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String rawPayload;
}