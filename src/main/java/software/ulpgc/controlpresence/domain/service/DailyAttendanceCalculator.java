
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.domain.model.DailyAttendanceSummary;
import software.ulpgc.controlpresence.domain.model.WorkingInterval;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Calculates daily attendance information by comparing
 * scheduled working intervals with actual attendance records.
 */
public class DailyAttendanceCalculator {

    /**
     * Calculates the attendance summary for a person on a given date.
     *
     * Attendance records must belong to the specified person and form
     * a valid chronological sequence starting with a clock-in.
     * An unmatched clock-in is considered pending and is not counted
     * as completed working time.
     *
     * @param personId the identifier of the person being evaluated
     * @param date the date being evaluated
     * @param expectedIntervals the scheduled working intervals
     * @param attendanceRecords the person's attendance records
     * @return the calculated daily attendance summary
     * @throws NullPointerException if any required argument is null
     * @throws IllegalArgumentException if attendance records are incoherent
     */
    public DailyAttendanceSummary calculate(
            UUID personId,
            LocalDate date,
            List<WorkingInterval> expectedIntervals,
            List<AttendanceRecord> attendanceRecords
    ) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");
        Objects.requireNonNull(
                expectedIntervals,
                "Expected intervals cannot be null"
        );
        Objects.requireNonNull(
                attendanceRecords,
                "Attendance records cannot be null"
        );

        List<WorkingInterval> intervals = expectedIntervals.stream()
                .sorted(Comparator.comparing(WorkingInterval::startDateTime))
                .toList();

        List<AttendanceRecord> records = attendanceRecords.stream()
                .sorted(Comparator.comparing(AttendanceRecord::dateTime))
                .toList();

        if (records.stream().anyMatch(
                record -> !record.person().id().equals(personId)
        )) {
            throw new IllegalArgumentException(
                    "Attendance records must belong to the specified person"
            );
        }

        validateRecords(records);

        List<CompletedInterval> completedIntervals =
                getCompletedIntervals(records);

        AttendanceRecord pendingRecord = getPendingRecord(records);

        Duration scheduledTime = Duration.ZERO;
        Duration workedTime = Duration.ZERO;
        Duration lateness = Duration.ZERO;
        Duration earlyDeparture = Duration.ZERO;

        boolean hasAttendance = false;
        boolean pendingClockOut = pendingRecord != null;

        for (WorkingInterval expected : intervals) {
            scheduledTime = scheduledTime.plus(
                    Duration.between(
                            expected.startDateTime(),
                            expected.endDateTime()
                    )
            );

            List<CompletedInterval> matchingIntervals =
                    completedIntervals.stream()
                            .filter(actual -> overlaps(
                                    actual.start(),
                                    actual.end(),
                                    expected.startDateTime(),
                                    expected.endDateTime()
                            ))
                            .toList();

            if (!matchingIntervals.isEmpty()) {
                hasAttendance = true;

                for (CompletedInterval actual : matchingIntervals) {
                    workedTime = workedTime.plus(
                            calculateOverlap(
                                    actual.start(),
                                    actual.end(),
                                    expected.startDateTime(),
                                    expected.endDateTime()
                            )
                    );
                }

                CompletedInterval first = matchingIntervals.getFirst();
                CompletedInterval last = matchingIntervals.getLast();

                if (first.start().isAfter(expected.startDateTime())) {
                    lateness = lateness.plus(
                            Duration.between(
                                    expected.startDateTime(),
                                    first.start()
                            )
                    );
                }

                if (last.end().isBefore(expected.endDateTime())) {
                    earlyDeparture = earlyDeparture.plus(
                            Duration.between(
                                    last.end(),
                                    expected.endDateTime()
                            )
                    );
                }
            } else if (pendingRecord != null
                    && pendingRecord.dateTime().isBefore(
                    expected.endDateTime()
            )) {
                hasAttendance = true;
            }
        }

        boolean absent = !intervals.isEmpty() && !hasAttendance;

