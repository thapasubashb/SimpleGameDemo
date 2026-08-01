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

    private static final int SIDE_PANEL_WIDTH = 280;
    private static final Color BACKGROUND_TOP = new Color(8, 14, 42);
    private static final Color BACKGROUND_BOTTOM = new Color(24, 42, 88);
    private static final Color HUD_PANEL_COLOR = new Color(0, 0, 0, 150);
    private static final Color HUD_TEXT_COLOR = new Color(230, 240, 255, 220);
    private static final Color HUD_HINT_COLOR = new Color(220, 230, 255, 200);

    private static final Font HUD_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font HUD_TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 18);
    private static final Font HUD_HINT_FONT = new Font("Segoe UI", Font.PLAIN, 14);
    private static final Font OVERLAY_TITLE_FONT = new Font("Segoe UI", Font.BOLD, 52);
    private static final Font OVERLAY_TEXT_FONT = new Font("Segoe UI", Font.PLAIN, 20);

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
        initializePanel();
        timer = new Timer(TIMER_DELAY, this);
        resetGame(false);
    }

    private void initializePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(BACKGROUND_TOP);
        setFocusable(true);
        addKeyListener(new GameInputAdapter());
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
            pauseGame();
        } else if (gameState == GameState.PAUSED) {
            resumeGame();
        }
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

    private void pauseGame() {
        gameState = GameState.PAUSED;
        timer.stop();
        notifyStatus();
        repaint();
    }

    private void resumeGame() {
        gameState = GameState.PLAYING;
        timer.start();
        notifyStatus();
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameState == GameState.PLAYING) {
            updateGame();
        }
        notifyStatus();
        repaint();
    }

    private void updateGame() {
        updatePlayer();
        updateObstacles();
        checkCollisions();
        score++;
        obstacleSpeed = INITIAL_SPEED + score / 700;
        spawnObstacleIfNeeded();
    }

    private void updatePlayer() {
        if (movingLeft) {
            playerX = Math.max(20, playerX - 8);
        }
        if (movingRight) {
            playerX = Math.min(WIDTH - PLAYER_WIDTH - 20, playerX + 8);
        }
    }

    private void spawnObstacleIfNeeded() {
        spawnCounter += TIMER_DELAY;
        int delay = Math.max(360, 1000 - score / 2);
        if (spawnCounter >= delay) {
            spawnCounter = 0;
            int x = random.nextInt(WIDTH - OBSTACLE_SIZE - 40) + 20;
            obstacles.add(new Rectangle(x, -OBSTACLE_SIZE, OBSTACLE_SIZE, OBSTACLE_SIZE));
        }
    }

    private void updateObstacles() {
        for (Iterator<Rectangle> iterator = obstacles.iterator(); iterator.hasNext();) {
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
                endGame();
                break;
            }
        }
    }

    private void endGame() {
        gameState = GameState.GAME_OVER;
        timer.stop();
        highScore = Math.max(highScore, score);
    }

    private void resetGame(boolean startPlaying) {
        obstacles.clear();
        playerX = WIDTH / 2 - PLAYER_WIDTH / 2;
        score = 0;
        obstacleSpeed = INITIAL_SPEED;
        spawnCounter = 0;
        movingLeft = false;
        movingRight = false;

        gameState = startPlaying ? GameState.PLAYING : GameState.START;
        if (startPlaying && !timer.isRunning()) {
            timer.start();
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
        SwingUtilities.invokeLater(DodgeGame::createAndShowGui);
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("Dodge Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        DodgeGame gamePanel = new DodgeGame();
        JPanel sidePanel = buildSidePanel(gamePanel);

        JPanel container = new JPanel(new BorderLayout());
        container.add(gamePanel, BorderLayout.CENTER);
        container.add(sidePanel, BorderLayout.EAST);

        frame.setContentPane(container);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
    }

    private static JPanel buildSidePanel(DodgeGame gamePanel) {
        JPanel sidePanel = new JPanel();
        sidePanel.setBackground(new Color(18, 24, 52));
        sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, HEIGHT));
        sidePanel.setLayout(new GridBagLayout());

        JLabel titleLabel = createInfoLabel("Dodge UI", new Font("Segoe UI", Font.BOLD, 26), new Color(210, 230, 255));
        JLabel scoreLabel = createInfoLabel("Score: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel highScoreLabel = createInfoLabel("Best: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel speedLabel = createInfoLabel("Speed: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel stateLabel = createInfoLabel("State: Start", new Font("Segoe UI", Font.PLAIN, 16),
                new Color(180, 220, 255));

        JButton startButton = createControlButton("Start", () -> {
            gamePanel.startGame();
            gamePanel.requestFocusInWindow();
        });
        JButton pauseButton = createControlButton("Pause", () -> {
            gamePanel.togglePause();
            gamePanel.requestFocusInWindow();
        });
        JButton restartButton = createControlButton("Restart", () -> {
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

        return sidePanel;
    }

    private static JLabel createInfoLabel(String text, Font font, Color color) {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(font);
        return label;
    }

    private static JButton createControlButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.addActionListener(e -> action.run());
        return button;
    }
}
