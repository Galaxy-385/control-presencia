
package software.ulpgc.controlpresence.ports.in.scheduling;

import software.ulpgc.controlpresence.domain.model.Schedule;

/**
 * Defines the use case for creating a working schedule.
 */
public interface CreateScheduleUseCase {

    /**
     * Creates and stores a working schedule.
     *
     * @param schedule the schedule to create
     * @return the created schedule
     */
    Schedule create(Schedule schedule);
}
