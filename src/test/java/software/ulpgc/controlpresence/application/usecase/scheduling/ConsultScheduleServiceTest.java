
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.ports.out.ScheduleRepository;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsultScheduleServiceTest {

    private FakeScheduleRepository repository;
    private ConsultScheduleService service;

    @BeforeEach
    void setUp() {
        repository = new FakeScheduleRepository();
        service = new ConsultScheduleService(repository);
    }

    @Test
    void shouldFindScheduleById() {
        Schedule schedule = createSchedule();
        repository.save(schedule);

        Optional<Schedule> result = service.findById(schedule.id());

        assertTrue(result.isPresent());
        assertEquals(schedule, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenScheduleDoesNotExist() {
        Optional<Schedule> result = service.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullScheduleId() {
        assertThrows(
                NullPointerException.class,
                () -> service.findById(null)
        );
    }

    @Test
    void shouldReturnAllSchedules() {
        Schedule first = createSchedule();
        Schedule second = createSchedule();

        repository.save(first);
        repository.save(second);

        List<Schedule> result = service.findAll();

        assertEquals(2, result.size());
        assertTrue(result.contains(first));
        assertTrue(result.contains(second));
    }

    @Test
    void shouldReturnEmptyListWhenThereAreNoSchedules() {
        List<Schedule> result = service.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new ConsultScheduleService(null)
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
