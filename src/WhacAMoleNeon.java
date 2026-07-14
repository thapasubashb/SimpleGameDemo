import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class WhacAMoleNeon extends JPanel implements ActionListener {
    
    // --- CONFIGURATION ---
    private final int BOARD_WIDTH = 640;
    private final int BOARD_HEIGHT = 700;
    private final int CELL_SIZE = 140;
    private final int GAP = 20;
    
    // --- COLORS (Cyber-Neon Palette) ---
    private final Color BG_DARK = new Color(10, 5, 25);
    private final Color TILE_BASE = new Color(40, 20, 70);
    private final Color TILE_GLOW = new Color(100, 50, 180);
    private final Color MOLE_COLOR = new Color(255, 0, 127); // Neon Pink
    private final Color PLANT_COLOR = new Color(255, 50, 50); // Neon Red
    private final Color HIT_COLOR = new Color(57, 255, 20);   // Neon Lime
    
    // --- GAME STATE ---
    private JButton[] tiles = new JButton[9];
    private JButton currentMole = null;
    private JButton currentPlant = null;
    private int score = 0;
    private int lives = 3;
    private int gameSpeed = 800; // ms
    private boolean gameRunning = false;
    
    // --- TIMERS ---
    private Timer moleTimer;
    private Timer plantTimer;
    private Timer gameLoopTimer; // For particles
    
    // --- PARTICLES ---
    private List<Particle> particles = new ArrayList<>();
    
    // --- UI COMPONENTS ---
    private JLabel scoreLabel;
    private JLabel livesLabel;
    private JLabel statusLabel;
    private JButton startButton;
    private Timer particleTimer;

    public WhacAMoleNeon() {
        setLayout(new BorderLayout());
        setBackground(BG_DARK);
        setFocusable(true);
        requestFocusInWindow();

        // --- HUD PANEL ---
        JPanel hudPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        hudPanel.setOpaque(false);
        hudPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        scoreLabel = createNeonLabel("SCORE: 0", HIT_COLOR, 36);
        livesLabel = createNeonLabel("LIVES: 3", PLANT_COLOR, 36);
        statusLabel = createNeonLabel("READY?", MOLE_COLOR, 28);
        
        hudPanel.add(scoreLabel);
        hudPanel.add(livesLabel);
        hudPanel.add(statusLabel);
        
        // --- GAME BOARD ---
        JPanel boardPanel = new JPanel(new GridLayout(3, 3, GAP, GAP));
        boardPanel.setOpaque(false);
        boardPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        
        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton();
            tile.setFont(new Font("Arial", Font.BOLD, 24));
            tile.setBackground(TILE_BASE);
            tile.setOpaque(true);
            tile.setBorderPainted(false);
            tile.setFocusPainted(false);
            tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Add glow border effect
            tile.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(TILE_GLOW, 2),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));
            
            final int index = i;
            tile.addActionListener(e -> whackTile(index));
            tiles[i] = tile;
            boardPanel.add(tile);
        }
        
        // --- CONTROLS ---
        JPanel controlPanel = new JPanel();
        controlPanel.setOpaque(false);
        startButton = new JButton("INITIATE NEON PROTOCOL");
        startButton.setFont(new Font("Arial", Font.BOLD, 18));
        startButton.setForeground(Color.WHITE);
        startButton.setBackground(new Color(0, 255, 230)); // Cyan
        startButton.setFocusPainted(false);
        startButton.setBorder(BorderFactory.createLineBorder(Color.WHITE, 3));
        startButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        startButton.addActionListener(e -> startGame());
        controlPanel.add(startButton);
        controlPanel.setBorder(new EmptyBorder(20, 0, 20, 0));

        // --- LAYOUT ---
        add(hudPanel, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // --- PARTICLE SYSTEM SETUP ---
        particleTimer = new Timer(30, e -> {
            updateParticles();
            repaint();
        });
        particleTimer.start();
    }

    private JLabel createNeonLabel(String text, Color color, int size) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, size));
        label.setForeground(color);
        label.setOpaque(false);
        // Simulate neon glow with a text shadow (approximate via painting)
        return label;
    }

    private void startGame() {
        if (gameRunning) return;
        
        score = 0;
        lives = 3;
        gameSpeed = 800;
        updateHUD();
        statusLabel.setText("LET'S WHACK!");
        startButton.setEnabled(false);
        gameRunning = true;
        
        // Reset tiles
        for (JButton tile : tiles) {
            tile.setIcon(null);
            tile.setText("");
            tile.setBackground(TILE_BASE);
        }
        
        moleTimer = new Timer(gameSpeed, e -> showMole());
        plantTimer = new Timer(1200, e -> showPlant()); // Plants appear slower
        
        moleTimer.start();
        plantTimer.start();
    }

    private void showMole() {
        if (!gameRunning) return;
        
        // Clear previous mole
        if (currentMole != null) {
            currentMole.setIcon(null);
            currentMole.setText("");
            currentMole.setBackground(TILE_BASE);
        }
        
        // Pick random tile
        int index = new Random().nextInt(9);
        currentMole = tiles[index];
        
        // Ensure we don't pick the same one twice in a row if possible
        while (currentMole == tiles[index] && currentMole != null && tiles.length > 1) {
            index = new Random().nextInt(9);
            currentMole = tiles[index];
        }
        
        // Visuals
        currentMole.setIcon(createIcon(MOLE_COLOR, "🍄")); // Mushroom/Mole
        currentMole.setBackground(MOLE_COLOR);
        currentMole.setForeground(Color.BLACK);
        
        // Speed up slightly as score increases
        int newSpeed = Math.max(300, 800 - (score * 10));
        if (moleTimer.getDelay() != newSpeed) {
            moleTimer.setDelay(newSpeed);
        }
    }

    private void showPlant() {
        if (!gameRunning) return;
        
        // Clear previous plant
        if (currentPlant != null) {
            currentPlant.setIcon(null);
            currentPlant.setText("");
            currentPlant.setBackground(TILE_BASE);
        }
        
        // Pick random tile (different from mole if possible)
        int index = new Random().nextInt(9);
        currentPlant = tiles[index];
        
        // Visuals
        currentPlant.setIcon(createIcon(PLANT_COLOR, "💣")); // Bomb
        currentPlant.setBackground(PLANT_COLOR);
        currentPlant.setForeground(Color.WHITE);
        
        // Plant disappears faster
        Timer plantHideTimer = new Timer(600, e -> {
            if (currentPlant != null) {
                currentPlant.setIcon(null);
                currentPlant.setText("");
                currentPlant.setBackground(TILE_BASE);
                currentPlant = null;
            }
        });
        plantHideTimer.setRepeats(false);
        plantHideTimer.start();
    }

    private void whackTile(int index) {
        if (!gameRunning) return;
        
        JButton tile = tiles[index];
        Icon currentIcon = tile.getIcon();
        
        if (currentIcon != null) {
            // Check if it's a mole
            if (currentIcon == createIcon(MOLE_COLOR, "🍄")) {
                score += 10;
                livesLabel.setForeground(Color.WHITE); // Flash white
                spawnParticles(tile.getX() + tile.getWidth()/2, tile.getY() + tile.getHeight()/2, HIT_COLOR);
                tile.setBackground(HIT_COLOR);
                tile.setIcon(null);
                currentMole = null;
                
                // Reset border glow
                tile.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Color.WHITE, 4),
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)
                ));
                
                // Tiny delay for visual feedback
                Timer resetBorder = new Timer(100, e -> {
                    tile.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(TILE_GLOW, 2),
                        BorderFactory.createEmptyBorder(10, 10, 10, 10)
                    ));
                });
                resetBorder.setRepeats(false);
                resetBorder.start();
                
            } else if (currentIcon == createIcon(PLANT_COLOR, "💣")) {
                // Hit a plant/bomb
                lives--;
                spawnParticles(tile.getX() + tile.getWidth()/2, tile.getY() + tile.getHeight()/2, PLANT_COLOR);
                tile.setBackground(PLANT_COLOR.darker());
                currentPlant = null;
                
                if (lives <= 0) {
                    gameOver();
                }
            }
            updateHUD();
        }
    }

    private void gameOver() {
        gameRunning = false;
        moleTimer.stop();
        if (plantTimer != null) plantTimer.stop();
        
        statusLabel.setText("GAME OVER!");
        statusLabel.setForeground(PLANT_COLOR);
        startButton.setEnabled(true);
        startButton.setText("RETRY SYSTEM");
        
        JOptionPane.showMessageDialog(this, 
            "SYSTEM FAILURE. \nFINAL SCORE: " + score, 
            "GAME OVER", 
            JOptionPane.ERROR_MESSAGE);
    }

    private void updateHUD() {
        scoreLabel.setText("SCORE: " + score);
        livesLabel.setText("LIVES: " + lives);
        
        // Pulse effect for score
        scoreLabel.setForeground(HIT_COLOR);
        Timer pulse = new Timer(100, e -> scoreLabel.setForeground(Color.WHITE));
        pulse.setRepeats(false);
        pulse.start();
    }

    // --- PARTICLE SYSTEM ---
    private void spawnParticles(int x, int y, Color color) {
        for (int i = 0; i < 20; i++) {
            particles.add(new Particle(x, y, color));
        }
    }

    private void updateParticles() {
        for (int i = particles.size() - 1; i >= 0; i--) {
            Particle p = particles.get(i);
            p.update();
            if (p.life <= 0) {
                particles.remove(i);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw particles
        for (Particle p : particles) {
            g2d.setColor(p.color);
            g2d.fillOval((int)p.x, (int)p.y, p.size, p.size);
        }
    }

    private ImageIcon createIcon(Color color, String emoji) {
        // Create a simple icon with the emoji centered
        BufferedImage img = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(color);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw a glowing circle background
        g2.setColor(color.darker().darker());
        g2.fillOval(0, 0, 64, 64);
        g2.setColor(color);
        g2.fillOval(4, 4, 56, 56);
        
        // Draw Emoji
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        FontMetrics fm = g2.getFontMetrics();
        int x = (64 - fm.stringWidth(emoji)) / 2;
        int y = (64 + fm.getAscent()) / 2 - 2;
        g2.drawString(emoji, x, y);
        
        g2.dispose();
        return new ImageIcon(img);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("★ MEGA ARCADE: WHAC-A-MOLE ★");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.add(new WhacAMoleNeon());
            frame.pack();
            frame.setVisible(true);
        });
    }
    
    // --- INNER CLASSES ---
    static class Particle {
        double x, y;
        double vx, vy;
        int size;
        Color color;
        int life = 50; // frames
        
        Particle(int x, int y, Color color) {
            this.x = x;
            this.y = y;
            this.color = color;
            this.size = new Random().nextInt(6) + 4;
            double angle = Math.random() * Math.PI * 2;
            double speed = Math.random() * 4 + 1;
            this.vx = Math.cos(angle) * speed;
            this.vy = Math.sin(angle) * speed;
        }
        
        void update() {
            x += vx;
            y += vy;
            life--;
            size *= 0.95; // shrink
        }
    }
}