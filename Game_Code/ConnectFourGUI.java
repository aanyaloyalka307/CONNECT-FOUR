import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

// A flashy point-and-click window (GUI) for playing Connect Four.
//
// IMPORTANT: this class does NOT re-implement the game. All the real rules
// live in ConnectFour (the graded class), which is used here completely
// unmodified. Every move is made through ConnectFour.makeMove("A <col>") or
// "R <col>", and winner/turn come from getWinner()/getNextPlayer(). We only
// peek at the private board with reflection so we can DRAW it - the logic is
// still 100% the real game.
public class ConnectFourGUI {

    // ---- palette ----
    private static final Color BG_TOP    = new Color(0x0E, 0x14, 0x28);
    private static final Color BG_BOT    = new Color(0x1A, 0x24, 0x44);
    private static final Color BOARD     = new Color(0x1E, 0x50, 0xE0);
    private static final Color BOARD_EDGE= new Color(0x14, 0x39, 0xA8);
    private static final Color HOLE      = new Color(0x0C, 0x12, 0x24);
    private static final Color RED        = new Color(0xE6, 0x3B, 0x3B); // Player 1 (X)
    private static final Color RED_HI      = new Color(0xFF, 0x6B, 0x6B);
    private static final Color YELLOW     = new Color(0xF6, 0xC9, 0x1A); // Player 2 (O)
    private static final Color YELLOW_HI  = new Color(0xFF, 0xE2, 0x6B);
    private static final Color TEXT       = new Color(0xED, 0xF1, 0xF9);
    private static final Color MUTED      = new Color(0x9A, 0xA7, 0xC4);

    private static final int ROWS = 6;
    private static final int COLS = 7;

    private ConnectFour game;
    private Field boardField;      // cached reflective handle to the private board

    private JFrame frame;
    private BoardPanel boardPanel;
    private JLabel turnLabel;
    private JLabel messageLabel;
    private JToggleButton removeMode;

    // animation state
    private final Timer dropTimer;
    private boolean animating = false;
    private int animCol, animRow;
    private double animY, animVel;
    private Color animColor;

    // win highlight
    private List<Point> winCells = new ArrayList<>();

    public ConnectFourGUI() {
        newGame();
        dropTimer = new Timer(12, e -> stepDrop());
        buildUI();
    }

    // ------------------------------------------------------------------
    private void newGame() {
        game = new ConnectFour();
        try {
            if (boardField == null) {
                boardField = ConnectFour.class.getDeclaredField("board");
                boardField.setAccessible(true);
            }
        } catch (NoSuchFieldException ex) {
            throw new RuntimeException("Could not access the board for drawing.", ex);
        }
        winCells.clear();
        animating = false;
    }

