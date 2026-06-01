package puzzle.decorator;

import puzzle.core.Puzzle;

// Adds hint functionality with a limited hint count
public class HintDecorator extends PuzzleDecorator {

    private int hintsRemaining;

    public HintDecorator(Puzzle wrapped, int hintCount) {
        super(wrapped);
        this.hintsRemaining = hintCount;
    }

    public int getHintsRemaining() { return hintsRemaining; }
    public boolean hasHints()      { return hintsRemaining > 0; }

    public String useHint() {
        if (hintsRemaining <= 0) return "No hints remaining!";
        hintsRemaining--;
        return wrapped.getHint();
    }
}