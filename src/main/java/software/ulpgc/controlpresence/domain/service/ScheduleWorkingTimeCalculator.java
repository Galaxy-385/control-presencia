package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.util.Objects;

/**
 * Calculates the planned working time for different schedule types.
 */
public class ScheduleWorkingTimeCalculator {

    /**
     * Calculates the working time of a continuous schedule.
     *
     * @param schedule the continuous schedule
     * @return the planned working duration
     */
    public Duration calculate(ContinuousSchedule schedule) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        return calculateTimeSlot(schedule.timeSlot());
    }

    /**
     * Calculates the working time of a continuous schedule on a given day.
     *
     * @param schedule the continuous schedule
     * @param day the day of the week
     * @return the planned duration, or zero if it is not a working day
     */
    public Duration calculate(ContinuousSchedule schedule, DayOfWeek day) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(day, "Day cannot be null");

        return schedule.workingDays().contains(day)
                ? calculate(schedule)
                : Duration.ZERO;
    }

    /**
     * Calculates the total working time of a split schedule.
     *
     * @param schedule the split schedule
     * @return the total planned working duration
     */
    public Duration calculate(SplitSchedule schedule) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");

        return schedule.timeSlots()
                .stream()
                .map(this::calculateTimeSlot)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Calculates the working time of a split schedule on a given day.
     *
     * @param schedule the split schedule
     * @param day the day of the week
     * @return the planned duration, or zero if it is not a working day
     */
    public Duration calculate(SplitSchedule schedule, DayOfWeek day) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(day, "Day cannot be null");

        return schedule.workingDays().contains(day)
                ? calculate(schedule)
                : Duration.ZERO;
    }

    /**
     * Calculates the working time of an individual shift.
     *
     * @param shift the shift to calculate
     * @return the planned working duration
     */
    public Duration calculate(Shift shift) {
        Objects.requireNonNull(shift, "Shift cannot be null");
        return calculateTimeSlot(shift.timeSlot());
    }

    /**
     * Calculates the working time assigned to a day in a shift schedule.
     *
     * @param schedule the shift schedule
     * @param day the day of the week
     * @return the planned duration, or zero if no shift is assigned
     */
    public Duration calculate(ShiftSchedule schedule, DayOfWeek day) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(day, "Day cannot be null");

        Shift shift = schedule.shiftsByDay().get(day);

        return shift == null
                ? Duration.ZERO
                : calculate(shift);
    }

    /**
     * Calculates the duration of a time slot, including overnight intervals.
     *
     * @param timeSlot the time slot to calculate
     * @return the duration of the time slot
     */
    private Duration calculateTimeSlot(
            software.ulpgc.controlpresence.domain.model.TimeSlot timeSlot
    ) {
        Duration duration = Duration.between(
                timeSlot.startTime(),
                timeSlot.endTime()
        );

        if (duration.isNegative()) {
            duration = duration.plusDays(1);
        }

        return duration;
    }
}