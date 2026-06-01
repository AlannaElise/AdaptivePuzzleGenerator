package puzzle.composite;

import java.util.ArrayList;

// Composite pattern - abstract class that holds the shared child list for all composite nodes
public abstract class CompositePuzzleElement implements PuzzleComponent {
    protected ArrayList<PuzzleComponent> al = new ArrayList<>();

    public void add(PuzzleComponent component) { al.add(component); }

    public ArrayList<PuzzleComponent> getChildren() { return al; }
}