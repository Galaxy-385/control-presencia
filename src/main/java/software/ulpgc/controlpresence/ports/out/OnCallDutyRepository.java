
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.OnCallDuty;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the persistence operations for on-call duties.
 */
public interface OnCallDutyRepository {

    /**
     * Saves an on-call duty.
     *
     * @param duty the on-call duty to save
     * @return the saved on-call duty
     */
    OnCallDuty save(OnCallDuty duty);

    /**
     * Finds an on-call duty by its identifier.
     *
     * @param id the on-call duty identifier
     * @return an optional containing the duty if found
     */
    Optional<OnCallDuty> findById(UUID id);

    /**
     * Retrieves all on-call duties assigned to a person.
     *
     * @param personId the person's identifier
     * @return the person's on-call duties
     */
    List<OnCallDuty> findByPersonId(UUID personId);
}
