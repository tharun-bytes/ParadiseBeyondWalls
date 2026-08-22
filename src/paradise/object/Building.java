package paradise.object;

import paradise.core.GamePanel;
import javax.imageio.ImageIO;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Building {

    public int worldX, worldY;
    public BufferedImage image;
    public Rectangle hitbox;
    public int drawWidth, drawHeight;

    // We make these class variables so the draw method can use them for the red box
    public int sidePadding;
    public int topPadding;
    public int boxWidth;
    public int boxHeight;

    public Building(int startX, int startY, int buildingNumber) {
        this.worldX = startX;
        this.worldY = startY;

        try {
            String filePath = "src/paradise/object/building" + buildingNumber + ".png";
            image = ImageIO.read(new File(filePath));

            int scale = 3;
            drawWidth = image.getWidth() * scale;
            drawHeight = image.getHeight() * scale;

        } catch (IOException e) {
            System.out.println("Could not load building" + buildingNumber + ".png!");
        }

        // --- HITBOX TUNING KNOBS ---
        // 1. sidePadding: Increased to 75 to shrink the sides and open the gaps between buildings!
        sidePadding = 75;

        // 2. topPadding: Set to 50 so the wall blocks you before you walk onto the roof
        topPadding = 50;

        // 3. boxHeight: Set to cover the main structure down to the door
        boxHeight = drawHeight - 70;

        boxWidth = drawWidth - (sidePadding * 2);

        this.hitbox = new Rectangle(worldX + sidePadding, worldY + topPadding, boxWidth, boxHeight);
    }

    public void draw(Graphics2D g2, GamePanel gp) {
        if (image != null) {
            int screenX = worldX - gp.playerX + gp.playerScreenX;
            int screenY = worldY - gp.playerY + gp.playerScreenY;

            g2.drawImage(image, screenX, screenY, drawWidth, drawHeight, null);

            // --- DEBUG MODE: ON ---
            // When you are done testing the gap, just put "//" in front of the bottom two lines to hide the red boxes!
           // g2.setColor(new java.awt.Color(255, 0, 0, 100));
            //g2.fillRect(screenX + sidePadding, screenY + topPadding, boxWidth, boxHeight);
        }
    }
}