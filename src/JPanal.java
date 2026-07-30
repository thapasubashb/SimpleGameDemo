
import javax.swing.*;

public class JPanal {
    public static void main(String[] args) {
        String[] options = { "Box Box Game", "Snake Game", "Whac-a-Mole", "Dodge Game", "Pixel Quest" };
        String message = "<html><center><b>Welcome to Simple Game Demo</b><br>Choose a game to start playing.</center></html>";

        int choice = JOptionPane.showOptionDialog(
                null,
                message,
                "Simple Game Demo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
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
            case 4:
                pixelquest.PixelQuestGame.main(args);
                break;
            case 0:
            default:
                BoxBoxGame.main(args);
                break;
        }
    }
}
