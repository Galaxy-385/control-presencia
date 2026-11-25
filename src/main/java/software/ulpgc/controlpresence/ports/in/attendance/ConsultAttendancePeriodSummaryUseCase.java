
package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.AttendancePeriodSummary;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines the use case for consulting attendance summaries over a period.
 */
public interface ConsultAttendancePeriodSummaryUseCase {

    /**
     * Calculates the attendance summary for a person over a date range.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the attendance summary for the requested period
     */
    AttendancePeriodSummary consult(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    );
}
