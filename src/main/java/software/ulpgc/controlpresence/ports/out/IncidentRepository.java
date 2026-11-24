
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.Incident;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Defines the persistence operations for attendance incidents.
 */
public interface IncidentRepository {

    /**
     * Saves an attendance incident.
     *
     * @param incident the incident to save
     * @return the saved incident
     */
    Incident save(Incident incident);

    /**
     * Finds all incidents belonging to a person.
     *
     * @param personId the person's identifier
     * @return the person's incidents ordered by date
     */
    List<Incident> findByPersonIdOrderByDate(UUID personId);

    /**
     * Finds all incidents belonging to a person within a date range.
     *
     * Both dates are inclusive.
     *
     * @param personId the person's identifier
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the matching incidents ordered by date
     */
    List<Incident> findByPersonIdAndDateBetweenOrderByDate(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    );
}
