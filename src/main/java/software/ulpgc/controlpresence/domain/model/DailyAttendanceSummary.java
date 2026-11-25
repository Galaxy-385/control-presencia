
package software.ulpgc.controlpresence.domain.model;

import java.time.Duration;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the daily attendance summary of a person.
 *
 * @param date the date being summarized
 * @param personId the identifier of the person
 * @param scheduledTime the total expected working time
 * @param workedTime the working time completed within scheduled intervals
 * @param lateness the total time of late arrivals
 * @param earlyDeparture the total time of early departures
 * @param absent whether the person has no attendance during the scheduled day
 * @param pendingClockOut whether there is an unclosed clock-in event
 */
public record DailyAttendanceSummary(
        LocalDate date,
        UUID personId,
        Duration scheduledTime,
        Duration workedTime,
        Duration lateness,
        Duration earlyDeparture,
        boolean absent,
        boolean pendingClockOut
) {

    /**
     * Validates the daily attendance summary.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if any duration is negative
     */
    public DailyAttendanceSummary {
        Objects.requireNonNull(date, "Date cannot be null");
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(scheduledTime, "Scheduled time cannot be null");
        Objects.requireNonNull(workedTime, "Worked time cannot be null");
        Objects.requireNonNull(lateness, "Lateness cannot be null");
        Objects.requireNonNull(earlyDeparture, "Early departure cannot be null");

        if (scheduledTime.isNegative()
                || workedTime.isNegative()
                || lateness.isNegative()
                || earlyDeparture.isNegative()) {
            throw new IllegalArgumentException(
                    "Attendance durations cannot be negative"
            );
        }
    }
}
