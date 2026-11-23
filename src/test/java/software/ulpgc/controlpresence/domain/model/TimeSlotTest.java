
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the TimeSlot domain model.
 */
class TimeSlotTest {

    /**
     * Verifies that a valid time slot can be created.
     */
    @Test
    void shouldCreateTimeSlotWithValidTimes() {
        TimeSlot timeSlot = new TimeSlot(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        assertEquals(LocalTime.of(9, 0), timeSlot.startTime());
        assertEquals(LocalTime.of(17, 0), timeSlot.endTime());
    }

    /**
     * Verifies that a null start time is rejected.
     */
    @Test
    void shouldRejectNullStartTime() {
        assertThrows(NullPointerException.class, () ->
                new TimeSlot(null, LocalTime.of(17, 0))
        );
    }

    /**
     * Verifies that a null end time is rejected.
     */
    @Test
    void shouldRejectNullEndTime() {
        assertThrows(NullPointerException.class, () ->
                new TimeSlot(LocalTime.of(9, 0), null)
        );
    }

    /**
     * Verifies that equal start and end times are rejected.
     */
    @Test
    void shouldRejectEqualStartAndEndTimes() {
        assertThrows(IllegalArgumentException.class, () ->
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(9, 0)
                )
        );
    }

    /**
     * Verifies that a time slot crossing midnight is accepted.
     */
    @Test
    void shouldAllowTimeSlotCrossingMidnight() {
        TimeSlot timeSlot = new TimeSlot(
                LocalTime.of(22, 0),
                LocalTime.of(6, 0)
        );

        assertEquals(LocalTime.of(22, 0), timeSlot.startTime());
        assertEquals(LocalTime.of(6, 0), timeSlot.endTime());
    }
}