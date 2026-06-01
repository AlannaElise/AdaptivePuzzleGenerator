package puzzle.decorator;

import puzzle.core.Puzzle;

// Adds score tracking with a difficulty multiplier
public class ScoringDecorator extends PuzzleDecorator {

    private int score = 0;
    private int correctEntries = 0;
    private int wrongEntries = 0;
    private final int multiplier;

    private static final int POINTS_CORRECT = 50;
    private static final int POINTS_WRONG   = -20;
    private static final int POINTS_HINT    = -30;
    private static final int POINTS_SOLVE   = 200;

    public ScoringDecorator(Puzzle wrapped, int multiplier) {
        super(wrapped);
        this.multiplier = multiplier;
    }

    public int getScore()          { return score; }
    public int getCorrectEntries() { return correctEntries; }
    public int getWrongEntries()   { return wrongEntries; }

    public void recordCorrect()  { correctEntries++; score += POINTS_CORRECT * multiplier; }
    public void recordWrong()    { wrongEntries++;   score = Math.max(0, score + POINTS_WRONG); }
    public void recordHintUsed() { score = Math.max(0, score + POINTS_HINT); }
    public void recordSolved()   { score += POINTS_SOLVE * multiplier; }

    // Speed bonus: faster solve = more bonus points
    public int calculateFinalScore(int secondsElapsed, int fastThreshold) {
        int speedBonus = 0;
        if (secondsElapsed > 0 && fastThreshold > 0) {
            speedBonus = Math.max(0, (fastThreshold - secondsElapsed) * multiplier * 2);
        }
        return score + speedBonus;
    }
}