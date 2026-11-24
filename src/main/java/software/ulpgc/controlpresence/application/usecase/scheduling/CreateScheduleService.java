
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.ports.in.scheduling.CreateScheduleUseCase;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.util.Objects;

/**
 * Implements the use case for creating working schedules.
 */
public class CreateScheduleService implements CreateScheduleUseCase {

    private final ScheduleRepository scheduleRepository;

    /**
     * Creates the service with its required repository.
     *
     * @param scheduleRepository the repository used to store schedules
     */
    public CreateScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = Objects.requireNonNull(
                scheduleRepository,
                "Schedule repository cannot be null"
        );
    }

    /**
     * Creates and stores a working schedule.
     *
     * @param schedule the schedule to create
     * @return the created schedule
     * @throws NullPointerException if the schedule is null
     * @throws IllegalArgumentException if a schedule with the same ID already exists
     */
    @Override
    public Schedule create(Schedule schedule) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");

        if (scheduleRepository.findById(schedule.id()).isPresent()) {
            throw new IllegalArgumentException(
                    "A schedule with this ID already exists"
            );
        }

        return scheduleRepository.save(schedule);
    }
}
