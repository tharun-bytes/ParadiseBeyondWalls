package paradise.entity;

import paradise.core.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;

/** A towering titan that patrols the courtyard and slams the player when it closes in. */
public class Boss {
    private static final int AGGRO_RANGE = 460;
    private static final int ATTACK_RANGE = 210;
    private static final int WINDUP_FRAMES = 45;
    private static final int STRIKE_COOLDOWN = 75;

    private final GamePanel game;
    private final String name;
    private final int speed;
    private final int damage;
    private final int startX;
    private final int startY;

    public int worldX;
    public int worldY;
    public int hp;
    public final int maxHp;
    public boolean alive = true;
    public final int size;

    private boolean facingLeft;
    private boolean windingUp;
    private int windUpTimer;
    private int cooldown;
    private int flashTimer;

    public Boss(GamePanel game, int column, int row, String name, int hp, int speed, int damage) {
        this.game = game;
        this.name = name;
        this.speed = speed;
        this.damage = damage;
        this.maxHp = hp;
        this.hp = hp;
        this.size = game.tileSize * 3;
        this.startX = column * game.tileSize;
        this.startY = row * game.tileSize;
        this.worldX = startX;
        this.worldY = startY;
    }

    public Rectangle hitbox() {
        return new Rectangle(worldX + 14, worldY + size / 3, size - 28, size - size / 3);
    }

    public void resetToSpawn() {
        this.hp = maxHp;
        this.alive = true;
        this.windingUp = false;
        this.windUpTimer = 0;
        this.cooldown = 0;
        this.worldX = startX;
        this.worldY = startY;
    }

    public void takeDamage(int amount) {
        if (!alive) return;
        hp -= amount;
        flashTimer = 14;
        if (hp <= 0) {
            hp = 0;
            alive = false;
        }
    }

    public String getName() {
        return name;
    }

    public boolean isWindingUp() {
        return windingUp;
    }

    public void update() {
        if (!alive) return;
        if (flashTimer > 0) flashTimer--;

        if (windingUp) {
            windUpTimer++;
            if (windUpTimer >= WINDUP_FRAMES) strike();
            return;
        }
        if (cooldown > 0) {
            cooldown--;
            return;
        }

        int playerCenterX = game.playerX + game.playerSize / 2;
        int playerCenterY = game.playerY + game.playerSize / 2;
        int bossCenterX = worldX + size / 2;
        int bossCenterY = worldY + size / 2;
        double distance = Math.hypot(playerCenterX - bossCenterX, playerCenterY - bossCenterY);

        if (distance < AGGRO_RANGE) {
            int dx = playerCenterX - bossCenterX;
            int dy = playerCenterY - bossCenterY;
            facingLeft = dx < 0;

            if (distance > ATTACK_RANGE) {
                moveToward(dx, dy);
            } else if (!game.isHiding) {
                windingUp = true;
                windUpTimer = 0;
            }
        } else {
            idleDrift(bossCenterX, bossCenterY);
        }
    }

    private void idleDrift(int bossCenterX, int bossCenterY) {
        int spawnCenterX = startX + size / 2;
        int spawnCenterY = startY + size / 2;
        int dx = spawnCenterX - bossCenterX;
        int dy = spawnCenterY - bossCenterY;
        if (Math.abs(dx) + Math.abs(dy) > speed) moveToward(dx, dy);
    }

    private void moveToward(int dx, int dy) {
        int stepX = Integer.compare(dx, 0) * speed;
        int stepY = Integer.compare(dy, 0) * speed;
        if (Math.abs(dx) >= Math.abs(dy)) {
            if (!move(stepX, 0)) move(0, stepY);
        } else {
            if (!move(0, stepY)) move(stepX, 0);
        }
    }

