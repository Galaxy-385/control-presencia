package software.ulpgc.controlpresence.domain.model;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Represents a split working schedule consisting of
 * multiple time intervals separated by breaks.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param timeSlots the working time intervals
 * @param workingDays the days of the week when work is scheduled
 */
public record SplitSchedule(
        UUID id,
        String name,
        List<TimeSlot> timeSlots,
        Set<DayOfWeek> workingDays
) implements Schedule {

    /**
     * Validates the split schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank,
     * the schedule has no time intervals, or no working days
     */
    public SplitSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(timeSlots, "Time slots cannot be null");
        Objects.requireNonNull(workingDays, "Working days cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }

        if (timeSlots.isEmpty()) {
            throw new IllegalArgumentException(
                    "A split schedule must contain at least one time slot"
            );
        }

        if (workingDays.isEmpty()) {
            throw new IllegalArgumentException(
                    "A split schedule must have at least one working day"
            );
        }

        timeSlots = List.copyOf(timeSlots);
        workingDays = Set.copyOf(workingDays);
    }
}