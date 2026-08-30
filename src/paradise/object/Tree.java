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

    public Tree(int startX, int startY, int cropX, int cropY) {
        this.worldX = startX;
        this.worldY = startY;

        try {
            BufferedImage spriteSheet = ImageIO.read(new File("src/paradise/object/trees.png"));
            // The scissors are 32x32 (perfect for one tree)
            image = spriteSheet.getSubimage(cropX, cropY, 32, 32);

        } catch (IOException e) {
            System.out.println("Could not load tree image!");
        }

        // We leave the physical hitbox small so your player can still walk behind the giant canopy!
        this.hitbox = new Rectangle(worldX + 8, worldY + 16, 16, 16);
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (image != null) {
            int screenX = worldX - gp.playerX + gp.playerScreenX;
            int screenY = worldY - gp.playerY + gp.playerScreenY;

            // Shift left by half a tile and up by a full tile to keep the trunk planted exactly where it was
            int drawX = screenX - (gp.tileSize / 2);
            int drawY = screenY - gp.tileSize;

            // Draw at double size (gp.tileSize * 2)
            g2.drawImage(image, drawX, drawY, gp.tileSize * 2, gp.tileSize * 2, null);
        }
    }
}