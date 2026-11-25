package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.*;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleWorkingTimeCalculator.
 */
class ScheduleWorkingTimeCalculatorTest {

    private ScheduleWorkingTimeCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ScheduleWorkingTimeCalculator();
    }

    @Test
    void shouldCalculateContinuousScheduleWorkingTime() {
        ContinuousSchedule schedule = createContinuousSchedule();

        assertEquals(Duration.ofHours(8), calculator.calculate(schedule));
    }

    @Test
    void shouldCalculateContinuousScheduleOnWorkingDay() {
        ContinuousSchedule schedule = createContinuousSchedule();

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule, DayOfWeek.MONDAY)
        );
    }

    @Test
    void shouldReturnZeroForNonWorkingDayInContinuousSchedule() {
        ContinuousSchedule schedule = createContinuousSchedule();

        assertEquals(
                Duration.ZERO,
                calculator.calculate(schedule, DayOfWeek.SATURDAY)
        );
    }

    @Test
    void shouldCalculateOvernightContinuousSchedule() {
        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Night Schedule",
                new TimeSlot(LocalTime.of(22, 0), LocalTime.of(6, 0)),
                Set.of(DayOfWeek.MONDAY)
        );

        assertEquals(Duration.ofHours(8), calculator.calculate(schedule));
    }

    @Test
    void shouldCalculateSplitScheduleWorkingTime() {
        SplitSchedule schedule = createSplitSchedule();

        assertEquals(Duration.ofHours(8), calculator.calculate(schedule));
    }

    @Test
    void shouldCalculateSplitScheduleOnWorkingDay() {
        SplitSchedule schedule = createSplitSchedule();

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule, DayOfWeek.MONDAY)
        );
    }

    @Test
    void shouldReturnZeroForNonWorkingDayInSplitSchedule() {
        SplitSchedule schedule = createSplitSchedule();

        assertEquals(
                Duration.ZERO,
                calculator.calculate(schedule, DayOfWeek.SUNDAY)
        );
    }

    @Test
    void shouldCalculateSplitScheduleWithOvernightSlot() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Overnight Split Schedule",
                List.of(
                        new TimeSlot(LocalTime.of(20, 0), LocalTime.of(23, 0)),
                        new TimeSlot(LocalTime.of(23, 30), LocalTime.of(2, 30))
                ),
                Set.of(DayOfWeek.FRIDAY)
        );

        assertEquals(Duration.ofHours(6), calculator.calculate(schedule));
    }

    @Test
    void shouldCalculateShiftWorkingTime() {
        Shift shift = createMorningShift();

        assertEquals(Duration.ofHours(8), calculator.calculate(shift));
    }

    @Test
    void shouldCalculateAssignedShiftForDay() {
        ShiftSchedule schedule = createShiftSchedule();

        assertEquals(
                Duration.ofHours(8),
                calculator.calculate(schedule, DayOfWeek.MONDAY)
        );
    }

    @Test
    void shouldReturnZeroWhenNoShiftIsAssignedForDay() {
        ShiftSchedule schedule = createShiftSchedule();

        assertEquals(
                Duration.ZERO,
                calculator.calculate(schedule, DayOfWeek.SUNDAY)
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

    @Test
    void shouldRejectNullDayForContinuousSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(createContinuousSchedule(), null)
        );
    }

    @Test
    void shouldRejectNullDayForSplitSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(createSplitSchedule(), null)
        );
    }

    @Test
    void shouldRejectNullShiftSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate((ShiftSchedule) null, DayOfWeek.MONDAY)
        );
    }

    @Test
    void shouldRejectNullDayForShiftSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(createShiftSchedule(), null)
        );
    }

    private ContinuousSchedule createContinuousSchedule() {
        return new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                new TimeSlot(LocalTime.of(9, 0), LocalTime.of(17, 0)),
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                )
        );
    }

    private SplitSchedule createSplitSchedule() {
        return new SplitSchedule(
                UUID.randomUUID(),
                "Split Schedule",
                List.of(
                        new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
                        new TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0))
                ),
                Set.of(
                        DayOfWeek.MONDAY,
                        DayOfWeek.TUESDAY,
                        DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY,
                        DayOfWeek.FRIDAY
                )
        );
    }

    private Shift createMorningShift() {
        return new Shift(
                UUID.randomUUID(),
                "Morning Shift",
                new TimeSlot(LocalTime.of(6, 0), LocalTime.of(14, 0))
        );
    }

    private ShiftSchedule createShiftSchedule() {
        return new ShiftSchedule(
                UUID.randomUUID(),
                "Hospital Schedule",
                Map.of(
                        DayOfWeek.MONDAY, createMorningShift(),
                        DayOfWeek.TUESDAY, createMorningShift(),
                        DayOfWeek.WEDNESDAY, createMorningShift()
                )
        );
    }
}