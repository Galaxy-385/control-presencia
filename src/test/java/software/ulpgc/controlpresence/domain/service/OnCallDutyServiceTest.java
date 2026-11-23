
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.OnCallDuty;
import software.ulpgc.controlpresence.domain.model.Person;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OnCallDutyServiceTest {

    private OnCallDutyService service;
    private OnCallDuty duty;
    private LocalDateTime start;
    private LocalDateTime end;

    @BeforeEach
    void setUp() {
        service = new OnCallDutyService();

        Person person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );

        start = LocalDateTime.of(2026, 11, 23, 20, 0);
        end = LocalDateTime.of(2026, 11, 24, 8, 0);

        duty = new OnCallDuty(
                UUID.randomUUID(),
                person,
                start,
                end
        );
    }

    @Test
    void shouldBeActiveAtStartDateTime() {
        assertTrue(service.isActiveAt(duty, start));
    }

    @Test
    void shouldBeActiveDuringDuty() {
        assertTrue(
                service.isActiveAt(
                        duty,
                        LocalDateTime.of(2026, 11, 24, 2, 0)
                )
        );
    }

    @Test
    void shouldNotBeActiveAtEndDateTime() {
        assertFalse(service.isActiveAt(duty, end));
    }

    @Test
    void shouldNotBeActiveBeforeStartDateTime() {
        assertFalse(
                service.isActiveAt(
                        duty,
                        start.minusMinutes(1)
                )
        );
    }

    @Test
    void shouldNotBeActiveAfterEndDateTime() {
        assertFalse(
                service.isActiveAt(
                        duty,
                        end.plusMinutes(1)
                )
        );
    }

    @Test
    void shouldRejectNullDuty() {
        assertThrows(
                NullPointerException.class,
                () -> service.isActiveAt(null, start)
        );
    }

    @Test
    void shouldRejectNullDateTime() {
        assertThrows(
                NullPointerException.class,
                () -> service.isActiveAt(duty, null)
        );
    }
}
