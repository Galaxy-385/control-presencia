
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the ScheduleAssignment domain model.
 */
class ScheduleAssignmentTest {

    private Person createPerson() {
        return new Person(UUID.randomUUID(), "EMP001", "John Smith");
    }

    private Schedule createSchedule() {
        return new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
                ),
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                )
        );
    }

    @Test
    void shouldCreateAssignmentWithValidData() {
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        ScheduleAssignment assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                createPerson(),
                createSchedule(),
                startDate,
                Optional.of(LocalDate.of(2026, 12, 31))
        );

        assertNotNull(assignment);
        assertEquals(startDate, assignment.startDate());
        assertTrue(assignment.endDate().isPresent());
    }

    @Test
    void shouldAllowAssignmentWithoutEndDate() {
        ScheduleAssignment assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                createPerson(),
                createSchedule(),
                LocalDate.of(2026, 1, 1),
                Optional.empty()
        );

        assertTrue(assignment.endDate().isEmpty());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new ScheduleAssignment(
                        null,
                        createPerson(),
                        createSchedule(),
                        LocalDate.of(2026, 1, 1),
                        Optional.empty()
                )
        );
    }

    @Test
    void shouldRejectNullPerson() {
        assertThrows(NullPointerException.class, () ->
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        null,
                        createSchedule(),
                        LocalDate.of(2026, 1, 1),
                        Optional.empty()
                )
        );
    }

    @Test
    void shouldRejectNullSchedule() {
        assertThrows(NullPointerException.class, () ->
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        createPerson(),
                        null,
                        LocalDate.of(2026, 1, 1),
                        Optional.empty()
                )
        );
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThrows(NullPointerException.class, () ->
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        createPerson(),
                        createSchedule(),
                        null,
                        Optional.empty()
                )
        );
    }

    @Test
    void shouldRejectNullEndDateOptional() {
        assertThrows(NullPointerException.class, () ->
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        createPerson(),
                        createSchedule(),
                        LocalDate.of(2026, 1, 1),
                        null
                )
        );
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        createPerson(),
                        createSchedule(),
                        LocalDate.of(2026, 6, 1),
                        Optional.of(LocalDate.of(2026, 5, 1))
                )
        );
    }
}