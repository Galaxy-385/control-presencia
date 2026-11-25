
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
import java.time.Duration;
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
        saveAttendanceRecord(
                workingDate.atTime(9, 0),
                AttendanceEventType.CLOCK_IN
        );

        saveAttendanceRecord(
                workingDate.atTime(17, 0),
                AttendanceEventType.CLOCK_OUT
        );

        DailyAttendanceSummary summary =
                service.consult(person.id(), workingDate);

        assertEquals(workingDate, summary.date());
        assertEquals(person.id(), summary.personId());
        assertEquals(Duration.ofHours(8), summary.scheduledTime());
        assertEquals(Duration.ofHours(8), summary.workedTime());
        assertEquals(Duration.ZERO, summary.lateness());
        assertEquals(Duration.ZERO, summary.earlyDeparture());
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
        assertEquals(Duration.ZERO, summary.scheduledTime());
        assertEquals(Duration.ZERO, summary.workedTime());
        assertFalse(summary.absent());
        assertFalse(summary.pendingClockOut());
    }

    @Test
    void shouldIncludeOvernightClockOutInTheDayTheShiftStarts() {
        configureOvernightSchedule();

        saveAttendanceRecord(
                workingDate.atTime(22, 0),
                AttendanceEventType.CLOCK_IN
        );

        saveAttendanceRecord(
                workingDate.plusDays(1).atTime(6, 0),
                AttendanceEventType.CLOCK_OUT
        );

        DailyAttendanceSummary summary =
                service.consult(person.id(), workingDate);

        assertEquals(workingDate, summary.date());
        assertEquals(Duration.ofHours(8), summary.scheduledTime());
        assertEquals(Duration.ofHours(8), summary.workedTime());
        assertFalse(summary.absent());
        assertFalse(summary.pendingClockOut());
    }

    @Test
    void shouldNotCountPreviousDayClockOutAgainInNextDailySummary() {
        configureOvernightSchedule();

        saveAttendanceRecord(
                workingDate.atTime(22, 0),
                AttendanceEventType.CLOCK_IN
        );

        saveAttendanceRecord(
                workingDate.plusDays(1).atTime(6, 0),
                AttendanceEventType.CLOCK_OUT
        );

        saveAttendanceRecord(
                workingDate.plusDays(1).atTime(22, 0),
                AttendanceEventType.CLOCK_IN
        );

        saveAttendanceRecord(
                workingDate.plusDays(2).atTime(6, 0),
                AttendanceEventType.CLOCK_OUT
        );

        DailyAttendanceSummary summary =
                service.consult(person.id(), workingDate.plusDays(1));

        assertEquals(workingDate.plusDays(1), summary.date());
        assertEquals(Duration.ofHours(8), summary.scheduledTime());
        assertEquals(Duration.ofHours(8), summary.workedTime());
        assertFalse(summary.absent());
        assertFalse(summary.pendingClockOut());
    }

    private void configureOvernightSchedule() {
        ContinuousSchedule overnightSchedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Overnight Schedule",
                new TimeSlot(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                ),
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY,
                        DayOfWeek.SATURDAY,
                        DayOfWeek.SUNDAY
                )
        );

        assignmentRepository.assignments.clear();

        ScheduleAssignment overnightAssignment = new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                overnightSchedule,
                workingDate.minusMonths(1),
                Optional.empty()
        );

        assignmentRepository.save(overnightAssignment);
    }

    private void saveAttendanceRecord(
            LocalDateTime dateTime,
            AttendanceEventType eventType
    ) {
        attendanceRecordRepository.save(new AttendanceRecord(
                UUID.randomUUID(),
                person,
                dateTime,
                eventType,
                "TEST"
        ));
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
