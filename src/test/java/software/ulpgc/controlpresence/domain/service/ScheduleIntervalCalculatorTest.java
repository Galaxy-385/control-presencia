
package software.ulpgc.controlpresence.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import software.ulpgc.controlpresence.domain.model.ContinuousSchedule;
import software.ulpgc.controlpresence.domain.model.Schedule;
import software.ulpgc.controlpresence.domain.model.Shift;
import software.ulpgc.controlpresence.domain.model.ShiftSchedule;
import software.ulpgc.controlpresence.domain.model.SplitSchedule;
import software.ulpgc.controlpresence.domain.model.TimeSlot;
import software.ulpgc.controlpresence.domain.model.WorkingInterval;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ScheduleIntervalCalculator.
 */
class ScheduleIntervalCalculatorTest {

    private ScheduleIntervalCalculator calculator;

    private final LocalDate monday = LocalDate.of(2026, 11, 23);

    @BeforeEach
    void setUp() {
        calculator = new ScheduleIntervalCalculator();
    }

    @Test
    void shouldCalculateContinuousScheduleInterval() {
        ContinuousSchedule schedule = createContinuousSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(1, intervals.size());
        assertEquals(
                new WorkingInterval(
                        monday.atTime(9, 0),
                        monday.atTime(17, 0)
                ),
                intervals.getFirst()
        );
    }

    @Test
    void shouldReturnEmptyListForNonWorkingDayInContinuousSchedule() {
        ContinuousSchedule schedule = createContinuousSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday.plusDays(5)
        );

        assertTrue(intervals.isEmpty());
    }

    @Test
    void shouldCalculateOvernightContinuousSchedule() {
        ContinuousSchedule schedule = new ContinuousSchedule(
                UUID.randomUUID(),
                "Night Schedule",
                new TimeSlot(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                ),
                Set.of(DayOfWeek.MONDAY)
        );

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(
                new WorkingInterval(
                        monday.atTime(22, 0),
                        monday.plusDays(1).atTime(6, 0)
                ),
                intervals.getFirst()
        );
    }

    @Test
    void shouldCalculateAllIntervalsForSplitSchedule() {
        SplitSchedule schedule = createSplitSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(2, intervals.size());

        assertEquals(
                new WorkingInterval(
                        monday.atTime(9, 0),
                        monday.atTime(13, 0)
                ),
                intervals.get(0)
        );

        assertEquals(
                new WorkingInterval(
                        monday.atTime(15, 0),
                        monday.atTime(19, 0)
                ),
                intervals.get(1)
        );
    }

    @Test
    void shouldSortSplitScheduleIntervalsByStartTime() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Unordered Split Schedule",
                List.of(
                        new TimeSlot(
                                LocalTime.of(15, 0),
                                LocalTime.of(19, 0)
                        ),
                        new TimeSlot(
                                LocalTime.of(9, 0),
                                LocalTime.of(13, 0)
                        )
                ),
                Set.of(DayOfWeek.MONDAY)
        );

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(monday.atTime(9, 0), intervals.get(0).startDateTime());
        assertEquals(monday.atTime(15, 0), intervals.get(1).startDateTime());
    }

    @Test
    void shouldReturnEmptyListForNonWorkingDayInSplitSchedule() {
        SplitSchedule schedule = createSplitSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday.plusDays(5)
        );

        assertTrue(intervals.isEmpty());
    }

    @Test
    void shouldCalculateOvernightIntervalInSplitSchedule() {
        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Night Split Schedule",
                List.of(
                        new TimeSlot(
                                LocalTime.of(22, 0),
                                LocalTime.of(6, 0)
                        )
                ),
                Set.of(DayOfWeek.MONDAY)
        );

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(
                new WorkingInterval(
                        monday.atTime(22, 0),
                        monday.plusDays(1).atTime(6, 0)
                ),
                intervals.getFirst()
        );
    }

    @Test
    void shouldCalculateAssignedShiftForDate() {
        ShiftSchedule schedule = createShiftSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(1, intervals.size());

        assertEquals(
                new WorkingInterval(
                        monday.atTime(6, 0),
                        monday.atTime(14, 0)
                ),
                intervals.getFirst()
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoShiftIsAssignedForDate() {
        ShiftSchedule schedule = createShiftSchedule();

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday.plusDays(6)
        );

        assertTrue(intervals.isEmpty());
    }

    @Test
    void shouldCalculateOvernightShift() {
        Shift nightShift = new Shift(
                UUID.randomUUID(),
                "Night Shift",
                new TimeSlot(
                        LocalTime.of(22, 0),
                        LocalTime.of(6, 0)
                )
        );

        ShiftSchedule schedule = new ShiftSchedule(
                UUID.randomUUID(),
                "Night Schedule",
                Map.of(DayOfWeek.MONDAY, nightShift)
        );

        List<WorkingInterval> intervals = calculator.calculate(
                schedule,
                monday
        );

        assertEquals(
                new WorkingInterval(
                        monday.atTime(22, 0),
                        monday.plusDays(1).atTime(6, 0)
                ),
                intervals.getFirst()
        );
    }

    @Test
    void shouldRejectNullSchedule() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(null, monday)
        );
    }

    @Test
    void shouldRejectNullDate() {
        assertThrows(
                NullPointerException.class,
                () -> calculator.calculate(createContinuousSchedule(), null)
        );
    }

    @Test
    void shouldRejectUnsupportedScheduleType() {
        Schedule unsupportedSchedule = new Schedule() {
            @Override
            public UUID id() {
                return UUID.randomUUID();
            }

            @Override
            public String name() {
                return "Unsupported Schedule";
            }
        };

        assertThrows(
                IllegalArgumentException.class,
                () -> calculator.calculate(unsupportedSchedule, monday)
        );
    }

    private ContinuousSchedule createContinuousSchedule() {
        return new ContinuousSchedule(
                UUID.randomUUID(),
                "Office Schedule",
                new TimeSlot(
                        LocalTime.of(9, 0),
                        LocalTime.of(17, 0)
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

    private SplitSchedule createSplitSchedule() {
        return new SplitSchedule(
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

    private ShiftSchedule createShiftSchedule() {
        Shift morningShift = new Shift(
                UUID.randomUUID(),
                "Morning Shift",
                new TimeSlot(
                        LocalTime.of(6, 0),
                        LocalTime.of(14, 0)
                )
        );

        return new ShiftSchedule(
                UUID.randomUUID(),
                "Hospital Schedule",
                Map.of(
                        DayOfWeek.MONDAY, morningShift,
                        DayOfWeek.TUESDAY, morningShift,
                        DayOfWeek.WEDNESDAY, morningShift
                )
        );
    }
}
