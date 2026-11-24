
package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.Incident;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultIncidentUseCase;
import software.ulpgc.controlpresence.ports.out.IncidentRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for consulting attendance incidents.
 */
public class ConsultIncidentService implements ConsultIncidentUseCase {

    private final PersonRepository personRepository;
    private final IncidentRepository incidentRepository;

    /**
     * Creates the incident consultation service.
     *
     * @param personRepository repository used to find employees
     * @param incidentRepository repository used to retrieve incidents
     */
    public ConsultIncidentService(
            PersonRepository personRepository,
            IncidentRepository incidentRepository
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.incidentRepository = Objects.requireNonNull(
                incidentRepository,
                "Incident repository cannot be null"
        );
    }

    /**
     * Retrieves the incidents of an existing person within a date range.
     *
     * Both dates are inclusive.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the incidents ordered by date
     * @throws IllegalArgumentException if the person does not exist
     * or the date range is invalid
     */
    @Override
    public List<Incident> consult(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person not found: " + personId
                ));

        return incidentRepository
                .findByPersonIdAndDateBetweenOrderByDate(
                        personId,
                        startDate,
                        endDate
                );
    }
}
