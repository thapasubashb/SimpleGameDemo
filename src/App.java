import javax.swing.SwingUtilities;

public class App {
    public static void main(String[] args) {
        // Runs the game on the safe Java UI thread
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new whacAMole();
            }
        });
    }
}