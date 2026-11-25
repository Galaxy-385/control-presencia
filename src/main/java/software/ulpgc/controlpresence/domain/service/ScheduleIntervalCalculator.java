
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.domain.model.Shift;
import software.ulpgc.controlpresence.domain.model.ShiftSchedule;
import software.ulpgc.controlpresence.domain.model.SplitSchedule;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.domain.model.WorkingInterval;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Calculates the concrete working intervals for a schedule on a given date.
 */
public class ScheduleIntervalCalculator {

    /**
     * Calculates the working intervals of a schedule for a given date.
     *
     * @param schedule the schedule to evaluate
     * @param date the date on which the intervals start
     * @return the working intervals, ordered by start date and time
     * @throws NullPointerException if the schedule or date is null
     * @throws IllegalArgumentException if the schedule type is unsupported
     */
    public List<WorkingInterval> calculate(
            Schedule schedule,
            LocalDate date
    ) {
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");

        List<WorkingInterval> intervals = new ArrayList<>();

        if (schedule instanceof ContinuousSchedule continuousSchedule) {
            if (continuousSchedule.workingDays().contains(date.getDayOfWeek())) {
                intervals.add(createInterval(
                        continuousSchedule.timeSlot(),
                        date
                ));
            }
        } else if (schedule instanceof SplitSchedule splitSchedule) {
            if (splitSchedule.workingDays().contains(date.getDayOfWeek())) {
                splitSchedule.timeSlots().stream()
                        .map(timeSlot -> createInterval(timeSlot, date))
                        .forEach(intervals::add);
            }
        } else if (schedule instanceof ShiftSchedule shiftSchedule) {
            Shift shift = shiftSchedule.shiftsByDay().get(date.getDayOfWeek());

            if (shift != null) {
                intervals.add(createInterval(shift.timeSlot(), date));
            }
        } else {
            throw new IllegalArgumentException(
                    "Unsupported schedule type: " + schedule.getClass().getName()
            );
        }

        return intervals.stream()
                .sorted(Comparator.comparing(WorkingInterval::startDateTime))
                .toList();
    }

    /**
     * Converts a time slot into a concrete working interval.
     *
     * If the end time is earlier than the start time, the interval
     * is considered to end on the following day.
     *
     * @param timeSlot the time slot to convert
     * @param date the date on which the interval starts
     * @return the concrete working interval
     */
    private WorkingInterval createInterval(
            TimeSlot timeSlot,
            LocalDate date
    ) {
        LocalTime startTime = timeSlot.startTime();
        LocalTime endTime = timeSlot.endTime();

        LocalDateTime startDateTime = LocalDateTime.of(date, startTime);
        LocalDateTime endDateTime = LocalDateTime.of(date, endTime);

        if (!endDateTime.isAfter(startDateTime)) {
            endDateTime = endDateTime.plusDays(1);
        }

        return new WorkingInterval(startDateTime, endDateTime);
    }
}
