package puzzle.gui;

import puzzle.composite.PuzzleGrid;
import puzzle.core.ArithmeticPuzzle;
import puzzle.core.LogicGridPuzzle;
import puzzle.core.Puzzle;
import puzzle.core.SudokuPuzzle;
import puzzle.decorator.HintDecorator;
import puzzle.decorator.ScoringDecorator;
import puzzle.decorator.TimeLimitDecorator;
import puzzle.factory.PuzzleFactory;
import puzzle.scheduler.GenerationRequest;
import puzzle.scheduler.PuzzleScheduler;
import puzzle.state.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class MainWindow extends JFrame {

    // Colors
    private static final Color BG     = new Color(13, 17, 23);
    private static final Color PANEL  = new Color(22, 27, 34);
    private static final Color BORDER = new Color(33, 38, 45);
    private static final Color ACCENT = new Color(88, 166, 255);
    private static final Color GREEN  = new Color(63, 185, 80);
    private static final Color RED    = new Color(255, 123, 114);
    private static final Color ORANGE = new Color(255, 166, 87);
    private static final Color PURPLE = new Color(210, 168, 255);
    private static final Color MUTED  = new Color(139, 148, 158);
    private static final Color WHITE  = new Color(230, 237, 243);

    // Patterns
    private final PuzzleFactory   factory   = new PuzzleFactory();
    private final UserSession     session   = new UserSession();
    private final PuzzleScheduler scheduler = new PuzzleScheduler();

    // Current puzzle stack
    private Puzzle             basePuzzle;
    private HintDecorator      hintDecorator;
    private TimeLimitDecorator timerDecorator;
    private ScoringDecorator   scoringDecorator;
    private PuzzleGrid         puzzleGrid;
    private Puzzle             nextPuzzle = null;
    private String             currentType = "Sudoku";

    // Timers
    private Timer countdownTimer;

    // Grid UI
    private JPanel      gridPanel;
    private JButton[][] cellButtons;
    private int         currentGridSize = 0;

    // Custom-painted sidebar panel
    private SidebarPanel sidebar;

    // Top bar and status labels
    private JLabel      validationLabel;
    private JLabel      puzzleDescLabel;
    private JButton     newPuzzleButton;
    private JComboBox<String> typeSelector;

    // Clues panel for Logic Grid
    private JTextArea  cluesArea;
    private JScrollPane cluesScroll;

    // Arithmetic panel
    private JPanel arithmeticPanel;

    public MainWindow() {
        super("Adaptive Puzzle Generator  |  SWENG 421");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(1050, 700));
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout(0, 0));

        // Disable Swing tooltip system for the whole window
        ToolTipManager.sharedInstance().setEnabled(false);
        ToolTipManager.sharedInstance().setInitialDelay(Integer.MAX_VALUE);
        ToolTipManager.sharedInstance().setDismissDelay(0);

        buildUI();
        unregisterAllTooltips(getContentPane());
        pack();
        setLocationRelativeTo(null);

        scheduler.start();
        loadNewPuzzle("Sudoku");
    }

    // ── UI Construction ──────────────────────────────────────────────────────

    private void buildUI() {
        add(buildTopBar(),    BorderLayout.NORTH);
        add(buildCenter(),    BorderLayout.CENTER);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildBottomBar(), BorderLayout.SOUTH);
    }

    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };
        bar.setBackground(BG);
        bar.setOpaque(true);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER),
                new EmptyBorder(10, 16, 10, 16)));
        bar.setPreferredSize(new Dimension(0, 52));

        puzzleDescLabel = new NoTooltipLabel("Adaptive Puzzle Generator");
        puzzleDescLabel.setFont(new Font("Calibri", Font.BOLD, 16));
        puzzleDescLabel.setForeground(WHITE);
        puzzleDescLabel.setOpaque(true);
        puzzleDescLabel.setBackground(BG);
        puzzleDescLabel.setPreferredSize(new Dimension(380, 32));
        puzzleDescLabel.setMaximumSize(new Dimension(380, 32));
        ToolTipManager.sharedInstance().unregisterComponent(puzzleDescLabel);
        bar.add(puzzleDescLabel, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        right.setBackground(BG);
        right.setOpaque(true);
        right.setDoubleBuffered(true);
        right.add(lbl("Type:", MUTED, 11, false));
        typeSelector = new JComboBox<>(new String[]{"Sudoku", "LogicGrid", "Arithmetic"});
        typeSelector.setToolTipText(null);
        styleCombo(typeSelector);
        right.add(typeSelector);

        newPuzzleButton = btn("New Puzzle", ACCENT);
        newPuzzleButton.setOpaque(true);
        newPuzzleButton.addActionListener(e -> onNewPuzzle());
        right.add(newPuzzleButton);
        bar.add(right, BorderLayout.EAST);
        return bar;
    }

    private JPanel buildCenter() {
        JPanel center = dark(new BorderLayout(0, 8));
        center.setBorder(new EmptyBorder(16, 16, 8, 8));


        gridPanel = dark(new GridLayout(1, 1));
        gridPanel.setPreferredSize(new Dimension(440, 440));


        arithmeticPanel = dark(new GridBagLayout());


        cluesArea = new JTextArea();
        cluesArea.setEditable(false);
        cluesArea.setBackground(PANEL);
        cluesArea.setForeground(MUTED);
        cluesArea.setFont(new Font("Consolas", Font.PLAIN, 11));
        cluesArea.setBorder(new EmptyBorder(8, 8, 8, 8));
        cluesScroll = new JScrollPane(cluesArea);
        cluesScroll.setBorder(BorderFactory.createLineBorder(BORDER));
        cluesScroll.setPreferredSize(new Dimension(440, 100));
        cluesScroll.getViewport().setBackground(PANEL);

        validationLabel = new NoTooltipLabel("Select a puzzle type and press New Puzzle", SwingConstants.CENTER);
        validationLabel.setFont(new Font("Calibri", Font.ITALIC, 12));
        validationLabel.setForeground(MUTED);
        validationLabel.setBorder(new EmptyBorder(4, 0, 0, 0));

        center.add(gridPanel, BorderLayout.CENTER);

        JPanel south = dark(new BorderLayout());
        south.add(cluesScroll, BorderLayout.NORTH);
        south.add(validationLabel, BorderLayout.SOUTH);
        center.add(south, BorderLayout.SOUTH);

        cluesScroll.setVisible(false);
        return center;
    }

    private SidebarPanel buildSidePanel() {
        sidebar = new SidebarPanel();
        sidebar.setOnHint(this::onUseHint);
        sidebar.setOnCheck(this::onCheck);
        sidebar.setOnClear(this::onClear);
        scheduler.setStatusListener(msg -> sidebar.setSchedulerText(msg));
        return sidebar;
    }

    private JPanel buildBottomBar() {
        JPanel bar = dark(new BorderLayout());
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, BORDER),
                new EmptyBorder(5, 14, 5, 14)));
        bar.add(lbl("Factory Method  |  Composite  |  Decorator  |  State  |  Scheduler", MUTED, 9, false),
                BorderLayout.CENTER);
        return bar;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private JPanel buildCard(String name, Color color) {
        JPanel c = new JPanel();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        c.setBackground(PANEL);
        c.setOpaque(true);
        c.setDoubleBuffered(true);
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, color),
                new EmptyBorder(7, 9, 7, 9)));
        JLabel nl = new NoTooltipLabel(name);
        nl.setFont(new Font("Consolas", Font.PLAIN, 8));
        nl.setForeground(color);
        nl.setBackground(PANEL);
        nl.setOpaque(true);
        nl.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(nl);
        c.add(Box.createVerticalStrut(3));
        return c;
    }

    private JPanel dark(LayoutManager lm) {
        JPanel p = new JPanel(lm);
        p.setBackground(BG);
        return p;
    }

    // Unregisters all components from the tooltip manager
    private void unregisterAllTooltips(java.awt.Container container) {
        for (java.awt.Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                ToolTipManager.sharedInstance().unregisterComponent((JComponent) comp);
                ((JComponent) comp).setToolTipText(null);
            }
            if (comp instanceof java.awt.Container) {
                unregisterAllTooltips((java.awt.Container) comp);
            }
        }
    }

    // Prevents JLabel from auto-showing its text as a tooltip when truncated
    private static class NoTooltipLabel extends JLabel {
        public NoTooltipLabel(String text) { super(text); }
        public NoTooltipLabel(String text, int alignment) { super(text, alignment); }
        @Override public String getToolTipText() { return null; }
        @Override public String getToolTipText(MouseEvent e) { return null; }
    }

    private JLabel lbl(String t, Color c, int size, boolean bold) {
        JLabel l = new NoTooltipLabel(t);
        l.setFont(new Font("Calibri", bold ? Font.BOLD : Font.PLAIN, size));
        l.setForeground(c);
        return l;
    }

    private JButton btn(String text, Color color) {


        Color normalBg = blendWithDark(color, 0.15f);
        Color hoverBg  = blendWithDark(color, 0.30f);
        JButton b = new JButton(text);
        b.setBackground(normalBg);
        b.setForeground(color);
        b.setFont(new Font("Calibri", Font.BOLD, 11));
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                new EmptyBorder(5, 12, 5, 12)));
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setToolTipText(null);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { b.setBackground(hoverBg); }
            public void mouseExited(MouseEvent e)  { b.setBackground(normalBg); }
        });
        return b;
    }

    // Returns a fully opaque color blended between the accent color and the dark background
    private Color blendWithDark(Color c, float ratio) {
        int r = (int)(BG.getRed()   + (c.getRed()   - BG.getRed())   * ratio);
        int g = (int)(BG.getGreen() + (c.getGreen() - BG.getGreen()) * ratio);
        int b = (int)(BG.getBlue()  + (c.getBlue()  - BG.getBlue())  * ratio);
        return new Color(
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b))
        );
    }

    private void styleCombo(JComboBox<String> cb) {
        cb.setBackground(PANEL);
        cb.setForeground(WHITE);
        cb.setFont(new Font("Calibri", Font.PLAIN, 12));
        cb.setBorder(BorderFactory.createLineBorder(BORDER));
    }

    // ── Grid Building ────────────────────────────────────────────────────────

    private void buildGridUI(int size) {
        if (size == currentGridSize && !(basePuzzle instanceof ArithmeticPuzzle)) return;
        currentGridSize = size;
        gridPanel.removeAll();

        if (basePuzzle instanceof ArithmeticPuzzle) {
            buildArithmeticUI();
        } else {
            gridPanel.setLayout(new GridLayout(size, size, 2, 2));
            cellButtons = new JButton[size][size];
            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    JButton b = new JButton();
                    int fontSize = size <= 4 ? 20 : size <= 6 ? 16 : 13;
                    b.setFont(new Font("Calibri", Font.BOLD, fontSize));
                    b.setBackground(PANEL);
                    b.setForeground(ACCENT);
                    b.setFocusPainted(false);
                    b.setOpaque(true);
                    b.setToolTipText(null);
                    final int row = r, col = c;
                    b.addActionListener(e -> onCellClick(row, col));
                    cellButtons[r][c] = b;
                    gridPanel.add(b);
                }
            }
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void buildArithmeticUI() {
        ArithmeticPuzzle ap = (ArithmeticPuzzle) basePuzzle;
        int rows = ap.getSize();
        gridPanel.setLayout(new GridBagLayout());
        cellButtons = new JButton[rows][ArithmeticPuzzle.COLS];

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 6, 4, 6);

        for (int r = 0; r < rows; r++) {
            int[][] grid = ap.getGrid();
            char op = ap.getOperator(r);

            for (int c = 0; c < ArithmeticPuzzle.COLS; c++) {
                gbc.gridx = c * 2;
                gbc.gridy = r;

                if (ap.isCellEditable(r, c)) {
                    JButton b = new JButton(grid[r][c] == 0 ? "?" : String.valueOf(grid[r][c]));
                    b.setFont(new Font("Calibri", Font.BOLD, 18));
                    b.setPreferredSize(new Dimension(60, 40));
                    b.setBackground(PANEL);
                    b.setForeground(ACCENT);
                    b.setFocusPainted(false);
                    b.setOpaque(true);
                    b.setToolTipText(null);
                    b.setBorder(BorderFactory.createLineBorder(ACCENT, 1));
                    final int row = r, col = c;
                    b.addActionListener(e -> onArithmeticCellClick(row, col));
                    cellButtons[r][c] = b;
                    gridPanel.add(b, gbc);
                } else {
                    JLabel valLbl = new NoTooltipLabel(String.valueOf(grid[r][c]), SwingConstants.CENTER);
                    valLbl.setFont(new Font("Calibri", Font.BOLD, 18));
                    valLbl.setForeground(WHITE);
                    valLbl.setPreferredSize(new Dimension(60, 40));
                    gridPanel.add(valLbl, gbc);
                }


                if (c < ArithmeticPuzzle.COLS - 1) {
                    gbc.gridx = c * 2 + 1;
                    String sym = (c == 0) ? String.valueOf(op) : "=";
                    JLabel opLbl = new NoTooltipLabel(sym, SwingConstants.CENTER);
                    opLbl.setFont(new Font("Calibri", Font.BOLD, 18));
                    opLbl.setForeground(ORANGE);
                    opLbl.setPreferredSize(new Dimension(24, 40));
                    gridPanel.add(opLbl, gbc);
                }
            }
        }
    }

    // ── Game Logic ────────────────────────────────────────────────────────────

    private void loadNewPuzzle(String type) {
        currentType = type;
        if (countdownTimer != null) countdownTimer.stop();

        PuzzleFactory.Difficulty diff = session.getCurrentState().getDifficulty();

        if (nextPuzzle != null && nextPuzzle.getType().equals(type)) {
            basePuzzle = nextPuzzle;
            nextPuzzle = null;
        } else {
            basePuzzle = factory.createPuzzle(type, diff);
        }

        hintDecorator    = new HintDecorator(basePuzzle, session.getHintCount());
        timerDecorator   = new TimeLimitDecorator(hintDecorator);
        scoringDecorator = new ScoringDecorator(timerDecorator, session.getScoreMultiplier());

        if (!(basePuzzle instanceof ArithmeticPuzzle)) {
            puzzleGrid = PuzzleGrid.buildFrom(basePuzzle);
        }

        currentGridSize = -1;
        buildGridUI(basePuzzle.getSize());
        refreshGridDisplay();
        updateClues();
        updateSidebar();
        startTimer();
        scheduleNext(type);

        puzzleDescLabel.setText(basePuzzle.getDescription() + "  |  " +
                session.getCurrentState().getName() + " difficulty");
        puzzleDescLabel.setToolTipText(null);
        puzzleDescLabel.setPreferredSize(new Dimension(380, 32));
        setStatus("Puzzle loaded. Good luck!", MUTED);
    }

    private void scheduleNext(String type) {
        PuzzleFactory.Difficulty diff = session.getCurrentState().getDifficulty();
        scheduler.schedule(type, diff, GenerationRequest.Priority.LOW,
                "Pre-generating next " + type,
                p -> nextPuzzle = p);
    }

    private void startTimer() {
        timerDecorator.reset();
        timerDecorator.start();
        sidebar.setTimerText("0:00");
        countdownTimer = new Timer(1000, e -> {
            timerDecorator.tick();
            sidebar.setTimerText(timerDecorator.getFormattedTime());
        });
        countdownTimer.start();
    }

    private void onCellClick(int row, int col) {
        if (!basePuzzle.isCellEditable(row, col)) return;

        int max = basePuzzle.getMaxValue();
        String input = JOptionPane.showInputDialog(this,
                "Enter a number (1-" + max + ", or 0 to clear):", "Input",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            int val = Integer.parseInt(input.trim());
            if (val < 0 || val > max) {
                setStatus("Value must be between 0 and " + max, RED);
                return;
            }
            basePuzzle.setCell(row, col, val);
            if (puzzleGrid != null) puzzleGrid.updateFromPuzzle(basePuzzle);

            refreshGridDisplay();
            updateSidebar();
            updateValidationStatus();

            if (val != 0) {
                boolean correct = puzzleGrid != null
                        ? puzzleGrid.getCell(row, col).validate()
                        : basePuzzle.validate();
                if (correct) { scoringDecorator.recordCorrect(); flashCell(row, col, GREEN); }
                else         { scoringDecorator.recordWrong();   flashCell(row, col, RED); }
            }
        } catch (NumberFormatException ex) {
            setStatus("Please enter a valid number.", RED);
            return;
        }

        if (basePuzzle.isSolved()) onPuzzleSolved();
    }

    private void onArithmeticCellClick(int row, int col) {
        ArithmeticPuzzle ap = (ArithmeticPuzzle) basePuzzle;
        int current = ap.getGrid()[row][col];
        int max     = ap.getMaxValue();


        String input = JOptionPane.showInputDialog(this, "Enter value:", "Input",
                JOptionPane.PLAIN_MESSAGE);
        if (input == null) return;
        try {
            int val = Integer.parseInt(input.trim());
            if (val < 0 || val > max) {
                setStatus("Value must be between 0 and " + max, RED);
                return;
            }
            ap.setCell(row, col, val);

            if (val != 0) {
                if (ap.validate()) { scoringDecorator.recordCorrect(); }
                else               { scoringDecorator.recordWrong(); }
            }


            currentGridSize = -1;
            gridPanel.removeAll();
            buildArithmeticUI();
            gridPanel.revalidate();
            gridPanel.repaint();

            updateSidebar();
            updateValidationStatus();
            if (ap.isSolved()) onPuzzleSolved();
        } catch (NumberFormatException ex) {
            setStatus("Please enter a valid number.", RED);
        }
    }

    private void onUseHint() {
        if (!hintDecorator.hasHints()) {
            setStatus("No hints remaining!", RED);
            return;
        }
        scoringDecorator.recordHintUsed();
        String hint = hintDecorator.useHint();
        setStatus("Hint: " + hint, PURPLE);
        updateSidebar();
    }

    private void onCheck() {
        if (basePuzzle.isSolved()) {
            onPuzzleSolved();
            return;
        }


        boolean allFilled = true;
        int[][] grid = basePuzzle.getGrid();
        for (int r = 0; r < basePuzzle.getSize(); r++) {
            int cols = basePuzzle instanceof ArithmeticPuzzle ? ArithmeticPuzzle.COLS : basePuzzle.getSize();
            for (int c = 0; c < cols; c++) {
                if (basePuzzle.isCellEditable(r, c) && grid[r][c] == 0) {
                    allFilled = false;
                    break;
                }
            }
        }


        boolean valid = basePuzzle.validate();
        if (allFilled && valid) {

            onPuzzleSolved();
        } else if (valid) {
            setStatus("No mistakes so far. Keep going!", GREEN);
        } else {
            setStatus("There is a mistake somewhere.", RED);
        }
    }

    private void onClear() {
        int[][] grid = basePuzzle.getGrid();
        for (int r = 0; r < basePuzzle.getSize(); r++)
            for (int c = 0; c < (basePuzzle instanceof ArithmeticPuzzle ?
                    ArithmeticPuzzle.COLS : basePuzzle.getSize()); c++)
                if (basePuzzle.isCellEditable(r, c)) basePuzzle.setCell(r, c, 0);
        if (puzzleGrid != null) puzzleGrid.updateFromPuzzle(basePuzzle);
        currentGridSize = -1;
        buildGridUI(basePuzzle.getSize());
        refreshGridDisplay();
        setStatus("Board cleared.", MUTED);
    }

    private void onPuzzleSolved() {
        if (countdownTimer != null) countdownTimer.stop();
        timerDecorator.stop();
        sidebar.setTimerText(timerDecorator.getFormattedTime());
        scoringDecorator.recordSolved();

        int elapsed       = timerDecorator.getSecondsElapsed();
        int fastThreshold = session.getCurrentState().getFastThreshold();
        int baseScore     = scoringDecorator.getScore();
        int speedBonus    = Math.max(0, (fastThreshold - elapsed) * session.getScoreMultiplier() * 2);
        int finalScore    = scoringDecorator.calculateFinalScore(elapsed, fastThreshold);

        boolean transitioned = session.evaluateAndTransition(finalScore, elapsed);
        updateSidebar();

        String time = timerDecorator.getFormattedTime();
        String msg = "Solved in " + time + "!  Score: " + finalScore + " pts.";
        if (transitioned) msg += "  " + session.getLastTransitionMessage() + "!";
        setStatus(msg, GREEN);

        scheduler.schedule(currentType, session.getCurrentState().getDifficulty(),
                GenerationRequest.Priority.HIGH, "Preparing next puzzle",
                p -> nextPuzzle = p);

        Timer delay = new Timer(1200, e ->
                showResultDialog(finalScore, baseScore, speedBonus, time, transitioned));
        delay.setRepeats(false);
        delay.start();
    }

    private void onNewPuzzle() {
        String type = (String) typeSelector.getSelectedItem();
        PuzzleFactory.Difficulty diff = session.getCurrentState().getDifficulty();

        setStatus("Generating new puzzle...", MUTED);
        scheduler.schedule(type, diff, GenerationRequest.Priority.HIGH,
                "Generating " + type + " (user request)",
                puzzle -> {
                    nextPuzzle = puzzle;
                    SwingUtilities.invokeLater(() -> loadNewPuzzle(type));
                });
    }

    private void showResultDialog(int totalScore, int baseScore, int speedBonus,
                                  String timeTaken, boolean transitioned) {
        JDialog dialog = new JDialog(this, "Puzzle Complete", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(PANEL);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(PANEL);
        content.setBorder(new EmptyBorder(24, 32, 24, 32));

        JLabel scoreL = new NoTooltipLabel("Total: " + totalScore + " pts", SwingConstants.CENTER);
        scoreL.setFont(new Font("Calibri", Font.BOLD, 22));
        scoreL.setForeground(GREEN);
        scoreL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel timeL = new NoTooltipLabel("Time: " + timeTaken, SwingConstants.CENTER);
        timeL.setFont(new Font("Calibri", Font.PLAIN, 13));
        timeL.setForeground(WHITE);
        timeL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel baseL = new NoTooltipLabel("Base score: " + baseScore + " pts", SwingConstants.CENTER);
        baseL.setFont(new Font("Calibri", Font.PLAIN, 12));
        baseL.setForeground(MUTED);
        baseL.setAlignmentX(Component.CENTER_ALIGNMENT);

        Color bonusColor = speedBonus > 0 ? ORANGE : MUTED;
        String bonusText = speedBonus > 0
                ? "Speed bonus: +" + speedBonus + " pts"
                : "Speed bonus: none (solve faster to earn bonus pts)";
        JLabel bonusL = new NoTooltipLabel(bonusText, SwingConstants.CENTER);
        bonusL.setFont(new Font("Calibri", Font.PLAIN, 12));
        bonusL.setForeground(bonusColor);
        bonusL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel totalL = new NoTooltipLabel("Session: " + session.getTotalScore() + " pts  |  " +
                session.getTotalPuzzlesSolved() + " solved", SwingConstants.CENTER);
        totalL.setFont(new Font("Calibri", Font.PLAIN, 12));
        totalL.setForeground(MUTED);
        totalL.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel stateL = new NoTooltipLabel("Difficulty: " + session.getCurrentState().getName(),
                SwingConstants.CENTER);
        stateL.setFont(new Font("Calibri", Font.BOLD, 13));
        stateL.setForeground(session.getCurrentState().getColor());
        stateL.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (transitioned) {
            JLabel transL = new NoTooltipLabel(session.getLastTransitionMessage(), SwingConstants.CENTER);
            transL.setFont(new Font("Calibri", Font.BOLD, 12));
            transL.setForeground(ACCENT);
            transL.setAlignmentX(Component.CENTER_ALIGNMENT);
            content.add(transL);
            content.add(Box.createVerticalStrut(6));
        }

        JButton nextBtn = btn("Next Puzzle", ACCENT);
        nextBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        nextBtn.addActionListener(e -> {
            dialog.dispose();
            loadNewPuzzle((String) typeSelector.getSelectedItem());
        });

        content.add(scoreL);
        content.add(Box.createVerticalStrut(4));
        content.add(timeL);
        content.add(Box.createVerticalStrut(4));
        content.add(baseL);
        content.add(Box.createVerticalStrut(2));
        content.add(bonusL);
        content.add(Box.createVerticalStrut(8));
        content.add(totalL);
        content.add(Box.createVerticalStrut(4));
        content.add(stateL);
        content.add(Box.createVerticalStrut(16));
        content.add(nextBtn);

        dialog.add(content, BorderLayout.CENTER);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    // ── Display Updates ───────────────────────────────────────────────────────

    private void refreshGridDisplay() {
        if (basePuzzle instanceof ArithmeticPuzzle) return;
        if (cellButtons == null) return;

        int[][] grid = basePuzzle.getGrid();
        int size = basePuzzle.getSize();

        int boxRows = 1, boxCols = 1;
        if (basePuzzle instanceof SudokuPuzzle) {
            boxRows = ((SudokuPuzzle) basePuzzle).getBoxRows();
            boxCols = ((SudokuPuzzle) basePuzzle).getBoxCols();
        }

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                JButton b = cellButtons[r][c];
                if (b == null) continue;
                int val = grid[r][c];
                b.setText(val == 0 ? "" : String.valueOf(val));

                if (!basePuzzle.isCellEditable(r, c)) {
                    b.setBackground(new Color(30, 37, 46));
                    b.setForeground(MUTED);
                } else {
                    b.setBackground(PANEL);
                    b.setForeground(ACCENT);
                }


                if (basePuzzle instanceof SudokuPuzzle) {
                    int top    = (r % boxRows == 0) ? 2 : 1;
                    int left   = (c % boxCols == 0) ? 2 : 1;
                    int bottom = (r % boxRows == boxRows - 1) ? 2 : 1;
                    int right  = (c % boxCols == boxCols - 1) ? 2 : 1;
                    b.setBorder(BorderFactory.createMatteBorder(top, left, bottom, right,
                            ACCENT.darker().darker()));
                } else {
                    b.setBorder(BorderFactory.createLineBorder(BORDER, 1));
                }
            }
        }
    }

    private void updateClues() {
        if (basePuzzle instanceof LogicGridPuzzle) {
            String[] clues = ((LogicGridPuzzle) basePuzzle).getClues();
            StringBuilder sb = new StringBuilder("Clues:\n");
            for (String c : clues) sb.append("  ").append(c).append("\n");
            cluesArea.setText(sb.toString());
            cluesScroll.setVisible(true);
        } else {
            cluesScroll.setVisible(false);
        }
    }

    private void updateSidebar() {
        sidebar.setTimerText(timerDecorator.getFormattedTime());
        sidebar.setScoreText(scoringDecorator.getScore() + " pts");
        sidebar.setStatsText(session.getTotalPuzzlesSolved() + " solved  |  " +
                session.getTotalScore() + " total pts");
        sidebar.setHintsText(hintDecorator.getHintsRemaining() + " hint(s) remaining");
        sidebar.setHintEnabled(hintDecorator.hasHints());
        sidebar.setStateName(session.getCurrentState().getName());
        sidebar.setStateColor(session.getCurrentState().getColor());
        sidebar.setStateDesc(session.getCurrentState().getDescription());
    }

    private void updateValidationStatus() {
        if (basePuzzle.isSolved()) return;
        boolean valid = basePuzzle.validate();
        if (valid) setStatus("No mistakes so far.", GREEN);
        else       setStatus("Conflict detected.", RED);
    }

    private void setStatus(String msg, Color color) {
        validationLabel.setText(msg);
        validationLabel.setForeground(color);
    }

    private void flashCell(int r, int c, Color color) {
        if (cellButtons == null || cellButtons[r][c] == null) return;
        JButton b = cellButtons[r][c];
        Color orig = b.getBackground();
        b.setBackground(blendWithDark(color, 0.5f));
        Timer t = new Timer(350, e -> b.setBackground(orig));
        t.setRepeats(false);
        t.start();
    }

    // ── Entry Point ────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception ignored) {}
        ToolTipManager.sharedInstance().setEnabled(false);
        SwingUtilities.invokeLater(() -> new MainWindow().setVisible(true));
    }
}