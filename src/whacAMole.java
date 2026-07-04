import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.util.Random;
import javax.swing.*;

public class whacAMole {
    int boardWidth = 640;
    int boardHeight = 760;

    JFrame frame = new JFrame("Mario: Whac A Mole");
    JLabel textLabel = new JLabel();
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

    // --- PREMIUM PALETTE ---
    Color bgDark = new Color(18, 18, 32); // Deep space canvas
    Color surfaceDark = new Color(28, 28, 45); // Card surfaces
    Color neonCyan = new Color(0, 242, 254); // Vibrant text / accent
    Color neonPink = new Color(253, 56, 114); // Game Over tint
    Color tileBorder = new Color(50, 50, 75); // Clean grid outlines

    public whacAMole() {
        frame.setSize(boardWidth, boardHeight);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setBackground(bgDark);
        frame.setLayout(new BorderLayout(0, 10));

        // --- UI UPGRADE: Rounded, Gradient Top Banner ---
        textPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Premium Subtle Top-To-Bottom Gradient
                GradientPaint gp = new GradientPaint(0, 0, new Color(36, 36, 62), 0, getHeight(),
                        new Color(24, 24, 40));
                g2d.setPaint(gp);
                g2d.fillRoundRect(15, 15, getWidth() - 30, getHeight() - 15, 20, 20);
                g2d.dispose();
            }
        };

        textPanel.setOpaque(false);
        textPanel.setPreferredSize(new Dimension(boardWidth, 110));
        textPanel.setLayout(new GridBagLayout()); // Perfect absolute centering

        textLabel.setFont(new Font("SansSerif", Font.BOLD, 32));
        textLabel.setText("SCORE : " + String.format("%04d", score));
        textLabel.setForeground(neonCyan);
        textPanel.add(textLabel);
        frame.add(textPanel, BorderLayout.NORTH);

        // --- UI UPGRADE: Main Game Board Window Container ---
        boardPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setColor(surfaceDark);
                g2d.fillRoundRect(15, 0, getWidth() - 30, getHeight() - 20, 24, 24);
                g2d.dispose();
            }
        };
        boardPanel.setOpaque(false);
        boardPanel.setLayout(new GridLayout(3, 3, 15, 15)); // Generous modern spacing
        boardPanel.setBorder(BorderFactory.createEmptyBorder(20, 35, 40, 35));
        frame.add(boardPanel, BorderLayout.CENTER);

        // Smooth image downscaling
        Image plantImg = new ImageIcon(getClass().getResource("/piranha.png")).getImage();
        plantIcon = new ImageIcon(plantImg.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH));

        Image moleImg = new ImageIcon(getClass().getResource("/monty.png")).getImage();
        moleIcon = new ImageIcon(moleImg.getScaledInstance(120, 120, java.awt.Image.SCALE_SMOOTH));

        // --- UI UPGRADE: Beautiful Custom Grid Buttons ---
        for (int i = 0; i < 9; i++) {
            final int index = i;
            JButton tile = new JButton() {
                private float hoverAlpha = 0.0f;
                private Timer hoverTimer;

                {
                    // Smooth fading hover animations using basic timed hooks
                    addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseEntered(MouseEvent e) {
                            if (!isEnabled())
                                return;
                            if (hoverTimer != null)
                                hoverTimer.stop();
                            hoverTimer = new Timer(15, ev -> {
                                hoverAlpha = Math.min(1.0f, hoverAlpha + 0.15f);
                                repaint();
                                if (hoverAlpha >= 1.0f)
                                    hoverTimer.stop();
                            });
                            hoverTimer.start();
                        }

                        @Override
                        public void mouseExited(MouseEvent e) {
                            if (!isEnabled())
                                return;
                            if (hoverTimer != null)
                                hoverTimer.stop();
                            hoverTimer = new Timer(15, ev -> {
                                hoverAlpha = Math.max(0.0f, hoverAlpha - 0.15f);
                                repaint();
                                if (hoverAlpha <= 0.0f)
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

                    // Background base style
                    if (!isEnabled()) {
                        g2d.setColor(new Color(22, 22, 32));
                    } else if (getBackground() == Color.GREEN) {
                        g2d.setColor(new Color(46, 213, 115)); // Slick Emerald Green hit flash
                    } else {
                        g2d.setColor(new Color(40, 40, 60)); // Standard tile depth
                    }
                    g2d.fillRoundRect(0, 0, w, h, 18, 18);

                    // Hover overlay glow
                    if (isEnabled() && hoverAlpha > 0) {
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, hoverAlpha * 0.15f));
                        g2d.setColor(neonCyan);
                        g2d.fillRoundRect(0, 0, w, h, 18, 18);
                    }

                    // Re-enable alpha for normal painting layers
                    g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));

                    // Border Drawing
                    g2d.setStroke(new BasicStroke(2f));
                    g2d.setColor(!isEnabled() ? new Color(35, 35, 45) : tileBorder);
                    g2d.drawRoundRect(1, 1, w - 2, h - 2, 18, 18);

                    g2d.dispose();
                    super.paintComponent(g); // Draws icons over our custom shape safely
                }
            };

            board[i] = tile;
            boardPanel.add(tile);

            tile.setFocusable(false);
            tile.setContentAreaFilled(false); // Disables ugly standard grey system painting
            tile.setBorderPainted(false);
            tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

            tile.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
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
                    } else if (tile == currPlantTile) {
                        textLabel.setText("GAME OVER | SCORE: " + String.format("%04d", score));
                        textLabel.setForeground(neonPink);
                        setMoleTimer.stop();
                        setPlantTimer.stop();
                        for (int i = 0; i < 9; i++) {
                            board[i].setEnabled(false);
                        }
                        boardPanel.repaint();
                    }
                }
            });
        }

        // Timers
        setMoleTimer = new Timer(1000, e -> {
            if (currMoleTile != null) {
                currMoleTile.setIcon(null);
                currMoleTile = null;
            }
            int num = random.nextInt(9);
            JButton tile = board[num];
            if (currPlantTile == tile)
                return;
            currMoleTile = tile;
            currMoleTile.setIcon(moleIcon);
        });

        setPlantTimer = new Timer(1500, e -> {
            if (currPlantTile != null) {
                currPlantTile.setIcon(null);
                currPlantTile = null;
            }
            int num = random.nextInt(9);
            JButton tile = board[num];
            if (currMoleTile == tile)
                return;
            currPlantTile = tile;
            currPlantTile.setIcon(plantIcon);
        });

        setMoleTimer.start();
        setPlantTimer.start();
        frame.setVisible(true);
    }
}