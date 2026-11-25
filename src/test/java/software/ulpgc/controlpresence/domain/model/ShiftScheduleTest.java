package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ShiftSchedule domain model.
 */
class ShiftScheduleTest {

    private Shift createMorningShift() {
        return new Shift(
                UUID.randomUUID(),
                "Morning",
                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
        );
    }

    private Shift createEveningShift() {
        return new Shift(
                UUID.randomUUID(),
                "Evening",
                new TimeSlot(LocalTime.of(14, 0), LocalTime.of(22, 0))
        );
    }

    private Map<DayOfWeek, Shift> createShiftsByDay() {
        return Map.of(
                DayOfWeek.MONDAY, createMorningShift(),
                DayOfWeek.TUESDAY, createMorningShift(),
                DayOfWeek.WEDNESDAY, createEveningShift()
        );
    }

    @Test
    void shouldCreateShiftScheduleWithValidData() {
        Map<DayOfWeek, Shift> shifts = createShiftsByDay();

        ShiftSchedule schedule = new ShiftSchedule(
                UUID.randomUUID(),
                "Hospital Schedule",
                shifts
        );

        assertNotNull(schedule);
        assertEquals("Hospital Schedule", schedule.name());
        assertEquals(3, schedule.shiftsByDay().size());
        assertEquals(shifts.get(DayOfWeek.MONDAY),
                schedule.shiftsByDay().get(DayOfWeek.MONDAY));
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(null, "Hospital Schedule", createShiftsByDay())
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(UUID.randomUUID(), null, createShiftsByDay())
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShiftSchedule(UUID.randomUUID(), " ", createShiftsByDay())
        );
    }

    @Test
    void shouldRejectNullShiftsByDay() {
        assertThrows(NullPointerException.class, () ->
                new ShiftSchedule(UUID.randomUUID(), "Hospital Schedule", null)
        );
    }

    @Test
    void shouldRejectEmptyShiftsByDay() {
        assertThrows(IllegalArgumentException.class, () ->
                new ShiftSchedule(UUID.randomUUID(), "Hospital Schedule", Map.of())
        );
    }

    @Test
    void shouldCreateDefensiveCopyOfShiftsByDay() {
        Map<DayOfWeek, Shift> shifts = new HashMap<>(createShiftsByDay());

        ShiftSchedule schedule = new ShiftSchedule(
                UUID.randomUUID(), "Hospital Schedule", shifts
        );

        shifts.clear();

        assertEquals(3, schedule.shiftsByDay().size());
    }
}