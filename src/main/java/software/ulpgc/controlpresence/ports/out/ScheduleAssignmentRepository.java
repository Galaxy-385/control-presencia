
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;

import java.util.List;
import java.util.UUID;

/**
 * Defines the persistence operations for schedule assignments.
 */
public interface ScheduleAssignmentRepository {

    /**
     * Finds all schedule assignments belonging to a person.
     *
     * @param personId the person's identifier
     * @return the person's schedule assignments
     */
    List<ScheduleAssignment> findByPersonId(UUID personId);
}
