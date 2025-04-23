package com.example.auditservice.kafka;

import com.example.auditservice.model.AuditEvent;
import com.example.auditservice.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AuditEventListener {

    private static final Logger logger = LoggerFactory.getLogger(AuditEventListener.class);
    private final AuditEventRepository auditEventRepository;
    private final ObjectMapper objectMapper;

    public AuditEventListener(AuditEventRepository auditEventRepository, ObjectMapper objectMapper) {
        this.auditEventRepository = auditEventRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
        topics = "audit-events",
        groupId = "audit-service",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void listen(ConsumerRecord<String, String> record) {
        String eventId = null;
        try {
            String rawPayload = record.value();

            AuditEvent event = objectMapper.readValue(rawPayload, AuditEvent.class);
            eventId = event.getEventId();
            
            // Validate required fields
            if (event.getEventId() == null || event.getEventId().trim().isEmpty()) {
                throw new IllegalArgumentException("Event ID cannot be null or empty");
            }

            if (event.getTimestamp() == null) {
                event.setTimestamp(Instant.now().toString());
            }

            event.setRawPayload(rawPayload);

            auditEventRepository.save(event);
            logger.info("Successfully saved audit event: {}", eventId);

        } catch (Exception e) {
            logger.error("Failed to process Kafka message for eventId: {}. Error: {}", 
                eventId, e.getMessage(), e);
            // The exception will be propagated to trigger Kafka's retry mechanism
            throw new RuntimeException("Failed to process audit event", e);
        }
    }
}
