package software.ulpgc.control_precense.domain.model;

import java.time.LocalTime;

/**
 * Represents a time interval within a working schedule.
 *
 * @param startTime the beginning of the time interval
 * @param endTime the end of the time interval
 */
public record TimeSlot(
        LocalTime startTime,
        LocalTime endTime
) {
}
