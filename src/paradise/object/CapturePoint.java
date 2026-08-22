package paradise.object;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;

/** A glowing point needed to stabilise a level. */
public class CapturePoint {
    public final int worldX;
    public final int worldY;

    public CapturePoint(GamePanel game, int column, int row) {
        this.worldX = column * game.tileSize;
        this.worldY = row * game.tileSize;
    }

    public void draw(Graphics2D graphics, GamePanel game, int animationFrame) {
        if (!game.isOnScreen(worldX, worldY, game.tileSize, game.tileSize)) return;
        int screenX = worldX - game.playerX + game.playerScreenX;
        int screenY = worldY - game.playerY + game.playerScreenY;
        int pulse = (int) (Math.sin(animationFrame * 0.15 + worldX) * 3);

        graphics.setColor(new Color(255, 204, 72, 48));
        graphics.fillOval(screenX + 5 - pulse, screenY + 5 - pulse, game.tileSize - 10 + pulse * 2, game.tileSize - 10 + pulse * 2);
        graphics.setColor(new Color(255, 222, 104));
        int[] x = {screenX + game.tileSize / 2, screenX + game.tileSize - 12, screenX + game.tileSize / 2, screenX + 12};
        int[] y = {screenY + 9, screenY + game.tileSize / 2, screenY + game.tileSize - 9, screenY + game.tileSize / 2};
        graphics.fillPolygon(x, y, 4);
        graphics.setColor(new Color(255, 246, 201));
        graphics.fillOval(screenX + game.tileSize / 2 - 5, screenY + game.tileSize / 2 - 5, 10, 10);
    }
}
