
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ContinuousSchedule domain model.
 */
class ContinuousScheduleTest {

    @Test
    void shouldCreateContinuousScheduleWithValidData() {
        TimeSlot timeSlot = new TimeSlot(
                LocalTime.of(9, 0),
                LocalTime.of(17, 0)
        );

        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                timeSlot
        );

        assertNotNull(schedule);
        assertEquals("Office Schedule", schedule.name());
        assertEquals(timeSlot, schedule.timeSlot());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        null,
                        "Office Schedule",
                        new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0))
                )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(),
                        null,
                        new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0))
                )
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(),
                        " ",
                        new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0))
                )
        );
    }

    @Test
    void shouldRejectNullTimeSlot() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(),
                        "Office Schedule",
                        null
                )
        );
    }
}