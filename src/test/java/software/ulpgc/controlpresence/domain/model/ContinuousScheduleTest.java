package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ContinuousSchedule domain model.
 */
class ContinuousScheduleTest {

    private final TimeSlot timeSlot = new TimeSlot(
            LocalTime.of(9, 0),
            LocalTime.of(17, 0)
    );

    private final Set<DayOfWeek> workingDays = Set.of(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
    );

    @Test
    void shouldCreateContinuousScheduleWithValidData() {
        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                timeSlot,
                workingDays
        );

        assertNotNull(schedule);
        assertEquals("Office Schedule", schedule.name());
        assertEquals(timeSlot, schedule.timeSlot());
        assertEquals(workingDays, schedule.workingDays());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        null, "Office Schedule", timeSlot, workingDays
                )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(), null, timeSlot, workingDays
                )
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(), " ", timeSlot, workingDays
                )
        );
    }

    @Test
    void shouldRejectNullTimeSlot() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(), "Office Schedule", null, workingDays
                )
        );
    }

    @Test
    void shouldRejectNullWorkingDays() {
        assertThrows(NullPointerException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(), "Office Schedule", timeSlot, null
                )
        );
    }

    @Test
    void shouldRejectEmptyWorkingDays() {
        assertThrows(IllegalArgumentException.class, () ->
                new ContinuousSchedule(
                        UUID.randomUUID(), "Office Schedule", timeSlot, Set.of()
                )
        );
    }

    @Test
    void shouldCreateDefensiveCopyOfWorkingDays() {
        Set<DayOfWeek> days = new java.util.HashSet<>(
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY)
        );

        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(), "Office Schedule", timeSlot, days
        );

        days.clear();

        assertEquals(2, schedule.workingDays().size());
    }
}