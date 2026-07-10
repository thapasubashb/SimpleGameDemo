import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class whacAMole {
    int boardWidth = 640;
    int boardHeight = 800; // Increased slightly for comfort and button spacing

    JFrame frame = new JFrame("Mario: Whac A Mole");
    JLabel textLabel = new JLabel();
    JButton restartButton = new JButton("PLAY AGAIN");
    JPanel textPanel;
    JPanel boardPanel;

    JButton[] board = new JButton[9];
    ImageIcon moleIcon;
    ImageIcon plantIcon;

    JButton currMoleTile;
    JButton currPlantTile;

    Random random = new Random();
    Timer setMoleTimer;
    Timer setPlantTimer;
    int score = 0;

    // --- PREMIUM CYBERPUNK/ARCADE PALETTE ---
    Color bgDark = new Color(13, 13, 23); // Ultra deep canvas
    Color surfaceDark = new Color(22, 22, 38); // Card background
    Color tileBase = new Color(34, 34, 54); // Unoccupied tile base
    Color neonCyan = new Color(0, 242, 254); // High-frequency branding color
    Color neonPink = new Color(253, 56, 114); // Game Over tint
    Color neonGreen = new Color(46, 213, 115); // Success flash color
    Color tileBorder = new Color(48, 48, 74); // Deep clean grid lines

    public whacAMole() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bgDark);
        frame.setLayout(new BorderLayout(0, 15));

        // --- UI UPGRADE: Rounded, Sleek Neon HUD Banner ---
        textPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep background glassmorphism look
                GradientPaint gp = new GradientPaint(0, 0, new Color(26, 26, 46), 0, getHeight(),
                        new Color(18, 18, 32));
                g2d.setPaint(gp);
                g2d.fillRoundRect(20, 15, getWidth() - 40, getHeight() - 15, 24, 24);

                // Fine cyber outline
                g2d.setStroke(new BasicStroke(1.5f));
                g2d.setColor(new Color(60, 60, 90, 180));
                g2d.drawRoundRect(20, 15, getWidth() - 40, getHeight() - 15, 24, 24);
                g2d.dispose();
            }
        };

        textPanel.setOpaque(false);
        textPanel.setPreferredSize(new Dimension(boardWidth, 130));
        textPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(10, 0, 5, 0);

        // Bold Retro-Arcade font stylings
        textLabel.setFont(new Font("Impact", Font.PLAIN, 36));
        textLabel.setText("SCORE : " + String.format("%04d", score));
        textLabel.setForeground(neonCyan);
        textPanel.add(textLabel, gbc);

        // --- UI UPGRADE: Integrated Restart Mechanism ---
        gbc.gridy = 1;
        gbc.insets = new Insets(5, 0, 0, 0);
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(new Color(40, 40, 65));
        restartButton.setFocusable(false);
        restartButton.setBorder(BorderFactory.createEmptyBorder(6, 20, 6, 20));
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restartButton.setVisible(false); // Only visible on game over

        restartButton.addActionListener(e -> resetGame());
        textPanel.add(restartButton, gbc);

        frame.add(textPanel, BorderLayout.NORTH);

        // --- UI UPGRADE: Main Game Board Window Container ---
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(surfaceDark);
                g2d.fillRoundRect(20, 0, getWidth() - 40, getHeight() - 25, 28, 28);
                g2d.dispose();
            }
        };
        boardPanel.setOpaque(false);
        boardPanel.setLayout(new GridLayout(3, 3, 18, 18)); // Even cleaner gaps
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 45, 40));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Resource Image Scaling Upgrades
        try {
            Image plantImg = new ImageIcon(getClass().getResource("/piranha.png")).getImage();
            plantIcon = new ImageIcon(plantImg.getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH));
            Image moleImg = new ImageIcon(getClass().getResource("/monty.png")).getImage();
            moleIcon = new ImageIcon(moleImg.getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.out.println("Resource images missing. Defaulting to safe fallback behavior.");
        }

        // Initialize Grid Build
        initializeGrid();

        // Game Speed Loop Controls
        setMoleTimer = new Timer(900, e -> spawnEntity(true));
        setPlantTimer = new Timer(1400, e -> spawnEntity(false));

        setMoleTimer.start();
        setPlantTimer.start();
        frame.setVisible(true);
    }

    private void initializeGrid() {
        for (int i = 0; i < 9; i++) {
            JButton tile = new JButton() {
                private float hoverAlpha = 0.0f;
                private Timer hoverTimer;

                {
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (!isEnabled())
                                return;
                            fadeHover(true);
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (!isEnabled())
                                return;
                            fadeHover(false);
                        }

                        private void fadeHover(boolean fadeIn) {
                            if (hoverTimer != null)
                                hoverTimer.stop();
                            hoverTimer = new Timer(10, ev -> {
                                hoverAlpha = fadeIn ? Math.min(1.0f, hoverAlpha + 0.2f)
                                        : Math.max(0.0f, hoverAlpha - 0.2f);
                                repaint();
                                if (hoverAlpha <= 0.0f || hoverAlpha >= 1.0f)
                                    hoverTimer.stop();
                            });
                            hoverTimer.start();
                        }
                    });
                }

                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int w = getWidth();
                    int h = getHeight();

                    // Tile State Engine Colors
                    if (!isEnabled()) {
                        g2d.setColor(new Color(16, 16, 26));
                    } else if (getBackground() == Color.GREEN) {
                        g2d.setColor(neonGreen);
                    } else {
                        g2d.setColor(tileBase);
                    }
                    g2d.fillRoundRect(0, 0, w, h, 22, 22);

                    // Modern Neon Glow Layer Overlays
                    if (isEnabled() && hoverAlpha > 0) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverAlpha * 0.12f));
                        g2d.setColor(neonCyan);
                        g2d.fillRoundRect(0, 0, w, h, 22, 22);
                    }

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Precision 3D Matte Bevel Edges
                    g2d.setStroke(new BasicStroke(2.5f));
                    if (!isEnabled()) {
                        g2d.setColor(new Color(28, 28, 40));
                    } else if (getBackground() == Color.GREEN) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(tileBorder);
                    }
                    g2d.drawRoundRect(1, 1, w - 2, h - 2, 22, 22);

                    g2d.dispose();
                    super.paintComponent(g);
                }
            };

            board[i] = tile;
            boardPanel.add(tile);

            tile.setFocusable(false);
            tile.setContentAreaFilled(false);
            tile.setBorderPainted(false);
            tile.setCursor(new Cursor(Cursor.HAND_CURSOR));
            tile.addActionListener(this::handleTileAction);
        }
    }

    private void handleTileAction(ActionEvent e) {
        JButton tile = (JButton) e.getSource();
        if (tile == currMoleTile) {
            score += 10;
            textLabel.setText("SCORE : " + String.format("%04d", score));

            tile.setBackground(Color.GREEN);
            Timer resetColor = new Timer(120, ev -> {
                tile.setBackground(null);
                tile.repaint();
            });
            resetColor.setRepeats(false);
            resetColor.start();

            // Clear mole instantly on hit for snappy game feel
            currMoleTile.setIcon(null);
            currMoleTile = null;

        } else if (tile == currPlantTile) {
            // Execution of cleanly formatted Game Over State
            textLabel.setText("GAME OVER | SCORE: " + String.format("%04d", score));
            textLabel.setForeground(neonPink);
            setMoleTimer.stop();
            setPlantTimer.stop();

            for (JButton jButton : board) {
                jButton.setEnabled(false);
            }
            restartButton.setVisible(true);
            textPanel.revalidate();
            textPanel.repaint();
            boardPanel.repaint();
        }
    }

    private void spawnEntity(boolean isMole) {
        if (isMole) {
            if (currMoleTile != null)
                currMoleTile.setIcon(null);
            int num = random.nextInt(9);
            if (board[num] == currPlantTile)
                return;
            currMoleTile = board[num];
            if (moleIcon != null)
                currMoleTile.setIcon(moleIcon);
        } else {
            if (currPlantTile != null)
                currPlantTile.setIcon(null);
            int num = random.nextInt(9);
            if (board[num] == currMoleTile)
                return;
            currPlantTile = board[num];
            if (plantIcon != null)
                currPlantTile.setIcon(plantIcon);
        }
    }

    private void resetGame() {
        score = 0;
        textLabel.setText("SCORE : " + String.format("%04d", score));
        textLabel.setForeground(neonCyan);
        restartButton.setVisible(false);

        if (currMoleTile != null)
            currMoleTile.setIcon(null);
        if (currPlantTile != null)
            currPlantTile.setIcon(null);
        currMoleTile = null;
        currPlantTile = null;

        for (JButton tile : board) {
            tile.setEnabled(true);
            tile.setBackground(null);
        }

        textPanel.revalidate();
        textPanel.repaint();
        boardPanel.repaint();

        setMoleTimer.start();
        setPlantTimer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(whacAMole::new);
    }
}