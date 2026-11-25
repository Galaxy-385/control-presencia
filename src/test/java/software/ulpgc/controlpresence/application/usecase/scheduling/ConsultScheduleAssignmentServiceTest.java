
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsultScheduleAssignmentServiceTest {

    private FakeScheduleAssignmentRepository repository;
    private ConsultScheduleAssignmentService service;

    private Person person;
    private ScheduleAssignment firstAssignment;
    private ScheduleAssignment secondAssignment;

    @BeforeEach
    void setUp() {
        repository = new FakeScheduleAssignmentRepository();
        service = new ConsultScheduleAssignmentService(repository);

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Morning schedule",
                new TimeSlot(
                        LocalTime.of(8, 0),
                        LocalTime.of(15, 0)
                ),
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                )
        );

        firstAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                LocalDate.of(2026, 1, 1),
                Optional.of(LocalDate.of(2026, 3, 31))
        );

        secondAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                LocalDate.of(2026, 4, 1),
                Optional.empty()
        );

        repository.save(firstAssignment);
        repository.save(secondAssignment);
    }

    @Test
    void shouldFindAssignmentById() {
        Optional<ScheduleAssignment> result =
                service.findById(firstAssignment.id());

        assertTrue(result.isPresent());
        assertEquals(firstAssignment, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenAssignmentDoesNotExist() {
        Optional<ScheduleAssignment> result =
                service.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullAssignmentId() {
        assertThrows(
                NullPointerException.class,
                () -> service.findById(null)
        );
    }

    @Test
    void shouldFindAssignmentsByPersonId() {
        List<ScheduleAssignment> result =
                service.findByPersonId(person.id());

        assertEquals(2, result.size());
        assertTrue(result.contains(firstAssignment));
        assertTrue(result.contains(secondAssignment));
    }

    @Test
    void shouldReturnEmptyListWhenPersonHasNoAssignments() {
        List<ScheduleAssignment> result =
                service.findByPersonId(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.findByPersonId(null)
        );
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new ConsultScheduleAssignmentService(null)
        );
    }

    private static class FakeScheduleAssignmentRepository
            implements ScheduleAssignmentRepository {

        private final List<ScheduleAssignment> assignments = new ArrayList<>();

        @Override
        public ScheduleAssignment save(ScheduleAssignment assignment) {
            assignments.removeIf(existing ->
                    existing.id().equals(assignment.id())
            );
            assignments.add(assignment);
            return assignment;
        }

        @Override
        public Optional<ScheduleAssignment> findById(UUID id) {
            return assignments.stream()
                    .filter(assignment -> assignment.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<ScheduleAssignment> findByPersonId(UUID personId) {
            return assignments.stream()
                    .filter(assignment ->
                            assignment.person().id().equals(personId)
                    )
                    .toList();
        }
    }
}
