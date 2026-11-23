
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ShiftSchedule domain model.
 */
class ShiftScheduleTest {

    @Test
    void shouldCreateShiftScheduleWithValidData() {
        List<Shift> shifts = List.of(
                new Shift(
                        UUID.randomUUID(),
                        "Morning",
                        new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
                ),
                new Shift(
                        UUID.randomUUID(),
                        "Evening",
                        new TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0))
                )
        );

        ShiftSchedule schedule = new ShiftSchedule(
                UUID.randomUUID(),
                "Hospital Schedule",
                shifts
        );

        assertNotNull(schedule);
        assertEquals("Hospital Schedule", schedule.name());
        assertEquals(2, schedule.shifts().size());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(
                        null,
                        "Hospital Schedule",
                        List.of(new Shift(
                                UUID.randomUUID(),
                                "Morning",
                                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
                        ))
                )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(
                        UUID.randomUUID(),
                        null,
                        List.of(new Shift(
                                UUID.randomUUID(),
                                "Morning",
                                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
                        ))
                )
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShiftSchedule(
                        UUID.randomUUID(),
                        " ",
                        List.of(new Shift(
                                UUID.randomUUID(),
                                "Morning",
                                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
                        ))
                )
        );
    }

    @Test
    void shouldRejectNullShifts() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(
                        UUID.randomUUID(),
                        "Hospital Schedule",
                        null
                )
        );
    }

    @Test
    void shouldRejectEmptyShifts() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShiftSchedule(
                        UUID.randomUUID(),
                        "Hospital Schedule",
                        List.of()
                )
        );
    }

    @Test
    void shouldCreateDefensiveCopyOfShifts() {
        java.util.ArrayList<Shift> shifts = new java.util.ArrayList<>();
        shifts.add(new Shift(
                UUID.randomUUID(),
                "Morning",
                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
        ));

        ShiftSchedule schedule = new ShiftSchedule(
                UUID.randomUUID(),
                "Hospital Schedule",
                shifts
        );

        shifts.clear();

        assertEquals(1, schedule.shifts().size());
    }
}