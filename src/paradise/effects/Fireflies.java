package paradise.effects;

import paradise.core.GamePanel;
import paradise.object.Building;
import paradise.object.Tree;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Small glowing fireflies that hover near buildings and trees at night. Each
 * firefly drifts in a lazy loop around a fixed world-space anchor (a
 * building or a tree) and twinkles in brightness over time. Anchors are
 * built once from {@link GamePanel#mapBuildings} and {@link GamePanel#mapTrees},
 * so call {@link #init()} only after those arrays are populated. Positions
 * are world-space so fireflies scroll correctly with the camera, and
 * off-screen ones are skipped when drawing.
 */
public class Fireflies {
    private static final int PER_BUILDING = 4;
    private static final int PER_TREE_CLUSTER = 2;
    private static final int TREE_SAMPLE_STEP = 4; // only give every Nth tree its own firefly cluster

    private final GamePanel gp;
    private final List<Firefly> fireflies = new ArrayList<>();

    private static class Firefly {
        double anchorX, anchorY;
        double radiusX, radiusY;
        double speedX, speedY;
        double phaseX, phaseY;
        double twinklePhase, twinkleSpeed;
    }

    public Fireflies(GamePanel gp) {
        this.gp = gp;
    }

    /** Builds firefly clusters from the current buildings/trees. Call after both are set up. */
    public void init() {
        fireflies.clear();
        Random rand = new Random(2024);

        if (gp.mapBuildings != null) {
            for (Building b : gp.mapBuildings) {
                if (b == null) continue;
                double cx = b.worldX + b.drawWidth / 2.0;
                double cy = b.worldY + b.drawHeight * 0.8;
                addCluster(cx, cy, PER_BUILDING, rand);
            }
        }

        if (gp.mapTrees != null) {
            for (int i = 0; i < gp.mapTrees.length; i += TREE_SAMPLE_STEP) {
                Tree t = gp.mapTrees[i];
                if (t == null) continue;
                double cx = t.worldX + gp.tileSize / 2.0;
                double cy = t.worldY + gp.tileSize * 0.6;
                addCluster(cx, cy, PER_TREE_CLUSTER, rand);
            }
        }
    }

    private void addCluster(double cx, double cy, int count, Random rand) {
        for (int i = 0; i < count; i++) {
            Firefly f = new Firefly();
            f.anchorX = cx + (rand.nextDouble() - 0.5) * 24;
            f.anchorY = cy + (rand.nextDouble() - 0.5) * 24;
            f.radiusX = 14 + rand.nextDouble() * 20;
            f.radiusY = 8 + rand.nextDouble() * 14;
            f.speedX = 0.35 + rand.nextDouble() * 0.4;
            f.speedY = 0.3 + rand.nextDouble() * 0.4;
            f.phaseX = rand.nextDouble() * Math.PI * 2;
            f.phaseY = rand.nextDouble() * Math.PI * 2;
            f.twinklePhase = rand.nextDouble() * Math.PI * 2;
            f.twinkleSpeed = 0.025 + rand.nextDouble() * 0.05;
            fireflies.add(f);
        }
    }

    /** Draw AFTER the night overlay so the glow visibly cuts through the dark. */
    public void draw(Graphics2D g2) {
        int frame = gp.animationFrame;
        for (Firefly f : fireflies) {
            double wx = f.anchorX + Math.cos(frame * 0.012 * f.speedX + f.phaseX) * f.radiusX;
            double wy = f.anchorY + Math.sin(frame * 0.015 * f.speedY + f.phaseY) * f.radiusY;

            if (!gp.isOnScreen((int) wx - 12, (int) wy - 12, 24, 24)) continue;

            int screenX = (int) Math.round(wx - gp.playerX + gp.playerScreenX);
            int screenY = (int) Math.round(wy - gp.playerY + gp.playerScreenY);

            double twinkle = 0.3 + 0.7 * (0.5 + 0.5 * Math.sin(frame * f.twinkleSpeed + f.twinklePhase));
            if (twinkle < 0.15) continue; // fireflies occasionally go fully dark

            int glowAlpha = (int) (120 * twinkle);
            int coreAlpha = (int) (235 * twinkle);

            float glowRadius = 8f;
            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point2D.Float(screenX, screenY), glowRadius,
                    new float[]{0f, 1f},
                    new Color[]{new Color(215, 255, 140, glowAlpha), new Color(215, 255, 140, 0)}
            );
            g2.setPaint(glow);
            g2.fillOval((int) (screenX - glowRadius), (int) (screenY - glowRadius), (int) (glowRadius * 2), (int) (glowRadius * 2));

            g2.setColor(new Color(240, 255, 205, coreAlpha));
            g2.fillOval(screenX - 2, screenY - 2, 4, 4);
        }
    }
}
