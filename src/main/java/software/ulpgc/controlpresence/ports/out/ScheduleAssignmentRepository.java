
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines the persistence operations for schedule assignments.
 */
public interface ScheduleAssignmentRepository {

    /**
     * Saves a schedule assignment.
     *
     * @param assignment the assignment to save
     * @return the saved assignment
     */
    ScheduleAssignment save(ScheduleAssignment assignment);

    /**
     * Finds a schedule assignment by its identifier.
     *
     * @param id the assignment identifier
     * @return an optional containing the assignment if found
     */
    Optional<ScheduleAssignment> findById(UUID id);

    /**
     * Retrieves all schedule assignments belonging to a person.
     *
     * @param personId the person's identifier
     * @return the person's schedule assignments
     */
    List<ScheduleAssignment> findByPersonId(UUID personId);
}
