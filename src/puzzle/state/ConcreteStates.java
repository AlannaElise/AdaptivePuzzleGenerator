package puzzle.state;

import puzzle.factory.PuzzleFactory;
import java.awt.Color;

class BeginnerState extends DifficultyState {
    @Override public String getName()                          { return "Beginner"; }
    @Override public String getDescription()                   { return "Solve under 2 min to advance"; }
    @Override public int getHintCount()                        { return 3; }
    @Override public int getScoreMultiplier()                  { return 1; }
    @Override public PuzzleFactory.Difficulty getDifficulty()  { return PuzzleFactory.Difficulty.BEGINNER; }
    @Override public Color getColor()                          { return new Color(63, 185, 80); }
    @Override public int getFastThreshold()                    { return 60; }   // under 1 min = perfect
    @Override public int getNormalThreshold()                  { return 120; }  // under 2 min = advance

    @Override
    protected DifficultyState nextState(int event) {
        switch (event) {
            case PERFECT:      return challenge;
            case SOLVED_FAST:  return intermediate;
            case SOLVED_NORMAL:return intermediate;
            default:           return this;
        }
    }
}

class IntermediateState extends DifficultyState {
    @Override public String getName()                          { return "Intermediate"; }
    @Override public String getDescription()                   { return "Solve under 3 min to advance, slow drops back"; }
    @Override public int getHintCount()                        { return 2; }
    @Override public int getScoreMultiplier()                  { return 2; }
    @Override public PuzzleFactory.Difficulty getDifficulty()  { return PuzzleFactory.Difficulty.INTERMEDIATE; }
    @Override public Color getColor()                          { return new Color(255, 166, 87); }
    @Override public int getFastThreshold()                    { return 90; }   // under 1.5 min = perfect
    @Override public int getNormalThreshold()                  { return 180; }  // under 3 min = advance

    @Override
    protected DifficultyState nextState(int event) {
        switch (event) {
            case PERFECT:      return challenge;
            case SOLVED_FAST:  return advanced;
            case SOLVED_NORMAL:return advanced;
            case SOLVED_SLOW:  return beginner;
            default:           return this;
        }
    }
}

class AdvancedState extends DifficultyState {
    @Override public String getName()                          { return "Advanced"; }
    @Override public String getDescription()                   { return "Solve under 8 min for Challenge, slow drops back"; }
    @Override public int getHintCount()                        { return 1; }
    @Override public int getScoreMultiplier()                  { return 3; }
    @Override public PuzzleFactory.Difficulty getDifficulty()  { return PuzzleFactory.Difficulty.ADVANCED; }
    @Override public Color getColor()                          { return new Color(255, 123, 114); }
    @Override public int getFastThreshold()                    { return 240; }  // under 4 min = perfect
    @Override public int getNormalThreshold()                  { return 480; }  // under 8 min = stay

    @Override
    protected DifficultyState nextState(int event) {
        switch (event) {
            case PERFECT:      return challenge;
            case SOLVED_FAST:  return challenge;
            case SOLVED_NORMAL:return this;
            case SOLVED_SLOW:  return intermediate;
            default:           return this;
        }
    }
}

// Unlocked by a perfect run from any state
class ChallengeState extends DifficultyState {
    @Override public String getName()                          { return "Challenge"; }
    @Override public String getDescription()                   { return "Maximum difficulty. Solve under 10 min to stay"; }
    @Override public int getHintCount()                        { return 0; }
    @Override public int getScoreMultiplier()                  { return 5; }
    @Override public PuzzleFactory.Difficulty getDifficulty()  { return PuzzleFactory.Difficulty.CHALLENGE; }
    @Override public Color getColor()                          { return new Color(210, 168, 255); }
    @Override public int getFastThreshold()                    { return 300; }  // under 5 min = stay
    @Override public int getNormalThreshold()                  { return 600; }  // under 10 min = stay

    @Override
    protected DifficultyState nextState(int event) {
        switch (event) {
            case SOLVED_SLOW:  return advanced;
            default:           return this;
        }
    }
}