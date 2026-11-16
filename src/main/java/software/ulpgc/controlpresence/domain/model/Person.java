
package software.ulpgc.controlpresence.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents an employee registered in the attendance system.
 *
 * @param id the unique identifier of the employee
 * @param employeeNumber the employee's identification number within the organization
 * @param fullName the employee's full name
 */
public record Person(
        UUID id,
        String employeeNumber,
        String fullName
) {

    /**
     * Creates a person after validating the required information.
     *
     * @throws NullPointerException if any required field is null
     * @throws IllegalArgumentException if any required text field is blank
     */
    public Person {
        Objects.requireNonNull(id, "Person ID cannot be null");
        Objects.requireNonNull(employeeNumber, "Employee number cannot be null");
        Objects.requireNonNull(fullName, "Full name cannot be null");

        if (employeeNumber.isBlank()) {
            throw new IllegalArgumentException("Employee number cannot be blank");
        }

        if (fullName.isBlank()) {
            throw new IllegalArgumentException("Full name cannot be blank");
        }
    }
}
