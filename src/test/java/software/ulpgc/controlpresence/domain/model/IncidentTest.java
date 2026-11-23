
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Incident domain model.
 */
class IncidentTest {

    private Person createPerson() {
        return new Person(UUID.randomUUID(), "EMP001", "John Smith");
    }

    @Test
    void shouldCreateIncidentWithValidData() {
        LocalDate date = LocalDate.of(2026, 6, 1);

        Incident incident = new Incident(
                UUID.randomUUID(),
                createPerson(),
                date,
                IncidentType.LATENESS,
                "Arrived late due to traffic"
        );

        assertNotNull(incident);
        assertEquals(date, incident.date());
        assertEquals(IncidentType.LATENESS, incident.type());
        assertEquals("Arrived late due to traffic", incident.description());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new Incident(
                        null,
                        createPerson(),
                        LocalDate.now(),
                        IncidentType.ABSENCE,
                        "Unexpected absence"
                )
        );
    }

    @Test
    void shouldRejectNullPerson() {
        assertThrows(NullPointerException.class, () ->
                new Incident(
                        UUID.randomUUID(),
                        null,
                        LocalDate.now(),
                        IncidentType.ABSENCE,
                        "Unexpected absence"
                )
        );
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(NullPointerException.class, () ->
                new Incident(
                        UUID.randomUUID(),
                        createPerson(),
                        null,
                        IncidentType.ABSENCE,
                        "Unexpected absence"
                )
        );
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(NullPointerException.class, () ->
                new Incident(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDate.now(),
                        null,
                        "Unexpected absence"
                )
        );
    }

    @Test
    void shouldRejectNullDescription() {
        assertThrows(NullPointerException.class, () ->
                new Incident(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDate.now(),
                        IncidentType.ABSENCE,
                        null
                )
        );
    }

    @Test
    void shouldRejectBlankDescription() {
        assertThrows(IllegalArgumentException.class, () ->
                new Incident(
                        UUID.randomUUID(),
                        createPerson(),
                        LocalDate.now(),
                        IncidentType.ABSENCE,
                        " "
                )
        );
    }

    @Test
    void shouldContainExpectedIncidentTypes() {
        assertArrayEquals(
                new IncidentType[]{
                        IncidentType.ABSENCE,
                        IncidentType.LATENESS,
                        IncidentType.EARLY_DEPARTURE,
                        IncidentType.OTHER
                },
                IncidentType.values()
        );
    }
}