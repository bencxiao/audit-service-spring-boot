package com.example.auditservice.kafka;

import com.example.auditservice.repository.AuditEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuditEventListenerTest {

    private AuditEventListener auditEventListener;

    @Mock
    private AuditEventRepository repository;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        auditEventListener = new AuditEventListener(repository, objectMapper);
    }

    @Test
    void testProcessAuditEvent_SuccessfulProcessing() {
        String auditEventJson = """
            {
                "eventType": "USER_LOGIN",
                "eventId": "123",
                "serviceName":"orderService",
                "entityType": "CUSTOMER",
                "entityId": "1234",
                "timestamp": "2024-03-20T10:00:00Z"
            }
            """;
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("audit-topic", 0, 0L, "key", auditEventJson);

        auditEventListener.listen(record);

        verify(repository, times(1)).save(ArgumentMatchers.any());
    }

    @Test
    void testProcessAuditEvent_InvalidJson() {
        String invalidJson = "invalid json";
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("audit-topic", 0, 0L, "key", invalidJson);

        assertThrows(RuntimeException.class, () ->
                auditEventListener.listen(record));
    }

    @Test
    void testProcessAuditEvent_NullMessage() {
        ConsumerRecord<String, String> record =
                new ConsumerRecord<>("audit-topic", 0, 0L, "key", null);

        assertThrows(RuntimeException.class, () ->
                auditEventListener.listen(record));
    }
}