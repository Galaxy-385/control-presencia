
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateScheduleAssignmentServiceTest {

    private FakePersonRepository personRepository;
    private FakeScheduleRepository scheduleRepository;
    private FakeScheduleAssignmentRepository assignmentRepository;

    private CreateScheduleAssignmentService service;

    private Person person;
    private ContinuousSchedule schedule;

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        scheduleRepository = new FakeScheduleRepository();
        assignmentRepository = new FakeScheduleAssignmentRepository();

        service = new CreateScheduleAssignmentService(
                personRepository,
                scheduleRepository,
                assignmentRepository
        );

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Morning schedule",
                new TimeSlot(
                        LocalTime.of(8, 0),
                        LocalTime.of(15, 0)
                )
        );

        personRepository.save(person);
        scheduleRepository.save(schedule);
    }

    @Test
    void shouldCreateScheduleAssignment() {
        ScheduleAssignment assignment = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.of(LocalDate.of(2026, 6, 30))
        );

        ScheduleAssignment result = service.create(assignment);

        assertEquals(assignment, result);
        assertEquals(
                assignment,
                assignmentRepository.findById(assignment.id()).orElseThrow()
        );
    }

    @Test
    void shouldRejectNullAssignment() {
        assertThrows(
                NullPointerException.class,
                () -> service.create(null)
        );
    }

    @Test
    void shouldRejectAssignmentWithUnknownPerson() {
        Person unknownPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Jane Smith"
        );

        ScheduleAssignment assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                unknownPerson,
                schedule,
                LocalDate.of(2026, 1, 1),
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(assignment)
        );
    }

    @Test
    void shouldRejectAssignmentWithUnknownSchedule() {
        ContinuousSchedule unknownSchedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Unknown schedule",
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
                )
        );

        ScheduleAssignment assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                unknownSchedule,
                LocalDate.of(2026, 1, 1),
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(assignment)
        );
    }

    @Test
    void shouldRejectDuplicateAssignmentId() {
        ScheduleAssignment assignment = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.empty()
        );

        assignmentRepository.save(assignment);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(assignment)
        );
    }

    @Test
    void shouldRejectOverlappingAssignments() {
        ScheduleAssignment existing = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.of(LocalDate.of(2026, 3, 31))
        );

        assignmentRepository.save(existing);

        ScheduleAssignment overlapping = createAssignment(
                LocalDate.of(2026, 3, 1),
                Optional.of(LocalDate.of(2026, 5, 31))
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(overlapping)
        );
    }

    @Test
    void shouldRejectAssignmentsStartingOnExistingEndDate() {
        ScheduleAssignment existing = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.of(LocalDate.of(2026, 3, 31))
        );

        assignmentRepository.save(existing);

        ScheduleAssignment overlapping = createAssignment(
                LocalDate.of(2026, 3, 31),
                Optional.empty()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(overlapping)
        );
    }

    @Test
    void shouldAllowNonOverlappingAssignments() {
        ScheduleAssignment existing = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.of(LocalDate.of(2026, 3, 31))
        );

        assignmentRepository.save(existing);

        ScheduleAssignment next = createAssignment(
                LocalDate.of(2026, 4, 1),
                Optional.empty()
        );

        ScheduleAssignment result = service.create(next);

        assertEquals(next, result);
    }

    @Test
    void shouldAllowAssignmentWithoutEndDate() {
        ScheduleAssignment assignment = createAssignment(
                LocalDate.of(2026, 1, 1),
                Optional.empty()
        );

        ScheduleAssignment result = service.create(assignment);

        assertEquals(assignment, result);
    }

    @Test
    void shouldRejectNullPersonRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateScheduleAssignmentService(
                        null,
                        scheduleRepository,
                        assignmentRepository
                )
        );
    }

    @Test
    void shouldRejectNullScheduleRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateScheduleAssignmentService(
                        personRepository,
                        null,
                        assignmentRepository
                )
        );
    }

    @Test
    void shouldRejectNullAssignmentRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateScheduleAssignmentService(
                        personRepository,
                        scheduleRepository,
                        null
                )
        );
    }

    private ScheduleAssignment createAssignment(
            LocalDate startDate,
            Optional<LocalDate> endDate
    ) {
        return new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                startDate,
                endDate
        );
    }

    private static class FakePersonRepository implements PersonRepository {

        private final Map<UUID, Person> people = new HashMap<>();

        void save(Person person) {
            people.put(person.id(), person);
        }

        @Override
        public Optional<Person> findById(UUID id) {
            return Optional.ofNullable(people.get(id));
        }
    }

    private static class FakeScheduleRepository implements ScheduleRepository {

        private final Map<UUID, software.ulpgc.controlpresence.domain.model.Schedule>
                schedules = new HashMap<>();

        @Override
        public software.ulpgc.controlpresence.domain.model.Schedule save(
                software.ulpgc.controlpresence.domain.model.Schedule schedule
        ) {
            schedules.put(schedule.id(), schedule);
            return schedule;
        }

        @Override
        public Optional<software.ulpgc.controlpresence.domain.model.Schedule> findById(
                UUID id
        ) {
            return Optional.ofNullable(schedules.get(id));
        }

        @Override
        public List<software.ulpgc.controlpresence.domain.model.Schedule> findAll() {
            return List.copyOf(schedules.values());
        }
    }

    private static class FakeScheduleAssignmentRepository
            implements ScheduleAssignmentRepository {

        private final Map<UUID, ScheduleAssignment> assignments = new HashMap<>();

        @Override
        public ScheduleAssignment save(ScheduleAssignment assignment) {
            assignments.put(assignment.id(), assignment);
            return assignment;
        }

        @Override
        public Optional<ScheduleAssignment> findById(UUID id) {
            return Optional.ofNullable(assignments.get(id));
        }

        @Override
        public List<ScheduleAssignment> findByPersonId(UUID personId) {
            return assignments.values()
                    .stream()
                    .filter(assignment ->
                            assignment.person().id().equals(personId)
                    )
                    .toList();
        }
    }
}
