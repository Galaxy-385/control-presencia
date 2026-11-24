
package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.Incident;
import software.ulpgc.controlpresence.domain.model.IncidentType;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines the use case for registering attendance incidents.
 */
public interface RegisterIncidentUseCase {

    /**
     * Registers an incident for an existing person.
     *
     * @param personId the identifier of the person
     * @param date the date of the incident
     * @param type the type of incident
     * @param description the incident description
     * @return the registered incident
     */
    Incident register(
            UUID personId,
            LocalDate date,
            IncidentType type,
            String description
    );
}
