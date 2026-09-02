package paradise.entity;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;

public class NPC {
    private final GamePanel gp;
    public int worldX, worldY;
    public String name;
    public String[] dialogues;

    public NPC(GamePanel gp, int col, int row, String name, String[] lines) {
        this.gp = gp;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = name;
        this.dialogues = lines;
    }

    // --- These are the methods your UI.java was looking for! ---
    public String getName() {
        return name;
    }

    public String line(int index) {
        if (dialogues != null && index >= 0 && index < dialogues.length) {
            return dialogues[index];
        }
        return ""; // Prevents out-of-bounds crashes if dialogue is missing
    }
    // -----------------------------------------------------------

    public int lineCount() {
        return dialogues != null ? dialogues.length : 0;
    }

    public boolean nearPlayer(int distance) {
        int px = gp.playerX + gp.playerSize / 2;
        int py = gp.playerY + gp.playerSize / 2;
        int nx = worldX + gp.tileSize / 2;
        int ny = worldY + gp.tileSize / 2;
        return Math.hypot(px - nx, py - ny) < distance;
    }

    public void draw(Graphics2D g2, int frame) {
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        if (gp.isOnScreen(worldX, worldY, gp.tileSize, gp.tileSize)) {
            // Placeholder Graphic for NPC
            g2.setColor(new Color(50, 150, 200));
            g2.fillOval(screenX, screenY, gp.tileSize, gp.tileSize);
        }
    }
}