
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the OnCallDuty domain model.
 */
class OnCallDutyTest {

    private Person createPerson() {
        return new Person(UUID.randomUUID(), "EMP001", "John Smith");
    }

    @Test
    void shouldCreateOnCallDutyWithValidData() {
        LocalDateTime start = LocalDateTime.of(2026, 6, 1, 8, 0);
        LocalDateTime end = LocalDateTime.of(2026, 6, 1, 20, 0);

        OnCallDuty duty = new OnCallDuty(
                UUID.randomUUID(),
                createPerson(),
                start,
                end
        );

        assertNotNull(duty);
        assertEquals(start, duty.startDateTime());
        assertEquals(end, duty.endDateTime());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new OnCallDuty(
                        null,
                        createPerson(),
                        LocalDateTime.of(2026, 6, 1, 8, 0),
                        LocalDateTime.of(2026, 6, 1, 20, 0)
                )
        );
    }

    @Test
    void shouldRejectNullPerson() {
        assertThrows(NullPointerException.class, () ->
                new OnCallDuty(
                        UUID.randomUUID(),
                        null,
                        LocalDateTime.of(2026, 6, 1, 8, 0),
                        LocalDateTime.of(2026, 6, 1, 20, 0)
                )
        );
    }

    @Test
    void shouldRejectNullStartDateTime() {
        assertThrows(NullPointerException.class, () ->
                new OnCallDuty(
                        UUID.randomUUID(),
                        createPerson(),
                        null,
                        LocalDateTime.of(2026, 6, 1, 20, 0)
                )
        );
    }

    @Test
    void shouldRejectNullEndDateTime() {
        assertThrows(NullPointerException.class, () ->
                new OnCallDuty(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDateTime.of(2026, 6, 1, 8, 0),
                        null
                )
        );
    }

    @Test
    void shouldRejectEndDateTimeBeforeStartDateTime() {
        assertThrows(IllegalArgumentException.class, () ->
                new OnCallDuty(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDateTime.of(2026, 6, 1, 20, 0),
                        LocalDateTime.of(2026, 6, 1, 8, 0)
                )
        );
    }

    @Test
    void shouldRejectEqualStartAndEndDateTime() {
        LocalDateTime dateTime = LocalDateTime.of(2026, 6, 1, 8, 0);

        assertThrows(IllegalArgumentException.class, () ->
                new OnCallDuty(
                        UUID.randomUUID(),
                        createPerson(),
                        dateTime,
                        dateTime
                )
        );
    }
}