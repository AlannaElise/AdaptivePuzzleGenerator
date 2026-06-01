package puzzle.core;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

// Fully generated Sudoku puzzle - supports 4x4, 6x6, and 9x9 grids
public class SudokuPuzzle implements Puzzle {

    private final int size;
    private final int boxRows;
    private final int boxCols;
    private final int[][] solution;
    private final int[][] grid;
    private final boolean[][] editable;
    private final int clues;

    public SudokuPuzzle() { this(4, 8); }

    public SudokuPuzzle(int size, int clues) {
        this.size = size;
        this.clues = clues;
        if (size == 4) { boxRows = 2; boxCols = 2; }
        else if (size == 6) { boxRows = 2; boxCols = 3; }
        else { boxRows = 3; boxCols = 3; }

        solution = new int[size][size];
        grid = new int[size][size];
        editable = new boolean[size][size];

        generate();
        createPuzzle();
    }

    private void generate() {
        fillGrid(0, 0);
    }

    private boolean fillGrid(int row, int col) {
        if (row == size) return true;
        int nextRow = (col == size - 1) ? row + 1 : row;
        int nextCol = (col == size - 1) ? 0 : col + 1;

        ArrayList<Integer> nums = new ArrayList<>();
        for (int i = 1; i <= size; i++) nums.add(i);
        Collections.shuffle(nums, new Random());

        for (int num : nums) {
            if (isValid(solution, row, col, num)) {
                solution[row][col] = num;
                if (fillGrid(nextRow, nextCol)) return true;
                solution[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int[][] board, int row, int col, int num) {
        for (int c = 0; c < size; c++)
            if (board[row][c] == num) return false;
        for (int r = 0; r < size; r++)
            if (board[r][col] == num) return false;
        int startRow = (row / boxRows) * boxRows;
        int startCol = (col / boxCols) * boxCols;
        for (int r = startRow; r < startRow + boxRows; r++)
            for (int c = startCol; c < startCol + boxCols; c++)
                if (board[r][c] == num) return false;
        return true;
    }

    private void createPuzzle() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = solution[r][c];

        int toRemove = (size * size) - clues;
        Random rand = new Random();
        ArrayList<Integer> cells = new ArrayList<>();
        for (int i = 0; i < size * size; i++) cells.add(i);
        Collections.shuffle(cells, rand);

        int removed = 0;
        for (int idx : cells) {
            if (removed >= toRemove) break;
            int r = idx / size, c = idx % size;
            grid[r][c] = 0;
            editable[r][c] = true;
            removed++;
        }
    }

    @Override public String getType()        { return "Sudoku"; }
    @Override public int getSize()           { return size; }
    @Override public int[][] getGrid()       { return grid; }
    @Override public int getMaxValue()       { return size; }
    @Override public String getDescription() { return size + "x" + size + " Sudoku"; }

    @Override
    public void setCell(int row, int col, int value) {
        if (editable[row][col] && value >= 0 && value <= size)
            grid[row][col] = value;
    }

    @Override
    public boolean isCellEditable(int row, int col) { return editable[row][col]; }

    @Override
    public boolean isSolved() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] != solution[r][c]) return false;
        return true;
    }

    @Override
    public boolean validate() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (grid[r][c] != 0 && grid[r][c] != solution[r][c]) return false;
        return true;
    }

    @Override
    public String getHint() {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (editable[r][c] && grid[r][c] != solution[r][c])
                    return "Row " + (r+1) + ", Col " + (c+1) + " should be " + solution[r][c];
        return "Looking good!";
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++)
                sb.append(grid[r][c] == 0 ? "." : grid[r][c]).append(" ");
            sb.append("\n");
        }
        return sb.toString();
    }

    public int getSolutionValue(int r, int c) { return solution[r][c]; }
    public int getBoxRows() { return boxRows; }
    public int getBoxCols() { return boxCols; }
}