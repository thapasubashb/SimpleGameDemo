package pixelquest;

import java.awt.Rectangle;

public class Enemy {
    public int x;
    public int y;
    public final int width = 28;
    public final int height = 28;
    public int direction;

    public Enemy(int x, int y, int direction) {
        this.x = x;
        this.y = y;
        this.direction = direction;
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }
}
