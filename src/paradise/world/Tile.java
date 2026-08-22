package paradise.world;

import java.awt.Color;

public class Tile {
    public final Color color;
    public final boolean collision;

    public Tile(Color color, boolean collision) {
        this.color = color;
        this.collision = collision;
    }
}
