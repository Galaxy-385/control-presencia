
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.OnCallDuty;
import software.ulpgc.controlpresence.ports.in.scheduling.CreateOnCallDutyUseCase;
import software.ulpgc.controlpresence.ports.out.OnCallDutyRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.util.Objects;

/**
 * Implements the use case for creating on-call duties.
 */
public class CreateOnCallDutyService implements CreateOnCallDutyUseCase {

    private final PersonRepository personRepository;
    private final OnCallDutyRepository onCallDutyRepository;

    /**
     * Creates the service with its required repositories.
     *
     * @param personRepository the repository used to retrieve people
     * @param onCallDutyRepository the repository used to manage on-call duties
     */
    public CreateOnCallDutyService(
            PersonRepository personRepository,
            OnCallDutyRepository onCallDutyRepository
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.onCallDutyRepository = Objects.requireNonNull(
                onCallDutyRepository,
                "On-call duty repository cannot be null"
        );
    }

    /**
     * Creates and stores an on-call duty.
     *
     * @param duty the on-call duty to create
     * @return the created on-call duty
     */
    @Override
    public OnCallDuty create(OnCallDuty duty) {
        Objects.requireNonNull(duty, "On-call duty cannot be null");

        if (personRepository.findById(duty.person().id()).isEmpty()) {
            throw new IllegalArgumentException("Person does not exist");
        }

        if (onCallDutyRepository.findById(duty.id()).isPresent()) {
            throw new IllegalArgumentException("On-call duty ID already exists");
        }

        boolean overlappingDuty = onCallDutyRepository
                .findByPersonId(duty.person().id())
                .stream()
                .anyMatch(existing -> overlaps(existing, duty));

        if (overlappingDuty) {
            throw new IllegalArgumentException(
                    "The person already has an on-call duty during this period"
            );
        }

        return onCallDutyRepository.save(duty);
    }

    private boolean overlaps(OnCallDuty first, OnCallDuty second) {
        return first.startDateTime().isBefore(second.endDateTime())
                && second.startDateTime().isBefore(first.endDateTime());
    }
}
