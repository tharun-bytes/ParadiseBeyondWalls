package paradise.object;

import paradise.core.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Procedural village dressing: dirt paths, a central well, lamp posts, and small
 * yard fences in front of each house. Purely decorative (drawn on top of the
 * ground tiles) and doesn't affect collision — it's meant to make the ring of
 * houses in {@link GamePanel#setupBuildings()} read as one connected village
 * instead of isolated buildings, using only shapes (no new image assets).
 */
public class VillageDecor {

    // Open plaza in the middle of the building ring.
    private static final int CENTER_COL = 25;
    private static final int CENTER_ROW = 25;

    // A path is drawn from the plaza to each of these points (roughly each house's door).
    private static final int[][] PATH_TARGETS = {
            {25, 18}, {36, 22}, {40, 32}, {34, 42}, {22, 42}, {12, 34}, {16, 22}
    };

    // A lamp post partway along each path.
    private static final int[][] LAMP_POSTS = {
            {25, 21}, {31, 24}, {31, 27}, {28, 33}, {19, 33}, {17, 29}, {19, 22}
    };

    private static final Color PATH_COLOR = new Color(150, 125, 90);
    private static final Color FENCE_COLOR = new Color(120, 90, 60);

    private VillageDecor() {}

    /** Dirt paths connecting the plaza to each house. Draw AFTER tiles, BEFORE buildings. */
    public static void drawPaths(Graphics2D g2, GamePanel gp) {
        g2.setColor(PATH_COLOR);
        for (int[] target : PATH_TARGETS) {
            drawPathLine(g2, gp, CENTER_COL, CENTER_ROW, target[0], target[1]);
        }
    }

    private static void drawPathLine(Graphics2D g2, GamePanel gp, int col0, int row0, int col1, int row1) {
        int steps = Math.max(Math.abs(col1 - col0), Math.abs(row1 - row0)) * 2;
        if (steps == 0) return;
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int col = (int) Math.round(col0 + (col1 - col0) * t);
            int row = (int) Math.round(row0 + (row1 - row0) * t);
            drawPathTile(g2, gp, col, row);
        }
    }

    private static void drawPathTile(Graphics2D g2, GamePanel gp, int col, int row) {
        int worldX = col * gp.tileSize;
        int worldY = row * gp.tileSize;
        if (!gp.isOnScreen(worldX, worldY, gp.tileSize, gp.tileSize)) return;
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;
        g2.fillRoundRect(screenX + 4, screenY + 4, gp.tileSize - 8, gp.tileSize - 8, 10, 10);
    }

    /** A simple stone well in the plaza. Draw AFTER paths, BEFORE buildings. */
    public static void drawWell(Graphics2D g2, GamePanel gp) {
        int worldX = CENTER_COL * gp.tileSize;
        int worldY = CENTER_ROW * gp.tileSize;
        if (!gp.isOnScreen(worldX, worldY, gp.tileSize * 2, gp.tileSize * 2)) return;
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        g2.setColor(new Color(90, 85, 80));
        g2.fillOval(screenX - 10, screenY - 10, gp.tileSize + 20, gp.tileSize + 20);
        g2.setColor(new Color(50, 90, 130));
        g2.fillOval(screenX + 6, screenY + 6, gp.tileSize - 12, gp.tileSize - 12);
        g2.setColor(new Color(130, 90, 55));
        g2.setStroke(new BasicStroke(4));
        g2.drawLine(screenX + gp.tileSize / 2, screenY - 18, screenX + gp.tileSize / 2, screenY + 6);
        g2.drawLine(screenX - 4, screenY - 18, screenX + gp.tileSize + 4, screenY - 18);
    }

    /** Glowing lamp posts along the paths. Draw AFTER buildings so they read as foreground props. */
    public static void drawLampPosts(Graphics2D g2, GamePanel gp, int animationFrame) {
        for (int[] lamp : LAMP_POSTS) {
            int worldX = lamp[0] * gp.tileSize;
            int worldY = lamp[1] * gp.tileSize;
            if (!gp.isOnScreen(worldX, worldY, gp.tileSize, gp.tileSize * 2)) continue;
            int screenX = worldX - gp.playerX + gp.playerScreenX + gp.tileSize / 2;
            int screenY = worldY - gp.playerY + gp.playerScreenY;

            g2.setColor(new Color(60, 45, 35));
            g2.fillRect(screenX - 3, screenY - 20, 6, 30);

            int flicker = (int) (Math.sin(animationFrame * 0.1 + lamp[0]) * 2);
            g2.setColor(new Color(255, 210, 120, 70));
            g2.fillOval(screenX - 16 - flicker, screenY - 34 - flicker, 32 + flicker * 2, 32 + flicker * 2);
            g2.setColor(new Color(255, 230, 160));
            g2.fillOval(screenX - 6, screenY - 24, 12, 12);
        }
    }

    /** A small picket fence along the front yard of one house. Draw AFTER that building. */
    public static void drawFence(Graphics2D g2, GamePanel gp, Building b) {
        if (b == null || b.image == null) return;
        int screenX = b.worldX - gp.playerX + gp.playerScreenX;
        int screenY = b.worldY - gp.playerY + gp.playerScreenY;
        int padding = 14;
        int fx = screenX - padding;
        int fy = screenY + b.drawHeight - 40;
        int fw = b.drawWidth + padding * 2;

        g2.setColor(FENCE_COLOR);
        g2.setStroke(new BasicStroke(3));
        for (int x = fx; x <= fx + fw; x += 14) {
            g2.drawLine(x, fy, x, fy + 16);
        }
        g2.drawLine(fx, fy + 6, fx + fw, fy + 6);
        g2.drawLine(fx, fy + 14, fx + fw, fy + 14);
    }
}