        return new DailyAttendanceSummary(
                date,
                personId,
                scheduledTime,
                workedTime,
                lateness,
                earlyDeparture,
                absent,
                pendingClockOut
        );
    }

    /**
     * Validates the chronological sequence of attendance records.
     *
     * @param records the attendance records to validate
     * @throws IllegalArgumentException if the records are incoherent
     */
    private void validateRecords(List<AttendanceRecord> records) {
        if (records.isEmpty()) {
            return;
        }

        UUID personId = records.getFirst().person().id();
        AttendanceEventType expectedType = AttendanceEventType.CLOCK_IN;

        AttendanceRecord previous = null;

        for (AttendanceRecord record : records) {
            if (!record.person().id().equals(personId)) {
                throw new IllegalArgumentException(
                        "Attendance records must belong to the same person"
                );
            }

            if (record.eventType() != expectedType) {
                throw new IllegalArgumentException(
                        "Attendance events must alternate between clock-in and clock-out"
                );
            }

            if (previous != null
                    && !record.dateTime().isAfter(previous.dateTime())) {
                throw new IllegalArgumentException(
                        "Attendance events must be in chronological order"
                );
            }

            expectedType = expectedType == AttendanceEventType.CLOCK_IN
                    ? AttendanceEventType.CLOCK_OUT
                    : AttendanceEventType.CLOCK_IN;

            previous = record;
        }
    }

    /**
     * Creates pairs of completed clock-in and clock-out events.
     *
     * @param records the validated attendance records
     * @return the completed attendance intervals
     */
    private List<CompletedInterval> getCompletedIntervals(
            List<AttendanceRecord> records
    ) {
        List<CompletedInterval> intervals = new ArrayList<>();

        for (int i = 0; i + 1 < records.size(); i += 2) {
            AttendanceRecord clockIn = records.get(i);
            AttendanceRecord clockOut = records.get(i + 1);

            intervals.add(new CompletedInterval(
                    clockIn.dateTime(),
                    clockOut.dateTime()
            ));
        }

        return intervals;
    }

    /**
     * Retrieves the last unmatched clock-in event, if one exists.
     *
     * @param records the validated attendance records
     * @return the pending clock-in event, or null
     */
    private AttendanceRecord getPendingRecord(
            List<AttendanceRecord> records
    ) {
        if (!records.isEmpty()
                && records.getLast().eventType() == AttendanceEventType.CLOCK_IN) {
            return records.getLast();
        }

        return null;
    }

    /**
     * Checks whether two time intervals overlap.
     *
     * @param firstStart the first interval start
     * @param firstEnd the first interval end
     * @param secondStart the second interval start
     * @param secondEnd the second interval end
     * @return true if the intervals overlap
     */
    private boolean overlaps(
            LocalDateTime firstStart,
            LocalDateTime firstEnd,
            LocalDateTime secondStart,
            LocalDateTime secondEnd
    ) {
        return firstStart.isBefore(secondEnd)
                && firstEnd.isAfter(secondStart);
    }

    /**
     * Calculates the duration shared by two overlapping intervals.
     *
     * @param actualStart the actual interval start
     * @param actualEnd the actual interval end
     * @param expectedStart the expected interval start
     * @param expectedEnd the expected interval end
     * @return the overlapping duration
     */
    private Duration calculateOverlap(
            LocalDateTime actualStart,
            LocalDateTime actualEnd,
            LocalDateTime expectedStart,
            LocalDateTime expectedEnd
    ) {
        LocalDateTime start = actualStart.isAfter(expectedStart)
                ? actualStart
                : expectedStart;

        LocalDateTime end = actualEnd.isBefore(expectedEnd)
                ? actualEnd
                : expectedEnd;

        return Duration.between(start, end);
    }

    /**
     * Represents a completed attendance interval.
     *
     * @param start the clock-in date and time
     * @param end the clock-out date and time
     */
    private record CompletedInterval(
            LocalDateTime start,
            LocalDateTime end
    ) {
    }
}
