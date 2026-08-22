package paradise.entity;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/** A wall-aware enemy that patrols until the player enters its sight range. */
public class Ghost {
    private static final int SIGHT_RANGE = 360;
    private static final Random RANDOM = new Random();

    private final GamePanel game;
    private final int speed;
    private int patrolTimer;
    private int directionX;
    private int directionY = 1;

    public int worldX;
    public int worldY;
    public final int size;

    public Ghost(GamePanel game, int startColumn, int startRow, int speed) {
        this.game = game;
        this.speed = speed;
        this.worldX = startColumn * game.tileSize + 6;
        this.worldY = startRow * game.tileSize + 4;
        this.size = game.tileSize - 12;
    }

    public void update() {
        int playerCenterX = game.playerX + game.playerSize / 2;
        int playerCenterY = game.playerY + game.playerSize / 2;
        int ghostCenterX = worldX + size / 2;
        int ghostCenterY = worldY + size / 2;

        if (Math.hypot(playerCenterX - ghostCenterX, playerCenterY - ghostCenterY) < SIGHT_RANGE) {
            chase(playerCenterX - ghostCenterX, playerCenterY - ghostCenterY);
        } else {
            patrol();
        }
    }

    private void chase(int differenceX, int differenceY) {
        int horizontal = Integer.compare(differenceX, 0) * speed;
        int vertical = Integer.compare(differenceY, 0) * speed;
        boolean moved;

        if (Math.abs(differenceX) >= Math.abs(differenceY)) {
            moved = move(horizontal, 0);
            if (!moved) move(0, vertical);
        } else {
            moved = move(0, vertical);
            if (!moved) move(horizontal, 0);
        }
    }

    private void patrol() {
        patrolTimer++;
        if (patrolTimer >= 90) {
            chooseDirection();
            patrolTimer = 0;
        }
        if (!move(directionX, directionY)) {
            chooseDirection();
        }
    }

    private void chooseDirection() {
        switch (RANDOM.nextInt(4)) {
            case 0:
                directionX = 0;
                directionY = -speed;
                break;
            case 1:
                directionX = 0;
                directionY = speed;
                break;
            case 2:
                directionX = -speed;
                directionY = 0;
                break;
            default:
                directionX = speed;
                directionY = 0;
                break;
        }
    }

    private boolean move(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) return false;
        if (!game.collisionChecker.canMove(worldX, worldY, size, size, deltaX, deltaY)) return false;
        worldX += deltaX;
        worldY += deltaY;
        return true;
    }

    public void draw(Graphics2D graphics, int animationFrame) {
        int screenX = worldX - game.playerX + game.playerScreenX;
        int screenY = worldY - game.playerY + game.playerScreenY + (int) (Math.sin(animationFrame * 0.12 + worldX) * 3);

        if (!game.isOnScreen(worldX, worldY, size, size)) return;

        Color mist = new Color(111, 220, 255, 70);
        graphics.setColor(mist);
        graphics.fillOval(screenX - 8, screenY + 9, size + 16, size + 16);

        graphics.setColor(new Color(210, 248, 255));
        graphics.fillRoundRect(screenX, screenY + 7, size, size - 5, 18, 18);
        graphics.fillOval(screenX, screenY, size, size / 2 + 8);

        graphics.setColor(new Color(38, 66, 100));
        graphics.fillOval(screenX + 10, screenY + 17, 7, 9);
        graphics.fillOval(screenX + size - 17, screenY + 17, 7, 9);

        graphics.setColor(new Color(79, 126, 158));
        int[] x = {screenX, screenX + size / 4, screenX + size / 2, screenX + size * 3 / 4, screenX + size};
        int[] y = {screenY + size - 2, screenY + size - 9, screenY + size - 2, screenY + size - 9, screenY + size - 2};
        graphics.fillPolygon(x, y, 5);
    }
}
