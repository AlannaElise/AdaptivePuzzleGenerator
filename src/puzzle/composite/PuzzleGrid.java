package puzzle.composite;

import puzzle.core.SudokuPuzzle;

// Top-level composite node - contains PuzzleRegion children, which contain PuzzleCell children
public class PuzzleGrid extends CompositePuzzleElement {

    private final PuzzleCell[][] cells;
    private final int size;

    public PuzzleGrid(int size) {
        this.size = size;
        this.cells = new PuzzleCell[size][size];
    }

    public static PuzzleGrid buildFrom(puzzle.core.Puzzle puzzle) {
        if (!(puzzle instanceof SudokuPuzzle) &&
                !puzzle.getType().equals("LogicGrid") &&
                !puzzle.getType().equals("Sudoku")) return null;

        int size = puzzle.getSize();
        int[][] grid = puzzle.getGrid();
        PuzzleGrid pg = new PuzzleGrid(size);

        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                pg.cells[r][c] = new PuzzleCell(r, c, grid[r][c],
                        getSolutionValue(puzzle, r, c),
                        puzzle.isCellEditable(r, c));

        if (puzzle instanceof SudokuPuzzle) {
            SudokuPuzzle sp = (SudokuPuzzle) puzzle;
            int bR = sp.getBoxRows(), bC = sp.getBoxCols();
            int idx = 0;
            for (int rOff = 0; rOff < size / bR; rOff++) {
                for (int cOff = 0; cOff < size / bC; cOff++) {
                    PuzzleRegion region = new PuzzleRegion("Region " + (++idx));
                    for (int r = rOff * bR; r < rOff * bR + bR; r++)
                        for (int c = cOff * bC; c < cOff * bC + bC; c++)
                            region.add(pg.cells[r][c]);
                    pg.al.add(region);
                }
            }
        } else {
            for (int r = 0; r < size; r++) {
                PuzzleRegion region = new PuzzleRegion("Row " + (r+1));
                for (int c = 0; c < size; c++)
                    region.add(pg.cells[r][c]);
                pg.al.add(region);
            }
        }
        return pg;
    }

    private static int getSolutionValue(puzzle.core.Puzzle puzzle, int r, int c) {
        if (puzzle instanceof SudokuPuzzle)
            return ((SudokuPuzzle) puzzle).getSolutionValue(r, c);
        return puzzle.getGrid()[r][c] == 0 ? 0 : puzzle.getGrid()[r][c];
    }

    public void updateFromPuzzle(puzzle.core.Puzzle puzzle) {
        int[][] grid = puzzle.getGrid();
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                if (cells[r][c] != null) cells[r][c].setValue(grid[r][c]);
    }

    public PuzzleCell getCell(int r, int c) { return cells[r][c]; }

    @Override
    public boolean validate() {
        for (PuzzleComponent region : al)
            if (!region.validate()) return false;
        return true;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++)
                sb.append(cells[r][c].render()).append(" ");
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getName() { return "PuzzleGrid"; }
}