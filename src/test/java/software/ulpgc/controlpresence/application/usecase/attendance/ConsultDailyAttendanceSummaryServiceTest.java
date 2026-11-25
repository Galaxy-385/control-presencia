
package software.ulpgc.controlpresence.application.usecase.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;
import software.ulpgc.controlpresence.domain.service.DailyAttendanceCalculator;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.domain.service.ScheduleIntervalCalculator;
import software.ulpgc.controlpresence.ports.out.AttendanceRecordRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsultDailyAttendanceSummaryServiceTest {

    private FakePersonRepository personRepository;
    private FakeScheduleAssignmentRepository assignmentRepository;
    private FakeAttendanceRecordRepository attendanceRecordRepository;

    private ConsultDailyAttendanceSummaryService service;

    private Person person;
    private ContinuousSchedule schedule;
    private ScheduleAssignment assignment;

    private final LocalDate workingDate = LocalDate.of(2026, 11, 23);

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        assignmentRepository = new FakeScheduleAssignmentRepository();
        attendanceRecordRepository = new FakeAttendanceRecordRepository();

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        schedule = new ContinuousSchedule(
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

        assignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                workingDate.minusMonths(1),
                Optional.empty()
        );

        personRepository.save(person);
        assignmentRepository.save(assignment);

        service = new ConsultDailyAttendanceSummaryService(
                personRepository,
                assignmentRepository,
                attendanceRecordRepository,
                new ScheduleAssignmentService(),
                new ScheduleIntervalCalculator(),
                new DailyAttendanceCalculator()
        );
    }

    @Test
    void shouldCalculateDailyAttendanceSummary() {
        attendanceRecordRepository.save(new AttendanceRecord(
                UUID.randomUUID(),
                person,
                workingDate.atTime(9, 0),
                AttendanceEventType.CLOCK_IN,
                "TEST"
        ));

        attendanceRecordRepository.save(new AttendanceRecord(
                UUID.randomUUID(),
                person,
                workingDate.atTime(17, 0),
                AttendanceEventType.CLOCK_OUT,
                "TEST"
        ));

        DailyAttendanceSummary summary =
                service.consult(person.id(), workingDate);

        assertEquals(workingDate, summary.date());
        assertEquals(person.id(), summary.personId());
        assertEquals(java.time.Duration.ofHours(8), summary.scheduledTime());
        assertEquals(java.time.Duration.ofHours(8), summary.workedTime());
        assertEquals(java.time.Duration.ZERO, summary.lateness());
        assertEquals(java.time.Duration.ZERO, summary.earlyDeparture());
        assertFalse(summary.absent());
        assertFalse(summary.pendingClockOut());
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.consult(null, workingDate)
        );
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(
                NullPointerException.class,
                () -> service.consult(person.id(), null)
        );
    }

    @Test
    void shouldRejectUnknownPerson() {
        UUID unknownPersonId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consult(unknownPersonId, workingDate)
        );
    }

    @Test
    void shouldRejectPersonWithoutActiveAssignment() {
        assignmentRepository.assignments.clear();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.consult(person.id(), workingDate)
        );
    }

    @Test
    void shouldRejectMultipleActiveAssignments() {
        ScheduleAssignment secondAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                workingDate.minusDays(10),
                Optional.empty()
        );

        assignmentRepository.save(secondAssignment);

        assertThrows(
                IllegalStateException.class,
                () -> service.consult(person.id(), workingDate)
        );
    }

    @Test
    void shouldCalculateSummaryOnNonWorkingDay() {
        LocalDate saturday = LocalDate.of(2026, 11, 28);

        DailyAttendanceSummary summary =
                service.consult(person.id(), saturday);

        assertEquals(saturday, summary.date());
        assertEquals(java.time.Duration.ZERO, summary.scheduledTime());
        assertEquals(java.time.Duration.ZERO, summary.workedTime());
        assertFalse(summary.absent());
        assertFalse(summary.pendingClockOut());
    }

    private static class FakePersonRepository
            implements PersonRepository {

        private final Map<UUID, Person> people = new HashMap<>();

        void save(Person person) {
            people.put(person.id(), person);
        }

        @Override
        public Optional<Person> findById(UUID id) {
            return Optional.ofNullable(people.get(id));
        }
    }

    private static class FakeScheduleAssignmentRepository
            implements ScheduleAssignmentRepository {

        private final List<ScheduleAssignment> assignments =
                new ArrayList<>();

        @Override
        public ScheduleAssignment save(ScheduleAssignment assignment) {
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

    private static class FakeAttendanceRecordRepository
            implements AttendanceRecordRepository {

        private final List<AttendanceRecord> records =
                new ArrayList<>();

        @Override
        public AttendanceRecord save(AttendanceRecord record) {
            records.add(record);
            return record;
        }

        @Override
        public List<AttendanceRecord> findByPersonIdOrderByDateTime(
                UUID personId
        ) {
            return records.stream()
                    .filter(record ->
                            record.person().id().equals(personId)
                    )
                    .sorted(Comparator.comparing(
                            AttendanceRecord::dateTime
                    ))
                    .toList();
        }

        @Override
        public List<AttendanceRecord>
        findByPersonIdAndDateTimeBetweenOrderByDateTime(
                UUID personId,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime
        ) {
            return records.stream()
                    .filter(record ->
                            record.person().id().equals(personId)
                    )
                    .filter(record ->
                            !record.dateTime().isBefore(startDateTime)
                                    && !record.dateTime().isAfter(endDateTime)
                    )
                    .sorted(Comparator.comparing(
                            AttendanceRecord::dateTime
                    ))
                    .toList();
        }
    }
}
