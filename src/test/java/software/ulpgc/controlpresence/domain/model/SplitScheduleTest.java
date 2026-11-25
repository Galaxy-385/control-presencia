package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SplitSchedule domain model.
 */
class SplitScheduleTest {

    private final List<TimeSlot> timeSlots = List.of(
            new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
            new TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0))
    );

    private final Set<DayOfWeek> workingDays = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    @Test
    void shouldCreateSplitScheduleWithValidData() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Restaurant Schedule",
                timeSlots,
                workingDays
        );

        assertNotNull(schedule);
        assertEquals("Restaurant Schedule", schedule.name());
        assertEquals(2, schedule.timeSlots().size());
        assertEquals(workingDays, schedule.workingDays());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(null, "Restaurant Schedule", timeSlots, workingDays)
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(UUID.randomUUID(), null, timeSlots, workingDays)
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new SplitSchedule(UUID.randomUUID(), " ", timeSlots, workingDays)
        );
    }

    @Test
    void shouldRejectNullTimeSlots() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(UUID.randomUUID(), "Restaurant Schedule", null, workingDays)
        );
    }

    @Test
    void shouldRejectEmptyTimeSlots() {
        assertThrows(IllegalArgumentException.class, () ->
                new SplitSchedule(UUID.randomUUID(), "Restaurant Schedule", List.of(), workingDays)
        );
    }

    @Test
    void shouldRejectNullWorkingDays() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(UUID.randomUUID(), "Restaurant Schedule", timeSlots, null)
        );
    }

    @Test
    void shouldRejectEmptyWorkingDays() {
        assertThrows(IllegalArgumentException.class, () ->
                new SplitSchedule(UUID.randomUUID(), "Restaurant Schedule", timeSlots, Set.of())
        );
    }

    @Test
    void shouldCreateDefensiveCopyOfTimeSlots() {
        java.util.ArrayList<TimeSlot> slots = new java.util.ArrayList<>(timeSlots);

        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(), "Restaurant Schedule", slots, workingDays
        );

        slots.clear();

        assertEquals(2, schedule.timeSlots().size());
    }

    @Test
    void shouldCreateDefensiveCopyOfWorkingDays() {
        java.util.HashSet<DayOfWeek> days = new java.util.HashSet<>(workingDays);

        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(), "Restaurant Schedule", timeSlots, days
        );

        days.clear();

        assertEquals(5, schedule.workingDays().size());
    }
}