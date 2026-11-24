
package software.ulpgc.controlpresence.ports.in.scheduling;

import software.ulpgc.controlpresence.domain.model.Schedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the use case for consulting working schedules.
 */
public interface ConsultScheduleUseCase {

    /**
     * Finds a schedule by its identifier.
     *
     * @param id the schedule identifier
     * @return an optional containing the schedule if found
     */
    Optional<Schedule> findById(UUID id);

    /**
     * Retrieves all registered schedules.
     *
     * @return all registered schedules
     */
    List<Schedule> findAll();
}
