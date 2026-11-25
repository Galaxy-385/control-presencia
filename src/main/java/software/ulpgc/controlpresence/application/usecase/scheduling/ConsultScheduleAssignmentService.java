
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.ports.in.scheduling.ConsultScheduleAssignmentUseCase;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Implements the use case for consulting schedule assignments.
 */
public class ConsultScheduleAssignmentService implements ConsultScheduleAssignmentUseCase {

    private final ScheduleAssignmentRepository assignmentRepository;

    /**
     * Creates the service with its required repository.
     *
     * @param assignmentRepository the repository used to retrieve assignments
     */
    public ConsultScheduleAssignmentService(
            ScheduleAssignmentRepository assignmentRepository
    ) {
        this.assignmentRepository = Objects.requireNonNull(
                assignmentRepository,
                "Assignment repository cannot be null"
        );
    }

    /**
     * Finds a schedule assignment by its identifier.
     *
     * @param id the assignment identifier
     * @return an optional containing the assignment if found
     */
    @Override
    public Optional<ScheduleAssignment> findById(UUID id) {
        Objects.requireNonNull(id, "Assignment ID cannot be null");
        return assignmentRepository.findById(id);
    }

    /**
     * Retrieves all schedule assignments belonging to a person.
     *
     * @param personId the person's identifier
     * @return the person's schedule assignments
     */
    @Override
    public List<ScheduleAssignment> findByPersonId(UUID personId) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        return assignmentRepository.findByPersonId(personId);
    }
}
