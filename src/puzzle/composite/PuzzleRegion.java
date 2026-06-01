package puzzle.composite;

import java.util.ArrayList;
import java.util.List;

// Composite node - groups cells into a 2x2 region, extends CompositePuzzleElement
public class PuzzleRegion extends CompositePuzzleElement {

    private final String name;

    public PuzzleRegion(String name) { this.name = name; }

    @Override
    public boolean validate() {
        List<Integer> filled = new ArrayList<>();
        for (PuzzleComponent child : al) {
            if (!child.validate()) return false;
            if (child instanceof PuzzleCell && ((PuzzleCell) child).getValue() != 0) {
                PuzzleCell cell = (PuzzleCell) child;
                if (filled.contains(cell.getValue())) return false;
                filled.add(cell.getValue());
            }
        }
        return true;
    }

    @Override
    public String render() {
        StringBuilder sb = new StringBuilder("[" + name + ": ");
        for (PuzzleComponent child : al)
            sb.append(child.render()).append(" ");
        sb.append("]");
        return sb.toString();
    }

    @Override
    public String getName() { return name; }
}