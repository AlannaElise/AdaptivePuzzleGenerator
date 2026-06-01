package puzzle.decorator;

import puzzle.core.Puzzle;

// Tracks elapsed time while the user solves a puzzle - counts up rather than down
public class TimeLimitDecorator extends PuzzleDecorator {

    private int secondsElapsed = 0;
    private boolean running = false;

    public TimeLimitDecorator(Puzzle wrapped) {
        super(wrapped);
    }

    public int getSecondsElapsed()   { return secondsElapsed; }
    public boolean isRunning()       { return running; }
    public boolean isExpired()       { return false; }
    public int getSecondsRemaining() { return 0; }
    public int getTimeLimitSeconds() { return 0; }

    public void tick()  { if (running) secondsElapsed++; }
    public void start() { running = true; }
    public void stop()  { running = false; }

    public void reset() {
        secondsElapsed = 0;
        running = false;
    }

    public String getFormattedTime() {
        return String.format("%d:%02d", secondsElapsed / 60, secondsElapsed % 60);
    }
}