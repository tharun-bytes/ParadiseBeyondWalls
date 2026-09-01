package paradise.effects;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

/**
 * Small leaves/petals drifting across the screen on the wind. Purely
 * decorative and drawn in screen space (not tied to the world), so it reads
 * as foreground atmosphere no matter where the player is standing. Motion
 * speeds up and slows down with the same "gust" rhythm used for the grass
 * and trees so everything feels like it's blowing in the same wind.
 */
public class AmbientParticles {
    private static final int PARTICLE_COUNT = 14;

    private final GamePanel gp;
    private final Random rand = new Random(7);

    private final float[] x = new float[PARTICLE_COUNT];
    private final float[] y = new float[PARTICLE_COUNT];
    private final float[] speed = new float[PARTICLE_COUNT];
    private final float[] phase = new float[PARTICLE_COUNT];
    private final float[] size = new float[PARTICLE_COUNT];
    private final int[] colorIndex = new int[PARTICLE_COUNT];

    private static final Color[] COLORS = {
            new Color(212, 192, 112, 190),
            new Color(180, 210, 140, 170),
            new Color(226, 172, 122, 180)
    };

    public AmbientParticles(GamePanel gp) {
        this.gp = gp;
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            resetParticle(i, rand.nextFloat() * gp.screenWidth);
        }
    }

    private void resetParticle(int i, float startX) {
        x[i] = startX;
        y[i] = rand.nextFloat() * gp.screenHeight;
        speed[i] = 0.4f + rand.nextFloat() * 0.9f;
        phase[i] = rand.nextFloat() * (float) (Math.PI * 2);
        size[i] = 3f + rand.nextFloat() * 3f;
        colorIndex[i] = rand.nextInt(COLORS.length);
    }

    public void update() {
        double gust = 0.6 + 0.4 * Math.sin(gp.animationFrame * 0.015);
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            x[i] += speed[i] * gust;
            y[i] += Math.sin(gp.animationFrame * 0.03 + phase[i]) * 0.4f;
            if (x[i] > gp.screenWidth + 10) {
                resetParticle(i, -10);
            }
        }
    }

    public void draw(Graphics2D g2) {
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            g2.setColor(COLORS[colorIndex[i]]);
            float s = size[i];
            double wobble = Math.sin(gp.animationFrame * 0.1 + phase[i]) * s * 0.3;
            g2.fillOval((int) (x[i] + wobble), (int) y[i], (int) s, (int) (s * 0.65f));
        }
    }
}
