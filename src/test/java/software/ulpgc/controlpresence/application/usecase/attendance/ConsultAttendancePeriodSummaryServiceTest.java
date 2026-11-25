
package software.ulpgc.controlpresence.application.usecase.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultDailyAttendanceSummaryUseCase;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ConsultAttendancePeriodSummaryServiceTest {

    private final UUID personId = UUID.randomUUID();

    private PersonRepository personRepository;
    private ScheduleAssignmentRepository assignmentRepository;
    private FakeDailySummaryUseCase dailySummaryUseCase;

    private ConsultAttendancePeriodSummaryService service;

    @BeforeEach
    void setUp() {
        Person person = new Person(
                personId,
                "EMP001",
                "John Smith"
        );

        personRepository = id ->
                id.equals(personId)
                        ? Optional.of(person)
                        : Optional.empty();

        assignmentRepository = new FakeScheduleAssignmentRepository();

        dailySummaryUseCase = new FakeDailySummaryUseCase();

        service = new ConsultAttendancePeriodSummaryService(
                personRepository,
                assignmentRepository,
                new ScheduleAssignmentService(),
                dailySummaryUseCase
        );
    }

    private ScheduleAssignment createAssignment(
            LocalDate startDate,
            LocalDate endDate
    ) {
        Person person = personRepository.findById(personId).orElseThrow();

        Schedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
                ),
                Set.of(
                        java.time.DayOfWeek.MONDAY,
                        java.time.DayOfWeek.TUESDAY,
                        java.time.DayOfWeek.WEDNESDAY,
                        java.time.DayOfWeek.THURSDAY,
                        java.time.DayOfWeek.FRIDAY
                )
        );

        return new ScheduleAssignment(
                UUID.randomUUID(),
                person,
                schedule,
                startDate,
                Optional.ofNullable(endDate)
        );
    }

    @Test
    void shouldConsultSummaryForEveryDateWithActiveAssignment() {
        LocalDate start = LocalDate.of(2026, 11, 23);
        LocalDate end = LocalDate.of(2026, 11, 25);

        ScheduleAssignment assignment = createAssignment(start, end);
        ((FakeScheduleAssignmentRepository) assignmentRepository)
                .assignments.add(assignment);

        dailySummaryUseCase.summaries.put(
                start,
                createDailySummary(start)
        );
        dailySummaryUseCase.summaries.put(
                start.plusDays(1),
                createDailySummary(start.plusDays(1))
        );
        dailySummaryUseCase.summaries.put(
                end,
                createDailySummary(end)
        );

        AttendancePeriodSummary result =
                service.consult(personId, start, end);

        assertEquals(personId, result.personId());
        assertEquals(start, result.startDate());
        assertEquals(end, result.endDate());
        assertEquals(3, result.dailySummaries().size());
        assertEquals(3, dailySummaryUseCase.consultedDates.size());
    }

    @Test
    void shouldIncludeStartAndEndDates() {
        LocalDate start = LocalDate.of(2026, 11, 23);
        LocalDate end = LocalDate.of(2026, 11, 24);

        ((FakeScheduleAssignmentRepository) assignmentRepository)
                .assignments.add(createAssignment(start, end));

        dailySummaryUseCase.summaries.put(start, createDailySummary(start));
        dailySummaryUseCase.summaries.put(end, createDailySummary(end));

        AttendancePeriodSummary result =
                service.consult(personId, start, end);

        assertEquals(start, result.dailySummaries().getFirst().date());
        assertEquals(end, result.dailySummaries().getLast().date());
    }

    @Test
    void shouldSkipDatesWithoutActiveAssignment() {
        LocalDate start = LocalDate.of(2026, 11, 23);
        LocalDate end = LocalDate.of(2026, 11, 25);

        ((FakeScheduleAssignmentRepository) assignmentRepository)
                .assignments.add(
                        createAssignment(start, start.plusDays(1))
                );

        dailySummaryUseCase.summaries.put(start, createDailySummary(start));
        dailySummaryUseCase.summaries.put(
                start.plusDays(1),
                createDailySummary(start.plusDays(1))
        );

        AttendancePeriodSummary result =
                service.consult(personId, start, end);

        assertEquals(2, result.dailySummaries().size());
        assertEquals(2, dailySummaryUseCase.consultedDates.size());
    }

    @Test
    void shouldReturnEmptySummaryWhenThereAreNoAssignments() {
        LocalDate start = LocalDate.of(2026, 11, 23);
        LocalDate end = LocalDate.of(2026, 11, 25);

        AttendancePeriodSummary result =
                service.consult(personId, start, end);

        assertTrue(result.dailySummaries().isEmpty());
        assertTrue(dailySummaryUseCase.consultedDates.isEmpty());
    }

    @Test
    void shouldRejectUnknownPerson() {
        UUID unknownPersonId = UUID.randomUUID();

        assertThrows(IllegalArgumentException.class, () ->
                service.consult(
                        unknownPersonId,
                        LocalDate.of(2026, 11, 23),
                        LocalDate.of(2026, 11, 25)
                )
        );
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                service.consult(
                        personId,
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 11, 23)
                )
        );
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(NullPointerException.class, () ->
                service.consult(
                        null,
                        LocalDate.now(),
                        LocalDate.now()
                )
        );
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThrows(NullPointerException.class, () ->
                service.consult(
                        personId,
                        null,
                        LocalDate.now()
                )
        );
    }

    @Test
    void shouldRejectNullEndDate() {
        assertThrows(NullPointerException.class, () ->
                service.consult(
                        personId,
                        LocalDate.now(),
                        null
                )
        );
    }

    @Test
    void shouldConsultSingleDayPeriod() {
        LocalDate date = LocalDate.of(2026, 11, 23);

        ((FakeScheduleAssignmentRepository) assignmentRepository)
                .assignments.add(createAssignment(date, date));

        dailySummaryUseCase.summaries.put(
                date,
                createDailySummary(date)
        );

        AttendancePeriodSummary result =
                service.consult(personId, date, date);

        assertEquals(1, result.dailySummaries().size());
        assertEquals(date, result.dailySummaries().getFirst().date());
    }

    private DailyAttendanceSummary createDailySummary(LocalDate date) {
        return new DailyAttendanceSummary(
                date,
                personId,
                Duration.ofHours(8),
                Duration.ofHours(8),
                Duration.ZERO,
                Duration.ZERO,
                false,
                false
        );
    }

    private static class FakeDailySummaryUseCase
            implements ConsultDailyAttendanceSummaryUseCase {

        private final Map<LocalDate, DailyAttendanceSummary> summaries =
                new HashMap<>();

        private final List<LocalDate> consultedDates = new ArrayList<>();

        @Override
        public DailyAttendanceSummary consult(UUID personId, LocalDate date) {
            consultedDates.add(date);

            DailyAttendanceSummary summary = summaries.get(date);

            if (summary == null) {
                throw new IllegalStateException(
                        "No daily summary configured for date: " + date
                );
            }

            return summary;
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
}
