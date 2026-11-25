
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.OnCallDuty;
import software.ulpgc.controlpresence.ports.in.scheduling.ConsultOnCallDutyUseCase;
import software.ulpgc.controlpresence.ports.out.OnCallDutyRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the use case for consulting on-call duties.
 */
public class ConsultOnCallDutyService implements ConsultOnCallDutyUseCase {

    private final OnCallDutyRepository onCallDutyRepository;

    /**
     * Creates the service with its required repository.
     *
     * @param onCallDutyRepository the repository used to retrieve on-call duties
     */
    public ConsultOnCallDutyService(
            OnCallDutyRepository onCallDutyRepository
    ) {
        this.onCallDutyRepository = Objects.requireNonNull(
                onCallDutyRepository,
                "On-call duty repository cannot be null"
        );
    }

    /**
     * Finds an on-call duty by its identifier.
     *
     * @param id the on-call duty identifier
     * @return an optional containing the duty if found
     */
    @Override
    public Optional<OnCallDuty> findById(UUID id) {
        Objects.requireNonNull(id, "On-call duty ID cannot be null");
        return onCallDutyRepository.findById(id);
    }

    /**
     * Retrieves all on-call duties assigned to a person.
     *
     * @param personId the person's identifier
     * @return the person's on-call duties
     */
    @Override
    public List<OnCallDuty> findByPersonId(UUID personId) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        return onCallDutyRepository.findByPersonId(personId);
    }
}
