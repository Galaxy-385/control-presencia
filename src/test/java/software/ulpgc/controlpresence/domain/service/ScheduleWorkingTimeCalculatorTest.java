
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ScheduleWorkingTimeCalculatorTest {

    private ScheduleWorkingTimeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ScheduleWorkingTimeCalculator();
    }

    @Test
    void shouldCalculateContinuousScheduleWorkingTime() {
        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Morning Schedule",
                new TimeSlot(
                        LocalTime.of(8, 0),
                        LocalTime.of(16, 0)
                )
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule)
        );
    }

    @Test
    void shouldCalculateOvernightContinuousSchedule() {
        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Night Schedule",
                new TimeSlot(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                )
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule)
        );
    }

    @Test
    void shouldCalculateSplitScheduleWorkingTime() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Split Schedule",
                List.of(
                        new TimeSlot(
                                LocalTime.of(9, 0),
                                LocalTime.of(13, 0)
                        ),
                        new TimeSlot(
                                LocalTime.of(15, 0),
                                LocalTime.of(19, 0)
                        )
                )
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule)
        );
    }

    @Test
    void shouldCalculateSplitScheduleWithOvernightSlot() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Overnight Split Schedule",
                List.of(
                        new TimeSlot(
                                LocalTime.of(20, 0),
                                LocalTime.of(23, 0)
                        ),
                        new TimeSlot(
                                LocalTime.of(23, 30),
                                LocalTime.of(2, 30)
                        )
                )
        );

        assertEquals(
                Duration.ofHours(6),
                calculator.calculate(schedule)
        );
    }

    @Test
    void shouldCalculateShiftWorkingTime() {
        Shift shift = new Shift(
                UUID.randomUUID(),
                "Night Shift",
                new TimeSlot(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                )
        );

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(shift)
        );
    }

    @Test
    void shouldRejectNullContinuousSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate((ContinuousSchedule) null)
        );
    }

    @Test
    void shouldRejectNullSplitSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate((SplitSchedule) null)
        );
    }

    @Test
    void shouldRejectNullShift() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate((Shift) null)
        );
    }
}
