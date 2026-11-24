package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.AttendanceRecord;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Defines the use case for consulting attendance records.
 */
public interface ConsultAttendanceUseCase {

    /**
     * Retrieves the attendance records of a person within a date range.
     *
     * Both the start date and end date are inclusive.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the attendance records ordered chronologically
     */
    List<AttendanceRecord> consult(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    );
}