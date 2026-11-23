
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Person domain model.
 */
class PersonTest {

    /**
     * Verifies that a person can be created with valid information.
     */
    @Test
    void shouldCreatePersonWithValidData() {
        Person person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        assertNotNull(person);
        assertEquals("EMP001", person.employeeNumber());
        assertEquals("John Smith", person.fullName());
    }

    /**
     * Verifies that a null identifier is rejected.
     */
    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new Person(null, "EMP001", "John Smith")
        );
    }

    /**
     * Verifies that a null employee number is rejected.
     */
    @Test
    void shouldRejectNullEmployeeNumber() {
        assertThrows(NullPointerException.class, () ->
                new Person(UUID.randomUUID(), null, "John Smith")
        );
    }

    /**
     * Verifies that a blank employee number is rejected.
     */
    @Test
    void shouldRejectBlankEmployeeNumber() {
        assertThrows(IllegalArgumentException.class, () ->
                new Person(UUID.randomUUID(), " ", "John Smith")
        );
    }

    /**
     * Verifies that a null full name is rejected.
     */
    @Test
    void shouldRejectNullFullName() {
        assertThrows(NullPointerException.class, () ->
                new Person(UUID.randomUUID(), "EMP001", null)
        );
    }

    /**
     * Verifies that a blank full name is rejected.
     */
    @Test
    void shouldRejectBlankFullName() {
        assertThrows(IllegalArgumentException.class, () ->
                new Person(UUID.randomUUID(), "EMP001", " ")
        );
    }
}