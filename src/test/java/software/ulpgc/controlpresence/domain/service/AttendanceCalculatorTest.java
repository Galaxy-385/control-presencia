
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.AttendanceEventType;
import software.ulpgc.controlpresence.domain.model.AttendanceRecord;
import software.ulpgc.controlpresence.domain.model.Person;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AttendanceCalculatorTest {

    private AttendanceCalculator calculator;
    private Person person;

    @BeforeEach
    void setUp() {
        calculator = new AttendanceCalculator();
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
    void shouldCalculateWorkedTime() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculateWorkedTime(clockIn, clockOut)
        );
    }

    @Test
    void shouldCalculateWorkedTimeAcrossMidnight() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 22, 0)
        );

        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 24, 6, 0)
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculateWorkedTime(clockIn, clockOut)
        );
    }

    @Test
    void shouldRejectNullClockIn() {
        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertThrows(
                NullPointerException.class,
                () -> calculator.calculateWorkedTime(null, clockOut)
        );
    }

    @Test
    void shouldRejectNullClockOut() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        assertThrows(
                NullPointerException.class,
                () -> calculator.calculateWorkedTime(clockIn, null)
        );
    }

    @Test
    void shouldRejectIncorrectFirstEventType() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateWorkedTime(first, second)
        );
    }

    @Test
    void shouldRejectIncorrectSecondEventType() {
        AttendanceRecord first = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord second = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateWorkedTime(first, second)
        );
    }

    @Test
    void shouldRejectRecordsFromDifferentPeople() {
        Person anotherPerson = new Person(
                UUID.randomUUID(),
                "EMP002",
                "Jane Smith"
        );

        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        AttendanceRecord clockOut = new AttendanceRecord(
                UUID.randomUUID(),
                anotherPerson,
                LocalDateTime.of(2026, 11, 23, 16, 0),
                AttendanceEventType.CLOCK_OUT,
                "TEST"
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateWorkedTime(clockIn, clockOut)
        );
    }

    @Test
    void shouldRejectClockOutBeforeClockIn() {
        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                LocalDateTime.of(2026, 11, 23, 16, 0)
        );

        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                LocalDateTime.of(2026, 11, 23, 8, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateWorkedTime(clockIn, clockOut)
        );
    }

    @Test
    void shouldRejectClockOutAtSameTimeAsClockIn() {
        LocalDateTime dateTime = LocalDateTime.of(
                2026, 11, 23, 8, 0
        );

        AttendanceRecord clockIn = createRecord(
                AttendanceEventType.CLOCK_IN,
                dateTime
        );

        AttendanceRecord clockOut = createRecord(
                AttendanceEventType.CLOCK_OUT,
                dateTime
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculateWorkedTime(clockIn, clockOut)
        );
    }
}
