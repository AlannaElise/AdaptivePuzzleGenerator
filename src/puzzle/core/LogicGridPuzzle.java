package puzzle.core;

import java.util.Random;
import java.util.ArrayList;
import java.util.Collections;

// Logic grid puzzle - rows and columns must each contain 1..size with no repeats
// No box regions, so only row and column constraints apply
public class LogicGridPuzzle implements Puzzle {

    private final int size;
    private final int[][] solution;
    private final int[][] grid;
    private final boolean[][] editable;
    private final String[] clues;

    public LogicGridPuzzle() { this(4, 8); }

    public LogicGridPuzzle(int size, int clues) {
        this.size = size;
        solution = new int[size][size];
        grid = new int[size][size];
        editable = new boolean[size][size];

        generate();
        createPuzzle(clues);
        this.clues = buildClues();
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
            if (isValid(row, col, num)) {
                solution[row][col] = num;
                if (fillGrid(nextRow, nextCol)) return true;
                solution[row][col] = 0;
            }
        }
        return false;
    }

    private boolean isValid(int row, int col, int num) {
        for (int c = 0; c < size; c++)
            if (solution[row][c] == num) return false;
        for (int r = 0; r < size; r++)
            if (solution[r][col] == num) return false;
        return true;
    }

    private void createPuzzle(int clueCount) {
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                grid[r][c] = solution[r][c];

        int toRemove = (size * size) - clueCount;
        ArrayList<Integer> cells = new ArrayList<>();
        for (int i = 0; i < size * size; i++) cells.add(i);
        Collections.shuffle(cells, new Random());

        int removed = 0;
        for (int idx : cells) {
            if (removed >= toRemove) break;
            int r = idx / size, c = idx % size;
            grid[r][c] = 0;
            editable[r][c] = true;
            removed++;
        }
    }

    private String[] buildClues() {
        ArrayList<String> result = new ArrayList<>();
        // Build row sum clues
        for (int r = 0; r < size; r++) {
            int sum = 0;
            for (int c = 0; c < size; c++) sum += solution[r][c];
            result.add("Row " + (r+1) + " sums to " + sum);
        }
        // Build column sum clues
        for (int c = 0; c < size; c++) {
            int sum = 0;
            for (int r = 0; r < size; r++) sum += solution[r][c];
            result.add("Col " + (c+1) + " sums to " + sum);
        }
        return result.toArray(new String[0]);
    }

    public String[] getClues() { return clues; }

    @Override public String getType()        { return "LogicGrid"; }
    @Override public int getSize()           { return size; }
    @Override public int[][] getGrid()       { return grid; }
    @Override public int getMaxValue()       { return size; }
    @Override public String getDescription() { return size + "x" + size + " Logic Grid"; }

    @Override
    public void setCell(int row, int col, int value) {
        if (editable[row][col] && value >= 0 && value <= size)
            grid[row][col] = value;
    }

    @Override public boolean isCellEditable(int row, int col) { return editable[row][col]; }

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
}