
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an on-call duty assigned to a person
 * during a specific period.
 *
 * @param id the unique identifier of the on-call duty
 * @param person the person assigned to the duty
 * @param startDateTime the beginning of the on-call period
 * @param endDateTime the end of the on-call period
 */
public record OnCallDuty(
        UUID id,
        Person person,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {

    /**
     * Validates the on-call duty information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the end date and time
     * is not after the start date and time
     */
    public OnCallDuty {
        Objects.requireNonNull(id, "On-call duty ID cannot be null");
        Objects.requireNonNull(person, "Person cannot be null");
        Objects.requireNonNull(startDateTime, "Start date and time cannot be null");
        Objects.requireNonNull(endDateTime, "End date and time cannot be null");

        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException(
                    "End date and time must be after start date and time"
            );
        }
    }
}
