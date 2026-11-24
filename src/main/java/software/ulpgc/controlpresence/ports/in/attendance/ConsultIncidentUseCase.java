
package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.Incident;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Defines the use case for consulting attendance incidents.
 */
public interface ConsultIncidentUseCase {

    /**
     * Retrieves the incidents of a person within a date range.
     *
     * Both dates are inclusive.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the incidents ordered by date
     */
    List<Incident> consult(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    );
}
