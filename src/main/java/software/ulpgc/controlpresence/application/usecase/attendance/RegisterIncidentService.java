
package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.Incident;
import software.ulpgc.controlpresence.domain.model.IncidentType;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.ports.in.attendance.RegisterIncidentUseCase;
import software.ulpgc.controlpresence.ports.out.IncidentRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for registering attendance incidents.
 */
public class RegisterIncidentService implements RegisterIncidentUseCase {

    private final PersonRepository personRepository;
    private final IncidentRepository incidentRepository;

    /**
     * Creates the incident registration service.
     *
     * @param personRepository repository used to find employees
     * @param incidentRepository repository used to save incidents
     */
    public RegisterIncidentService(
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
     * Registers an incident for an existing person.
     *
     * @param personId the identifier of the person
     * @param date the date of the incident
     * @param type the type of incident
     * @param description the incident description
     * @return the saved incident
     * @throws IllegalArgumentException if the person does not exist
     */
    @Override
    public Incident register(
            UUID personId,
            LocalDate date,
            IncidentType type,
            String description
    ) {
        Objects.requireNonNull(personId, "Person ID cannot be null");

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person not found: " + personId
                ));

        Incident incident = new Incident(
                UUID.randomUUID(),
                person,
                date,
                type,
                description
        );

        return incidentRepository.save(incident);
    }
}
