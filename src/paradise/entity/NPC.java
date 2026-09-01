package paradise.entity;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;

/** A friendly scout who stands at a spot and offers tips and route hints when talked to. */
public class NPC {
    private final GamePanel game;
    public final int worldX;
    public final int worldY;
    public final String name;
    private final String[] lines;

    public NPC(GamePanel game, int column, int row, String name, String... lines) {
        this.game = game;
        this.worldX = column * game.tileSize;
        this.worldY = row * game.tileSize;
        this.name = name;
        this.lines = lines;
    }

    public int lineCount() {
        return lines.length;
    }

    public String line(int index) {
        return lines[index];
    }

    public boolean nearPlayer(double range) {
        int playerCenterX = game.playerX + game.playerSize / 2;
        int playerCenterY = game.playerY + game.playerSize / 2;
        int npcCenterX = worldX + game.tileSize / 2;
        int npcCenterY = worldY + game.tileSize;
        return Math.hypot(playerCenterX - npcCenterX, playerCenterY - npcCenterY) < range;
    }

    public void draw(Graphics2D g2, int animationFrame) {
        if (!game.isOnScreen(worldX, worldY, game.tileSize, game.tileSize * 2)) return;
        int sx = worldX - game.playerX + game.playerScreenX;
        int sy = worldY - game.playerY + game.playerScreenY;
        int bob = (int) (Math.sin(animationFrame * 0.05 + worldX) * 2);

        // Shadow
        g2.setColor(new Color(0, 0, 0, 90));
        g2.fillOval(sx + 4, sy + 54, 42, 10);

        // Legs
        g2.setColor(new Color(60, 70, 90));
        g2.fillRoundRect(sx + 13, sy + 40 + bob, 10, 18, 4, 4);
        g2.fillRoundRect(sx + 29, sy + 40 + bob, 10, 18, 4, 4);

        // Cloak body (Survey Corps green)
        g2.setColor(new Color(44, 96, 64));
        g2.fill(new RoundRectangle2D.Double(sx + 6, sy + 18 + bob, 40, 40, 16, 16));
        g2.setColor(new Color(30, 70, 48));
        g2.fill(new RoundRectangle2D.Double(sx + 6, sy + 32 + bob, 40, 26, 10, 10));

        // Small gear box on the back
        g2.setColor(new Color(92, 82, 62));
        g2.fillRoundRect(sx + 10, sy + 24 + bob, 15, 12, 4, 4);

        // Hood + head
        g2.setColor(new Color(70, 78, 104));
        g2.fill(new RoundRectangle2D.Double(sx + 8, sy - 2 + bob, 36, 26, 14, 14));
        g2.setColor(new Color(222, 208, 184));
        g2.fillOval(sx + 20, sy + 6 + bob, 12, 12);

        // Attention marker when the player can talk
        if (nearPlayer(64)) {
            float pulse = 0.6f + 0.4f * (float) Math.sin(animationFrame * 0.2);
            g2.setColor(new Color(255, 220, 90, (int) (120 + pulse * 100)));
            g2.fillOval(sx + 40, sy - 12 + bob, 10, 10);
            g2.setColor(new Color(255, 235, 140));
            g2.setFont(new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            g2.drawString("!", sx + 43, sy - 1 + bob);
        }
    }
}