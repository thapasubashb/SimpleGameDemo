import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
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
    private BufferedImage backgroundImage;

    // Box positions
    private int[] boxX = new int[GRID_ROWS * GRID_COLS];
    private int[] boxY = new int[GRID_ROWS * GRID_COLS];

    public BoxBoxGame() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(20, 20, 35));
        setFocusable(true);
        loadBackgroundImage();
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

    private void loadBackgroundImage() {
        try {
            // Randomly select one of the two images
            String[] images = {"piranha.png", "monty.png"};
            String imageName = images[random.nextInt(images.length)];
            String imagePath = new File(BoxBoxGame.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath()).getParent() + File.separator + imageName;
            
            backgroundImage = ImageIO.read(new File(imagePath));
        } catch (Exception e) {
            // If image loading fails, continue without background image
            System.err.println("Could not load background image: " + e.getMessage());
            backgroundImage = null;
        }
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
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw gradient background
        GradientPaint backgroundGradient = new GradientPaint(
                0, 0, new Color(20, 20, 35),
                0, HEIGHT, new Color(35, 35, 60));
        g2d.setPaint(backgroundGradient);
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw header background
        g2d.setColor(new Color(10, 10, 20));
        g2d.fillRect(0, 0, WIDTH, HEADER_HEIGHT);

        // Draw header border
        g2d.setColor(new Color(100, 150, 255));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawLine(0, HEADER_HEIGHT - 2, WIDTH, HEADER_HEIGHT - 2);

        // Draw score text
        g2d.setColor(new Color(100, 200, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 28));
        g2d.drawString("Score: " + score, 40, 55);

        // Draw high score text
        g2d.setColor(new Color(255, 200, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 20));
        g2d.drawString("High Score: " + highScore, WIDTH - 280, 55);

        // Draw boxes with enhanced styling
        for (int i = 0; i < GRID_ROWS * GRID_COLS; i++) {
            drawEnhancedBox(g2d, i);
        }

        // Draw game over overlay
        if (gameOver) {
            drawGameOverScreen(g2d);
        }
    }

    private void drawEnhancedBox(Graphics2D g2d, int boxIndex) {
        int x = boxX[boxIndex];
        int y = boxY[boxIndex];
        boolean isCorrect = (boxIndex == correctBox);
        boolean isHovered = (boxIndex == hoveredBox);

        // Draw shadow
        g2d.setColor(new Color(0, 0, 0, 80));
        int shadowOffset = isHovered ? 8 : 10;
        g2d.fillRect(x + 5, y + shadowOffset, BOX_SIZE, BOX_SIZE);

        // Draw glow for correct box
        if (isCorrect) {
            for (int i = 10; i > 0; i--) {
                g2d.setColor(new Color(0, 255, 150, 20 - (i * 2)));
                g2d.setStroke(new BasicStroke(i / 2f));
                g2d.drawRect(x - i / 2, y - i / 2, BOX_SIZE + i, BOX_SIZE + i);
            }
        }

        // Draw main box
        if (isCorrect) {
            // Green gradient for correct box
            GradientPaint boxGradient = new GradientPaint(
                    x, y, new Color(50, 220, 130),
                    x, y + BOX_SIZE, new Color(0, 180, 100));
            g2d.setPaint(boxGradient);
        } else {
            // Red gradient for wrong boxes
            GradientPaint boxGradient = new GradientPaint(
                    x, y, new Color(255, 100, 100),
                    x, y + BOX_SIZE, new Color(200, 40, 40));
            g2d.setPaint(boxGradient);
        }

        int boxOffset = isHovered ? -5 : 0;
        g2d.fillRect(x, y + boxOffset, BOX_SIZE, BOX_SIZE);

        // Draw border with glow effect for hovered
        if (isHovered) {
            g2d.setColor(new Color(255, 255, 200));
            g2d.setStroke(new BasicStroke(5));
        } else {
            g2d.setColor(Color.WHITE);
            g2d.setStroke(new BasicStroke(3));
        }
        g2d.drawRect(x, y + boxOffset, BOX_SIZE, BOX_SIZE);

        // Draw label for correct box
        if (isCorrect) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g2d.getFontMetrics();
            String label = "CLICK ME!";
            int labelX = x + (BOX_SIZE - fm.stringWidth(label)) / 2;
            int labelY = y + boxOffset + ((BOX_SIZE - fm.getHeight()) / 2) + fm.getAscent();
            g2d.drawString(label, labelX, labelY);
        }
    }

    private void drawGameOverScreen(Graphics2D g2d) {
        // Semi-transparent overlay
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.fillRect(0, 0, WIDTH, HEIGHT);

        // Draw game over panel
        int panelWidth = 500;
        int panelHeight = 300;
        int panelX = (WIDTH - panelWidth) / 2;
        int panelY = (HEIGHT - panelHeight) / 2;

        // Panel background with gradient
        GradientPaint panelGradient = new GradientPaint(
                panelX, panelY, new Color(40, 40, 70),
                panelX, panelY + panelHeight, new Color(20, 20, 40));
        g2d.setPaint(panelGradient);
        g2d.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Panel border
        g2d.setColor(new Color(255, 100, 100));
        g2d.setStroke(new BasicStroke(3));
        g2d.drawRoundRect(panelX, panelY, panelWidth, panelHeight, 20, 20);

        // Draw "GAME OVER" text
        g2d.setColor(new Color(255, 100, 100));
        g2d.setFont(new Font("Arial", Font.BOLD, 56));
        FontMetrics fm = g2d.getFontMetrics();
        String gameOverText = "GAME OVER";
        int textX = (WIDTH - fm.stringWidth(gameOverText)) / 2;
        g2d.drawString(gameOverText, textX, panelY + 80);

        // Draw final score
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        fm = g2d.getFontMetrics();
        String scoreText = "Final Score: " + score;
        textX = (WIDTH - fm.stringWidth(scoreText)) / 2;
        g2d.drawString(scoreText, textX, panelY + 150);

        // Draw restart instruction
        g2d.setColor(new Color(150, 200, 255));
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        String restartText = "Click anywhere to restart!";
        textX = (WIDTH - fm.stringWidth(restartText)) / 2;
        g2d.drawString(restartText, textX, panelY + 230);
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
