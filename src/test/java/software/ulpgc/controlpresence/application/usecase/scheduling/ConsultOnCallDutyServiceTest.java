
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.OnCallDuty;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.ports.out.OnCallDutyRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ConsultOnCallDutyServiceTest {

    private FakeOnCallDutyRepository repository;
    private ConsultOnCallDutyService service;

    private Person person;
    private OnCallDuty firstDuty;
    private OnCallDuty secondDuty;

    @BeforeEach
    void setUp() {
        repository = new FakeOnCallDutyRepository();
        service = new ConsultOnCallDutyService(repository);

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        firstDuty = new OnCallDuty(
                UUID.randomUUID(),
                person,
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        );

        secondDuty = new OnCallDuty(
                UUID.randomUUID(),
                person,
                LocalDateTime.of(2026, 11, 26, 8, 0),
                LocalDateTime.of(2026, 11, 26, 20, 0)
        );

        repository.save(firstDuty);
        repository.save(secondDuty);
    }

    @Test
    void shouldFindDutyById() {
        Optional<OnCallDuty> result = service.findById(firstDuty.id());

        assertTrue(result.isPresent());
        assertEquals(firstDuty, result.orElseThrow());
    }

    @Test
    void shouldReturnEmptyWhenDutyDoesNotExist() {
        Optional<OnCallDuty> result = service.findById(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullDutyId() {
        assertThrows(
                NullPointerException.class,
                () -> service.findById(null)
        );
    }

    @Test
    void shouldFindDutiesByPersonId() {
        List<OnCallDuty> result = service.findByPersonId(person.id());

        assertEquals(2, result.size());
        assertTrue(result.contains(firstDuty));
        assertTrue(result.contains(secondDuty));
    }

    @Test
    void shouldReturnEmptyListWhenPersonHasNoDuties() {
        List<OnCallDuty> result = service.findByPersonId(UUID.randomUUID());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.findByPersonId(null)
        );
    }

    @Test
    void shouldRejectNullRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new ConsultOnCallDutyService(null)
        );
    }

    private static class FakeOnCallDutyRepository implements OnCallDutyRepository {

        private final List<OnCallDuty> duties = new ArrayList<>();

        @Override
        public OnCallDuty save(OnCallDuty duty) {
            duties.add(duty);
            return duty;
        }

        @Override
        public Optional<OnCallDuty> findById(UUID id) {
            return duties.stream()
                    .filter(duty -> duty.id().equals(id))
                    .findFirst();
        }

        @Override
        public List<OnCallDuty> findByPersonId(UUID personId) {
            return duties.stream()
                    .filter(duty -> duty.person().id().equals(personId))
                    .toList();
        }
    }
}
