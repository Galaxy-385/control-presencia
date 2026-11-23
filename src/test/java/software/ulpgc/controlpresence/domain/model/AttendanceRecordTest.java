
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the AttendanceRecord domain model.
 */
class AttendanceRecordTest {

    private Person createPerson() {
        return new Person(UUID.randomUUID(), "EMP001", "John Smith");
    }

    @Test
    void shouldCreateAttendanceRecordWithValidData() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 1, 9, 0);

        AttendanceRecord record = new AttendanceRecord(
                UUID.randomUUID(),
                createPerson(),
                dateTime,
                AttendanceEventType.CLOCK_IN,
                "WEB"
        );

        assertNotNull(record);
        assertEquals(dateTime, record.dateTime());
        assertEquals(AttendanceEventType.CLOCK_IN, record.eventType());
        assertEquals("WEB", record.origin());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new AttendanceRecord(
                        null,
                        createPerson(),
                        LocalDateTime.now(),
                        AttendanceEventType.CLOCK_IN,
                        "WEB"
                )
        );
    }

    @Test
    void shouldRejectNullPerson() {
        assertThrows(NullPointerException.class, () ->
                new AttendanceRecord(
                        UUID.randomUUID(),
                        null,
                        LocalDateTime.now(),
                        AttendanceEventType.CLOCK_IN,
                        "WEB"
                )
        );
    }

    @Test
    void shouldRejectNullDateTime() {
        assertThrows(NullPointerException.class, () ->
                new AttendanceRecord(
                        UUID.randomUUID(),
                        createPerson(),
                        null,
                        AttendanceEventType.CLOCK_IN,
                        "WEB"
                )
        );
    }

    @Test
    void shouldRejectNullEventType() {
        assertThrows(NullPointerException.class, () ->
                new AttendanceRecord(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDateTime.now(),
                        null,
                        "WEB"
                )
        );
    }

    @Test
    void shouldRejectNullOrigin() {
        assertThrows(NullPointerException.class, () ->
                new AttendanceRecord(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDateTime.now(),
                        AttendanceEventType.CLOCK_IN,
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankOrigin() {
        assertThrows(IllegalArgumentException.class, () ->
                new AttendanceRecord(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDateTime.now(),
                        AttendanceEventType.CLOCK_IN,
                        " "
                )
        );
    }

    @Test
    void shouldSupportClockOutEvent() {
        AttendanceRecord record = new AttendanceRecord(
                UUID.randomUUID(),
                createPerson(),
                LocalDateTime.now(),
                AttendanceEventType.CLOCK_OUT,
                "MOBILE"
        );

        assertEquals(AttendanceEventType.CLOCK_OUT, record.eventType());
    }

    @Test
    void shouldContainExpectedAttendanceEventTypes() {
        assertArrayEquals(
                new AttendanceEventType[]{
                        AttendanceEventType.CLOCK_IN,
                        AttendanceEventType.CLOCK_OUT
                },
                AttendanceEventType.values()
        );
    }
}