
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;

import java.util.List;
import java.util.Objects;

/**
 * Validates the sequence of attendance events recorded for a person.
 */
public class AttendanceEventValidator {

    /**
     * Validates whether an attendance event can be added after
     * the previously recorded events.
     *
     * Events must alternate between clock-in and clock-out,
     * starting with a clock-in event. Their timestamps must
     * also be in chronological order.
     *
     * @param previousRecords the previously recorded attendance events
     * @param newRecord the attendance event to validate
     * @throws NullPointerException if any required argument is null
     * @throws IllegalArgumentException if the new event is incoherent
     */
    public void validateNextEvent(
            List<AttendanceRecord> previousRecords,
            AttendanceRecord newRecord
    ) {
        Objects.requireNonNull(previousRecords, "Previous records cannot be null");
        Objects.requireNonNull(newRecord, "New attendance record cannot be null");

        if (previousRecords.isEmpty()) {
            if (newRecord.eventType() != AttendanceEventType.CLOCK_IN) {
                throw new IllegalArgumentException(
                        "The first attendance event must be a clock-in"
                );
            }
            return;
        }

        AttendanceRecord lastRecord = previousRecords.get(
                previousRecords.size() - 1
        );

        if (!lastRecord.person().id().equals(newRecord.person().id())) {
            throw new IllegalArgumentException(
                    "Attendance records must belong to the same person"
            );
        }

        if (!newRecord.dateTime().isAfter(lastRecord.dateTime())) {
            throw new IllegalArgumentException(
                    "Attendance events must be in chronological order"
            );
        }

        if (lastRecord.eventType() == newRecord.eventType()) {
            throw new IllegalArgumentException(
                    "Attendance events must alternate between clock-in and clock-out"
            );
        }
    }
}
