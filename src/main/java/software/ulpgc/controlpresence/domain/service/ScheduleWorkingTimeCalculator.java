
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Shift;
import software.ulpgc.controlpresence.domain.model.SplitSchedule;
import software.ulpgc.controlpresence.domain.model.TimeSlot;

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
     * @throws NullPointerException if the schedule is null
     */
    public Duration calculate(ContinuousSchedule schedule) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");

        return calculateTimeSlot(schedule.timeSlot());
    }

    /**
     * Calculates the total working time of a split schedule.
     *
     * Breaks between time slots are not included.
     *
     * @param schedule the split schedule
     * @return the total planned working duration
     * @throws NullPointerException if the schedule is null
     */
    public Duration calculate(SplitSchedule schedule) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");

        return schedule.timeSlots()
                .stream()
                .map(this::calculateTimeSlot)
                .reduce(Duration.ZERO, Duration::plus);
    }

    /**
     * Calculates the working time of an individual shift.
     *
     * @param shift the shift to calculate
     * @return the planned working duration
     * @throws NullPointerException if the shift is null
     */
    public Duration calculate(Shift shift) {
        Objects.requireNonNull(shift, "Shift cannot be null");

        return calculateTimeSlot(shift.timeSlot());
    }

    /**
     * Calculates the duration of a time slot, including overnight intervals.
     *
     * @param timeSlot the time slot to calculate
     * @return the duration of the time slot
     */
    private Duration calculateTimeSlot(TimeSlot timeSlot) {
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
