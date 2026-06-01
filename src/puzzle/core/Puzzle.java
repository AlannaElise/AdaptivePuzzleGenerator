package puzzle.core;

// Core interface implemented by all puzzle types and decorators
public interface Puzzle {
    String getType();
    boolean validate();
    String render();
    int[][] getGrid();
    int getSize();
    void setCell(int row, int col, int value);
    boolean isCellEditable(int row, int col);
    boolean isSolved();
    String getHint();
    int getMaxValue();
    String getDescription();
}