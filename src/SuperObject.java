import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class SuperObject {

    public String name;
    public boolean collision = false;
    public int worldX, worldY;
    public Rectangle solidArea = new Rectangle(0, 0, 48, 48);
    public Color color;

    public void draw(Graphics2D g2, GamePanel gp) {
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        if (worldX + gp.tileSize > gp.playerX - gp.playerScreenX &&
                worldX - gp.tileSize < gp.playerX + gp.playerScreenX &&
                worldY + gp.tileSize > gp.playerY - gp.playerScreenY &&
                worldY - gp.tileSize < gp.playerY + gp.playerScreenY) {

            g2.setColor(color);
            g2.fillRect(screenX + 12, screenY + 12, gp.tileSize / 2, gp.tileSize / 2);
        }
    }
}