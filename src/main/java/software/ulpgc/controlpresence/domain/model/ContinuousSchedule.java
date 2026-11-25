package software.ulpgc.controlpresence.domain.model;

import java.time.DayOfWeek;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a continuous working schedule consisting of
 * a single uninterrupted time interval.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param timeSlot the working time interval
 * @param workingDays the days of the week when work is scheduled
 */
public record ContinuousSchedule(
        UUID id,
        String name,
        TimeSlot timeSlot,
        Set<DayOfWeek> workingDays
) implements Schedule {

    /**
     * Validates the continuous schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     * or no working days are provided
     */
    public ContinuousSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(timeSlot, "Time slot cannot be null");
        Objects.requireNonNull(workingDays, "Working days cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }

        if (workingDays.isEmpty()) {
            throw new IllegalArgumentException(
                    "A continuous schedule must have at least one working day"
            );
        }

        workingDays = Set.copyOf(workingDays);
    }
}