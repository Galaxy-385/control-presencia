package software.ulpgc.controlpresence.domain.model;

import java.time.DayOfWeek;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a working schedule organized into shifts
 * assigned to specific days of the week.
 *
 * @param id the unique identifier of the schedule
 * @param name the schedule name
 * @param shiftsByDay the shift assigned to each working day
 */
public record ShiftSchedule(
        UUID id,
        String name,
        Map<DayOfWeek, Shift> shiftsByDay
) implements Schedule {

    /**
     * Validates the shift schedule information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if the name is blank
     * or the schedule contains no assigned shifts
     */
    public ShiftSchedule {
        Objects.requireNonNull(id, "Schedule ID cannot be null");
        Objects.requireNonNull(name, "Schedule name cannot be null");
        Objects.requireNonNull(shiftsByDay, "Shifts by day cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Schedule name cannot be blank"
            );
        }

        if (shiftsByDay.isEmpty()) {
            throw new IllegalArgumentException(
                    "A shift schedule must contain at least one assigned shift"
            );
        }

        shiftsByDay = Map.copyOf(shiftsByDay);
    }
}