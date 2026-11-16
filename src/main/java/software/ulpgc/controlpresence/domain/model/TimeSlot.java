
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalTime;
import java.util.Objects;

/**
 * Represents a time interval within a working schedule.
 *
 * An interval may cross midnight, allowing the representation
 * of overnight shifts.
 *
 * @param startTime the beginning of the time interval
 * @param endTime the end of the time interval
 */
public record TimeSlot(
        LocalTime startTime,
        LocalTime endTime
) {

    /**
     * Validates the time interval.
     *
     * @throws NullPointerException if either time is null
     * @throws IllegalArgumentException if both times are equal
     */
    public TimeSlot {
        Objects.requireNonNull(startTime, "Start time cannot be null");
        Objects.requireNonNull(endTime, "End time cannot be null");

        if (startTime.equals(endTime)) {
            throw new IllegalArgumentException(
                    "Start time and end time cannot be equal"
            );
        }
    }
}
