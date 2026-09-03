package paradise.object;

import paradise.core.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Ruins {

    public int worldX, worldY;
    public BufferedImage image;
    public int drawWidth, drawHeight;

    // Scale the pixel-art ruins up so they read as ruins on the 48px tile map
    private static final int SCALE = 3;

    public Ruins(int startX, int startY, String fileName) {
        this.worldX = startX;
        this.worldY = startY;

        try {
            image = ImageIO.read(new File("src/paradise/object/ruins/" + fileName));
            drawWidth = image.getWidth() * SCALE;
            drawHeight = image.getHeight() * SCALE;
        } catch (IOException e) {
            System.out.println("Could not load ruin image: " + fileName);
        }
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (image == null) return;

        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);
    }
}
