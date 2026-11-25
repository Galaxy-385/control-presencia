
package software.ulpgc.controlpresence.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the attendance summary of a person over a date range.
 */
public record AttendancePeriodSummary(
        UUID personId,
        LocalDate startDate,
        LocalDate endDate,
        List<DailyAttendanceSummary> dailySummaries
) {

    /**
     * Validates and initializes the attendance period summary.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @param dailySummaries the daily attendance summaries
     */
    public AttendancePeriodSummary {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");
        Objects.requireNonNull(
                dailySummaries,
                "Daily summaries cannot be null"
        );

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        dailySummaries = dailySummaries.stream()
                .sorted(java.util.Comparator.comparing(
                        DailyAttendanceSummary::date
                ))
                .toList();

        if (dailySummaries.stream().anyMatch(summary ->
                !summary.personId().equals(personId)
                        || summary.date().isBefore(startDate)
                        || summary.date().isAfter(endDate)
        )) {
            throw new IllegalArgumentException(
                    "Daily summaries must belong to the person "
                            + "and fall within the specified period"
            );
        }

        if (dailySummaries.stream()
                .map(DailyAttendanceSummary::date)
                .distinct()
                .count() != dailySummaries.size()) {
            throw new IllegalArgumentException(
                    "Daily summaries cannot contain duplicate dates"
            );
        }
    }

    /**
     * Calculates the total scheduled time for the period.
     *
     * @return the total scheduled duration
     */
    public Duration scheduledTime() {
        return dailySummaries.stream()
                .map(DailyAttendanceSummary::scheduledTime)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Calculates the total worked time for the period.
     *
     * @return the total worked duration
     */
    public Duration workedTime() {
        return dailySummaries.stream()
                .map(DailyAttendanceSummary::workedTime)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Calculates the total lateness for the period.
     *
     * @return the total lateness duration
     */
    public Duration lateness() {
        return dailySummaries.stream()
                .map(DailyAttendanceSummary::lateness)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Calculates the total early departure time for the period.
     *
     * @return the total early departure duration
     */
    public Duration earlyDeparture() {
        return dailySummaries.stream()
                .map(DailyAttendanceSummary::earlyDeparture)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Counts the days with an absence.
     *
     * @return the number of absent days
     */
    public long absenceDays() {
        return dailySummaries.stream()
                .filter(DailyAttendanceSummary::absent)
                .count();
    }

    /**
     * Counts the days with a pending clock-out.
     *
     * @return the number of days with a pending clock-out
     */
    public long pendingClockOutDays() {
        return dailySummaries.stream()
                .filter(DailyAttendanceSummary::pendingClockOut)
                .count();
    }
}
