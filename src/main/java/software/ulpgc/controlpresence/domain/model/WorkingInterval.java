
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a concrete working interval with a start and end date and time.
 *
 * @param startDateTime the beginning of the working interval
 * @param endDateTime the end of the working interval
 */
public record WorkingInterval(
        LocalDateTime startDateTime,
        LocalDateTime endDateTime
) {

    /**
     * Validates the working interval.
     *
     * @throws NullPointerException if either date and time is null
     * @throws IllegalArgumentException if the end is not after the start
     */
    public WorkingInterval {
        Objects.requireNonNull(
                startDateTime,
                "Start date and time cannot be null"
        );
        Objects.requireNonNull(
                endDateTime,
                "End date and time cannot be null"
        );

        if (!endDateTime.isAfter(startDateTime)) {
            throw new IllegalArgumentException(
                    "End date and time must be after start date and time"
            );
        }
    }
}
