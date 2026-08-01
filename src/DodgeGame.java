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

    private enum GameState {
        START,
        PLAYING,
        PAUSED,
        GAME_OVER
    }

    private final List<Rectangle> obstacles = new ArrayList<>();
    private final Random random = new Random();
    private final Timer timer;

    private int playerX;
    private boolean movingLeft;
    private boolean movingRight;
    private int score;
    private int highScore;
    private int obstacleSpeed;
    private int spawnCounter;
    private GameState gameState = GameState.START;

    private GameListener statusListener;

    public DodgeGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(12, 18, 42));
        setFocusable(true);
        addKeyListener(new InputAdapter());

        timer = new Timer(TIMER_DELAY, this);
        timer.start();
        resetGame(false);
    }

    public void setGameListener(GameListener listener) {
        statusListener = listener;
        notifyStatus();
    }

    public void startGame() {
        if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
            resetGame(true);
        }
    }

    public void togglePause() {
        if (gameState == GameState.PLAYING) {
            gameState = GameState.PAUSED;
            timer.stop();
        } else if (gameState == GameState.PAUSED) {
            gameState = GameState.PLAYING;
            timer.start();
        }
        notifyStatus();
        repaint();
    }

    public void restartGame() {
        resetGame(true);
    }

    public GameState getGameState() {
        return gameState;
    }

    public int getScore() {
        return score;
    }

    public int getHighScore() {
        return highScore;
    }

    public int getSpeed() {
        return obstacleSpeed;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updatePlayer();
            updateObstacles();
            checkCollisions();
            score++;
            obstacleSpeed = INITIAL_SPEED + score / 700;
            spawnCounter += TIMER_DELAY;
            if (spawnCounter >= Math.max(360, 1000 - score / 2)) {
                spawnCounter = 0;
                spawnObstacle();
            }
        }
        notifyStatus();
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
                gameState = GameState.GAME_OVER;
                timer.stop();
                highScore = Math.max(highScore, score);
                break;
            }
        }
    }

    private void resetGame(boolean startPlaying) {
        obstacles.clear();
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        score = 0;
        obstacleSpeed = INITIAL_SPEED;
        spawnCounter = 0;
        movingLeft = false;
        movingRight = false;

        if (startPlaying) {
            gameState = GameState.PLAYING;
            if (!timer.isRunning()) {
                timer.start();
            }
        } else {
            gameState = GameState.START;
        }

        notifyStatus();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint background = new GradientPaint(0, 0, new Color(8, 14, 42), 0, HEIGHT, new Color(24, 42, 88));
        g2.setPaint(background);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        drawStars(g2);
        drawGrid(g2);
        drawPlayer(g2);
        drawObstacles(g2);
        drawHud(g2);

        if (gameState == GameState.START) {
            drawStartScreen(g2);
        } else if (gameState == GameState.PAUSED) {
            drawPauseScreen(g2);
        } else if (gameState == GameState.GAME_OVER) {
            drawGameOverScreen(g2);
        }

        g2.dispose();
    }

    private void drawStars(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 40));
        for (int i = 0; i < 45; i++) {
            int x = (i * 37) % WIDTH;
            int y = (i * 17 + 13) % HEIGHT;
            int size = 2 + (i % 3);
            g2.fillOval(x, y, size, size);
        }
    }

    private void drawGrid(Graphics2D g2) {
        g2.setColor(new Color(255, 255, 255, 18));
        for (int x = 0; x < WIDTH; x += 48) {
            g2.drawLine(x, 0, x, HEIGHT);
        }
        for (int y = 0; y < HEIGHT; y += 48) {
            g2.drawLine(0, y, WIDTH, y);
        }
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRoundRect(16, 16, 250, 120, 24, 24);

        g2.setColor(new Color(230, 240, 255, 220));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 22));
        g2.drawString("Dodge Game", 32, 46);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2.drawString("Score: " + score, 32, 74);
        g2.drawString("Best: " + highScore, 32, 98);
        g2.drawString("Speed: " + obstacleSpeed, 32, 122);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        g2.setColor(new Color(220, 230, 255, 200));
        g2.drawString("Use ← / → to move", 32, 150);
        g2.drawString("Press P to pause", 32, 170);
        g2.drawString("Space to begin or restart", 32, 190);
    }

    private void drawPlayer(Graphics2D g2) {
        GradientPaint playerPaint = new GradientPaint(playerX, PLAYER_Y, new Color(120, 220, 190),
                playerX + PLAYER_WIDTH, PLAYER_Y + PLAYER_HEIGHT, new Color(60, 110, 190));
        g2.setPaint(playerPaint);
        g2.fillRoundRect(playerX, PLAYER_Y, PLAYER_WIDTH, PLAYER_HEIGHT, 22, 22);
        g2.setColor(new Color(255, 255, 255, 160));
        g2.fillRoundRect(playerX + 14, PLAYER_Y + 4, 24, 8, 12, 12);
    }

    private void drawObstacles(Graphics2D g2) {
        for (Rectangle obstacle : obstacles) {
            GradientPaint obstaclePaint = new GradientPaint(obstacle.x, obstacle.y, new Color(255, 170, 70),
                    obstacle.x + OBSTACLE_SIZE, obstacle.y + OBSTACLE_SIZE, new Color(220, 60, 40));
            g2.setPaint(obstaclePaint);
            g2.fillOval(obstacle.x, obstacle.y, obstacle.width, obstacle.height);
            g2.setColor(new Color(255, 255, 255, 140));
            g2.fillOval(obstacle.x + 6, obstacle.y + 6, 10, 10);
        }
    }

    private void drawStartScreen(Graphics2D g2) {
        g2.setColor(new Color(2, 10, 28, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(255, 255, 255, 220));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 52));
        String title = "Dodge the Fall";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, HEIGHT / 2 - 40);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        String message = "Move quickly, avoid the obstacles, and beat your best score.";
        g2.drawString(message, (WIDTH - g2.getFontMetrics().stringWidth(message)) / 2, HEIGHT / 2 + 10);

        String prompt = "Press SPACE or click Start to begin";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, HEIGHT / 2 + 50);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 170));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(255, 255, 255, 230));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
        String text = "Paused";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, HEIGHT / 2 - 10);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        String prompt = "Press P to resume";
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, HEIGHT / 2 + 30);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
        String text = "Game Over";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, HEIGHT / 2 - 40);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 24));
        String scoreText = "Final Score: " + score;
        g2.drawString(scoreText, (WIDTH - g2.getFontMetrics().stringWidth(scoreText)) / 2, HEIGHT / 2 + 10);
        String bestText = "Best Score: " + highScore;
        g2.drawString(bestText, (WIDTH - g2.getFontMetrics().stringWidth(bestText)) / 2, HEIGHT / 2 + 45);

        String prompt = "Press SPACE or click Restart";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, HEIGHT / 2 + 85);
    }

    private class InputAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_LEFT:
                    if (gameState == GameState.PLAYING) {
                        movingLeft = true;
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if (gameState == GameState.PLAYING) {
                        movingRight = true;
                    }
                    break;
                case KeyEvent.VK_SPACE:
                    if (gameState == GameState.START || gameState == GameState.GAME_OVER) {
                        startGame();
                    }
                    break;
                case KeyEvent.VK_P:
                    if (gameState == GameState.PLAYING || gameState == GameState.PAUSED) {
                        togglePause();
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            if (gameState != GameState.PLAYING) {
                return;
            }
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

    private void notifyStatus() {
        if (statusListener != null) {
            statusListener.onStatusUpdate(score, highScore, obstacleSpeed, gameState);
        }
    }

    public interface GameListener {
        void onStatusUpdate(int score, int highScore, int speed, GameState state);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Dodge Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            DodgeGame gamePanel = new DodgeGame();

            JPanel sidePanel = new JPanel();
            sidePanel.setBackground(new Color(18, 24, 52));
            sidePanel.setPreferredSize(new Dimension(280, HEIGHT));
            sidePanel.setLayout(new GridBagLayout());

            JLabel titleLabel = new JLabel("Dodge UI");
            titleLabel.setForeground(new Color(210, 230, 255));
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 26));

            JLabel scoreLabel = new JLabel("Score: 0");
            scoreLabel.setForeground(Color.WHITE);
            scoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

            JLabel highScoreLabel = new JLabel("Best: 0");
            highScoreLabel.setForeground(Color.WHITE);
            highScoreLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

            JLabel speedLabel = new JLabel("Speed: 0");
            speedLabel.setForeground(Color.WHITE);
            speedLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));

            JLabel stateLabel = new JLabel("State: Start");
            stateLabel.setForeground(new Color(180, 220, 255));
            stateLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));

            JButton startButton = new JButton("Start");
            startButton.setFocusPainted(false);
            startButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            startButton.addActionListener(e -> {
                gamePanel.startGame();
                gamePanel.requestFocusInWindow();
            });

            JButton pauseButton = new JButton("Pause");
            pauseButton.setFocusPainted(false);
            pauseButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            pauseButton.addActionListener(e -> {
                gamePanel.togglePause();
                gamePanel.requestFocusInWindow();
            });

            JButton restartButton = new JButton("Restart");
            restartButton.setFocusPainted(false);
            restartButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            restartButton.addActionListener(e -> {
                gamePanel.restartGame();
                gamePanel.requestFocusInWindow();
            });

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.insets = new Insets(16, 16, 12, 16);
            gbc.anchor = GridBagConstraints.WEST;
            gbc.fill = GridBagConstraints.HORIZONTAL;

            gbc.gridy = 0;
            sidePanel.add(titleLabel, gbc);
            gbc.gridy = 1;
            sidePanel.add(scoreLabel, gbc);
            gbc.gridy = 2;
            sidePanel.add(highScoreLabel, gbc);
            gbc.gridy = 3;
            sidePanel.add(speedLabel, gbc);
            gbc.gridy = 4;
            sidePanel.add(stateLabel, gbc);
            gbc.gridy = 5;
            gbc.insets = new Insets(24, 16, 4, 16);
            sidePanel.add(startButton, gbc);
            gbc.gridy = 6;
            sidePanel.add(pauseButton, gbc);
            gbc.gridy = 7;
            sidePanel.add(restartButton, gbc);

            gamePanel.setGameListener((currentScore, bestScore, speed, state) -> {
                scoreLabel.setText("Score: " + currentScore);
                highScoreLabel.setText("Best: " + bestScore);
                speedLabel.setText("Speed: " + speed);
                stateLabel.setText("State: " + state.name());
                pauseButton.setText(state == GameState.PLAYING ? "Pause" : "Resume");
            });

            JPanel container = new JPanel(new BorderLayout());
            container.add(gamePanel, BorderLayout.CENTER);
            container.add(sidePanel, BorderLayout.EAST);

            frame.setContentPane(container);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            gamePanel.requestFocusInWindow();
        });
    }
}
