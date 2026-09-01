package paradise.entity;

import paradise.core.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;
import java.util.Random;

/** A wall-aware enemy that patrols until the player enters its sight range. */
public class Ghost {
    private static final int SIGHT_RANGE = 360;
    private static final Random RANDOM = new Random();

    private final GamePanel game;
    private final int speed;
    private final int startColumn;
    private final int startRow;
    private int patrolTimer;
    private int directionX;
    private int directionY = 1;

    public int worldX;
    public int worldY;
    public final int size;
    public boolean alive = true;

    public Ghost(GamePanel game, int startColumn, int startRow, int speed) {
        this.game = game;
        this.speed = speed;
        this.startColumn = startColumn;
        this.startRow = startRow;
        this.worldX = startColumn * game.tileSize + 6;
        this.worldY = startRow * game.tileSize + 4;
        this.size = game.tileSize - 12;
    }

    public void resetToSpawn() {
        this.worldX = startColumn * game.tileSize + 6;
        this.worldY = startRow * game.tileSize + 4;
        this.patrolTimer = 0;
        this.directionX = 0;
        this.directionY = 1;
    }

    /** Slain by the player's blade; stays dead for the rest of this level. */
    public void kill() {
        alive = false;
    }

    public void update() {
        // If player is hiding inside a house, ghosts do NOT chase; they wander peacefully
        if (game.isHiding) {
            patrol();
            return;
        }

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

    public void draw(Graphics2D g2, int animationFrame) {
        if (!game.isOnScreen(worldX, worldY, size, size)) return;

        int screenX = worldX - game.playerX + game.playerScreenX;
        int screenY = worldY - game.playerY + game.playerScreenY + (int) (Math.sin(animationFrame * 0.1 + worldX) * 3);
        int top = screenY;
        int bottom = screenY + size + 6;
        int left = screenX;
        int right = screenX + size;
        int cx = screenX + size / 2;

        // Faint dread aura pooling beneath the robe
        g2.setColor(new Color(90, 40, 120, 55));
        g2.fillOval(left - 10, screenY + size / 3, size + 20, size + 14);

        // Robe silhouette: rounded hood tapering into a tattered, jagged hem
        Path2D.Double robe = new Path2D.Double();
        robe.moveTo(cx, top);
        robe.curveTo(left - 4, top + size * 0.15, left - 2, top + size * 0.55, left + 4, bottom - 14);
        int hemPoints = 5;
        for (int i = 0; i <= hemPoints; i++) {
            double t = (double) i / hemPoints;
            double x = left + 4 + t * (size - 8);
            double y = (i % 2 == 0) ? bottom : bottom - 12;
            robe.lineTo(x, y);
        }
        robe.curveTo(right - 2, top + size * 0.55, right + 4, top + size * 0.15, cx, top);
        robe.closePath();

        g2.setPaint(new GradientPaint(cx, top, new Color(78, 60, 102), cx, bottom, new Color(28, 20, 40)));
        g2.fill(robe);
        g2.setPaint(null);

        g2.setColor(new Color(18, 12, 26));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(robe);

        // Shadowed hollow beneath the hood peak
        g2.setColor(new Color(12, 8, 20, 170));
        g2.fillOval(cx - size / 4, top + 4, size / 2, size / 3);

        // Slowly pulsing eyes
        float glow = (float) (0.5 + 0.5 * Math.sin(animationFrame * 0.15));
        g2.setColor(new Color(150, 235, 255, (int) (180 + glow * 60)));
        g2.fillOval(cx - size / 5, top + size / 3, 6, 8);
        g2.fillOval(cx + size / 5 - 6, top + size / 3, 6, 8);
    }
}