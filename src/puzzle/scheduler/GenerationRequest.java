package puzzle.scheduler;

import puzzle.core.Puzzle;
import puzzle.factory.PuzzleFactory;
import java.util.function.Consumer;

// A generation request with a priority and difficulty level
public class GenerationRequest implements ScheduleOrdering {

    public enum Priority { HIGH(0), NORMAL(1), LOW(2);
        final int value;
        Priority(int v) { this.value = v; }
    }

    private final String puzzleType;
    private final PuzzleFactory.Difficulty difficulty;
    private final Priority priority;
    private final String description;
    final Consumer<Puzzle> callback;

    public GenerationRequest(String puzzleType, PuzzleFactory.Difficulty difficulty,
                             Priority priority, String description, Consumer<Puzzle> callback) {
        this.puzzleType  = puzzleType;
        this.difficulty  = difficulty;
        this.priority    = priority;
        this.description = description;
        this.callback    = callback;
    }

    public String getPuzzleType()              { return puzzleType; }
    public PuzzleFactory.Difficulty getDifficulty() { return difficulty; }
    public Priority getPriority()              { return priority; }

    @Override public String getDescription()   { return description; }

    @Override
    public boolean scheduleBefore(ScheduleOrdering other) {
        if (other instanceof GenerationRequest)
            return this.priority.value < ((GenerationRequest) other).priority.value;
        return false;
    }
}