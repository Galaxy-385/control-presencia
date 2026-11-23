
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Provides operations for checking schedule assignments.
 */
public class ScheduleAssignmentService {

    /**
     * Checks whether a schedule assignment is active on a given date.
     *
     * The start and end dates are inclusive. An assignment without
     * an end date remains active indefinitely after its start date.
     *
     * @param assignment the schedule assignment to check
     * @param date the date to evaluate
     * @return true if the assignment is active on the given date
     * @throws NullPointerException if the assignment or date is null
     */
    public boolean isActiveOn(
            ScheduleAssignment assignment,
            LocalDate date
    ) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");

        boolean afterStart = !date.isBefore(assignment.startDate());

        boolean beforeEnd = assignment.endDate()
                .map(endDate -> !date.isAfter(endDate))
                .orElse(true);

        return afterStart && beforeEnd;
    }
}
