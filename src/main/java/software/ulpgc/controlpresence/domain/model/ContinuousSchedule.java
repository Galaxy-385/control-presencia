
package software.ulpgc.controlpresence.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a continuous working schedule consisting of
 * a single uninterrupted time interval.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param timeSlot the working time interval
 */
public record ContinuousSchedule(
        UUID id,
        String name,
        TimeSlot timeSlot
) implements Schedule {

    /**
     * Validates the continuous schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     */
    public ContinuousSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(timeSlot, "Time slot cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }
    }
}
