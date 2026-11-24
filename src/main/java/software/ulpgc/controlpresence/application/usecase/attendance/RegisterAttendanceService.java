
package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.service.AttendanceEventValidator;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.ports.in.attendance.RegisterAttendanceUseCase;
import software.ulpgc.controlpresence.ports.out.AttendanceRecordRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for registering attendance events.
 *
 * Coordinates employee lookup, schedule assignment validation,
 * attendance event validation and persistence.
 */
public class RegisterAttendanceService implements RegisterAttendanceUseCase {

    private final PersonRepository personRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleAssignmentRepository scheduleAssignmentRepository;
    private final AttendanceEventValidator attendanceEventValidator;
    private final ScheduleAssignmentService scheduleAssignmentService;

    /**
     * Creates the attendance registration service.
     *
     * @param personRepository repository used to find employees
     * @param attendanceRecordRepository repository used to retrieve and save attendance records
     * @param scheduleAssignmentRepository repository used to retrieve schedule assignments
     * @param attendanceEventValidator validator for attendance event sequences
     * @param scheduleAssignmentService service used to check active assignments
     */
    public RegisterAttendanceService(
            PersonRepository personRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            ScheduleAssignmentRepository scheduleAssignmentRepository,
            AttendanceEventValidator attendanceEventValidator,
            ScheduleAssignmentService scheduleAssignmentService
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.attendanceRecordRepository = Objects.requireNonNull(
                attendanceRecordRepository,
                "Attendance record repository cannot be null"
        );
        this.scheduleAssignmentRepository = Objects.requireNonNull(
                scheduleAssignmentRepository,
                "Schedule assignment repository cannot be null"
        );
        this.attendanceEventValidator = Objects.requireNonNull(
                attendanceEventValidator,
                "Attendance event validator cannot be null"
        );
        this.scheduleAssignmentService = Objects.requireNonNull(
                scheduleAssignmentService,
                "Schedule assignment service cannot be null"
        );
    }

    /**
     * Registers an attendance event for an existing person.
     *
     * @param personId the identifier of the person
     * @param eventType the type of attendance event
     * @param dateTime the date and time of the event
     * @param origin the source of the attendance event
     * @return the saved attendance record
     * @throws IllegalArgumentException if the person does not exist,
     * has no active schedule assignment, or the event is incoherent
     */
    @Override
    public AttendanceRecord register(
            UUID personId,
            AttendanceEventType eventType,
            LocalDateTime dateTime,
            String origin
    ) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(dateTime, "Date and time cannot be null");

        Person person = personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person not found: " + personId
                ));

        List<ScheduleAssignment> assignments =
                scheduleAssignmentRepository.findByPersonId(personId);

        boolean hasActiveAssignment = assignments.stream()
                .anyMatch(assignment ->
                        scheduleAssignmentService.isActiveOn(
                                assignment,
                                dateTime.toLocalDate()
                        )
                );

        if (!hasActiveAssignment) {
            throw new IllegalArgumentException(
                    "Person has no active schedule assignment on "
                            + dateTime.toLocalDate()
            );
        }

        List<AttendanceRecord> previousRecords =
                attendanceRecordRepository.findByPersonIdOrderByDateTime(personId);

        AttendanceRecord newRecord = new AttendanceRecord(
                UUID.randomUUID(),
                person,
                dateTime,
                eventType,
                origin
        );

        attendanceEventValidator.validateNextEvent(
                previousRecords,
                newRecord
        );

        return attendanceRecordRepository.save(newRecord);
    }
}
