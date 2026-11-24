package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.AttendanceRecord;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Defines the persistence operations for attendance records.
 */
public interface AttendanceRecordRepository {

    /**
     * Finds all attendance records belonging to a person,
     * ordered chronologically.
     *
     * @param personId the person's identifier
     * @return the person's attendance records
     */
    List<AttendanceRecord> findByPersonIdOrderByDateTime(UUID personId);

    /**
     * Finds attendance records belonging to a person within a time interval.
     *
     * @param personId the person's identifier
     * @param startDateTime the beginning of the interval
     * @param endDateTime the end of the interval
     * @return the matching attendance records, ordered chronologically
     */
    List<AttendanceRecord> findByPersonIdAndDateTimeBetweenOrderByDateTime(
            UUID personId,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime
    );

    /**
     * Saves an attendance record.
     *
     * @param record the attendance record to save
     * @return the saved attendance record
     */
    AttendanceRecord save(AttendanceRecord record);
}