package puzzle.factory;

import puzzle.core.Puzzle;
import puzzle.core.SudokuPuzzle;
import puzzle.core.LogicGridPuzzle;
import puzzle.core.ArithmeticPuzzle;

import java.util.HashMap;
import java.util.Map;

// Factory Method pattern - uses reflection and a discriminator to create puzzle objects
public class PuzzleFactory {

    public enum Difficulty { BEGINNER, INTERMEDIATE, ADVANCED, CHALLENGE }

    private static final Map<String, String> TYPE_MAP = new HashMap<>();

    static {
        TYPE_MAP.put("Sudoku",     "puzzle.core.SudokuPuzzle");
        TYPE_MAP.put("LogicGrid",  "puzzle.core.LogicGridPuzzle");
        TYPE_MAP.put("Arithmetic", "puzzle.core.ArithmeticPuzzle");
    }

    public Puzzle createPuzzle(String type) {
        return createPuzzle(type, Difficulty.BEGINNER);
    }

    public Puzzle createPuzzle(String type, Difficulty difficulty) {
        switch (type) {
            case "Sudoku":     return createSudoku(difficulty);
            case "LogicGrid":  return createLogicGrid(difficulty);
            case "Arithmetic": return createArithmetic(difficulty);
            default:
                try {
                    return (Puzzle) Class.forName(TYPE_MAP.get(type))
                            .getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new RuntimeException("Could not create puzzle: " + type, e);
                }
        }
    }

    private SudokuPuzzle createSudoku(Difficulty d) {
        switch (d) {
            case BEGINNER:     return new SudokuPuzzle(4, 10);
            case INTERMEDIATE: return new SudokuPuzzle(6, 20);
            case ADVANCED:     return new SudokuPuzzle(9, 40);
            case CHALLENGE:    return new SudokuPuzzle(9, 28);
            default:           return new SudokuPuzzle(4, 10);
        }
    }

    private LogicGridPuzzle createLogicGrid(Difficulty d) {
        switch (d) {
            case BEGINNER:     return new LogicGridPuzzle(4, 10);
            case INTERMEDIATE: return new LogicGridPuzzle(5, 16);
            case ADVANCED:     return new LogicGridPuzzle(6, 22);
            case CHALLENGE:    return new LogicGridPuzzle(6, 18);
            default:           return new LogicGridPuzzle(4, 10);
        }
    }

    private ArithmeticPuzzle createArithmetic(Difficulty d) {
        switch (d) {
            case BEGINNER:     return new ArithmeticPuzzle(4, 10);
            case INTERMEDIATE: return new ArithmeticPuzzle(6, 20);
            case ADVANCED:     return new ArithmeticPuzzle(8, 30);
            case CHALLENGE:    return new ArithmeticPuzzle(10, 50);
            default:           return new ArithmeticPuzzle(4, 10);
        }
    }

    public static String[] getAvailableTypes() {
        return TYPE_MAP.keySet().toArray(new String[0]);
    }
}