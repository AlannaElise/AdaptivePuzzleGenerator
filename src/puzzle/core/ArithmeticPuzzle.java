package puzzle.core;

import java.util.Random;
import java.util.ArrayList;

// Arithmetic puzzle - fill in the missing numbers to complete equations
// Each row is an equation: A op B = C, one value is hidden per equation
public class ArithmeticPuzzle implements Puzzle {

    public static final int COLS = 3; // [A, B, C] where A op B = C

    private final int rows;
    private final int[][] solution; // [row][0=A, 1=B, 2=C]
    private final int[][] grid;
    private final boolean[][] editable;
    private final char[] operators;
    private final int maxVal;

    public ArithmeticPuzzle() { this(4, 10); }

    public ArithmeticPuzzle(int rows, int maxVal) {
        this.rows = rows;
        this.maxVal = maxVal;
        solution = new int[rows][COLS];
        grid = new int[rows][COLS];
        editable = new boolean[rows][COLS];
        operators = new char[rows];
        generate();
    }

    private void generate() {
        Random rand = new Random();
        char[] ops = {'+', '-', '*'};

        for (int r = 0; r < rows; r++) {
            char op = ops[rand.nextInt(ops.length)];
            operators[r] = op;
            int a, b, c;

            // Generate valid equations
            do {
                a = rand.nextInt(maxVal) + 1;
                b = rand.nextInt(maxVal) + 1;
                if (op == '+') c = a + b;
                else if (op == '-') { if (a < b) { int t = a; a = b; b = t; } c = a - b; }
                else c = a * b;
            } while (c <= 0 || c > maxVal * maxVal);

            solution[r][0] = a;
            solution[r][1] = b;
            solution[r][2] = c;
            grid[r][0] = a;
            grid[r][1] = b;
            grid[r][2] = c;

            // Hide one value per row
            int hide = rand.nextInt(3);
            grid[r][hide] = 0;
            editable[r][hide] = true;
        }
    }

    public char getOperator(int row) { return operators[row]; }

    @Override public String getType()        { return "Arithmetic"; }
    @Override public int getSize()           { return rows; }
    @Override public int[][] getGrid()       { return grid; }
    @Override public int getMaxValue()       { return maxVal * maxVal; }
    @Override public String getDescription() { return rows + " Equation Arithmetic"; }

    @Override
    public void setCell(int row, int col, int value) {
        if (editable[row][col] && value >= 0)
            grid[row][col] = value;
    }

    @Override public boolean isCellEditable(int row, int col) { return editable[row][col]; }

    @Override
    public boolean isSolved() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] != solution[r][c]) return false;
        return true;
    }

    @Override
    public boolean validate() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < COLS; c++)
                if (grid[r][c] != 0 && grid[r][c] != solution[r][c]) return false;
        return true;
    }

    @Override
    public String getHint() {
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < COLS; c++)
                if (editable[r][c] && grid[r][c] != solution[r][c]) {
                    String pos = c == 0 ? "first" : c == 1 ? "second" : "result";
                    return "Equation " + (r+1) + ": the " + pos + " number should be " + solution[r][c];
                }
        return "Looking good!";
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < rows; r++) {
            String a = grid[r][0] == 0 ? "?" : String.valueOf(grid[r][0]);
            String b = grid[r][1] == 0 ? "?" : String.valueOf(grid[r][1]);
            String c = grid[r][2] == 0 ? "?" : String.valueOf(grid[r][2]);
            sb.append(a).append(" ").append(operators[r]).append(" ").append(b)
                    .append(" = ").append(c).append("\n");
        }
        return sb.toString();
    }
}