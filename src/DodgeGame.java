import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class DodgeGame extends JPanel implements ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int PLAYER_WIDTH = 80;
    private static final int PLAYER_HEIGHT = 16;
    private static final int PLAYER_Y = HEIGHT - 80;
    private static final int OBSTACLE_SIZE = 28;
    private static final int INITIAL_SPEED = 4;
    private static final int TIMER_DELAY = 20;

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Random random = new Random();
    private final Timer timer;
    private int playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
    private boolean movingLeft;
    private boolean movingRight;
    private boolean gameOver;
    private int score;
    private int obstacleSpeed = INITIAL_SPEED;
    private int spawnCounter;

    public DodgeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(12, 18, 42));
        setFocusable(true);
        addKeyListener(new InputAdapter());
        timer = new Timer(TIMER_DELAY, this);
        timer.start();
        resetGame();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            updatePlayer();
            updateObstacles();
            checkCollisions();
            score++;
            obstacleSpeed = INITIAL_SPEED + score / 700;
            spawnCounter += TIMER_DELAY;
            if (spawnCounter >= Math.max(400, 1000 - score / 2)) {
                spawnCounter = 0;
                spawnObstacle();
            }
        }
        repaint();
    }

    private void updatePlayer() {
        if (movingLeft) {
            playerX = Math.max(20, playerX - 8);
        }
        if (movingRight) {
            playerX = Math.min(WIDTH - PLAYER_WIDTH - 20, playerX + 8);
        }
    }

    private void spawnObstacle() {
        int x = random.nextInt(WIDTH - OBSTACLE_SIZE - 40) + 20;
        obstacles.add(new Rectangle(x, -OBSTACLE_SIZE, OBSTACLE_SIZE, OBSTACLE_SIZE));
    }

    private void updateObstacles() {
        Iterator<Rectangle> iterator = obstacles.iterator();
        while (iterator.hasNext()) {
            Rectangle obstacle = iterator.next();
            obstacle.y += obstacleSpeed;
            if (obstacle.y > HEIGHT) {
                iterator.remove();
            }
        }
    }

    private void checkCollisions() {
        Rectangle player = new Rectangle(playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT);
        for (Rectangle obstacle : obstacles) {
            if (player.intersects(obstacle)) {
                gameOver = true;
                timer.stop();
                break;
            }
        }
    }

    private void resetGame() {
        obstacles.clear();
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        score = 0;
        gameOver = false;
        obstacleSpeed = INITIAL_SPEED;
        spawnCounter = 0;
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, new Color(10, 16, 44), 0, HEIGHT, new Color(22, 38, 82));
        g2.setPaint(background);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawHud(g2);
        drawPlayer(g2);
        drawObstacles(g2);

        if (gameOver) {
            drawGameOver(g2);
        }

        g2.dispose();
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 180));
        g2.setFont(new Font("SansSerif", Font.BOLD, 24));
        g2.drawString("Dodge Game", 24, 34);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 18));
        g2.drawString("Score: " + score, 24, 60);
        g2.drawString("Use left/right arrows to dodge", 24, 86);
        g2.drawString("Space to restart after game over", 24, 110);

        g2.setColor(new Color(255, 255, 255, 30));
        for (int x = 0; x < WIDTH; x += 40) {
            g2.drawLine(x, 0, x, HEIGHT);
        }
    }

    private void drawPlayer(Graphics2D g2) {
        GradientPaint playerPaint = new GradientPaint(playerX, PLAYER_Y, new Color(125, 220, 190),
                playerX + PLAYER_WIDTH, PLAYER_Y + PLAYER_HEIGHT, new Color(40, 120, 180));
        g2.setPaint(playerPaint);
        g2.fillRoundRect(playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, 18, 18);
        g2.setColor(new Color(255, 255, 255, 120));
        g2.fillRoundRect(playerX + 12, PLAYER_Y + 4, 20, 8, 12, 12);
    }

    private void drawObstacles(Graphics2D g2) {
        for (Rectangle obstacle : obstacles) {
            GradientPaint obstaclePaint = new GradientPaint(obstacle.x, obstacle.y, new Color(255, 150, 70),
                    obstacle.x + OBSTACLE_SIZE, obstacle.y + OBSTACLE_SIZE, new Color(220, 50, 40));
            g2.setPaint(obstaclePaint);
            g2.fillOval(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            g2.setColor(new Color(255, 255, 255, 130));
            g2.fillOval(obstacle.x + 6, obstacle.y + 6, 10, 10);
        }
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 48));
        String text = "Game Over";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, HEIGHT / 2 - 20);

        g2.setFont(new Font("SansSerif", Font.PLAIN, 22));
        String scoreText = "Final Score: " + score;
        g2.drawString(scoreText, (WIDTH - g2.getFontMetrics().stringWidth(scoreText)) / 2, HEIGHT / 2 + 20);

        String restart = "Press SPACE to restart";
        g2.drawString(restart, (WIDTH - g2.getFontMetrics().stringWidth(restart)) / 2, HEIGHT / 2 + 55);
    }

    private class InputAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    movingLeft = true;
                    break;
                case KeyEvent.VK_RIGHT:
                    movingRight = true;
                    break;
                case KeyEvent.VK_SPACE:
                    if (gameOver) {
                        resetGame();
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    movingLeft = false;
                    break;
                case KeyEvent.VK_RIGHT:
                    movingRight = false;
                    break;
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dodge Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            DodgeGame gamePanel = new DodgeGame();
            frame.add(gamePanel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}
