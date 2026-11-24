
package software.ulpgc.controlpresence.application.usecase.attendance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.Incident;
import software.ulpgc.controlpresence.domain.model.IncidentType;
import software.ulpgc.controlpresence.domain.model.Person;
import software.ulpgc.controlpresence.ports.out.IncidentRepository;
import software.ulpgc.controlpresence.ports.out.PersonRepository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RegisterIncidentServiceTest {

    private FakePersonRepository personRepository;
    private FakeIncidentRepository incidentRepository;
    private RegisterIncidentService service;

    private Person person;
    private LocalDate date;

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        incidentRepository = new FakeIncidentRepository();

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        date = LocalDate.of(2026, 11, 24);

        personRepository.add(person);

        service = new RegisterIncidentService(
                personRepository,
                incidentRepository
        );
    }

    @Test
    void shouldRegisterAbsenceIncident() {
        Incident result = service.register(
                person.id(),
                date,
                IncidentType.ABSENCE,
                "Employee absence"
        );

        assertNotNull(result);
        assertEquals(person.id(), result.person().id());
        assertEquals(IncidentType.ABSENCE, result.type());
        assertEquals(date, result.date());
        assertEquals("Employee absence", result.description());
        assertEquals(1, incidentRepository.incidents.size());
    }

    @Test
    void shouldRegisterLatenessIncident() {
        Incident result = service.register(
                person.id(),
                date,
                IncidentType.LATENESS,
                "Late arrival"
        );

        assertEquals(IncidentType.LATENESS, result.type());
    }

    @Test
    void shouldRegisterEarlyDepartureIncident() {
        Incident result = service.register(
                person.id(),
                date,
                IncidentType.EARLY_DEPARTURE,
                "Early departure"
        );

        assertEquals(IncidentType.EARLY_DEPARTURE, result.type());
    }

    @Test
    void shouldRegisterOtherIncident() {
        Incident result = service.register(
                person.id(),
                date,
                IncidentType.OTHER,
                "Other incident"
        );

        assertEquals(IncidentType.OTHER, result.type());
    }

    @Test
    void shouldGenerateUniqueIdentifier() {
        Incident result = service.register(
                person.id(),
                date,
                IncidentType.ABSENCE,
                "Employee absence"
        );

        assertNotNull(result.id());
    }

    @Test
    void shouldNotRegisterIncidentWhenPersonDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        UUID.randomUUID(),
                        date,
                        IncidentType.ABSENCE,
                        "Employee absence"
                )
        );

        assertTrue(incidentRepository.incidents.isEmpty());
    }

    @Test
    void shouldNotRegisterIncidentWithNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.register(
                        null,
                        date,
                        IncidentType.ABSENCE,
                        "Employee absence"
                )
        );

        assertTrue(incidentRepository.incidents.isEmpty());
    }

    @Test
    void shouldNotRegisterIncidentWithNullDate() {
        assertThrows(
                NullPointerException.class,
                () -> service.register(
                        person.id(),
                        null,
                        IncidentType.ABSENCE,
                        "Employee absence"
                )
        );

        assertTrue(incidentRepository.incidents.isEmpty());
    }

    @Test
    void shouldNotRegisterIncidentWithNullType() {
        assertThrows(
                NullPointerException.class,
                () -> service.register(
                        person.id(),
                        date,
                        null,
                        "Employee absence"
                )
        );

        assertTrue(incidentRepository.incidents.isEmpty());
    }

    @Test
    void shouldNotRegisterIncidentWithBlankDescription() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.register(
                        person.id(),
                        date,
                        IncidentType.ABSENCE,
                        " "
                )
        );

        assertTrue(incidentRepository.incidents.isEmpty());
    }

    private static class FakePersonRepository implements PersonRepository {

        private final List<Person> people = new ArrayList<>();

        void add(Person person) {
            people.add(person);
        }

        @Override
        public Optional<Person> findById(UUID id) {
            return people.stream()
                    .filter(person -> person.id().equals(id))
                    .findFirst();
        }
    }

    private static class FakeIncidentRepository implements IncidentRepository {

        private final List<Incident> incidents = new ArrayList<>();

        @Override
        public Incident save(Incident incident) {
            incidents.add(incident);
            return incident;
        }

        @Override
        public List<Incident> findByPersonIdOrderByDate(UUID personId) {
            return incidents.stream()
                    .filter(incident -> incident.person().id().equals(personId))
                    .sorted((first, second) ->
                            first.date().compareTo(second.date()))
                    .toList();
        }

        @Override
        public List<Incident> findByPersonIdAndDateBetweenOrderByDate(
                UUID personId,
                LocalDate startDate,
                LocalDate endDate
        ) {
            return incidents.stream()
                    .filter(incident -> incident.person().id().equals(personId))
                    .filter(incident -> !incident.date().isBefore(startDate))
                    .filter(incident -> !incident.date().isAfter(endDate))
                    .sorted((first, second) ->
                            first.date().compareTo(second.date()))
                    .toList();
        }
    }
}
