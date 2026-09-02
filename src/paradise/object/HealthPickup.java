package paradise.object;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;

public class HealthPickup {
    public final int worldX;
    public final int worldY;
    public final int healAmount;
    private int lifetime = 600;

    public HealthPickup(GamePanel game, int column, int row, int healAmount) {
        this.worldX = column * game.tileSize;
        this.worldY = row * game.tileSize;
        this.healAmount = healAmount;
    }

    public boolean isExpired() {
        return lifetime <= 0;
    }

    public void update() {
        if (lifetime > 0) lifetime--;
    }

    public void draw(Graphics2D graphics, GamePanel game, int animationFrame) {
        if (!game.isOnScreen(worldX, worldY, game.tileSize, game.tileSize)) return;
        int screenX = worldX - game.playerX + game.playerScreenX;
        int screenY = worldY - game.playerY + game.playerScreenY;

        float pulse = (float) (Math.sin(animationFrame * 0.12) * 2);
        boolean blink = lifetime < 120 && (animationFrame / 4) % 2 == 0;
        if (blink) return;

        graphics.setColor(new Color(50, 255, 100, 50));
        graphics.fillOval(screenX + 4 - (int) pulse, screenY + 4 - (int) pulse,
                game.tileSize - 8 + (int) (pulse * 2), game.tileSize - 8 + (int) (pulse * 2));

        int cx = screenX + game.tileSize / 2;
        int cy = screenY + game.tileSize / 2;

        graphics.setColor(new Color(80, 255, 120));
        graphics.fillRect(cx - 2, cy - 7, 4, 14);
        graphics.fillRect(cx - 7, cy - 2, 14, 4);

        graphics.setColor(new Color(180, 255, 200));
        graphics.fillRect(cx - 1, cy - 5, 2, 10);
        graphics.fillRect(cx - 5, cy - 1, 10, 2);
    }
}
