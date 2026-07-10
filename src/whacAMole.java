import java.awt.*;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;

public class whacAMole {
    int boardWidth = 640;
    int boardHeight = 820; // Expanded slightly for better layout balance

    JFrame frame = new JFrame("★ MEGA ARCADE: WHAC-A-MOLE ★");
    JLabel textLabel = new JLabel();
    JButton restartButton = new JButton("PRESS TO REPLAY");
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

    // --- HIGH-ENERGY NEON PALETTE ---
    Color bgLaserDark = new Color(15, 6, 27); // Intense deep synth-indigo canvas
    Color surfaceNeon = new Color(30, 15, 52); // Highly saturated card backgrounds
    Color tileBase = new Color(48, 25, 82); // Bright un-popped tile base
    Color neonLaserCyan = new Color(0, 255, 230); // Piercing electric cyan
    Color neonHotPink = new Color(255, 0, 127); // Hyper neon pink for Game Over
    Color neonLimeGreen = new Color(57, 255, 20); // Radioactive lime green for scoring
    Color borderGlow = new Color(139, 0, 255); // Radical violet border accent

    public whacAMole() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bgLaserDark);
        frame.setLayout(new BorderLayout(0, 20));

        // --- UI UPGRADE: ELECTRIC NEON HUD BANNER ---
        textPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Vibrant multi-stage background gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(45, 12, 78), 0, getHeight(), new Color(22, 7, 43));
                g2d.setPaint(gp);
                g2d.fillRoundRect(20, 15, getWidth() - 40, getHeight() - 15, 20, 20);

                // Double-layered laser border lines
                g2d.setStroke(new BasicStroke(3f));
                g2d.setColor(textLabel.getForeground() == neonHotPink ? neonHotPink : neonLaserCyan);
                g2d.drawRoundRect(20, 15, getWidth() - 40, getHeight() - 15, 20, 20);
                g2d.dispose();
            }
        };

        textPanel.setOpaque(false);
        textPanel.setPreferredSize(new Dimension(boardWidth, 140));
        textPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(12, 0, 8, 0);

        // High-energy Impact arcade scaling font
        textLabel.setFont(new Font("Impact", Font.ITALIC, 42));
        textLabel.setText("SCORE : " + String.format("%04d", score));
        textLabel.setForeground(neonLaserCyan);
        textPanel.add(textLabel, gbc);

        // --- UI UPGRADE: STYLIZED ARCADE COIN RESET BUTTON ---
        gbc.gridy = 1;
        gbc.insets = new Insets(2, 0, 0, 0);
        restartButton.setFont(new Font("Impact", Font.PLAIN, 18));
        restartButton.setForeground(Color.WHITE);
        restartButton.setBackground(neonHotPink);
        restartButton.setFocusable(false);
        restartButton.setBorder(BorderFactory.createEmptyBorder(6, 25, 6, 25));
        restartButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        restartButton.setVisible(false);

        restartButton.addActionListener(e -> resetGame());
        textPanel.add(restartButton, gbc);

        frame.add(textPanel, BorderLayout.NORTH);

        // --- UI UPGRADE: MAIN ENERGY ARENA CONTAINER ---
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Deep retro cabinet frame coloring
                g2d.setColor(surfaceNeon);
                g2d.fillRoundRect(20, 0, getWidth() - 40, getHeight() - 30, 24, 24);

                // Ambient frame outline
                g2d.setStroke(new BasicStroke(2.5f));
                g2d.setColor(borderGlow);
                g2d.drawRoundRect(20, 0, getWidth() - 40, getHeight() - 30, 24, 24);
                g2d.dispose();
            }
        };
        boardPanel.setOpaque(false);
        boardPanel.setLayout(new GridLayout(3, 3, 20, 20)); // Roomy layout gaps
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 45, 40));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Asset Setup
        try {
            Image plantImg = new ImageIcon(getClass().getResource("/piranha.png")).getImage();
            plantIcon = new ImageIcon(plantImg.getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH));
            Image moleImg = new ImageIcon(getClass().getResource("/monty.png")).getImage();
            moleIcon = new ImageIcon(moleImg.getScaledInstance(110, 110, java.awt.Image.SCALE_SMOOTH));
        } catch (Exception e) {
            System.out.println("Resource images missing. Defaulting to safe fallback behavior.");
        }

        initializeGrid();

        // High intensity tempo loops
        setMoleTimer = new Timer(850, e -> spawnEntity(true));
        setPlantTimer = new Timer(1300, e -> spawnEntity(false));

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
                            hoverTimer = new Timer(12, ev -> {
                                hoverAlpha = fadeIn ? Math.min(1.0f, hoverAlpha + 0.25f)
                                        : Math.max(0.0f, hoverAlpha - 0.25f);
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

                    // Component Palette State Routing
                    if (!isEnabled()) {
                        g2d.setColor(new Color(22, 11, 38));
                    } else if (getBackground() == Color.GREEN) {
                        g2d.setColor(neonLimeGreen);
                    } else {
                        g2d.setColor(tileBase);
                    }
                    g2d.fillRoundRect(0, 0, w, h, 20, 20);

                    // High intensity reactive lighting glow
                    if (isEnabled() && hoverAlpha > 0) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverAlpha * 0.25f));
                        g2d.setColor(neonLaserCyan);
                        g2d.fillRoundRect(0, 0, w, h, 20, 20);
                    }

                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Electric Accent Outlines
                    g2d.setStroke(new BasicStroke(3f));
                    if (!isEnabled()) {
                        g2d.setColor(new Color(40, 20, 65));
                    } else if (getBackground() == Color.GREEN) {
                        g2d.setColor(Color.WHITE);
                    } else {
                        g2d.setColor(borderGlow);
                    }
                    g2d.drawRoundRect(1, 1, w - 2, h - 2, 20, 20);

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
            Timer resetColor = new Timer(100, ev -> {
                tile.setBackground(null);
                tile.repaint();
            });
            resetColor.setRepeats(false);
            resetColor.start();

            currMoleTile.setIcon(null);
            currMoleTile = null;

        } else if (tile == currPlantTile) {
            textLabel.setText("GAME OVER | " + String.format("%04d", score));
            textLabel.setForeground(neonHotPink);
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
        textLabel.setForeground(neonLaserCyan);
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