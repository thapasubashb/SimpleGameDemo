package pixelquest;

import java.awt.Rectangle;

public class Coin {
    public final int x;
    public final int y;
    public final int size = 20;

    public Coin(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, size, size);
    }
}
