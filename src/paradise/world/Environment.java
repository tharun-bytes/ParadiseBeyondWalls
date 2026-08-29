package paradise.world;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.image.BufferedImage;

public class Environment {
    private final GamePanel gp;
    private BufferedImage darknessFilter;

    public Environment(GamePanel gp) {
        this.gp = gp;
        setupLighting();
    }

    public void setupLighting() {
        darknessFilter = new BufferedImage(gp.screenWidth, gp.screenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

        int centerX = gp.playerScreenX + gp.tileSize / 2;
        int centerY = gp.playerScreenY + gp.tileSize / 2;

        float[] dist = {0.0f, 0.45f, 1.0f};
        Color[] color = {
                new Color(0, 0, 0, 0),
                new Color(10, 15, 25, 140),
                new Color(5, 8, 15, 235)
        };

        RadialGradientPaint paint = new RadialGradientPaint(centerX, centerY, 320, dist, color);
        g2.setPaint(paint);
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);
        g2.dispose();
    }

    public void draw(Graphics2D g2) {
        if (darknessFilter != null) {
            g2.drawImage(darknessFilter, 0, 0, null);
        }
    }
}