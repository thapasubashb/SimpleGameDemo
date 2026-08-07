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
    private static final Color BACKGROUND_TOP = new Color(4, 10, 24);
    private static final Color BACKGROUND_BOTTOM = new Color(12, 35, 80);
    private static final Color HUD_PANEL_COLOR = new Color(6, 12, 36, 220);
    private static final Color HUD_TEXT_COLOR = new Color(241, 247, 255);
    private static final Color HUD_HINT_COLOR = new Color(188, 210, 255);
    private static final Color ACCENT_BLUE = new Color(91, 208, 255);
    private static final Color ACCENT_PINK = new Color(255, 96, 172);
    private static final Color PANEL_BORDER = new Color(96, 168, 255, 140);

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
        int panelX = 16;
        int panelY = 16;
        int panelW = 270;
        int panelH = 188;

        g2.setColor(HUD_PANEL_COLOR);
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 24, 24);
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(panelX + 1, panelY + 1, panelW - 2, panelH - 2, 24, 24);

        g2.setColor(ACCENT_BLUE);
        g2.fillRoundRect(panelX + 18, panelY + 18, 6, 24, 6, 6);
        g2.setColor(HUD_TEXT_COLOR);
        g2.setFont(HUD_TITLE_FONT);
        g2.drawString("Neon Dodge", panelX + 34, panelY + 40);

        g2.setFont(HUD_HINT_FONT);
        g2.setColor(HUD_HINT_COLOR);
        g2.drawString("Arcade reflexes, neon glow", panelX + 34, panelY + 62);

        drawHudStatCard(g2, panelX + 24, panelY + 78, "Score", String.valueOf(score), ACCENT_BLUE);
        drawHudStatCard(g2, panelX + 144, panelY + 78, "Best", String.valueOf(highScore), ACCENT_PINK);

        g2.setFont(HUD_TEXT_FONT);
        g2.setColor(HUD_TEXT_COLOR);
        g2.drawString("Speed: " + obstacleSpeed, panelX + 24, panelY + 146);
        g2.setFont(HUD_HINT_FONT);
        g2.setColor(HUD_HINT_COLOR);
        g2.drawString("← / → move   P pause   Space start", panelX + 24, panelY + 168);
    }

    private void drawHudStatCard(Graphics2D g2, int x, int y, String label, String value, Color accent) {
        g2.setColor(new Color(255, 255, 255, 18));
        g2.fillRoundRect(x, y, 96, 52, 16, 16);
        g2.setColor(accent);
        g2.drawRoundRect(x + 1, y + 1, 94, 50, 16, 16);
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(HUD_HINT_COLOR);
        g2.drawString(label, x + 12, y + 18);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 16));
        g2.setColor(HUD_TEXT_COLOR);
        g2.drawString(value, x + 12, y + 38);
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
        g2.setColor(new Color(2, 10, 28, 200));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int cardX = 120;
        int cardY = 140;
        int cardW = 560;
        int cardH = 300;
        g2.setColor(new Color(7, 19, 44, 235));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 32, 32);
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(cardX + 1, cardY + 1, cardW - 2, cardH - 2, 32, 32);

        g2.setColor(ACCENT_BLUE);
        g2.drawLine(cardX + 40, cardY + 50, cardX + cardW - 40, cardY + 50);

        g2.setColor(HUD_TEXT_COLOR);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 54));
        String title = "Dodge the Fall";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(title, (WIDTH - fm.stringWidth(title)) / 2, cardY + 112);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g2.setColor(HUD_HINT_COLOR);
        String message = "Move quickly, avoid the neon hazards, and beat your best score.";
        g2.drawString(message, (WIDTH - g2.getFontMetrics().stringWidth(message)) / 2, cardY + 150);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(ACCENT_PINK);
        String prompt = "Press SPACE or use Start to begin";
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, cardY + 210);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(2, 10, 28, 220));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int cardX = 210;
        int cardY = 200;
        int cardW = 380;
        int cardH = 180;
        g2.setColor(new Color(7, 19, 44, 235));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 30, 30);
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(cardX + 1, cardY + 1, cardW - 2, cardH - 2, 30, 30);

        g2.setColor(ACCENT_BLUE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 48));
        String text = "Paused";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, cardY + 80);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        g2.setColor(HUD_HINT_COLOR);
        String prompt = "Press P to resume your run";
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, cardY + 122);
    }

    private void drawGameOverScreen(Graphics2D g2) {
        g2.setColor(new Color(2, 10, 28, 220));
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        int cardX = 140;
        int cardY = 140;
        int cardW = 520;
        int cardH = 300;
        g2.setColor(new Color(7, 19, 44, 235));
        g2.fillRoundRect(cardX, cardY, cardW, cardH, 32, 32);
        g2.setColor(PANEL_BORDER);
        g2.drawRoundRect(cardX + 1, cardY + 1, cardW - 2, cardH - 2, 32, 32);

        g2.setColor(ACCENT_PINK);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 50));
        String text = "Run Complete";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(text, (WIDTH - fm.stringWidth(text)) / 2, cardY + 104);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, 22));
        g2.setColor(HUD_TEXT_COLOR);
        String scoreText = "Final Score: " + score;
        g2.drawString(scoreText, (WIDTH - g2.getFontMetrics().stringWidth(scoreText)) / 2, cardY + 152);
        String bestText = "Best Score: " + highScore;
        g2.drawString(bestText, (WIDTH - g2.getFontMetrics().stringWidth(bestText)) / 2, cardY + 186);

        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(ACCENT_BLUE);
        String prompt = "Press SPACE or use Restart to try again";
        g2.drawString(prompt, (WIDTH - g2.getFontMetrics().stringWidth(prompt)) / 2, cardY + 236);
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

    private class GameInputAdapter extends KeyAdapter {
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
        sidePanel.setBackground(new Color(7, 14, 34));
        sidePanel.setPreferredSize(new Dimension(SIDE_PANEL_WIDTH, HEIGHT));
        sidePanel.setLayout(new BorderLayout());
        sidePanel.setBorder(BorderFactory.createEmptyBorder(24, 20, 24, 20));

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        JLabel titleLabel = createInfoLabel("NEON DODGE", new Font("Segoe UI", Font.BOLD, 28),
                new Color(240, 248, 255));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel subtitleLabel = createInfoLabel("A modern arcade run", new Font("Segoe UI", Font.PLAIN, 15),
                new Color(180, 205, 255));
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel topCard = new JPanel();
        topCard.setLayout(new BoxLayout(topCard, BoxLayout.Y_AXIS));
        topCard.setOpaque(false);
        topCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(96, 168, 255, 120)),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)));
        topCard.add(titleLabel);
        topCard.add(Box.createVerticalStrut(6));
        topCard.add(subtitleLabel);

        JLabel scoreLabel = createInfoLabel("Score: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel highScoreLabel = createInfoLabel("Best: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel speedLabel = createInfoLabel("Speed: 0", HUD_TEXT_FONT, Color.WHITE);
        JLabel stateLabel = createInfoLabel("State: START", new Font("Segoe UI", Font.PLAIN, 15),
                new Color(180, 220, 255));
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        highScoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        speedLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        stateLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel statsCard = new JPanel();
        statsCard.setLayout(new BoxLayout(statsCard, BoxLayout.Y_AXIS));
        statsCard.setOpaque(false);
        statsCard.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        statsCard.add(scoreLabel);
        statsCard.add(Box.createVerticalStrut(8));
        statsCard.add(highScoreLabel);
        statsCard.add(Box.createVerticalStrut(8));
        statsCard.add(speedLabel);
        statsCard.add(Box.createVerticalStrut(8));
        statsCard.add(stateLabel);

        JButton startButton = createControlButton("Start Run", () -> {
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

        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setOpaque(false);
        buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
        buttonsPanel.add(startButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(pauseButton);
        buttonsPanel.add(Box.createVerticalStrut(10));
        buttonsPanel.add(restartButton);

        JLabel footerLabel = createInfoLabel("Tip: stay centered and react fast.", new Font("Segoe UI", Font.PLAIN, 13),
                new Color(170, 193, 225));
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        content.add(topCard);
        content.add(Box.createVerticalStrut(18));
        content.add(statsCard);
        content.add(Box.createVerticalStrut(20));
        content.add(buttonsPanel);
        content.add(Box.createVerticalStrut(18));
        content.add(footerLabel);
        sidePanel.add(content, BorderLayout.NORTH);

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
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(31, 99, 255));
        button.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.addActionListener(e -> action.run());
        return button;
    }
}
