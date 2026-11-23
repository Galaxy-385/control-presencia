
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;

import java.time.Duration;
import java.util.Objects;

/**
 * Calculates the working time between attendance events.
 */
public class AttendanceCalculator {

    /**
     * Calculates the duration between a clock-in and a clock-out event.
     *
     * @param clockIn the employee's clock-in record
     * @param clockOut the employee's clock-out record
     * @return the duration worked between both events
     * @throws NullPointerException if either record is null
     * @throws IllegalArgumentException if the records have invalid event
     * types, belong to different people, or have an invalid time order
     */
    public Duration calculateWorkedTime(
            AttendanceRecord clockIn,
            AttendanceRecord clockOut
    ) {
        Objects.requireNonNull(clockIn, "Clock-in record cannot be null");
        Objects.requireNonNull(clockOut, "Clock-out record cannot be null");

        if (clockIn.eventType() != AttendanceEventType.CLOCK_IN) {
            throw new IllegalArgumentException(
                    "First record must be a clock-in event"
            );
        }

        if (clockOut.eventType() != AttendanceEventType.CLOCK_OUT) {
            throw new IllegalArgumentException(
                    "Second record must be a clock-out event"
            );
        }

        if (!clockIn.person().id().equals(clockOut.person().id())) {
            throw new IllegalArgumentException(
                    "Attendance records must belong to the same person"
            );
        }

        if (!clockOut.dateTime().isAfter(clockIn.dateTime())) {
            throw new IllegalArgumentException(
                    "Clock-out time must be after clock-in time"
            );
        }

        return Duration.between(
                clockIn.dateTime(),
                clockOut.dateTime()
        );
    }
}
