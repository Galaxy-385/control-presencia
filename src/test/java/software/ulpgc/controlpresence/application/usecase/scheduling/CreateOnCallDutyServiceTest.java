
package software.ulpgc.controlpresence.application.usecase.scheduling;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.OnCallDuty;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.ports.out.OnCallDutyRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CreateOnCallDutyServiceTest {

    private FakePersonRepository personRepository;
    private FakeOnCallDutyRepository dutyRepository;
    private CreateOnCallDutyService service;

    private Person person;

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        dutyRepository = new FakeOnCallDutyRepository();

        service = new CreateOnCallDutyService(
                personRepository,
                dutyRepository
        );

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        personRepository.save(person);
    }

    @Test
    void shouldCreateOnCallDuty() {
        OnCallDuty duty = createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        );

        OnCallDuty result = service.create(duty);

        assertEquals(duty, result);
        assertEquals(duty, dutyRepository.findById(duty.id()).orElseThrow());
    }

    @Test
    void shouldRejectNullDuty() {
        assertThrows(
                NullPointerException.class,
                () -> service.create(null)
        );
    }

    @Test
    void shouldRejectDutyWhenPersonDoesNotExist() {
        Person unknownPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Jane Smith"
        );

        OnCallDuty duty = new OnCallDuty(
                UUID.randomUUID(),
                unknownPerson,
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(duty)
        );

        assertTrue(dutyRepository.duties.isEmpty());
    }

    @Test
    void shouldRejectDuplicateDutyId() {
        OnCallDuty duty = createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        );

        dutyRepository.save(duty);

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(duty)
        );
    }

    @Test
    void shouldRejectOverlappingDuties() {
        dutyRepository.save(createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        ));

        OnCallDuty overlapping = createDuty(
                LocalDateTime.of(2026, 11, 25, 12, 0),
                LocalDateTime.of(2026, 11, 25, 23, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(overlapping)
        );
    }

    @Test
    void shouldRejectDutyContainedWithinAnotherDuty() {
        dutyRepository.save(createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        ));

        OnCallDuty contained = createDuty(
                LocalDateTime.of(2026, 11, 25, 10, 0),
                LocalDateTime.of(2026, 11, 25, 15, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> service.create(contained)
        );
    }

    @Test
    void shouldAllowConsecutiveDuties() {
        dutyRepository.save(createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        ));

        OnCallDuty next = createDuty(
                LocalDateTime.of(2026, 11, 25, 20, 0),
                LocalDateTime.of(2026, 11, 26, 8, 0)
        );

        OnCallDuty result = service.create(next);

        assertEquals(next, result);
    }

    @Test
    void shouldAllowOverlappingDutiesForDifferentPeople() {
        Person anotherPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Jane Smith"
        );

        personRepository.save(anotherPerson);

        dutyRepository.save(createDuty(
                LocalDateTime.of(2026, 11, 25, 8, 0),
                LocalDateTime.of(2026, 11, 25, 20, 0)
        ));

        OnCallDuty anotherDuty = new OnCallDuty(
                UUID.randomUUID(),
                anotherPerson,
                LocalDateTime.of(2026, 11, 25, 10, 0),
                LocalDateTime.of(2026, 11, 25, 22, 0)
        );

        assertEquals(anotherDuty, service.create(anotherDuty));
    }

    @Test
    void shouldRejectNullPersonRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateOnCallDutyService(null, dutyRepository)
        );
    }

    @Test
    void shouldRejectNullDutyRepository() {
        assertThrows(
                NullPointerException.class,
                () -> new CreateOnCallDutyService(personRepository, null)
        );
    }

    private OnCallDuty createDuty(
            LocalDateTime start,
            LocalDateTime end
    ) {
        return new OnCallDuty(
                UUID.randomUUID(),
                person,
                start,
                end
        );
    }

    private static class FakePersonRepository implements PersonRepository {

        private final List<Person> people = new ArrayList<>();

        void save(Person person) {
            people.add(person);
        }

        @Override
        public Optional<Person> findById(UUID id) {
            return people.stream()
                    .filter(person -> person.id().equals(id))
                    .findFirst();
        }
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
