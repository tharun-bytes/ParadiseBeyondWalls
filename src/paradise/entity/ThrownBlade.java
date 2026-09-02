package paradise.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

public class ThrownBlade {
    public int x, y;
    public int vx, vy;
    public int life;
    private static final int SIZE = 14;

    public ThrownBlade(int x, int y, int vx, int vy, int maxDistance) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = maxDistance;
    }

    public void update() {
        x += vx;
        y += vy;
        life -= Math.max(Math.abs(vx), Math.abs(vy));
    }

    public Rectangle box() {
        return new Rectangle(x - SIZE / 2, y - SIZE / 2, SIZE, SIZE);
    }

    public void draw(Graphics2D g2, int playerX, int playerY, int screenX, int screenY) {
        int sx = x - playerX + screenX;
        int sy = y - playerY + screenY;

        g2.setColor(new Color(0, 240, 255));
        g2.setColor(new Color(220, 245, 255));
        int len = 16;
        int px2 = sx - vx / Math.max(1, Math.abs(vx));
        int py2 = sy - vy / Math.max(1, Math.abs(vy));

        g2.setStroke(new java.awt.BasicStroke(3));
        g2.drawLine(sx, sy, sx + len, sy + len);

        g2.setColor(new Color(0, 180, 255));
        g2.setStroke(new java.awt.BasicStroke(2));
        g2.fillOval(sx - 3, sy - 3, 7, 7);
    }
}
