
package software.ulpgc.controlpresence.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the assignment of a working schedule to a person
 * during a specific period.
 *
 * @param id the unique identifier of the assignment
 * @param person the person assigned to the schedule
 * @param schedule the assigned working schedule
 * @param startDate the first day of the assignment
 * @param endDate the last day of the assignment, if applicable
 */
public record ScheduleAssignment(
        UUID id,
        Person person,
        Schedule schedule,
        LocalDate startDate,
        Optional<LocalDate> endDate
) {

    /**
     * Validates the schedule assignment information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the end date is before
     * the start date
     */
    public ScheduleAssignment {
        Objects.requireNonNull(id, "Assignment ID cannot be null");
        Objects.requireNonNull(person, "Person cannot be null");
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isPresent() && endDate.get().isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }
    }
}
