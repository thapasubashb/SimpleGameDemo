
import javax.swing.*;

public class JPanal {
    public static void main(String[] args) {
        String[] options = { "Box Box Game", "Snake Game", "Mario Style", "Dodge Game" };
        int choice = JOptionPane.showOptionDialog(
                null,
                "Select a game to play:",
                "Simple Game Demo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        switch (choice) {
            case 1:
                SnakeGame.main(args);
                break;
            case 2:
                whacAMole.main(args);
                break;
            case 3:
                DodgeGame.main(args);
                break;
            case 0:
            default:
                BoxBoxGame.main(args);
                break;
        }
    }
}
