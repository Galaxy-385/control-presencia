
package software.ulpgc.controlpresence.ports.in.scheduling;

import software.ulpgc.controlpresence.domain.model.OnCallDuty;

/**
 * Defines the use case for creating on-call duties.
 */
public interface CreateOnCallDutyUseCase {

    /**
     * Creates and stores an on-call duty.
     *
     * @param duty the on-call duty to create
     * @return the created on-call duty
     */
    OnCallDuty create(OnCallDuty duty);
}
