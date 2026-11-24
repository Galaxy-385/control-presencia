
package software.ulpgc.controlpresence.ports.in.attendance;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Defines the use case for registering an attendance event.
 */
public interface RegisterAttendanceUseCase {

    /**
     * Registers an attendance event for a person.
     *
     * @param personId the identifier of the person
     * @param eventType the type of attendance event
     * @param dateTime the date and time of the event
     * @param origin the source of the attendance event
     * @return the registered attendance record
     */
    AttendanceRecord register(
            UUID personId,
            AttendanceEventType eventType,
            LocalDateTime dateTime,
            String origin
    );
}
