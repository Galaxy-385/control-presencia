
package software.ulpgc.controlpresence.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a split working schedule consisting of
 * multiple time intervals separated by breaks.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param timeSlots the working time intervals
 */
public record SplitSchedule(
        UUID id,
        String name,
        List<TimeSlot> timeSlots
) implements Schedule {

    /**
     * Validates the split schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     * or the schedule has no time intervals
     */
    public SplitSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(timeSlots, "Time slots cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }

        if (timeSlots.isEmpty()) {
            throw new IllegalArgumentException(
                    "A split schedule must contain at least one time slot"
            );
        }

        timeSlots = List.copyOf(timeSlots);
    }
}
