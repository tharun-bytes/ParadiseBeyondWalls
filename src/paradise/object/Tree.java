package paradise.object;

import paradise.core.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
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
            int drawSize = gp.tileSize * 2;

            // Gentle wind sway: rotate the whole canopy a couple of degrees around the
            // trunk's foot so the tree looks planted while it rocks in the breeze. A slow
            // "gust" envelope (unique per tree via worldX) is layered on a faster sway so
            // trees don't all sway in perfect unison.
            double gust = 0.5 + 0.5 * Math.sin(gp.animationFrame * 0.012 + worldX * 0.004);
            double swayAngle = Math.toRadians(Math.sin(gp.animationFrame * 0.045 + worldY * 0.02) * (1.2 + gust * 1.8));

            int anchorX = drawX + drawSize / 2;
            int anchorY = drawY + drawSize;

            AffineTransform originalTransform = g2.getTransform();
            g2.rotate(swayAngle, anchorX, anchorY);
            // Draw at double size (gp.tileSize * 2)
            g2.drawImage(image, drawX, drawY, drawSize, drawSize, null);
            g2.setTransform(originalTransform);
        }
    }
}