package paradise.object;

import paradise.core.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Tree {

    public int worldX, worldY;
    public BufferedImage image;
    public Rectangle hitbox;

    // NEW: We now tell the tree exactly where to cut the sprite sheet!
    public Tree(int startX, int startY, int cropX, int cropY) {
        this.worldX = startX;
        this.worldY = startY;

        try {
            BufferedImage spriteSheet = ImageIO.read(new File("src/paradise/object/trees.png"));

            // The scissors are now 32x32 (perfect for one tree)
            // cropX and cropY will shift the scissors to different colors
            image = spriteSheet.getSubimage(cropX, cropY, 32, 32);

        } catch (IOException e) {
            System.out.println("Could not load tree image!");
        }

        // Hitbox adjusted for a smaller tree
        this.hitbox = new Rectangle(worldX + 8, worldY + 16, 16, 16);
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (image != null) {
            int screenX = worldX - gp.playerX + gp.playerScreenX;
            int screenY = worldY - gp.playerY + gp.playerScreenY;

            // We draw it at gp.tileSize (48x48) so it matches your map grid perfectly!
            g2.drawImage(image, screenX, screenY, gp.tileSize, gp.tileSize, null);
        }
    }
}