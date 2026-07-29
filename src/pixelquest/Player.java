package pixelquest;

import java.awt.Rectangle;

public class Player {
    public int x;
    public int y;
    public final int width = 34;
    public final int height = 46;
    public int vy;
    public boolean onGround;

    public Player(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Rectangle getRect() {
        return new Rectangle(x, y, width, height);
    }

    public boolean intersects(Coin coin) {
        return getRect().intersects(coin.getRect());
    }
}