    private char[][] board() {
        try {
            return (char[][]) boardField.get(game);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex);
        }
    }

    // ------------------------------------------------------------------
    private void buildUI() {
        frame = new JFrame("Connect Four");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(760, 760);
        frame.setMinimumSize(new Dimension(560, 620));

        JPanel root = new JPanel(new BorderLayout());
        root.add(buildHeader(), BorderLayout.NORTH);

        boardPanel = new BoardPanel();
        root.add(boardPanel, BorderLayout.CENTER);

        root.add(buildFooter(), BorderLayout.SOUTH);
        frame.setContentPane(root);
        frame.setLocationRelativeTo(null);
        refreshTurn();
    }

    private JComponent buildHeader() {
        JPanel header = new GradientPanel();
        header.setLayout(new BorderLayout(10, 6));
        header.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel title = new JLabel("● ● ● ●  CONNECT FOUR");
        title.setFont(uiFont(Font.BOLD, 24f));
        title.setForeground(TEXT);
        header.add(title, BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        right.setOpaque(false);

        removeMode = new JToggleButton("Remove mode");
        removeMode.setFocusPainted(false);
        removeMode.setFont(uiFont(Font.BOLD, 12f));
        removeMode.setToolTipText("When ON, clicking a column removes YOUR token from the bottom.");
        right.add(removeMode);

        JButton reset = new JButton("↻ New Game");
        reset.setFocusPainted(false);
        reset.setFont(uiFont(Font.BOLD, 12f));
        reset.addActionListener(e -> {
            newGame();
            removeMode.setSelected(false);
            messageLabel.setText(" ");
            refreshTurn();
            boardPanel.repaint();
        });
        right.add(reset);

        header.add(right, BorderLayout.EAST);

        turnLabel = new JLabel("Player 1's turn");
        turnLabel.setFont(uiFont(Font.BOLD, 15f));
        turnLabel.setForeground(TEXT);
        turnLabel.setBorder(new EmptyBorder(6, 0, 0, 0));
        header.add(turnLabel, BorderLayout.SOUTH);

        return header;
    }

    private JComponent buildFooter() {
        JPanel footer = new GradientPanel();
        footer.setLayout(new BorderLayout());
        footer.setBorder(new EmptyBorder(8, 20, 14, 20));
        messageLabel = new JLabel("Click a column to drop your disc. Four in a row wins!");
        messageLabel.setFont(uiFont(Font.PLAIN, 13f));
        messageLabel.setForeground(MUTED);
        footer.add(messageLabel, BorderLayout.CENTER);
        return footer;
    }

    // ------------------------------------------------------------------
    private void refreshTurn() {
        int w = game.getWinner();
        if (w == 1 || w == 2) {
            turnLabel.setText("🏆  Player " + w + " wins!");
            turnLabel.setForeground(w == 1 ? RED_HI : YELLOW_HI);
        } else if (w == 0) {
            turnLabel.setText("It's a tie!");
            turnLabel.setForeground(TEXT);
        } else {
            int p = game.getNextPlayer();
            turnLabel.setText("Player " + p + "'s turn  (" + (p == 1 ? "red" : "yellow") + ")");
            turnLabel.setForeground(p == 1 ? RED_HI : YELLOW_HI);
        }
    }

    // Attempt a move in the given column using the REAL game logic.
    private void handleColumnClick(int col) {
        if (animating || game.isGameOver()) {
            return;
        }
        int player = game.getNextPlayer();
        Color color = player == 1 ? RED : YELLOW;
        String action = removeMode.isSelected() ? "R " : "A ";
        boolean wasAdd = !removeMode.isSelected();

        try {
            game.makeMove(action + (col + 1));
            messageLabel.setText(" ");
        } catch (IllegalArgumentException ex) {
            flash(wasAdd ? "That column is full - try another."
                         : "You can only remove your OWN token from the bottom of a column.");
            return;
        }

        if (wasAdd) {
            // Find where the new disc landed (top-most filled cell in that col).
            char[][] b = board();
            int landRow = 0;
            for (int r = 0; r < ROWS; r++) {
                if (b[r][col] != '.') { landRow = r; break; }
            }
            startDrop(col, landRow, color);
        } else {
            afterMove();
        }
    }

    private void flash(String msg) {
        messageLabel.setText("⚠  " + msg);
        Toolkit.getDefaultToolkit().beep();
    }

    // ---- drop animation ----
    private void startDrop(int col, int row, Color color) {
        animating = true;
        animCol = col;
        animRow = row;
        animColor = color;
        animY = -1;          // in cell units, above the board
        animVel = 0;
        dropTimer.start();
    }

    private void stepDrop() {
        animVel += 0.045;    // gravity (in cell units per tick^2)
        animY += animVel;
        if (animY >= animRow) {
            animY = animRow;
            dropTimer.stop();
            animating = false;
            afterMove();
        }
        boardPanel.repaint();
    }

    private void afterMove() {
        computeWinCells();
        refreshTurn();
        if (game.isGameOver()) {
            int w = game.getWinner();
            messageLabel.setText(w == 0 ? "Board full - it's a tie! Click 'New Game'."
                    : "Four in a row! Player " + w + " wins. Click 'New Game' to play again.");
        }
        boardPanel.repaint();
    }

    // View-side scan to highlight the winning four (purely cosmetic).
    private void computeWinCells() {
        winCells.clear();
        int w = game.getWinner();
        if (w != 1 && w != 2) return;
        char token = (w == 1) ? 'X' : 'O';
        char[][] b = board();
        int[][] dirs = {{0, 1}, {1, 0}, {1, 1}, {-1, 1}};
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                for (int[] d : dirs) {
                    List<Point> line = new ArrayList<>();
                    int rr = r, cc = c;
                    while (rr >= 0 && rr < ROWS && cc >= 0 && cc < COLS && b[rr][cc] == token) {
                        line.add(new Point(cc, rr));
                        if (line.size() == 4) {
                            winCells = line;
                            return;
                        }
                        rr += d[0];
                        cc += d[1];
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // The board drawing surface
    // ------------------------------------------------------------------
    private class BoardPanel extends JPanel {
        private int hoverCol = -1;

        BoardPanel() {
            setBackground(BG_TOP);
            MouseAdapter ma = new MouseAdapter() {
                @Override public void mouseMoved(MouseEvent e) {
                    int c = colAt(e.getX(), e.getY());
                    if (c != hoverCol) { hoverCol = c; repaint(); }
                }
                @Override public void mouseExited(MouseEvent e) {
                    hoverCol = -1; repaint();
                }
                @Override public void mouseClicked(MouseEvent e) {
                    int c = colAt(e.getX(), e.getY());
                    if (c >= 0) handleColumnClick(c);
                }
            };
            addMouseListener(ma);
            addMouseMotionListener(ma);
        }

        private int cell() {
            int pad = 24;
            return Math.max(20, Math.min((getWidth() - 2 * pad) / COLS,
                                         (getHeight() - 2 * pad) / ROWS));
        }
        private int originX() { return (getWidth() - cell() * COLS) / 2; }
        private int originY() { return (getHeight() - cell() * ROWS) / 2; }

        private int colAt(int x, int y) {
            int cell = cell(), ox = originX(), oy = originY();
            if (x < ox || x >= ox + cell * COLS || y < oy || y >= oy + cell * ROWS + cell) {
                return -1;
            }
            int c = (x - ox) / cell;
            return (c >= 0 && c < COLS) ? c : -1;
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // background gradient
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT));
            g2.fillRect(0, 0, getWidth(), getHeight());

            int cell = cell(), ox = originX(), oy = originY();
            char[][] b = board();
            boolean over = game.isGameOver();

            // hover ghost disc + column highlight (above the board)
            if (!animating && !over && hoverCol >= 0) {
                g2.setColor(new Color(255, 255, 255, 22));
                g2.fillRoundRect(ox + hoverCol * cell, oy, cell, cell * ROWS, 16, 16);
                Color ghost = game.getNextPlayer() == 1 ? RED : YELLOW;
                g2.setColor(new Color(ghost.getRed(), ghost.getGreen(), ghost.getBlue(), 120));
                fillDisc(g2, ox + hoverCol * cell, oy - cell, cell);
            }

            // the blue board
            g2.setColor(BOARD_EDGE);
            g2.fill(new RoundRectangle2D.Float(ox - 8, oy - 8, cell * COLS + 16, cell * ROWS + 16, 26, 26));
            g2.setColor(BOARD);
            g2.fill(new RoundRectangle2D.Float(ox - 4, oy - 4, cell * COLS + 8, cell * ROWS + 8, 22, 22));

            // holes + resting discs
            for (int r = 0; r < ROWS; r++) {
                for (int c = 0; c < COLS; c++) {
                    int x = ox + c * cell, y = oy + r * cell;
                    boolean skip = animating && c == animCol && r == animRow;
                    if (!skip && b[r][c] != '.') {
                        drawDisc(g2, x, y, cell, b[r][c] == 'X' ? RED : YELLOW);
                    } else {
                        g2.setColor(HOLE);
                        fillDisc(g2, x, y, cell);
                    }
                }
            }

            // falling disc
            if (animating) {
                int x = ox + animCol * cell;
                int y = oy + (int) Math.round(animY * cell);
                drawDisc(g2, x, y, cell, animColor);
            }

            // winning-four glow
            if (!winCells.isEmpty()) {
                g2.setStroke(new BasicStroke(4f));
                g2.setColor(Color.WHITE);
                for (Point p : winCells) {
                    int x = ox + p.x * cell, y = oy + p.y * cell;
                    int m = (int) (cell * 0.12);
                    g2.draw(new Ellipse2D.Float(x + m, y + m, cell - 2 * m, cell - 2 * m));
                }
            }
        }

        private void fillDisc(Graphics2D g2, int cellX, int cellY, int cell) {
            int m = (int) (cell * 0.12);
            g2.fill(new Ellipse2D.Float(cellX + m, cellY + m, cell - 2 * m, cell - 2 * m));
        }

        private void drawDisc(Graphics2D g2, int cellX, int cellY, int cell, Color base) {
            int m = (int) (cell * 0.12);
            int d = cell - 2 * m;
            Color hi = base.brighter();
            g2.setPaint(new GradientPaint(cellX + m, cellY + m, hi, cellX + m, cellY + m + d, base));
            g2.fill(new Ellipse2D.Float(cellX + m, cellY + m, d, d));
            g2.setColor(new Color(0, 0, 0, 45));
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Float(cellX + m, cellY + m, d, d));
            // little shine
            g2.setColor(new Color(255, 255, 255, 70));
            g2.fillOval(cellX + m + d / 5, cellY + m + d / 6, d / 4, d / 5);
        }
    }

    // A panel that paints the same vertical gradient as the board background.
    private static class GradientPanel extends JPanel {
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setPaint(new GradientPaint(0, 0, BG_TOP, 0, getHeight(), BG_BOT));
            g2.fillRect(0, 0, getWidth(), getHeight());
        }
    }

    private static Font uiFont(int style, float size) {
        String[] prefs = {"Segoe UI", "SF Pro Text", "Helvetica Neue", "Arial"};
        for (String name : prefs) {
            Font f = new Font(name, style, (int) size);
            if (f.getFamily().equalsIgnoreCase(name)) return f.deriveFont(size);
        }
        return new Font(Font.SANS_SERIF, style, (int) size).deriveFont(size);
    }

    // ------------------------------------------------------------------
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) { }
        SwingUtilities.invokeLater(() -> new ConnectFourGUI().frame.setVisible(true));
    }
}
