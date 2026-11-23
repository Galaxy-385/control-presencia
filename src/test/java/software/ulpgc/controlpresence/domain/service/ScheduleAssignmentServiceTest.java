
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleAssignmentServiceTest {

    private ScheduleAssignmentService service;
    private ScheduleAssignment assignment;
    private LocalDate startDate;
    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        service = new ScheduleAssignmentService();

        Person person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Morning Schedule",
                new TimeSlot(
                        LocalTime.of(8, 0),
                        LocalTime.of(16, 0)
                )
        );

        startDate = LocalDate.of(2026, 1, 1);
        endDate = LocalDate.of(2026, 12, 31);

        assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                startDate,
                Optional.of(endDate)
        );
    }

    @Test
    void shouldBeActiveOnStartDate() {
        assertTrue(service.isActiveOn(assignment, startDate));
    }

    @Test
    void shouldBeActiveOnEndDate() {
        assertTrue(service.isActiveOn(assignment, endDate));
    }

    @Test
    void shouldBeActiveBetweenStartAndEndDates() {
        assertTrue(
                service.isActiveOn(
                        assignment,
                        LocalDate.of(2026, 6, 15)
                )
        );
    }

    @Test
    void shouldNotBeActiveBeforeStartDate() {
        assertFalse(
                service.isActiveOn(
                        assignment,
                        startDate.minusDays(1)
                )
        );
    }

    @Test
    void shouldNotBeActiveAfterEndDate() {
        assertFalse(
                service.isActiveOn(
                        assignment,
                        endDate.plusDays(1)
                )
        );
    }

    @Test
    void shouldRemainActiveWithoutEndDate() {
        ScheduleAssignment openAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                assignment.person(),
                assignment.schedule(),
                startDate,
                Optional.empty()
        );

        assertTrue(
                service.isActiveOn(
                        openAssignment,
                        LocalDate.of(2030, 1, 1)
                )
        );
    }

    @Test
    void shouldNotBeActiveBeforeStartDateWithoutEndDate() {
        ScheduleAssignment openAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                assignment.person(),
                assignment.schedule(),
                startDate,
                Optional.empty()
        );

        assertFalse(
                service.isActiveOn(
                        openAssignment,
                        startDate.minusDays(1)
                )
        );
    }

    @Test
    void shouldRejectNullAssignment() {
        assertThrows(
                NullPointerException.class,
                () -> service.isActiveOn(null, startDate)
        );
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(
                NullPointerException.class,
                () -> service.isActiveOn(assignment, null)
        );
    }
}
