
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateScheduleServiceTest {

    private FakeScheduleRepository repository;
    private CreateScheduleService service;

    @BeforeEach
    void setUp() {
        repository = new FakeScheduleRepository();
        service = new CreateScheduleService(repository);
    }

    @Test
    void shouldCreateAndSaveSchedule() {
        Schedule schedule = createSchedule();

        Schedule result = service.create(schedule);

        assertEquals(schedule, result);
        assertEquals(Optional.of(schedule), repository.findById(schedule.id()));
    }

    @Test
    void shouldRejectNullSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> service.create(null)
        );
    }

    @Test
    void shouldRejectScheduleWithExistingId() {
        Schedule schedule = createSchedule();
        repository.save(schedule);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(schedule)
        );
    }

    @Test
    void shouldNotSaveScheduleWhenIdAlreadyExists() {
        Schedule original = createSchedule();
        repository.save(original);

        Schedule duplicate = new ContinuousSchedule(
                original.id(),
                "Different schedule",
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(duplicate)
        );

        assertEquals(original, repository.findById(original.id()).orElseThrow());
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateScheduleService(null)
        );
    }

    private Schedule createSchedule() {
        return new ContinuousSchedule(
                UUID.randomUUID(),
                "Morning schedule",
                new TimeSlot(
                        LocalTime.of(8, 0),
                        LocalTime.of(15, 0)
                )
        );
    }

    private static class FakeScheduleRepository implements ScheduleRepository {

        private final Map<UUID, Schedule> schedules = new HashMap<>();

        @Override
        public Schedule save(Schedule schedule) {
            schedules.put(schedule.id(), schedule);
            return schedule;
        }

        @Override
        public Optional<Schedule> findById(UUID id) {
            return Optional.ofNullable(schedules.get(id));
        }

        @Override
        public List<Schedule> findAll() {
            return List.copyOf(schedules.values());
        }
    }
}
