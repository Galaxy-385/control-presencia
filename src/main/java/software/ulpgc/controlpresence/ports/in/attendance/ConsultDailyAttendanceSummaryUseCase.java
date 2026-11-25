
package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.DailyAttendanceSummary;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Defines the use case for consulting a person's daily attendance summary.
 */
public interface ConsultDailyAttendanceSummaryUseCase {

    /**
     * Calculates the daily attendance summary for a person.
     *
     * @param personId the identifier of the person
     * @param date the date to evaluate
     * @return the calculated daily attendance summary
     */
    DailyAttendanceSummary consult(UUID personId, LocalDate date);
}
