
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.ports.in.scheduling.ConsultScheduleUseCase;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the use case for consulting working schedules.
 */
public class ConsultScheduleService implements ConsultScheduleUseCase {

    private final ScheduleRepository scheduleRepository;

    /**
     * Creates the service with its required repository.
     *
     * @param scheduleRepository the repository used to retrieve schedules
     */
    public ConsultScheduleService(ScheduleRepository scheduleRepository) {
        this.scheduleRepository = Objects.requireNonNull(
                scheduleRepository,
                "Schedule repository cannot be null"
        );
    }

    /**
     * Finds a schedule by its identifier.
     *
     * @param id the schedule identifier
     * @return an optional containing the schedule if found
     */
    @Override
    public Optional<Schedule> findById(UUID id) {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        return scheduleRepository.findById(id);
    }

    /**
     * Retrieves all registered working schedules.
     *
     * @return all registered schedules
     */
    @Override
    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }
}
