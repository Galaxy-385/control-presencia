
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class DailyAttendanceCalculatorTest {

    private DailyAttendanceCalculator calculator;
    private Person person;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        calculator = new DailyAttendanceCalculator();
        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "Test Employee"
        );
        date = LocalDate.of(2026, 11, 25);
    }

    @Test
    void shouldCalculateScheduledTime() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of()
        );

        assertEquals(Duration.ofHours(8), summary.scheduledTime());
    }

    @Test
    void shouldCalculateWorkedTime() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 0, AttendanceEventType.CLOCK_IN),
                        record(17, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofHours(8), summary.workedTime());
    }

    @Test
    void shouldCalculateLateness() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 30, AttendanceEventType.CLOCK_IN),
                        record(17, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofMinutes(30), summary.lateness());
    }

    @Test
    void shouldCalculateEarlyDeparture() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 0, AttendanceEventType.CLOCK_IN),
                        record(16, 30, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(
                Duration.ofMinutes(30),
                summary.earlyDeparture()
        );
    }

    @Test
    void shouldCalculateWorkedTimeForSplitSchedule() {
        DailyAttendanceSummary summary = calculate(
                List.of(
                        interval(9, 0, 13, 0),
                        interval(15, 0, 19, 0)
                ),
                List.of(
                        record(9, 0, AttendanceEventType.CLOCK_IN),
                        record(13, 0, AttendanceEventType.CLOCK_OUT),
                        record(15, 0, AttendanceEventType.CLOCK_IN),
                        record(19, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofHours(8), summary.scheduledTime());
        assertEquals(Duration.ofHours(8), summary.workedTime());
    }

    @Test
    void shouldCalculateLatenessForSplitSchedule() {
        DailyAttendanceSummary summary = calculate(
                List.of(
                        interval(9, 0, 13, 0),
                        interval(15, 0, 19, 0)
                ),
                List.of(
                        record(9, 15, AttendanceEventType.CLOCK_IN),
                        record(13, 0, AttendanceEventType.CLOCK_OUT),
                        record(15, 0, AttendanceEventType.CLOCK_IN),
                        record(19, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofMinutes(15), summary.lateness());
    }

    @Test
    void shouldDetectCompleteAbsence() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of()
        );

        assertTrue(summary.absent());
        assertEquals(Duration.ZERO, summary.workedTime());
    }

    @Test
    void shouldNotDetectAbsenceWhenAttendanceExists() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 0, AttendanceEventType.CLOCK_IN),
                        record(17, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertFalse(summary.absent());
    }

    @Test
    void shouldDetectPendingClockOut() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 0, AttendanceEventType.CLOCK_IN)
                )
        );

        assertTrue(summary.pendingClockOut());
        assertEquals(Duration.ZERO, summary.workedTime());
        assertFalse(summary.absent());
    }

    @Test
    void shouldNotCountPendingClockOutAsWorkedTime() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(9, 30, AttendanceEventType.CLOCK_IN)
                )
        );

        assertEquals(Duration.ZERO, summary.workedTime());
        assertEquals(Duration.ZERO, summary.lateness());
    }

    @Test
    void shouldCalculateOnlyTimeWithinScheduledInterval() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(8, 0, AttendanceEventType.CLOCK_IN),
                        record(18, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofHours(8), summary.workedTime());
    }

    @Test
    void shouldCalculateOverlapWithPartialAttendance() {
        DailyAttendanceSummary summary = calculate(
                List.of(interval(9, 0, 17, 0)),
                List.of(
                        record(10, 0, AttendanceEventType.CLOCK_IN),
                        record(16, 0, AttendanceEventType.CLOCK_OUT)
                )
        );

        assertEquals(Duration.ofHours(6), summary.workedTime());
        assertEquals(Duration.ofHours(1), summary.lateness());
        assertEquals(Duration.ofHours(1), summary.earlyDeparture());
    }

    @Test
    void shouldReturnZeroScheduledTimeForNonWorkingDay() {
        DailyAttendanceSummary summary = calculate(
                List.of(),
                List.of()
        );

        assertEquals(Duration.ZERO, summary.scheduledTime());
        assertFalse(summary.absent());
    }

    @Test
    void shouldRejectRecordsFromDifferentPeople() {
        Person anotherPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Another Employee"
        );

        List<AttendanceRecord> records = List.of(
                record(9, 0, AttendanceEventType.CLOCK_IN),
                new AttendanceRecord(
                        UUID.randomUUID(),
                        anotherPerson,
                        LocalDateTime.of(
                                date,
                                java.time.LocalTime.of(17, 0)
                        ),
                        AttendanceEventType.CLOCK_OUT,
                        "TEST"
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculate(
                        List.of(interval(9, 0, 17, 0)),
                        records
                )
        );
    }

    @Test
    void shouldRejectInvalidEventSequence() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculate(
                        List.of(interval(9, 0, 17, 0)),
                        List.of(
                                record(9, 0, AttendanceEventType.CLOCK_IN),
                                record(10, 0, AttendanceEventType.CLOCK_IN)
                        )
                )
        );
    }

    @Test
    void shouldRejectClockOutAsFirstEvent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> calculate(
                        List.of(interval(9, 0, 17, 0)),
                        List.of(
                                record(17, 0, AttendanceEventType.CLOCK_OUT)
                        )
                )
        );
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        null,
                        date,
                        List.of(),
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        person.id(),
                        null,
                        List.of(),
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullExpectedIntervals() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        person.id(),
                        date,
                        null,
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullAttendanceRecords() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(
                        person.id(),
                        date,
                        List.of(),
                        null
                )
        );
    }

    private DailyAttendanceSummary calculate(
            List<WorkingInterval> intervals,
            List<AttendanceRecord> records
    ) {
        return calculator.calculate(
                person.id(),
                date,
                intervals,
                records
        );
    }

    private WorkingInterval interval(
            int startHour,
            int startMinute,
            int endHour,
            int endMinute
    ) {
        LocalDateTime start = date.atTime(startHour, startMinute);
        LocalDateTime end = date.atTime(endHour, endMinute);

        return new WorkingInterval(start, end);
    }

    private AttendanceRecord record(
            int hour,
            int minute,
            AttendanceEventType eventType
    ) {
        return new AttendanceRecord(
                UUID.randomUUID(),
                person,
                date.atTime(hour, minute),
                eventType,
                "TEST"
        );
    }
}
