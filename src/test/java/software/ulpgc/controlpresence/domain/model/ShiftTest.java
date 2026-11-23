
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Shift domain model.
 */
class ShiftTest {

    @Test
    void shouldCreateShiftWithValidData() {
        TimeSlot timeSlot = new TimeSlot(
                LocalTime.of(8, 0),
                LocalTime.of(16, 0)
        );

        Shift shift = new Shift(
                UUID.randomUUID(),
                "Morning Shift",
                timeSlot
        );

        assertNotNull(shift);
        assertEquals("Morning Shift", shift.name());
        assertEquals(timeSlot, shift.timeSlot());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new Shift(
                        null,
                        "Morning Shift",
                        new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 0))
                )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new Shift(
                        UUID.randomUUID(),
                        null,
                        new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 0))
                )
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Shift(
                        UUID.randomUUID(),
                        " ",
                        new TimeSlot(LocalTime.of(8, 0), LocalTime.of(16, 0))
                )
        );
    }

    @Test
    void shouldRejectNullTimeSlot() {
        assertThrows(NullPointerException.class, () ->
                new Shift(
                        UUID.randomUUID(),
                        "Morning Shift",
                        null
                )
        );
    }
}