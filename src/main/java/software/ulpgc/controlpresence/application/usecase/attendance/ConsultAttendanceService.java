package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultAttendanceUseCase;
import software.ulpgc.controlpresence.ports.out.AttendanceRecordRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for consulting attendance records.
 */
public class ConsultAttendanceService implements ConsultAttendanceUseCase {

    private final PersonRepository personRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;

    /**
     * Creates the attendance consultation service.
     *
     * @param personRepository repository used to find employees
     * @param attendanceRecordRepository repository used to retrieve attendance records
     */
    public ConsultAttendanceService(
            PersonRepository personRepository,
            AttendanceRecordRepository attendanceRecordRepository
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.attendanceRecordRepository = Objects.requireNonNull(
                attendanceRecordRepository,
                "Attendance record repository cannot be null"
        );
    }

    /**
     * Retrieves the attendance records of an existing person
     * within an inclusive date range.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the attendance records ordered chronologically
     * @throws IllegalArgumentException if the person does not exist
     * or the date range is invalid
     */
    @Override
    public List<AttendanceRecord> consult(
            UUID personId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        Objects.requireNonNull(personId, "Person ID cannot be null");
        Objects.requireNonNull(startDate, "Start date cannot be null");
        Objects.requireNonNull(endDate, "End date cannot be null");

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date cannot be before start date"
            );
        }

        personRepository.findById(personId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Person not found: " + personId
                ));

        return attendanceRecordRepository
                .findByPersonIdAndDateTimeBetweenOrderByDateTime(
                        personId,
                        startDate.atStartOfDay(),
                        endDate.atTime(LocalTime.MAX)
                );
    }
}