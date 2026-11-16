
package software.ulpgc.controlpresence.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a working schedule organized into different shifts.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param shifts the shifts included in the schedule
 */
public record ShiftSchedule(
        UUID id,
        String name,
        List<Shift> shifts
) implements Schedule {

    /**
     * Validates the shift schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     * or the schedule contains no shifts
     */
    public ShiftSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(shifts, "Shifts cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }

        if (shifts.isEmpty()) {
            throw new IllegalArgumentException(
                    "A shift schedule must contain at least one shift"
            );
        }

        shifts = List.copyOf(shifts);
    }
}
