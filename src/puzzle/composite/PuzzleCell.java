package puzzle.composite;

// Leaf node - represents a single cell, validates against its correct value
public class PuzzleCell implements PuzzleComponent {

    private final int row;
    private final int col;
    private int value;
    private int correctValue;
    private final boolean editable;

    public PuzzleCell(int row, int col, int value, int correctValue, boolean editable) {
        this.row          = row;
        this.col          = col;
        this.value        = value;
        this.correctValue = correctValue;
        this.editable     = editable;
    }

    public void setValue(int value)          { if (editable) this.value = value; }
    public void setCorrectValue(int correct) { this.correctValue = correct; }
    public int getValue()                    { return value; }
    public int getRow()                      { return row; }
    public int getCol()                      { return col; }
    public boolean isEditable()              { return editable; }

    @Override
    public boolean validate() {
        return value == 0 || correctValue == 0 || value == correctValue;
    }

    @Override
    public String render()  { return value == 0 ? "." : String.valueOf(value); }

    @Override
    public String getName() { return "Cell[" + row + "," + col + "]"; }
}