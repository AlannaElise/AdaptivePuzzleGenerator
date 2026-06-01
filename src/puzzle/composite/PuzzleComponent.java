package puzzle.composite;

// Composite pattern - shared interface for leaf and composite nodes
public interface PuzzleComponent {
    boolean validate();
    String render();
    String getName();
}