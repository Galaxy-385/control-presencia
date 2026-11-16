
package software.ulpgc.controlpresence.domain.model;

import java.util.UUID;

/**
 * Defines the common contract for working schedules.
 *
 * Each schedule type provides its own representation
 * and rules for working hours.
 */
public interface Schedule {

    /**
     * Returns the unique identifier of the schedule.
     *
     * @return the schedule identifier
     */
    UUID id();

    /**
     * Returns the name of the schedule.
     *
     * @return the schedule name
     */
    String name();
}
