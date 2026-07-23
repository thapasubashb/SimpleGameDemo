import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

public class BoxBoxGame extends JPanel {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int BOX_SIZE = 100;
    private static final int GRID_ROWS = 3;
    private static final int GRID_COLS = 3;
    private static final int HEADER_HEIGHT = 80;

    private int score = 0;
    private int highScore = 0;
    private int correctBox = 0;
    private boolean gameOver = false;
    private String gameOverMessage = "";
    private Random random;
    private int hoveredBox = -1;

    // Box positions
    private int[] boxX = new int[GRID_ROWS * GRID_COLS];
    private int[] boxY = new int[GRID_ROWS * GRID_COLS];

    public BoxBoxGame() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 20, 35));
        setFocusable(true);
        initializeBoxPositions();
        correctBox = random.nextInt(GRID_ROWS * GRID_COLS);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e.getX(), e.getY());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updateHoveredBox(e.getX(), e.getY());
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                updateHoveredBox(e.getX(), e.getY());
            }
        });
    }

    private void updateHoveredBox(int mouseX, int mouseY) {
        int previousHovered = hoveredBox;
        hoveredBox = -1;

        for (int i = 0; i < GRID_ROWS * GRID_COLS; i++) {
            if (mouseX >= boxX[i] && mouseX <= boxX[i] + BOX_SIZE &&
                    mouseY >= boxY[i] && mouseY <= boxY[i] + BOX_SIZE) {
                hoveredBox = i;
                break;
            }
        }

        if (previousHovered != hoveredBox) {
            repaint();
        }
    }

    private void initializeBoxPositions() {
        int startX = (WIDTH - (GRID_COLS * BOX_SIZE)) / 2;
        int startY = HEADER_HEIGHT + (HEIGHT - HEADER_HEIGHT - (GRID_ROWS * BOX_SIZE)) / 2;
        int spacing = 20;

        int index = 0;
        for (int row = 0; row < GRID_ROWS; row++) {
            for (int col = 0; col < GRID_COLS; col++) {
                boxX[index] = startX + col * (BOX_SIZE + spacing);
                boxY[index] = startY + row * (BOX_SIZE + spacing);
                index++;
            }
        }
    }

    private void handleMouseClick(int mouseX, int mouseY) {
        if (gameOver) {
            resetGame();
            return;
        }

        for (int i = 0; i < GRID_ROWS * GRID_COLS; i++) {
            if (mouseX >= boxX[i] && mouseX <= boxX[i] + BOX_SIZE &&
                    mouseY >= boxY[i] && mouseY <= boxY[i] + BOX_SIZE) {

                if (i == correctBox) {
                    // Correct box clicked
                    score++;
                    correctBox = random.nextInt(GRID_ROWS * GRID_COLS);
                } else {
                    // Wrong box clicked - Game Over
                    gameOver = true;
                    gameOverMessage = "Game Over! Final Score: " + score + "\nClick to restart!";
                }
                repaint();
                return;
            }
        }
    }

    private void resetGame() {
        if (score > highScore) {
            highScore = score;
        }
        score = 0;
        gameOver = false;
        gameOverMessage = "";
        hoveredBox = -1;
        correctBox = random.nextInt(GRID_ROWS * GRID_COLS);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw boxes
        for (int i = 0; i < GRID_ROWS * GRID_COLS; i++) {
            if (i == correctBox) {
                g2d.setColor(new Color(0, 200, 100)); // Green for correct box
            } else {
                g2d.setColor(new Color(200, 50, 50)); // Red for wrong boxes
            }

            g2d.fillRect(boxX[i], boxY[i], BOX_SIZE, BOX_SIZE);

            // Draw border
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
            g2d.drawRect(boxX[i], boxY[i], BOX_SIZE, BOX_SIZE);
        }

        // Draw score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 32));
        g2d.drawString("Score: " + score, 50, 50);

        // Draw game over message
        if (gameOver) {
            g2d.setColor(new Color(0, 0, 0, 150)); // Semi-transparent black
            g2d.fillRect(0, 0, WIDTH, HEIGHT);

            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            FontMetrics fm = g2d.getFontMetrics();

            String[] lines = gameOverMessage.split("\n");
            int totalHeight = lines.length * fm.getHeight();
            int startY = (HEIGHT - totalHeight) / 2;

            for (int i = 0; i < lines.length; i++) {
                int x = (WIDTH - fm.stringWidth(lines[i])) / 2;
                int y = startY + (i + 1) * fm.getHeight();
                g2d.drawString(lines[i], x, y);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Box Box Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(new BoxBoxGame());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
