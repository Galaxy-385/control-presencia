
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

class ConsultIncidentServiceTest {

    private FakePersonRepository personRepository;
    private FakeIncidentRepository incidentRepository;
    private ConsultIncidentService service;

    private Person person;
    private LocalDate firstDate;
    private LocalDate secondDate;
    private LocalDate thirdDate;

    @BeforeEach
    void setUp() {
        personRepository = new FakePersonRepository();
        incidentRepository = new FakeIncidentRepository();

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        firstDate = LocalDate.of(2026, 11, 20);
        secondDate = LocalDate.of(2026, 11, 24);
        thirdDate = LocalDate.of(2026, 11, 28);

        personRepository.add(person);

        incidentRepository.add(createIncident(
                firstDate,
                IncidentType.ABSENCE,
                "Employee absence"
        ));

        incidentRepository.add(createIncident(
                secondDate,
                IncidentType.LATENESS,
                "Late arrival"
        ));

        incidentRepository.add(createIncident(
                thirdDate,
                IncidentType.EARLY_DEPARTURE,
                "Early departure"
        ));

        service = new ConsultIncidentService(
                personRepository,
                incidentRepository
        );
    }

    @Test
    void shouldConsultAllIncidentsWithinDateRange() {
        List<Incident> result = service.consult(
                person.id(),
                firstDate,
                thirdDate
        );

        assertEquals(3, result.size());
    }

    @Test
    void shouldConsultIncidentsWithinPartialDateRange() {
        List<Incident> result = service.consult(
                person.id(),
                secondDate,
                thirdDate
        );

        assertEquals(2, result.size());
    }

    @Test
    void shouldIncludeStartAndEndDates() {
        List<Incident> result = service.consult(
                person.id(),
                secondDate,
                secondDate
        );

        assertEquals(1, result.size());
        assertEquals(secondDate, result.getFirst().date());
    }

    @Test
    void shouldReturnEmptyListWhenNoIncidentsExistInDateRange() {
        List<Incident> result = service.consult(
                person.id(),
                firstDate.plusDays(1),
                firstDate.plusDays(2)
        );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldReturnIncidentsInChronologicalOrder() {
        List<Incident> result = service.consult(
                person.id(),
                firstDate,
                thirdDate
        );

        assertTrue(
                result.get(0).date().isBefore(result.get(1).date())
        );
        assertTrue(
                result.get(1).date().isBefore(result.get(2).date())
        );
    }

    @Test
    void shouldNotConsultIncidentsWhenPersonDoesNotExist() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consult(
                        UUID.randomUUID(),
                        firstDate,
                        thirdDate
                )
        );
    }

    @Test
    void shouldNotConsultWithNullPersonId() {
        assertThrows(
                NullPointerException.class,
                () -> service.consult(
                        null,
                        firstDate,
                        thirdDate
                )
        );
    }

    @Test
    void shouldNotConsultWithNullStartDate() {
        assertThrows(
                NullPointerException.class,
                () -> service.consult(
                        person.id(),
                        null,
                        thirdDate
                )
        );
    }

    @Test
    void shouldNotConsultWithNullEndDate() {
        assertThrows(
                NullPointerException.class,
                () -> service.consult(
                        person.id(),
                        firstDate,
                        null
                )
        );
    }

    @Test
    void shouldNotConsultWhenEndDateIsBeforeStartDate() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.consult(
                        person.id(),
                        thirdDate,
                        firstDate
                )
        );
    }

    private Incident createIncident(
            LocalDate date,
            IncidentType type,
            String description
    ) {
        return new Incident(
                UUID.randomUUID(),
                person,
                date,
                type,
                description
        );
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

        void add(Incident incident) {
            incidents.add(incident);
        }

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
