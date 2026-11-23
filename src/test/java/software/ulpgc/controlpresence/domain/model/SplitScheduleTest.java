
package software.ulpgc.controlpresence.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the SplitSchedule domain model.
 */
class SplitScheduleTest {

    @Test
    void shouldCreateSplitScheduleWithValidData() {
        List<TimeSlot> timeSlots = List.of(
                new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)),
                new TimeSlot(LocalTime.of(15, 0), LocalTime.of(19, 0))
        );

        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Restaurant Schedule",
                timeSlots
        );

        assertNotNull(schedule);
        assertEquals("Restaurant Schedule", schedule.name());
        assertEquals(2, schedule.timeSlots().size());
    }

    @Test
    void shouldRejectNullId() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(
                        null,
                        "Restaurant Schedule",
                        List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)))
                )
        );
    }

    @Test
    void shouldRejectNullName() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(
                        UUID.randomUUID(),
                        null,
                        List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)))
                )
        );
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(IllegalArgumentException.class, () ->
                new SplitSchedule(
                        UUID.randomUUID(),
                        " ",
                        List.of(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)))
                )
        );
    }

    @Test
    void shouldRejectNullTimeSlots() {
        assertThrows(NullPointerException.class, () ->
                new SplitSchedule(
                        UUID.randomUUID(),
                        "Restaurant Schedule",
                        null
                )
        );
    }

    @Test
    void shouldRejectEmptyTimeSlots() {
        assertThrows(IllegalArgumentException.class, () ->
                new SplitSchedule(
                        UUID.randomUUID(),
                        "Restaurant Schedule",
                        List.of()
                )
        );
    }

    @Test
    void shouldCreateDefensiveCopyOfTimeSlots() {
        java.util.ArrayList<TimeSlot> timeSlots = new java.util.ArrayList<>();
        timeSlots.add(new TimeSlot(LocalTime.of(9, 0), LocalTime.of(13, 0)));

        SplitSchedule schedule = new SplitSchedule(
                UUID.randomUUID(),
                "Restaurant Schedule",
                timeSlots
        );

        timeSlots.clear();

        assertEquals(1, schedule.timeSlots().size());
    }
}