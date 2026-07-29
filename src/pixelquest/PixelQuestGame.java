package pixelquest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

public class PixelQuestGame extends JPanel implements ActionListener {
    private static final int WIDTH = 900;
    private static final int HEIGHT = 560;
    private static final int GROUND_Y = 500;

    private final Timer timer;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<Coin> coins = new ArrayList<>();
    private final List<Enemy> enemies = new ArrayList<>();

    private Player player;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean jumpRequested;
    private boolean gameOver;
    private boolean won;
    private int score;
    private int lives = 3;

    public PixelQuestGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(80, 180, 255));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        leftPressed = true;
                        break;
                    case KeyEvent.VK_RIGHT:
                        rightPressed = true;
                        break;
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_SPACE:
                        if (!gameOver && !won) {
                            jumpRequested = true;
                        }
                        break;
                    case KeyEvent.VK_R:
                        resetLevel();
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        leftPressed = false;
                        break;
                    case KeyEvent.VK_RIGHT:
                        rightPressed = false;
                        break;
                }
            }
        });

        timer = new Timer(16, this);
        resetLevel();
        timer.start();
    }

    private void resetLevel() {
        score = 0;
        lives = 3;
        gameOver = false;
        won = false;
        platforms.clear();
        coins.clear();
        enemies.clear();

        player = new Player(90, GROUND_Y - 70);

        platforms.add(new Platform(0, GROUND_Y, WIDTH, 60));
        platforms.add(new Platform(120, 430, 180, 18));
        platforms.add(new Platform(340, 360, 180, 18));
        platforms.add(new Platform(570, 430, 180, 18));
        platforms.add(new Platform(710, 290, 150, 18));

        coins.add(new Coin(170, 390));
        coins.add(new Coin(410, 320));
        coins.add(new Coin(640, 390));
        coins.add(new Coin(760, 250));

        enemies.add(new Enemy(220, 402, 1));
        enemies.add(new Enemy(400, 332, -1));
        enemies.add(new Enemy(600, 402, 1));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver && !won) {
            handleMovement();
            applyGravity();
            handlePlatformCollisions();
            collectCoins();
            updateEnemies();
            checkEnemyHits();
            checkWin();
        }
        repaint();
    }

    private void handleMovement() {
        if (leftPressed) {
            player.x -= 5;
        }
        if (rightPressed) {
            player.x += 5;
        }
        if (jumpRequested && player.onGround) {
            player.vy = -15;
            player.onGround = false;
        }
        jumpRequested = false;
    }

    private void applyGravity() {
        player.vy += 1;
        player.y += player.vy;
        if (player.y + player.height >= GROUND_Y) {
            player.y = GROUND_Y - player.height;
            player.vy = 0;
            player.onGround = true;
        }
    }

    private void handlePlatformCollisions() {
        int prevBottom = player.y + player.height;
        for (Platform platform : platforms) {
            Rectangle playerRect = new Rectangle(player.x, player.y, player.width, player.height);
            Rectangle platformRect = new Rectangle(platform.x, platform.y, platform.width, platform.height);
            if (playerRect.intersects(platformRect)) {
                if (prevBottom <= platform.y + 8 && player.vy >= 0) {
                    player.y = platform.y - player.height;
                    player.vy = 0;
                    player.onGround = true;
                }
            }
        }
    }

    private void collectCoins() {
        for (int i = coins.size() - 1; i >= 0; i--) {
            Coin coin = coins.get(i);
            if (player.intersects(coin)) {
                coins.remove(i);
                score += 100;
            }
        }
    }

    private void updateEnemies() {
        for (Enemy enemy : enemies) {
            enemy.x += enemy.direction * 2;
            if (enemy.x <= 80 || enemy.x >= WIDTH - 80) {
                enemy.direction *= -1;
            }
        }
    }

    private void checkEnemyHits() {
        for (Enemy enemy : enemies) {
            Rectangle playerRect = player.getRect();
            Rectangle enemyRect = enemy.getRect();
            if (playerRect.intersects(enemyRect)) {
                lives--;
                player.x = 90;
                player.y = GROUND_Y - 70;
                player.vy = 0;
                player.onGround = true;
                if (lives <= 0) {
                    gameOver = true;
                }
                break;
            }
        }
    }

    private void checkWin() {
        if (player.x >= WIDTH - 120 && score >= 100) {
            won = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawBackground(g2);
        drawPlatforms(g2);
        drawCoins(g2);
        drawEnemies(g2);
        drawPlayer(g2);
        drawGoal(g2);
        drawHud(g2);

        if (won) {
            drawOverlay(g2, "You Win!", "Press R to play again");
        } else if (gameOver) {
            drawOverlay(g2, "Game Over", "Press R to try again");
        }

        g2.dispose();
    }

    private void drawBackground(Graphics2D g2) {
        GradientPaint sky = new GradientPaint(0, 0, new Color(30, 120, 220), 0, HEIGHT, new Color(160, 235, 255));
        g2.setPaint(sky);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(255, 220, 90));
        g2.fillOval(720, 60, 95, 95);

        g2.setColor(Color.WHITE);
        g2.fillOval(130, 70, 70, 45);
        g2.fillOval(180, 55, 95, 60);

        g2.setColor(new Color(120, 180, 120));
        g2.fillPolygon(new int[] { 0, 180, 360, 560, 740, 900 }, new int[] { GROUND_Y, 420, 380, 455, 410, GROUND_Y },
                6);
        g2.fillRect(0, GROUND_Y, WIDTH, HEIGHT - GROUND_Y);

        g2.setColor(new Color(90, 70, 40));
        g2.fillRect(0, GROUND_Y, WIDTH, 10);
    }

    private void drawPlatforms(Graphics2D g2) {
        for (Platform platform : platforms) {
            g2.setColor(new Color(140, 90, 40));
            g2.fillRect(platform.x, platform.y, platform.width, platform.height);
            g2.setColor(new Color(220, 165, 110));
            g2.fillRect(platform.x, platform.y, platform.width, 6);
        }
    }

    private void drawCoins(Graphics2D g2) {
        for (Coin coin : coins) {
            g2.setColor(new Color(255, 210, 60));
            g2.fillOval(coin.x, coin.y, coin.size, coin.size);
            g2.setColor(new Color(180, 130, 0));
            g2.drawOval(coin.x, coin.y, coin.size, coin.size);
        }
    }

    private void drawEnemies(Graphics2D g2) {
        for (Enemy enemy : enemies) {
            g2.setColor(new Color(190, 50, 50));
            g2.fillRect(enemy.x, enemy.y, enemy.width, enemy.height);
            g2.setColor(Color.WHITE);
            g2.fillRect(enemy.x + 10, enemy.y + 8, 8, 8);
            g2.fillRect(enemy.x + 22, enemy.y + 8, 8, 8);
        }
    }

    private void drawPlayer(Graphics2D g2) {
        g2.setColor(new Color(40, 160, 240));
        g2.fillRect(player.x, player.y, player.width, player.height);
        g2.setColor(Color.WHITE);
        g2.fillRect(player.x + 8, player.y + 8, 10, 10);
        g2.fillRect(player.x + 20, player.y + 8, 10, 10);
        g2.setColor(Color.BLACK);
        g2.fillRect(player.x + 10, player.y + 24, 8, 8);
        g2.fillRect(player.x + 22, player.y + 24, 8, 8);
        g2.setColor(new Color(250, 110, 60));
        g2.fillRect(player.x + 7, player.y + 30, 24, 8);
    }

    private void drawGoal(Graphics2D g2) {
        g2.setColor(new Color(80, 80, 80));
        g2.fillRect(WIDTH - 100, 350, 16, 120);
        g2.setColor(new Color(230, 60, 60));
        g2.fillRect(WIDTH - 100, 350, 70, 24);
        g2.setColor(new Color(255, 220, 90));
        g2.fillRect(WIDTH - 100, 374, 70, 18);
    }

    private void drawHud(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRoundRect(20, 20, 260, 95, 18, 18);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 22));
        g2.drawString("Pixel Quest", 40, 50);
        g2.setFont(new Font("Arial", Font.PLAIN, 16));
        g2.drawString("Score: " + score, 40, 78);
        g2.drawString("Lives: " + lives, 40, 100);
        g2.drawString("Move with arrows, jump with space", 300, 40);
    }

    private void drawOverlay(Graphics2D g2, String title, String message) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, WIDTH, HEIGHT);
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Arial", Font.BOLD, 42));
        g2.drawString(title, WIDTH / 2 - 110, HEIGHT / 2 - 20);
        g2.setFont(new Font("Arial", Font.PLAIN, 20));
        g2.drawString(message, WIDTH / 2 - 120, HEIGHT / 2 + 20);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Pixel Quest");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            PixelQuestGame game = new PixelQuestGame();
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
