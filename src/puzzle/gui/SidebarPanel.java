package puzzle.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Custom-painted sidebar panel. Draws all text directly via Graphics2D
 * so Metal LAF cannot interfere with component painting or show ghost text.
 */
public class SidebarPanel extends JPanel {

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


    private String timerText       = "0:00";
    private String scoreText       = "0 pts";
    private String statsText       = "0 solved  |  0 total pts";
    private String hintsText       = "3 hint(s) remaining";
    private String stateName       = "Beginner";
    private Color  stateColor      = GREEN;
    private String stateDesc       = "Solve under 2 min to advance";
    private String schedulerText   = "Idle";


    private Rectangle hintBtnBounds  = new Rectangle();
    private Rectangle checkBtnBounds = new Rectangle();
    private Rectangle clearBtnBounds = new Rectangle();

    private boolean hintHover  = false;
    private boolean checkHover = false;
    private boolean clearHover = false;

    private boolean hintEnabled = true;

    private Runnable onHint;
    private Runnable onCheck;
    private Runnable onClear;

    public SidebarPanel() {
        setPreferredSize(new Dimension(300, 0));
        setBackground(BG);
        setOpaque(true);

        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent e) {
                boolean h1 = hintBtnBounds.contains(e.getPoint());
                boolean h2 = checkBtnBounds.contains(e.getPoint());
                boolean h3 = clearBtnBounds.contains(e.getPoint());
                if (h1 != hintHover || h2 != checkHover || h3 != clearHover) {
                    hintHover = h1; checkHover = h2; clearHover = h3;
                    repaint();
                }
            }
        });

        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseExited(java.awt.event.MouseEvent e) {
                hintHover = false; checkHover = false; clearHover = false;
                repaint();
            }
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (hintBtnBounds.contains(e.getPoint()) && hintEnabled && onHint != null) onHint.run();
                else if (checkBtnBounds.contains(e.getPoint()) && onCheck != null) onCheck.run();
                else if (clearBtnBounds.contains(e.getPoint()) && onClear != null) onClear.run();
            }
        });
    }


    public void setTimerText(String t)     { timerText = t;    repaint(); }
    public void setScoreText(String t)     { scoreText = t;    repaint(); }
    public void setStatsText(String t)     { statsText = t;    repaint(); }
    public void setHintsText(String t)     { hintsText = t;    repaint(); }
    public void setHintEnabled(boolean b)  { hintEnabled = b;  repaint(); }
    public void setStateName(String t)     { stateName = t;    repaint(); }
    public void setStateColor(Color c)     { stateColor = c;   repaint(); }
    public void setStateDesc(String t)     { stateDesc = t;    repaint(); }
    public void setSchedulerText(String t) { schedulerText = t; repaint(); }

    public void setOnHint(Runnable r)  { onHint = r; }
    public void setOnCheck(Runnable r) { onCheck = r; }
    public void setOnClear(Runnable r) { onClear = r; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        // Clip to panel bounds
        g2.setClip(0, 0, getWidth(), getHeight());
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int w = getWidth();
        int pad = 12;
        int cardW = w - pad * 2;
        int y = pad;

        y = drawCard(g2, pad, y, cardW, ORANGE, "Time Elapsed",
                new String[]{ timerText }, new Font[]{ new Font("Consolas", Font.BOLD, 26) },
                new Color[]{ WHITE }, null, null, false);
        y += 8;

        y = drawCard(g2, pad, y, cardW, GREEN, "Score",
                new String[]{ scoreText, statsText },
                new Font[]{ new Font("Consolas", Font.BOLD, 18), new Font("Calibri", Font.PLAIN, 10) },
                new Color[]{ WHITE, MUTED }, null, null, false);
        y += 8;


        y = drawCard(g2, pad, y, cardW, PURPLE, "Hints",
                new String[]{ hintsText }, new Font[]{ new Font("Calibri", Font.PLAIN, 11) },
                new Color[]{ MUTED }, "Use Hint", hintBtnBounds, !hintEnabled || hintHover);
        y += 8;

        y = drawCard(g2, pad, y, cardW, RED, "Difficulty",
                new String[]{ stateName, stateDesc },
                new Font[]{ new Font("Calibri", Font.BOLD, 16), new Font("Calibri", Font.PLAIN, 10) },
                new Color[]{ stateColor, MUTED }, null, null, false);
        y += 8;

        y = drawCard(g2, pad, y, cardW, ACCENT, "Scheduler",
                new String[]{ schedulerText }, new Font[]{ new Font("Calibri", Font.PLAIN, 10) },
                new Color[]{ MUTED }, null, null, false);


        int btnY = getHeight() - 44;
        int halfW = (cardW - 8) / 2;
        drawButton(g2, pad, btnY, halfW, 34, "Check", ACCENT, checkHover);
        checkBtnBounds.setBounds(pad, btnY, halfW, 34);
        drawButton(g2, pad + halfW + 8, btnY, halfW, 34, "Clear", MUTED, clearHover);
        clearBtnBounds.setBounds(pad + halfW + 8, btnY, halfW, 34);

        g2.dispose();
    }

    private int drawCard(Graphics2D g2, int x, int y, int w,
                         Color accent, String title,
                         String[] lines, Font[] fonts, Color[] colors,
                         String btnLabel, Rectangle btnBoundsOut, boolean btnDimmed) {
        int lineH = 0;
        for (int i = 0; i < lines.length; i++) {
            FontMetrics fm = getFontMetrics(fonts[i]);
            lineH += fm.getHeight() + 4;
        }
        int btnH = btnLabel != null ? 32 + 8 : 0;
        int cardH = 6 + 14 + 4 + lineH + btnH + 8;


        g2.setColor(PANEL);
        g2.fillRect(x, y, w, cardH);


        g2.setColor(accent);
        g2.fillRect(x, y, w, 3);


        g2.setColor(accent);
        g2.setFont(new Font("Consolas", Font.PLAIN, 8));
        g2.drawString(title, x + 9, y + 14);


        int lineY = y + 22;
        for (int i = 0; i < lines.length; i++) {
            g2.setFont(fonts[i]);
            g2.setColor(colors[i]);
            FontMetrics fm = g2.getFontMetrics();
            lineY += fm.getAscent();
            g2.drawString(lines[i], x + 9, lineY);
            lineY += fm.getDescent() + 4;
        }


        if (btnLabel != null && btnBoundsOut != null) {
            int bx = x + 9, by = lineY + 2, bw = 100, bh = 26;
            btnBoundsOut.setBounds(bx, by, bw, bh);
            Color btnColor = hintEnabled ? PURPLE : MUTED;
            g2.setColor(btnDimmed
                    ? new Color(btnColor.getRed(), btnColor.getGreen(), btnColor.getBlue(), 80)
                    : new Color(btnColor.getRed(), btnColor.getGreen(), btnColor.getBlue(), 40));
            g2.fillRoundRect(bx, by, bw, bh, 4, 4);
            g2.setColor(btnColor);
            g2.drawRoundRect(bx, by, bw, bh, 4, 4);
            g2.setFont(new Font("Calibri", Font.BOLD, 11));
            FontMetrics fm = g2.getFontMetrics();
            int tx = bx + (bw - fm.stringWidth(btnLabel)) / 2;
            int ty = by + (bh + fm.getAscent() - fm.getDescent()) / 2;
            g2.drawString(btnLabel, tx, ty);
            lineY = by + bh;
        }

        return y + cardH;
    }

    private void drawButton(Graphics2D g2, int x, int y, int w, int h,
                            String label, Color color, boolean hover) {
        g2.setColor(hover
                ? new Color(color.getRed(), color.getGreen(), color.getBlue(), 80)
                : new Color(color.getRed(), color.getGreen(), color.getBlue(), 40));
        g2.fillRoundRect(x, y, w, h, 4, 4);
        g2.setColor(color);
        g2.drawRoundRect(x, y, w, h, 4, 4);
        g2.setFont(new Font("Calibri", Font.BOLD, 11));
        FontMetrics fm = g2.getFontMetrics();
        int tx = x + (w - fm.stringWidth(label)) / 2;
        int ty = y + (h + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(label, tx, ty);
    }
}