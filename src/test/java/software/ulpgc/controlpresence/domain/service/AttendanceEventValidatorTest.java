
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceEventValidatorTest {

    private AttendanceEventValidator validator;
    private Person person;

    @BeforeEach
    void setUp() {
        validator = new AttendanceEventValidator();

        person = new Person(
                UUID.randomUUID(),
                "EMP001",
                "John Smith"
        );
    }

    private AttendanceRecord createRecord(
            AttendanceEventType type,
            LocalDateTime dateTime
    ) {
        return new AttendanceRecord(
                UUID.randomUUID(),
                person,
                dateTime,
                type,
                "TEST"
        );
    }

    @Test
    void shouldAcceptFirstClockIn() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        assertDoesNotThrow(
                () -> validator.validateNextEvent(List.of(), clockIn)
        );
    }

    @Test
    void shouldRejectFirstClockOut() {
        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(List.of(), clockOut)
        );
    }

    @Test
    void shouldAcceptClockOutAfterClockIn() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertDoesNotThrow(
                () -> validator.validateNextEvent(
                        List.of(clockIn),
                        clockOut
                )
        );
    }

    @Test
    void shouldAcceptClockInAfterClockOut() {
        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        AttendanceRecord nextClockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 24, 8, 0)
        );

        assertDoesNotThrow(
                () -> validator.validateNextEvent(
                        List.of(clockOut),
                        nextClockIn
                )
        );
    }

    @Test
    void shouldRejectConsecutiveClockIns() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 9, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(
                        List.of(first),
                        second
                )
        );
    }

    @Test
    void shouldRejectConsecutiveClockOuts() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 9, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(
                        List.of(first),
                        second
                )
        );
    }

    @Test
    void shouldRejectEventsFromDifferentPeople() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        Person anotherPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Jane Smith"
        );

        AttendanceRecord second = new AttendanceRecord(
                UUID.randomUUID(),
                anotherPerson,
                LocalDateTime.of(2026, 11, 23, 16, 0),
                AttendanceEventType.CLOCK_OUT,
                "TEST"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(
                        List.of(first),
                        second
                )
        );
    }

    @Test
    void shouldRejectEventsOutOfChronologicalOrder() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 7, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(
                        List.of(first),
                        second
                )
        );
    }

    @Test
    void shouldRejectEventsAtSameTime() {
        LocalDateTime dateTime = LocalDateTime.of(
                2026, 11, 23, 8, 0
        );

        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_IN,
                dateTime
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_OUT,
                dateTime
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> validator.validateNextEvent(
                        List.of(first),
                        second
                )
        );
    }

    @Test
    void shouldRejectNullPreviousRecords() {
        AttendanceRecord record = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        assertThrows(
                NullPointerException.class,
                () -> validator.validateNextEvent(null, record)
        );
    }

    @Test
    void shouldRejectNullNewRecord() {
        assertThrows(
                NullPointerException.class,
                () -> validator.validateNextEvent(List.of(), null)
        );
    }
}
