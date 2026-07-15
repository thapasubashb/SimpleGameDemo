import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class whacAMole {
    int boardWidth = 3;
    int boardHeight = 3;
    JFrame frame;
    JLabel textLabel;
    JButton restartButton;
    JPanel textPanel;
    JPanel boardPanel;
    JButton[] board;
    ImageIcon moleIcon;
    ImageIcon plantIcon;
    JButton currMoleTile;
    JButton currPlantTile;
    Random random;
    Timer setMoleTimer;
    Timer setPlantTimer;
    int score;
    Color bgLaserDark = new Color(10, 14, 28);
    Color surfaceNeon = new Color(24, 42, 61);
    Color tileBase = new Color(41, 61, 83);
    Color neonLaserCyan = new Color(0, 255, 255);
    Color neonHotPink = new Color(255, 0, 128);
    Color neonLimeGreen = new Color(120, 255, 0);
    Color borderGlow = new Color(255, 255, 255);

    public whacAMole() {
        random = new Random();

        frame = new JFrame("Whac-A-Mole");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(460, 530);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(bgLaserDark);

        textPanel = new JPanel();
        textPanel.setBackground(bgLaserDark);
        textPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 0, 12));

        textLabel = new JLabel("Score: 0");
        textLabel.setForeground(neonLaserCyan);
        textLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        textPanel.add(textLabel);

        restartButton = new JButton("Restart");
        restartButton.setBackground(neonHotPink);
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> resetGame());
        textPanel.add(restartButton);

        boardPanel = new JPanel(new GridLayout(boardHeight, boardWidth, 10, 10));
        boardPanel.setBackground(bgLaserDark);
        boardPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        board = new JButton[boardWidth * boardHeight];
        for (int i = 0; i < board.length; i++) {
            JButton tile = new JButton();
            tile.setBackground(tileBase);
            tile.setForeground(Color.WHITE);
            tile.setBorder(BorderFactory.createLineBorder(borderGlow, 2));
            tile.setFocusPainted(false);
            tile.addActionListener(this::handleTileAction);
            board[i] = tile;
            boardPanel.add(tile);
        }

        loadIcons();

        frame.add(textPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        initializeGrid();
        resetGame();

        setMoleTimer = new Timer(900, e -> spawnEntity(true));
        setMoleTimer.setInitialDelay(700);
        setMoleTimer.start();

        setPlantTimer = new Timer(1100, e -> spawnEntity(false));
        setPlantTimer.setInitialDelay(900);
        setPlantTimer.start();
    }

    private void initializeGrid() {
        for (JButton tile : board) {
            tile.setText("");
            tile.setIcon(null);
            tile.setBackground(tileBase);
            tile.setOpaque(true);
            tile.setBorder(BorderFactory.createLineBorder(borderGlow, 2));
        }
        currMoleTile = null;
        currPlantTile = null;
    }

    private void handleTileAction(ActionEvent event) {
        JButton clickedTile = (JButton) event.getSource();

        if (clickedTile == currMoleTile) {
            score++;
            textLabel.setText("Score: " + score);
            clickedTile.setIcon(null);
            clickedTile.setText("");
            clickedTile.setBackground(tileBase);
            currMoleTile = null;
        } else if (clickedTile == currPlantTile) {
            score = Math.max(0, score - 1);
            textLabel.setText("Score: " + score);
            clickedTile.setIcon(null);
            clickedTile.setText("");
            clickedTile.setBackground(tileBase);
            currPlantTile = null;
        }
    }

    private void spawnEntity(boolean isMole) {
        initializeGrid();
        int index = random.nextInt(board.length);
        JButton tile = board[index];

        if (isMole) {
            tile.setIcon(moleIcon);
            tile.setBackground(neonLimeGreen);
            currMoleTile = tile;
        } else {
            tile.setIcon(plantIcon);
            tile.setBackground(neonHotPink);
            currPlantTile = tile;
        }
    }

    private void resetGame() {
        score = 0;
        textLabel.setText("Score: 0");
        initializeGrid();
    }

    private void loadIcons() {
        moleIcon = loadIcon("monty.png");
        plantIcon = loadIcon("piranha.png");
        if (moleIcon == null) {
            moleIcon = new ImageIcon();
        }
        if (plantIcon == null) {
            plantIcon = new ImageIcon();
        }
    }

    private ImageIcon loadIcon(String fileName) {
        File[] possibleLocations = {
                new File(fileName),
                new File("src", fileName),
                new File("." + File.separator + "src" + File.separator + fileName)
        };

        for (File file : possibleLocations) {
            if (file.exists()) {
                try {
                    return new ImageIcon(ImageIO.read(file));
                } catch (IOException ignored) {
                    // Fall back to a text-based tile if the image cannot be loaded.
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(whacAMole::new);
    }
}
