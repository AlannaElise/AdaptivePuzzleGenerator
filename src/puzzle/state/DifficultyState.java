package puzzle.state;

import puzzle.factory.PuzzleFactory;
import java.awt.Color;

// State pattern - abstract superclass with references to all sibling state instances
public abstract class DifficultyState {

    public static final int SOLVED_FAST   = 0;
    public static final int SOLVED_NORMAL = 1;
    public static final int SOLVED_SLOW   = 2;
    public static final int PERFECT       = 3;

    // Shared sibling references - initialized once and reused across transitions
    protected static DifficultyState beginner;
    protected static DifficultyState intermediate;
    protected static DifficultyState advanced;
    protected static DifficultyState challenge;

    public static DifficultyState create() {
        beginner     = new BeginnerState();
        intermediate = new IntermediateState();
        advanced     = new AdvancedState();
        challenge    = new ChallengeState();
        return beginner;
    }

    protected abstract DifficultyState nextState(int event);

    public abstract String getName();
    public abstract String getDescription();
    public abstract int getHintCount();
    public abstract int getScoreMultiplier();
    public abstract PuzzleFactory.Difficulty getDifficulty();
    public abstract Color getColor();

    // Time thresholds (seconds) for evaluating solve speed
    public abstract int getFastThreshold();
    public abstract int getNormalThreshold();

    public DifficultyState processEvent(int event) {
        return nextState(event);
    }
}