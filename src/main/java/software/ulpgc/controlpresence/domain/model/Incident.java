
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an incident associated with a person's attendance.
 *
 * @param id the unique identifier of the incident
 * @param person the person associated with the incident
 * @param date the date when the incident occurred
 * @param type the type of incident
 * @param description a description of the incident
 */
public record Incident(
        UUID id,
        Person person,
        LocalDate date,
        IncidentType type,
        String description
) {

    /**
     * Validates the incident information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the description is blank
     */
    public Incident {
        Objects.requireNonNull(id, "Incident ID cannot be null");
        Objects.requireNonNull(person, "Person cannot be null");
        Objects.requireNonNull(date, "Incident date cannot be null");
        Objects.requireNonNull(type, "Incident type cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");

        if (description.isBlank()) {
            throw new IllegalArgumentException(
                    "Incident description cannot be blank"
            );
        }
    }
}