
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an attendance event recorded by the system.
 *
 * @param id the unique identifier of the attendance record
 * @param person the person associated with the attendance event
 * @param dateTime the date and time when the event occurred
 * @param eventType the type of attendance event
 * @param origin the source from which the event was recorded
 */
public record AttendanceRecord(
        UUID id,
        Person person,
        LocalDateTime dateTime,
        AttendanceEventType eventType,
        String origin
) {

    /**
     * Validates the attendance record information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the origin is blank
     */
    public AttendanceRecord {
        Objects.requireNonNull(id, "Attendance record ID cannot be null");
        Objects.requireNonNull(person, "Person cannot be null");
        Objects.requireNonNull(dateTime, "Date and time cannot be null");
        Objects.requireNonNull(eventType, "Event type cannot be null");
        Objects.requireNonNull(origin, "Origin cannot be null");

        if (origin.isBlank()) {
            throw new IllegalArgumentException(
                    "Origin cannot be blank"
            );
        }
    }
}