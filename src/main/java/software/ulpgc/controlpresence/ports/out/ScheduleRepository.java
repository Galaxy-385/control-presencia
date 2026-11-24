
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.Schedule;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the persistence operations for working schedules.
 */
public interface ScheduleRepository {

    /**
     * Saves a working schedule.
     *
     * @param schedule the schedule to save
     * @return the saved schedule
     */
    Schedule save(Schedule schedule);

    /**
     * Finds a working schedule by its identifier.
     *
     * @param id the schedule identifier
     * @return an optional containing the schedule if found
     */
    Optional<Schedule> findById(UUID id);

    /**
     * Retrieves all registered working schedules.
     *
     * @return all registered schedules
     */
    List<Schedule> findAll();
}
