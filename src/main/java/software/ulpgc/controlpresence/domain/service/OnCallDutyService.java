
package software.ulpgc.controlpresence.domain.service;

import software.ulpgc.controlpresence.domain.model.OnCallDuty;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Provides operations for checking on-call duties.
 */
public class OnCallDutyService {

    /**
     * Checks whether an on-call duty is active at a given date and time.
     *
     * The start date and time are inclusive, while the end date and
     * time are exclusive.
     *
     * @param duty the on-call duty to check
     * @param dateTime the date and time to evaluate
     * @return true if the duty is active at the given date and time
     * @throws NullPointerException if the duty or date and time are null
     */
    public boolean isActiveAt(
            OnCallDuty duty,
            LocalDateTime dateTime
    ) {
        Objects.requireNonNull(duty, "On-call duty cannot be null");
        Objects.requireNonNull(dateTime, "Date and time cannot be null");

        return !dateTime.isBefore(duty.startDateTime())
                && dateTime.isBefore(duty.endDateTime());
    }
}
