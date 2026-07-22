import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int UNIT_SIZE = 20;
    private static final int GAME_UNITS = (WIDTH * HEIGHT) / (UNIT_SIZE * UNIT_SIZE);
    private static final int DELAY = 100;

    private final int x[] = new int[GAME_UNITS];
    private final int y[] = new int[GAME_UNITS];
    private int bodyParts = 6;
    private int applesEaten = 0;
    private int appleX;
    private int appleY;
    private char direction = 'R';
    private boolean running = false;
    private boolean paused = false;
    private Timer timer;
    private final Random random;

    public SnakeGame() {
        random = new Random();
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(new MyKeyAdapter());
        startGame();
    }

    private void startGame() {
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    private void newApple() {
        appleX = random.nextInt(WIDTH / UNIT_SIZE) * UNIT_SIZE;
        appleY = random.nextInt(HEIGHT / UNIT_SIZE) * UNIT_SIZE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (running && !paused) {
            move();
            checkApple();
            checkCollisions();
        }
        repaint();
    }

    private void move() {
        for (int i = bodyParts; i > 0; i--) {
            x[i] = x[i - 1];
            y[i] = y[i - 1];
        }

        switch (direction) {
            case 'U':
                y[0] = y[0] - UNIT_SIZE;
                break;
            case 'D':
                y[0] = y[0] + UNIT_SIZE;
                break;
            case 'L':
                x[0] = x[0] - UNIT_SIZE;
                break;
            case 'R':
                x[0] = x[0] + UNIT_SIZE;
                break;
        }
    }

    private void checkApple() {
        if (x[0] == appleX && y[0] == appleY) {
            bodyParts++;
            applesEaten++;
            newApple();
        }
    }

    private void checkCollisions() {
        // Check if head collides with body
        for (int i = bodyParts; i > 0; i--) {
            if (x[0] == x[i] && y[0] == y[i]) {
                running = false;
                break;
            }
        }
        // Check borders
        if (x[0] < 0 || x[0] >= WIDTH || y[0] < 0 || y[0] >= HEIGHT) {
            running = false;
        }

        if (!running) {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        draw(g);
    }

    private void draw(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        // Background gradient
        GradientPaint bg = new GradientPaint(0, 0, new Color(10, 24, 36), 0, HEIGHT, new Color(20, 90, 60));
        g2.setPaint(bg);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        // Subtle grid
        g2.setColor(new Color(255, 255, 255, 20));
        for (int i = 0; i < WIDTH; i += UNIT_SIZE) {
            g2.drawLine(i, 0, i, HEIGHT);
        }
        for (int j = 0; j < HEIGHT; j += UNIT_SIZE) {
            g2.drawLine(0, j, WIDTH, j);
        }

        // Draw apple with highlight
        g2.setColor(new Color(220, 40, 40));
        g2.fillOval(appleX + 2, appleY + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);
        g2.setColor(new Color(255, 180, 180, 180));
        g2.fillOval(appleX + 6, appleY + 4, UNIT_SIZE / 3, UNIT_SIZE / 3);

        // Draw snake
        for (int i = 0; i < bodyParts; i++) {
            if (i == 0) {
                g2.setColor(new Color(120, 220, 120));
                g2.fillRoundRect(x[i], y[i], UNIT_SIZE, UNIT_SIZE, UNIT_SIZE / 2, UNIT_SIZE / 2);
            } else {
                float t = (float) i / Math.max(1, bodyParts - 1);
                int r = (int) (30 + t * 80);
                int gr = (int) (120 + t * 80);
                int b = (int) (30 + t * 20);
                g2.setColor(new Color(r, gr, b));
                g2.fillOval(x[i] + 2, y[i] + 2, UNIT_SIZE - 4, UNIT_SIZE - 4);
            }
        }

        // HUD bar
        g2.setColor(new Color(0, 0, 0, 120));
        g2.fillRect(0, 0, WIDTH, 36);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 18));
        g2.drawString("Score: " + applesEaten, 12, 24);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString("Arrows: Move  ", 140, 20);
        g2.drawString("P: Pause  ", 240, 20);
        g2.drawString("Space: Restart (after game over)", 320, 20);

        if (!running) {
            gameOver(g2);
        } else if (paused) {
            g2.setColor(new Color(0, 0, 0, 160));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 48));
            FontMetrics fm = g2.getFontMetrics();
            String s = "PAUSED";
            g2.drawString(s, (WIDTH - fm.stringWidth(s)) / 2, HEIGHT / 2);
        }

        g2.dispose();
    }

    private void gameOver(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Ink Free", Font.BOLD, 40));
        FontMetrics metrics1 = getFontMetrics(g.getFont());
        g.drawString("Game Over", (WIDTH - metrics1.stringWidth("Game Over")) / 2, HEIGHT / 2 - 20);

        g.setFont(new Font("Ink Free", Font.PLAIN, 20));
        FontMetrics metrics2 = getFontMetrics(g.getFont());
        g.drawString("Score: " + applesEaten, (WIDTH - metrics2.stringWidth("Score: " + applesEaten)) / 2,
                HEIGHT / 2 + 10);

        g.setFont(new Font("Ink Free", Font.PLAIN, 16));
        g.drawString("Press SPACE to restart", (WIDTH - metrics2.stringWidth("Press SPACE to restart")) / 2,
                HEIGHT / 2 + 40);
    }

    private class MyKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (direction != 'R')
                        direction = 'L';
                    break;
                case KeyEvent.VK_RIGHT:
                    if (direction != 'L')
                        direction = 'R';
                    break;
                case KeyEvent.VK_UP:
                    if (direction != 'D')
                        direction = 'U';
                    break;
                case KeyEvent.VK_DOWN:
                    if (direction != 'U')
                        direction = 'D';
                    break;
                case KeyEvent.VK_P:
                    if (running) {
                        paused = !paused;
                        if (paused) {
                            if (timer != null)
                                timer.stop();
                        } else {
                            if (timer != null)
                                timer.start();
                        }
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (!running) {
                        resetGame();
                    }
                    break;
            }
        }
    }

    private void resetGame() {
        bodyParts = 6;
        applesEaten = 0;
        direction = 'R';
        paused = false;
        for (int i = 0; i < bodyParts; i++) {
            x[i] = 0;
            y[i] = 0;
        }
        x[0] = UNIT_SIZE * 5;
        y[0] = UNIT_SIZE * 5;
        newApple();
        running = true;
        timer = new Timer(DELAY, this);
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            SnakeGame gamePanel = new SnakeGame();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}
