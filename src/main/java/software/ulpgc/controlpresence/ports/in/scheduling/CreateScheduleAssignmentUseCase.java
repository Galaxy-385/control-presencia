
package software.ulpgc.controlpresence.ports.in.scheduling;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;

/**
 * Defines the use case for creating schedule assignments.
 */
public interface CreateScheduleAssignmentUseCase {

    /**
     * Creates and stores a schedule assignment.
     *
     * @param assignment the assignment to create
     * @return the created assignment
     */
    ScheduleAssignment create(ScheduleAssignment assignment);
}
