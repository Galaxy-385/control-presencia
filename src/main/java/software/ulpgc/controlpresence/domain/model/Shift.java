
package software.ulpgc.controlpresence.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an individual working shift.
 *
 * @param id the unique identifier of the shift
 * @param name the shift name
 * @param timeSlot the time interval assigned to the shift
 */
public record Shift(
        UUID id,
        String name,
        TimeSlot timeSlot
) {

    /**
     * Validates the shift information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     */
    public Shift {
        Objects.requireNonNull(id, "Shift ID cannot be null");
        Objects.requireNonNull(name, "Shift name cannot be null");
        Objects.requireNonNull(timeSlot, "Time slot cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Shift name cannot be blank"
            );
        }
    }
}