    private boolean move(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) return false;
        if (!game.collisionChecker.canMove(worldX, worldY, size, size, deltaX, deltaY)) return false;
        worldX += deltaX;
        worldY += deltaY;
        return true;
    }

    private void strike() {
        windingUp = false;
        cooldown = STRIKE_COOLDOWN;

        Rectangle slammed = slamZone();
        Rectangle playerBox = new Rectangle(game.playerX, game.playerY, game.playerSize, game.playerSize);
        if (slammed.intersects(playerBox) && !game.isHiding) {
            game.harmPlayerFromBoss(damage, worldX + size / 2, worldY + size / 2);
        }
    }

    private Rectangle slamZone() {
        int reach = game.tileSize * 2;
        if (facingLeft) {
            return new Rectangle(worldX - reach, worldY + size / 4, size + reach, size / 2);
        }
        return new Rectangle(worldX, worldY + size / 4, size + reach, size / 2);
    }

    public void draw(Graphics2D g2, int animationFrame) {
        if (!alive) return;
        if (!game.isOnScreen(worldX, worldY, size, size)) return;

        int sx = worldX - game.playerX + game.playerScreenX;
        int sy = worldY - game.playerY + game.playerScreenY;
        int bob = (int) (Math.sin(animationFrame * 0.05) * 3);
        int w = size;
        int h = size;

        boolean enraged = hp < maxHp / 2;
        float rageGlow = enraged ? (0.5f + 0.5f * (float) Math.sin(animationFrame * 0.15)) : 0f;

        // Ground aura pooling beneath the titan
        g2.setColor(new Color(60, 30, 20, 80));
        g2.fillOval(sx - 24, sy + size - 42, w + 48, 46);

        // Legs
        g2.setColor(new Color(38, 42, 58));
        g2.fillRoundRect(sx + 18, sy + size - 96 + bob, 40, 92 - bob, 16, 16);
        g2.fillRoundRect(sx + w - 58, sy + size - 96 + bob, 40, 92 - bob, 16, 16);

        // Torso
        GradientPaint torsoPaint = new GradientPaint(
                sx, sy + size / 3, flashTimer > 0 ? new Color(150, 60, 55) : new Color(52, 58, 80),
                sx + w, sy + size, flashTimer > 0 ? new Color(90, 30, 40) : new Color(26, 30, 44));
        g2.setPaint(torsoPaint);
        g2.fill(new RoundRectangle2D.Double(sx + 10, sy + size / 3, w - 20, size - size / 3, 40, 40));
        g2.setPaint(null);

        // Chest plate
        g2.setColor(new Color(120, 130, 150));
        g2.setStroke(new BasicStroke(3));
        g2.draw(new RoundRectangle2D.Double(sx + 34, sy + size / 3 + 26, w - 68, 44, 18, 18));

        // Arms
        g2.setStroke(new BasicStroke(22, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.setColor(flashTimer > 0 ? new Color(140, 55, 50) : new Color(40, 46, 64));
        if (windingUp) {
            // Both arms raised high overhead before the slam
            g2.drawLine(sx + 26, sy + size / 3 + 30, sx - 26, sy - 20 + bob);
            g2.drawLine(sx + w - 26, sy + size / 3 + 30, sx + w + 26, sy - 20 + bob);
            // Fists glint
            g2.setColor(new Color(210, 150, 90));
            g2.fillOval(sx - 38, sy - 28 + bob, 26, 26);
            g2.fillOval(sx + w + 12, sy - 28 + bob, 26, 26);
        } else {
            g2.drawLine(sx + 26, sy + size / 3 + 44, sx - 16, sy + size - 44);
            g2.drawLine(sx + w - 26, sy + size / 3 + 44, sx + w + 16, sy + size - 44);
        }

        // Neck + massive head
        g2.setColor(new Color(46, 52, 72));
        g2.fillRoundRect(sx + w / 2 - 26, sy + 8 + bob, 52, 44, 18, 18);
        g2.fill(new RoundRectangle2D.Double(sx + w / 2 - 52, sy - 6 + bob, w / 2, 62, 40, 40));

        // Steam wisps from the mouth when enraged
        if (enraged) {
            g2.setColor(new Color(220, 190, 160, (int) (60 + rageGlow * 80)));
            g2.fillOval(sx + w / 2 - 40, sy + 26 + bob, 40, 14);
            g2.fillOval(sx + w / 2 - 30, sy + 36 + bob, 52, 12);
        }

        // Glowing amber eyes + mouth
        Color eyeColor = windingUp ? new Color(255, 240, 220) : new Color(255, 170, 60, (int) (150 + rageGlow * 100));
        g2.setColor(eyeColor);
        g2.fillOval(sx + w / 2 - 40, sy + 12 + bob, 20, 16);
        g2.fillOval(sx + w / 2 + 20, sy + 12 + bob, 20, 16);
        g2.setColor(windingUp ? new Color(160, 40, 40) : new Color(120, 40, 30, 160));
        g2.fillRoundRect(sx + w / 2 - 22, sy + 34 + bob, 44, 8, 6, 6);

        // Red radial menace glow above the courtyard
        RadialGradientPaint aura = new RadialGradientPaint(
                new Point2D.Float(sx + w / 2f, sy + size / 2f), size,
                new float[]{0f, 1f},
                new Color[]{new Color(220, 70, 40, (int) (30 + rageGlow * 70)), new Color(220, 70, 40, 0)});
        g2.setPaint(aura);
        g2.fillOval(sx - 30, sy - 30, w + 60, h + 60);
        g2.setPaint(null);
    }
}