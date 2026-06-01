package puzzle.decorator;

import puzzle.core.Puzzle;

// Decorator pattern - wraps a Puzzle and delegates all calls by default
public abstract class PuzzleDecorator implements Puzzle {

    protected final Puzzle wrapped;

    public PuzzleDecorator(Puzzle wrapped) { this.wrapped = wrapped; }

    @Override public String getType()                         { return wrapped.getType(); }
    @Override public boolean validate()                       { return wrapped.validate(); }
    @Override public String render()                          { return wrapped.render(); }
    @Override public int[][] getGrid()                        { return wrapped.getGrid(); }
    @Override public int getSize()                            { return wrapped.getSize(); }
    @Override public void setCell(int row, int col, int val)  { wrapped.setCell(row, col, val); }
    @Override public boolean isCellEditable(int r, int c)     { return wrapped.isCellEditable(r, c); }
    @Override public boolean isSolved()                       { return wrapped.isSolved(); }
    @Override public String getHint()                         { return wrapped.getHint(); }
    @Override public int getMaxValue()                        { return wrapped.getMaxValue(); }
    @Override public String getDescription()                  { return wrapped.getDescription(); }
}