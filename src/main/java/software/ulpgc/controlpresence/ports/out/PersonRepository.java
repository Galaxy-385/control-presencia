
package software.ulpgc.controlpresence.ports.out;

import software.ulpgc.controlpresence.domain.model.Person;

import java.util.Optional;
import java.util.UUID;

/**
 * Defines the persistence operations for people.
 */
public interface PersonRepository {

    /**
     * Finds a person by their unique identifier.
     *
     * @param id the person's identifier
     * @return an optional containing the person if found
     */
    Optional<Person> findById(UUID id);
}
