package puzzle.scheduler;

// Scheduler pattern - tasks implement this to define ordering relative to each other
public interface ScheduleOrdering {
    boolean scheduleBefore(ScheduleOrdering other);
    String getDescription();
}