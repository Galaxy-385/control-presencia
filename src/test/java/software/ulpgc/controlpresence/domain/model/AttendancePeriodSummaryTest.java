
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttendancePeriodSummaryTest {

    private final UUID personId = UUID.randomUUID();

    private DailyAttendanceSummary createSummary(
            LocalDate date,
            Duration scheduled,
            Duration worked,
            Duration lateness,
            Duration earlyDeparture,
            boolean absent,
            boolean pendingClockOut
    ) {
        return new DailyAttendanceSummary(
                date,
                personId,
                scheduled,
                worked,
                lateness,
                earlyDeparture,
                absent,
                pendingClockOut
        );
    }

    @Test
    void shouldCreatePeriodSummary() {
        LocalDate start = LocalDate.of(2026, 11, 1);
        LocalDate end = LocalDate.of(2026, 11, 3);

        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                start,
                end,
                List.of()
        );

        assertEquals(personId, summary.personId());
        assertEquals(start, summary.startDate());
        assertEquals(end, summary.endDate());
        assertTrue(summary.dailySummaries().isEmpty());
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(NullPointerException.class, () ->
                new AttendancePeriodSummary(
                        null,
                        LocalDate.now(),
                        LocalDate.now(),
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullStartDate() {
        assertThrows(NullPointerException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        null,
                        LocalDate.now(),
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullEndDate() {
        assertThrows(NullPointerException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        LocalDate.now(),
                        null,
                        List.of()
                )
        );
    }

    @Test
    void shouldRejectNullDailySummaries() {
        assertThrows(NullPointerException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        LocalDate.now(),
                        LocalDate.now(),
                        null
                )
        );
    }

    @Test
    void shouldRejectEndDateBeforeStartDate() {
        assertThrows(IllegalArgumentException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        LocalDate.of(2026, 11, 5),
                        LocalDate.of(2026, 11, 1),
                        List.of()
                )
        );
    }

    @Test
    void shouldSortDailySummariesByDate() {
        LocalDate first = LocalDate.of(2026, 11, 1);
        LocalDate second = LocalDate.of(2026, 11, 2);

        DailyAttendanceSummary firstSummary = createSummary(
                first, Duration.ofHours(8), Duration.ofHours(8),
                Duration.ZERO, Duration.ZERO, false, false
        );

        DailyAttendanceSummary secondSummary = createSummary(
                second, Duration.ofHours(8), Duration.ofHours(7),
                Duration.ZERO, Duration.ZERO, false, false
        );

        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                first,
                second,
                List.of(secondSummary, firstSummary)
        );

        assertEquals(first, summary.dailySummaries().get(0).date());
        assertEquals(second, summary.dailySummaries().get(1).date());
    }

    @Test
    void shouldRejectSummaryFromAnotherPerson() {
        DailyAttendanceSummary otherPersonSummary =
                new DailyAttendanceSummary(
                        LocalDate.of(2026, 11, 1),
                        UUID.randomUUID(),
                        Duration.ofHours(8),
                        Duration.ofHours(8),
                        Duration.ZERO,
                        Duration.ZERO,
                        false,
                        false
                );

        assertThrows(IllegalArgumentException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 2),
                        List.of(otherPersonSummary)
                )
        );
    }

    @Test
    void shouldRejectSummaryOutsidePeriod() {
        DailyAttendanceSummary outsideSummary = createSummary(
                LocalDate.of(2026, 11, 3),
                Duration.ofHours(8),
                Duration.ofHours(8),
                Duration.ZERO,
                Duration.ZERO,
                false,
                false
        );

        assertThrows(IllegalArgumentException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 11, 2),
                        List.of(outsideSummary)
                )
        );
    }

    @Test
    void shouldRejectDuplicateDates() {
        LocalDate date = LocalDate.of(2026, 11, 1);

        DailyAttendanceSummary first = createSummary(
                date, Duration.ofHours(8), Duration.ofHours(8),
                Duration.ZERO, Duration.ZERO, false, false
        );

        DailyAttendanceSummary second = createSummary(
                date, Duration.ofHours(8), Duration.ofHours(7),
                Duration.ZERO, Duration.ZERO, false, false
        );

        assertThrows(IllegalArgumentException.class, () ->
                new AttendancePeriodSummary(
                        personId,
                        date,
                        date,
                        List.of(first, second)
                )
        );
    }

    @Test
    void shouldCalculateScheduledTime() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(7),
                                Duration.ofHours(7),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        )
                )
        );

        assertEquals(Duration.ofHours(15), summary.scheduledTime());
    }

    @Test
    void shouldCalculateWorkedTime() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ofHours(7),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(8),
                                Duration.ofHours(6),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        )
                )
        );

        assertEquals(Duration.ofHours(13), summary.workedTime());
    }

    @Test
    void shouldCalculateLateness() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ofHours(8),
                                Duration.ofMinutes(10),
                                Duration.ZERO,
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(8),
                                Duration.ofHours(8),
                                Duration.ofMinutes(5),
                                Duration.ZERO,
                                false,
                                false
                        )
                )
        );

        assertEquals(Duration.ofMinutes(15), summary.lateness());
    }

    @Test
    void shouldCalculateEarlyDeparture() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 2),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ofHours(7),
                                Duration.ZERO,
                                Duration.ofMinutes(20),
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(8),
                                Duration.ofHours(7),
                                Duration.ZERO,
                                Duration.ofMinutes(10),
                                false,
                                false
                        )
                )
        );

        assertEquals(Duration.ofMinutes(30), summary.earlyDeparture());
    }

    @Test
    void shouldCountAbsenceDays() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.ZERO,
                                true,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(8),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 3),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.ZERO,
                                true,
                                false
                        )
                )
        );

        assertEquals(2, summary.absenceDays());
    }

    @Test
    void shouldCountPendingClockOutDays() {
        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 3),
                List.of(
                        createSummary(
                                LocalDate.of(2026, 11, 1),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                true
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 2),
                                Duration.ofHours(8),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                false
                        ),
                        createSummary(
                                LocalDate.of(2026, 11, 3),
                                Duration.ofHours(8),
                                Duration.ZERO,
                                Duration.ZERO,
                                Duration.ZERO,
                                false,
                                true
                        )
                )
        );

        assertEquals(2, summary.pendingClockOutDays());
    }

    @Test
    void shouldReturnImmutableDailySummaries() {
        DailyAttendanceSummary dailySummary = createSummary(
                LocalDate.of(2026, 11, 1),
                Duration.ofHours(8),
                Duration.ofHours(8),
                Duration.ZERO,
                Duration.ZERO,
                false,
                false
        );

        AttendancePeriodSummary summary = new AttendancePeriodSummary(
                personId,
                LocalDate.of(2026, 11, 1),
                LocalDate.of(2026, 11, 1),
                List.of(dailySummary)
        );

        assertThrows(UnsupportedOperationException.class, () ->
                summary.dailySummaries().add(dailySummary)
        );
    }
}
