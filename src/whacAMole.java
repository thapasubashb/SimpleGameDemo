import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class whacAMole extends JPanel implements ActionListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 500;
    private static final int GROUND_Y = 420;

    private int playerX = 100;
    private int playerY = GROUND_Y - 50;
    private int playerWidth = 36;
    private int playerHeight = 50;
    private int playerVelY = 0;
    private boolean movingLeft = false;
    private boolean movingRight = false;
    private boolean jumping = false;
    private boolean gameOver = false;

    private int score = 0;
    private int enemyX = WIDTH - 80;
    private int enemyY = GROUND_Y - 32;
    private int enemyWidth = 30;
    private int enemyHeight = 32;
    private int enemySpeed = 4;

    private final Timer timer;

    public whacAMole() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(new Color(135, 206, 235));
        setFocusable(true);

        addKeyListener(new KeyAdapter() {
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
                        if (!jumping && !gameOver) {
                            jumping = true;
                            playerVelY = -15;
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
        });

        timer = new Timer(16, this);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!gameOver) {
            if (movingLeft && playerX > 0) {
                playerX -= 6;
            }
            if (movingRight && playerX < WIDTH - playerWidth) {
                playerX += 6;
            }

            playerVelY += 1;
            playerY += playerVelY;

            if (playerY + playerHeight >= GROUND_Y) {
                playerY = GROUND_Y - playerHeight;
                playerVelY = 0;
                jumping = false;
            }

            enemyX -= enemySpeed;
            if (enemyX + enemyWidth < 0) {
                enemyX = WIDTH;
                enemySpeed = 4 + (score / 5);
                score++;
            }

            if (checkCollision()) {
                gameOver = true;
            }
        }
        repaint();
    }

    private boolean checkCollision() {
        Rectangle playerRect = new Rectangle(playerX, playerY, playerWidth, playerHeight);
        Rectangle enemyRect = new Rectangle(enemyX, enemyY, enemyWidth, enemyHeight);
        return playerRect.intersects(enemyRect);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint sky = new GradientPaint(0, 0, new Color(20, 60, 140), 0, HEIGHT, new Color(120, 220, 255));
        g2.setPaint(sky);
        g2.fillRect(0, 0, WIDTH, HEIGHT);

        g2.setColor(new Color(255, 210, 80));
        g2.fillOval(620, 40, 90, 90);

        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval(80, 70, 60, 40);
        g2.fillOval(120, 60, 80, 50);
        g2.fillOval(560, 90, 70, 45);
        g2.fillOval(620, 75, 90, 55);

        g2.setColor(new Color(25, 90, 55));
        g2.fillRect(0, GROUND_Y, WIDTH, HEIGHT - GROUND_Y);

        g2.setColor(new Color(70, 150, 90));
        g2.fillPolygon(new int[] { 0, 140, 280, 420, 620, 800 }, new int[] { GROUND_Y, 330, 290, 360, 320, GROUND_Y },
                6);

        g2.setColor(new Color(40, 70, 120));
        g2.fillRect(0, GROUND_Y, WIDTH, 10);

        g2.setColor(new Color(160, 82, 45));
        g2.fillRect(250, 330, 140, 18);
        g2.fillRect(500, 300, 140, 18);

        g2.setColor(new Color(40, 80, 180));
        g2.fillOval(playerX, playerY, playerWidth, playerHeight);

        g2.setColor(new Color(255, 120, 30));
        g2.fillRect(playerX + 4, playerY + 18, playerWidth - 8, 20);

        g2.setColor(new Color(20, 20, 20));
        g2.fillRect(playerX + 8, playerY + 22, 8, 12);
        g2.fillRect(playerX + 20, playerY + 22, 8, 12);

        g2.setColor(new Color(255, 255, 255));
        g2.fillRect(playerX + 10, playerY + 8, 6, 6);
        g2.fillRect(playerX + 20, playerY + 8, 6, 6);

        g2.setColor(Color.BLACK);
        g2.drawOval(playerX + 10, playerY + 8, 6, 6);
        g2.drawOval(playerX + 20, playerY + 8, 6, 6);

        g2.setColor(Color.WHITE);
        g2.fillRect(playerX + 10, playerY + 12, 2, 2);
        g2.fillRect(playerX + 22, playerY + 12, 2, 2);

        g2.setColor(new Color(220, 40, 120));
        g2.fillRect(enemyX, enemyY, enemyWidth, enemyHeight);
        g2.setColor(new Color(30, 20, 20));
        g2.drawRect(enemyX, enemyY, enemyWidth, enemyHeight);

        g2.setColor(Color.BLACK);
        g2.setFont(new Font("Arial", Font.BOLD, 24));
        g2.drawString("Score: " + score, 20, 40);

        if (gameOver) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRect(0, 0, WIDTH, HEIGHT);
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 36));
            g2.drawString("Game Over", WIDTH / 2 - 110, HEIGHT / 2 - 10);
            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            g2.drawString("Close and run again to play", WIDTH / 2 - 145, HEIGHT / 2 + 25);
        }

        g2.dispose();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Mario Style Game");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);

            whacAMole game = new whacAMole();
            frame.add(game);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
