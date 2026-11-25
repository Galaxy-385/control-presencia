
package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.domain.model.DailyAttendanceSummary;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.model.WorkingInterval;
import software.ulpgc.controlpresence.domain.service.DailyAttendanceCalculator;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.domain.service.ScheduleIntervalCalculator;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultDailyAttendanceSummaryUseCase;
import software.ulpgc.controlpresence.ports.out.AttendanceRecordRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for consulting a person's daily attendance summary.
 */
public class ConsultDailyAttendanceSummaryService
        implements ConsultDailyAttendanceSummaryUseCase {

    private final PersonRepository personRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final ScheduleAssignmentService assignmentService;
    private final ScheduleIntervalCalculator intervalCalculator;
    private final DailyAttendanceCalculator attendanceCalculator;

    /**
     * Creates the service with its required dependencies.
     *
     * @param personRepository repository used to retrieve people
     * @param assignmentRepository repository used to retrieve schedule assignments
     * @param attendanceRecordRepository repository used to retrieve attendance records
     * @param assignmentService service used to check active assignments
     * @param intervalCalculator calculator used to obtain scheduled intervals
     * @param attendanceCalculator calculator used to calculate daily attendance
     */
    public ConsultDailyAttendanceSummaryService(
            PersonRepository personRepository,
            ScheduleAssignmentRepository assignmentRepository,
            AttendanceRecordRepository attendanceRecordRepository,
            ScheduleAssignmentService assignmentService,
            ScheduleIntervalCalculator intervalCalculator,
            DailyAttendanceCalculator attendanceCalculator
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.assignmentRepository = Objects.requireNonNull(
                assignmentRepository,
                "Assignment repository cannot be null"
        );
        this.attendanceRecordRepository = Objects.requireNonNull(
                attendanceRecordRepository,
                "Attendance record repository cannot be null"
        );
        this.assignmentService = Objects.requireNonNull(
                assignmentService,
                "Assignment service cannot be null"
        );
        this.intervalCalculator = Objects.requireNonNull(
                intervalCalculator,
                "Interval calculator cannot be null"
        );
        this.attendanceCalculator = Objects.requireNonNull(
                attendanceCalculator,
                "Attendance calculator cannot be null"
        );
    }

    /**
     * Calculates the daily attendance summary for an existing person.
     *
     * Attendance records are associated with the date of their clock-in.
     * This also applies to clock-outs occurring after midnight.
     *
     * @param personId the identifier of the person
     * @param date the date to evaluate
     * @return the calculated daily attendance summary
     * @throws IllegalArgumentException if the person does not exist
     * or has no active schedule assignment
     */
    @Override
    public DailyAttendanceSummary consult(UUID personId, LocalDate date) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(date, "Date cannot be null");

        personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person not found: " + personId
                ));

        List<ScheduleAssignment> activeAssignments =
                assignmentRepository.findByPersonId(personId).stream()
                        .filter(assignment ->
                                assignmentService.isActiveOn(assignment, date)
                        )
                        .sorted(Comparator.comparing(
                                ScheduleAssignment::startDate
                        ))
                        .toList();

        if (activeAssignments.isEmpty()) {
            throw new IllegalArgumentException(
                    "No active schedule assignment found for person "
                            + personId + " on " + date
            );
        }

        if (activeAssignments.size() > 1) {
            throw new IllegalStateException(
                    "Multiple active schedule assignments found for person "
                            + personId + " on " + date
            );
        }

        ScheduleAssignment assignment = activeAssignments.getFirst();

        List<WorkingInterval> expectedIntervals =
                intervalCalculator.calculate(assignment.schedule(), date);

        List<AttendanceRecord> allRecords =
                attendanceRecordRepository
                        .findByPersonIdOrderByDateTime(personId);

        List<AttendanceRecord> dailyRecords =
                selectRecordsForDate(allRecords, date);

        return attendanceCalculator.calculate(
                personId,
                date,
                expectedIntervals,
                dailyRecords
        );
    }

    /**
     * Selects attendance records associated with the date of their clock-in.
     *
     * A clock-out belongs to the date of its preceding clock-in, even
     * when the clock-out takes place on the following calendar day.
     *
     * @param records all attendance records ordered chronologically
     * @param date the date to consult
     * @return the records associated with the requested date
     */
    private List<AttendanceRecord> selectRecordsForDate(
            List<AttendanceRecord> records,
            LocalDate date
    ) {
        List<AttendanceRecord> dailyRecords = new java.util.ArrayList<>();

        LocalDateTime currentClockIn = null;

        for (AttendanceRecord record : records) {
            if (record.eventType() == AttendanceEventType.CLOCK_IN) {
                currentClockIn = record.dateTime();

                if (currentClockIn.toLocalDate().equals(date)) {
                    dailyRecords.add(record);
                }
            } else {
                if (currentClockIn != null
                        && currentClockIn.toLocalDate().equals(date)) {
                    dailyRecords.add(record);
                }

                currentClockIn = null;
            }
        }

        return List.copyOf(dailyRecords);
    }
}