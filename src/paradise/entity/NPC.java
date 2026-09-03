package paradise.entity;

import paradise.core.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class NPC {
    private final GamePanel gp;
    public int worldX, worldY;
    public String name;
    public String[] dialogues;

    private BufferedImage[] idleFrames;
    private int[] srcX, srcY, srcW, srcH;
    private int animIndex = 0;
    private int animCounter = 0;
    private static final int ANIM_SPEED = 8;

    private static final String[] FRAME_NAMES = {
            "1_IDLE_000.png", "1_IDLE_001.png", "1_IDLE_002.png",
            "1_IDLE_003.png", "1_IDLE_004.png"
    };

    public NPC(GamePanel gp, int col, int row, String name, String[] lines) {
        this.gp = gp;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = name;
        this.dialogues = lines;
        loadWizardFrames();
    }

    private void loadWizardFrames() {
        idleFrames = new BufferedImage[FRAME_NAMES.length];
        srcX = new int[FRAME_NAMES.length];
        srcY = new int[FRAME_NAMES.length];
        srcW = new int[FRAME_NAMES.length];
        srcH = new int[FRAME_NAMES.length];
        for (int i = 0; i < FRAME_NAMES.length; i++) {
            try {
                BufferedImage img = ImageIO.read(new File("src/paradise/entity/npc_wizard/" + FRAME_NAMES[i]));
                idleFrames[i] = img;
                int fw = img.getWidth(), fh = img.getHeight();
                int x0 = fw, x1 = 0, y0 = fh, y1 = 0;
                for (int x = 0; x < fw; x++) {
                    for (int y = 0; y < fh; y++) {
                        if ((img.getRGB(x, y) >>> 24) > 20) {
                            if (x < x0) x0 = x;
                            if (x > x1) x1 = x;
                            if (y < y0) y0 = y;
                            if (y > y1) y1 = y;
                        }
                    }
                }
                if (x1 <= x0 || y1 <= y0) { srcX[i]=0; srcY[i]=0; srcW[i]=fw; srcH[i]=fh; }
                else { srcX[i]=x0; srcY[i]=y0; srcW[i]=x1-x0+1; srcH[i]=y1-y0+1; }
            } catch (IOException e) {
                System.out.println("Could not load NPC frame: " + FRAME_NAMES[i]);
            }
        }
    }

    // --- These are the methods your UI.java was looking for! ---
    public String getName() {
        return name;
    }

    public String line(int index) {
        if (dialogues != null && index >= 0 && index < dialogues.length) {
            return dialogues[index];
        }
        return ""; // Prevents out-of-bounds crashes if dialogue is missing
    }
    // -----------------------------------------------------------

    public int lineCount() {
        return dialogues != null ? dialogues.length : 0;
    }

    public boolean nearPlayer(int distance) {
        int px = gp.playerX + gp.playerSize / 2;
        int py = gp.playerY + gp.playerSize / 2;
        int nx = worldX + gp.tileSize / 2;
        int ny = worldY + gp.tileSize / 2;
        return Math.hypot(px - nx, py - ny) < distance;
    }

    public void draw(Graphics2D g2, int frame) {
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        if (gp.isOnScreen(worldX, worldY, gp.tileSize, gp.tileSize)) {
            BufferedImage current = null;
            if (idleFrames != null && idleFrames[animIndex] != null) {
                current = idleFrames[animIndex];
            }

            if (current != null) {
                int sx = srcX[animIndex];
                int sy = srcY[animIndex];
                int sw = srcW[animIndex];
                int sh = srcH[animIndex];

                int drawH = (int) (gp.tileSize * 1.1);
                int drawW = (int) (sw * ((double) drawH / sh));

                int feetX = screenX + gp.tileSize / 2;
                int feetY = screenY + gp.tileSize;
                int drawX = feetX - drawW / 2;
                int drawY = feetY - drawH;

                g2.drawImage(current, drawX, drawY, drawX + drawW, drawY + drawH,
                        sx, sy, sx + sw, sy + sh, null);
            } else {
                // Fallback placeholder if the wizard frames failed to load
                g2.setColor(new Color(50, 150, 200));
                g2.fillOval(screenX, screenY, gp.tileSize, gp.tileSize);
            }
        }
    }

    public void updateAnimation() {
        animCounter++;
        if (animCounter >= ANIM_SPEED) {
            animCounter = 0;
            animIndex = (animIndex + 1) % FRAME_NAMES.length;
        }
    }
}
