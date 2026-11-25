
package software.ulpgc.controlpresence.application.usecase.attendance;

import software.ulpgc.controlpresence.domain.model.AttendancePeriodSummary;
import software.ulpgc.controlpresence.domain.model.DailyAttendanceSummary;
import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.domain.service.ScheduleAssignmentService;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultAttendancePeriodSummaryUseCase;
import software.ulpgc.controlpresence.ports.in.attendance.ConsultDailyAttendanceSummaryUseCase;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Implements the use case for consulting attendance summaries over a period.
 */
public class ConsultAttendancePeriodSummaryService
        implements ConsultAttendancePeriodSummaryUseCase {

    private final PersonRepository personRepository;
    private final ScheduleAssignmentRepository assignmentRepository;
    private final ScheduleAssignmentService assignmentService;
    private final ConsultDailyAttendanceSummaryUseCase dailySummaryUseCase;

    /**
     * Creates the service with its required dependencies.
     *
     * @param personRepository repository used to retrieve people
     * @param assignmentRepository repository used to retrieve assignments
     * @param assignmentService service used to check active assignments
     * @param dailySummaryUseCase use case used to calculate daily summaries
     */
    public ConsultAttendancePeriodSummaryService(
            PersonRepository personRepository,
            ScheduleAssignmentRepository assignmentRepository,
            ScheduleAssignmentService assignmentService,
            ConsultDailyAttendanceSummaryUseCase dailySummaryUseCase
    ) {
        this.personRepository = Objects.requireNonNull(
                personRepository,
                "Person repository cannot be null"
        );
        this.assignmentRepository = Objects.requireNonNull(
                assignmentRepository,
                "Assignment repository cannot be null"
        );
        this.assignmentService = Objects.requireNonNull(
                assignmentService,
                "Assignment service cannot be null"
        );
        this.dailySummaryUseCase = Objects.requireNonNull(
                dailySummaryUseCase,
                "Daily summary use case cannot be null"
        );
    }

    /**
     * Calculates the attendance summary for a person over a date range.
     *
     * Only dates with an active schedule assignment are included.
     *
     * @param personId the identifier of the person
     * @param startDate the first date of the period
     * @param endDate the last date of the period
     * @return the attendance summary for the requested period
     * @throws IllegalArgumentException if the person does not exist
     * or the date range is invalid
     */
    @Override
    public AttendancePeriodSummary consult(
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

        List<ScheduleAssignment> assignments =
                assignmentRepository.findByPersonId(personId);

        List<DailyAttendanceSummary> dailySummaries = new ArrayList<>();

        LocalDate date = startDate;

        while (!date.isAfter(endDate)) {
            LocalDate currentDate = date;

            boolean hasActiveAssignment = assignments.stream()
                    .anyMatch(assignment ->
                            assignmentService.isActiveOn(
                                    assignment,
                                    currentDate
                            )
                    );

            if (hasActiveAssignment) {
                dailySummaries.add(
                        dailySummaryUseCase.consult(personId, currentDate)
                );
            }

            if (date.equals(endDate)) {
                break;
            }

            date = date.plusDays(1);
        }

        return new AttendancePeriodSummary(
                personId,
                startDate,
                endDate,
                dailySummaries
        );
    }
}
