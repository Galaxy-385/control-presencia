
package software.ulpgc.controlpresence.application.usecase.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.domain.service.AttendanceEventValidator;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.ports.out.AttendanceRecordRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the RegisterAttendanceService.
 */
class RegisterAttendanceServiceTest {

    private FakePersonRepository personRepository;
    private FakeAttendanceRecordRepository attendanceRecordRepository;
    private FakeScheduleAssignmentRepository scheduleAssignmentRepository;
    private RegisterAttendanceService service;

    private Person person;
    private LocalDateTime dateTime;

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        attendanceRecordRepository = new FakeAttendanceRecordRepository();
        scheduleAssignmentRepository = new FakeScheduleAssignmentRepository();

        service = new RegisterAttendanceService(
                personRepository,
                attendanceRecordRepository,
                scheduleAssignmentRepository,
                new AttendanceEventValidator(),
                new ScheduleAssignmentService()
        );

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        dateTime = LocalDateTime.of(2026, 11, 24, 9, 0);

        personRepository.add(person);

        scheduleAssignmentRepository.add(
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        person,
                        new ContinuousSchedule(
                                UUID.randomUUID(),
                                "Morning schedule",
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
                        ),
                        dateTime.toLocalDate(),
                        Optional.empty()
                )
        );
    }

    @Test
    void shouldRegisterClockInWhenPersonHasActiveAssignment() {
        AttendanceRecord result = service.register(
                person.id(),
                AttendanceEventType.CLOCK_IN,
                dateTime,
                "WEB"
        );

        assertNotNull(result);
        assertEquals(person.id(), result.person().id());
        assertEquals(AttendanceEventType.CLOCK_IN, result.eventType());
        assertEquals(dateTime, result.dateTime());
        assertEquals("WEB", result.origin());
        assertEquals(1, attendanceRecordRepository.records.size());
    }

    @Test
    void shouldRegisterClockOutAfterClockIn() {
        attendanceRecordRepository.add(
                createRecord(
                        AttendanceEventType.CLOCK_IN,
                        dateTime
                )
        );

        AttendanceRecord result = service.register(
                person.id(),
                AttendanceEventType.CLOCK_OUT,
                dateTime.plusHours(8),
                "WEB"
        );

        assertEquals(AttendanceEventType.CLOCK_OUT, result.eventType());
        assertEquals(2, attendanceRecordRepository.records.size());
    }

    @Test
    void shouldGenerateUniqueIdentifierForNewRecord() {
        AttendanceRecord result = service.register(
                person.id(),
                AttendanceEventType.CLOCK_IN,
                dateTime,
                "WEB"
        );

        assertNotNull(result.id());
    }

    @Test
    void shouldNotRegisterEventWhenPersonDoesNotExist() {
        UUID unknownPersonId = UUID.randomUUID();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        unknownPersonId,
                        AttendanceEventType.CLOCK_IN,
                        dateTime,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    @Test
    void shouldNotRegisterEventWhenPersonHasNoActiveAssignment() {
        scheduleAssignmentRepository.assignments.clear();

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_IN,
                        dateTime,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    @Test
    void shouldNotRegisterEventWhenAssignmentHasExpired() {
        scheduleAssignmentRepository.assignments.clear();

        scheduleAssignmentRepository.add(
                new ScheduleAssignment(
                        UUID.randomUUID(),
                        person,
                        new ContinuousSchedule(
                                UUID.randomUUID(),
                                "Expired schedule",
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
                        ),
                        dateTime.toLocalDate().minusDays(10),
                        Optional.of(dateTime.toLocalDate().minusDays(1))
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_IN,
                        dateTime,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    @Test
    void shouldNotRegisterTwoConsecutiveClockIns() {
        attendanceRecordRepository.add(
                createRecord(
                        AttendanceEventType.CLOCK_IN,
                        dateTime.minusHours(1)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_IN,
                        dateTime,
                        "WEB"
                )
        );

        assertEquals(1, attendanceRecordRepository.records.size());
    }

    @Test
    void shouldNotRegisterClockOutAsFirstEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_OUT,
                        dateTime,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    @Test
    void shouldNotRegisterEventWithInvalidChronologicalOrder() {
        attendanceRecordRepository.add(
                createRecord(
                        AttendanceEventType.CLOCK_IN,
                        dateTime
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_OUT,
                        dateTime.minusMinutes(1),
                        "WEB"
                )
        );

        assertEquals(1, attendanceRecordRepository.records.size());
    }

    @Test
    void shouldNotRegisterEventWithNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.register(
                        null,
                        AttendanceEventType.CLOCK_IN,
                        dateTime,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    @Test
    void shouldNotRegisterEventWithNullDateTime() {
        assertThrows(
                NullPointerException.class,
                () -> service.register(
                        person.id(),
                        AttendanceEventType.CLOCK_IN,
                        null,
                        "WEB"
                )
        );

        assertTrue(attendanceRecordRepository.records.isEmpty());
    }

    private AttendanceRecord createRecord(
            AttendanceEventType eventType,
            LocalDateTime recordDateTime
    ) {
        return new AttendanceRecord(
                UUID.randomUUID(),
                person,
                recordDateTime,
                eventType,
                "WEB"
        );
    }

    private static class FakePersonRepository implements PersonRepository {

        private final List<Person> people = new ArrayList<>();

        void add(Person person) {
            people.add(person);
        }

        @Override
        public Optional<Person> findById(UUID id) {
            return people.stream()
                    .filter(person -> person.id().equals(id))
                    .findFirst();
        }
    }

    private static class FakeAttendanceRecordRepository
            implements AttendanceRecordRepository {

        private final List<AttendanceRecord> records = new ArrayList<>();

        void add(AttendanceRecord record) {
            records.add(record);
        }

        @Override
        public List<AttendanceRecord> findByPersonIdOrderByDateTime(UUID personId) {
            return records.stream()
                    .filter(record -> record.person().id().equals(personId))
                    .sorted((first, second) ->
                            first.dateTime().compareTo(second.dateTime()))
                    .toList();
        }

        @Override
        public List<AttendanceRecord> findByPersonIdAndDateTimeBetweenOrderByDateTime(
                UUID personId,
                LocalDateTime startDateTime,
                LocalDateTime endDateTime
        ) {
            return records.stream()
                    .filter(record -> record.person().id().equals(personId))
                    .filter(record -> !record.dateTime().isBefore(startDateTime))
                    .filter(record -> !record.dateTime().isAfter(endDateTime))
                    .sorted((first, second) ->
                            first.dateTime().compareTo(second.dateTime()))
                    .toList();
        }

        @Override
        public AttendanceRecord save(AttendanceRecord record) {
            records.add(record);
            return record;
        }
    }

    private static class FakeScheduleAssignmentRepository
            implements ScheduleAssignmentRepository {

        private final List<ScheduleAssignment> assignments = new ArrayList<>();

        void add(ScheduleAssignment assignment) {
            assignments.add(assignment);
        }

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
                            assignment.person().id().equals(personId))
                    .toList();
        }
    }
}
