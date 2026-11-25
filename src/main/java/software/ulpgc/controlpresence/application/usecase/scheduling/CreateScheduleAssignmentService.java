
package software.ulpgc.controlpresence.application.usecase.scheduling;

import software.ulpgc.controlpresence.domain.model.ScheduleAssignment;
import software.ulpgc.controlpresence.ports.in.scheduling.CreateScheduleAssignmentUseCase;
import software.ulpgc.controlpresence.ports.out.PersonRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleAssignmentRepository;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.util.Objects;


/**
 * Implements the use case for creating schedule assignments.
 */
public class CreateScheduleAssignmentService implements CreateScheduleAssignmentUseCase {

    private final PersonRepository personRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleAssignmentRepository assignmentRepository;

    /**
     * Creates the service with its required repositories.
     *
     * @param personRepository the repository used to retrieve people
     * @param scheduleRepository the repository used to retrieve schedules
     * @param assignmentRepository the repository used to manage assignments
     */
    public CreateScheduleAssignmentService(
            PersonRepository personRepository,
            ScheduleRepository scheduleRepository,
            ScheduleAssignmentRepository assignmentRepository
    ) {
        this.personRepository = Objects.requireNonNull(personRepository);
        this.scheduleRepository = Objects.requireNonNull(scheduleRepository);
        this.assignmentRepository = Objects.requireNonNull(assignmentRepository);
    }

    /**
     * Creates and stores a schedule assignment.
     *
     * @param assignment the assignment to create
     * @return the created assignment
     */
    @Override
    public ScheduleAssignment create(ScheduleAssignment assignment) {
        Objects.requireNonNull(assignment, "Assignment cannot be null");

        if (personRepository.findById(assignment.person().id()).isEmpty()) {
            throw new IllegalArgumentException("Person does not exist");
        }

        if (scheduleRepository.findById(assignment.schedule().id()).isEmpty()) {
            throw new IllegalArgumentException("Schedule does not exist");
        }

        if (assignmentRepository.findById(assignment.id()).isPresent()) {
            throw new IllegalArgumentException("Assignment ID already exists");
        }

        boolean overlappingAssignment = assignmentRepository
                .findByPersonId(assignment.person().id())
                .stream()
                .anyMatch(existing -> overlaps(existing, assignment));

        if (overlappingAssignment) {
            throw new IllegalArgumentException(
                    "The person already has an assignment during this period"
            );
        }

        return assignmentRepository.save(assignment);
    }

    private boolean overlaps(
            ScheduleAssignment first,
            ScheduleAssignment second
    ) {
        return !first.endDate().map(end -> end.isBefore(second.startDate())).orElse(false)
                && !second.endDate().map(end -> end.isBefore(first.startDate())).orElse(false);
    }
}
