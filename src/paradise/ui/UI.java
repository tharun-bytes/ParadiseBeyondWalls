package paradise.ui;

import paradise.core.GamePanel;
import paradise.core.GameState;
import paradise.entity.Boss;
import paradise.entity.Ghost;
import paradise.object.Building;
import paradise.object.CapturePoint;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class UI {

    private final GamePanel gp;

    // Wood theme (pixel-art) assets for the pause menu
    private BufferedImage panelTex;
    private BufferedImage buttonNormalTex;
    private BufferedImage buttonHoverTex;
    private BufferedImage heartSheet;
    private Font pixelFont;
    public UI(GamePanel gp) {
        this.gp = gp;
        loadTheme();
    }

    private void loadTheme() {
        try {
            panelTex = ImageIO.read(new File("src/paradise/ui/theme/nine_path_panel.png"));
            buttonNormalTex = ImageIO.read(new File("src/paradise/ui/theme/button_normal.png"));
            buttonHoverTex = ImageIO.read(new File("src/paradise/ui/theme/button_hover.png"));
            heartSheet = ImageIO.read(new File("src/paradise/ui/theme/Heart.png"));
        } catch (IOException e) {
            System.out.println("Could not load UI theme images: " + e.getMessage());
        }

        try {
            pixelFont = Font.createFont(Font.TRUETYPE_FONT, new File("src/paradise/ui/theme/NormalFont.ttf"));
        } catch (IOException | FontFormatException e) {
            System.out.println("Could not load pixel font, falling back to default: " + e.getMessage());
            pixelFont = new Font("Monospaced", Font.BOLD, 16);
        }
    }

    private Font pixelFont(float size) {
        return pixelFont.deriveFont(Font.PLAIN, size);
    }

    private void drawNinePatch(Graphics2D g2, BufferedImage img, int destX, int destY, int destW, int destH,
                               int left, int top, int right, int bottom) {
        if (img == null) return;
        Object oldInterp = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        int srcW = img.getWidth();
        int srcH = img.getHeight();
        int[] sx = {0, left, srcW - right, srcW};
        int[] sy = {0, top, srcH - bottom, srcH};
        int[] dx = {destX, destX + left, destX + destW - right, destX + destW};
        int[] dy = {destY, destY + top, destY + destH - bottom, destY + destH};

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                g2.drawImage(img, dx[col], dy[row], dx[col + 1], dy[row + 1],
                        sx[col], sy[row], sx[col + 1], sy[row + 1], null);
            }
        }

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                oldInterp != null ? oldInterp : RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    }

    public void draw(Graphics2D g2) {
        if (gp.gameState == GameState.PLAYING || gp.gameState == GameState.LEVEL_TRANSITION) {
            drawHeader(g2);
            drawHealth(g2);
            drawStamina(g2);
            drawMiniMap(g2);
            drawDoorLockHint(g2);
            drawStatusMessage(g2);
            if (gp.gameState == GameState.LEVEL_TRANSITION) drawTransition(g2);
            if (gp.dialogueActive) drawDialogue(g2);
        } else if (gp.gameState == GameState.PAUSED) {
            drawPauseMenu(g2);
        } else if (gp.gameState == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else if (gp.gameState == GameState.VICTORY) {
            drawVictory(g2);
        }
    }

    private void drawPauseMenu(Graphics2D g2) {
        g2.setColor(new Color(20, 15, 10, 190));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int panelWidth = 340;
        int panelHeight = 300;
        int panelX = gp.screenWidth / 2 - panelWidth / 2;
        int panelY = gp.screenHeight / 2 - panelHeight / 2;

        drawNinePatch(g2, panelTex, panelX, panelY, panelWidth, panelHeight, 4, 4, 4, 5);

        g2.setColor(Color.WHITE);
        g2.setFont(pixelFont(30f));
        String title = "PAUSED";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, panelX + panelWidth / 2 - titleWidth / 2, panelY + 55);

        int buttonWidth = panelWidth - 80;
        int buttonHeight = 42;
        int buttonX = panelX + 40;
        int buttonY = panelY + 90;
        int buttonGap = 14;

        for (int i = 0; i < gp.pauseMenuOptions.length; i++) {
            boolean selected = (i == gp.pauseMenuIndex);
            int by = buttonY + i * (buttonHeight + buttonGap);
            drawPixelButton(g2, buttonX, by, buttonWidth, buttonHeight, gp.pauseMenuOptions[i], selected);
        }

        g2.setFont(pixelFont(11f));
        g2.setColor(new Color(230, 215, 195));
        String hint = "W/S or Arrows  \u2022  Enter to Select  \u2022  Esc to Resume";
        int hintWidth = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, panelX + panelWidth / 2 - hintWidth / 2, panelY + panelHeight - 16);
    }

    private void drawPixelButton(Graphics2D g2, int x, int y, int w, int h, String label, boolean selected) {
        BufferedImage tex = selected ? buttonHoverTex : buttonNormalTex;
        drawNinePatch(g2, tex, x, y, w, h, 2, 2, 2, 2);

        g2.setColor(selected ? new Color(60, 30, 10) : new Color(255, 240, 220));
        g2.setFont(pixelFont(18f));
        int textWidth = g2.getFontMetrics().stringWidth(label);
        int textAscent = g2.getFontMetrics().getAscent();
        g2.drawString(label, x + w / 2 - textWidth / 2, y + h / 2 + textAscent / 2 - 4);
    }

    private void drawHeader(Graphics2D g2) {
        int panelHeight = 80;
        if (gp.areMonstersAlive()) panelHeight += 25;

        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(20, 20, 320, panelHeight, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        String title = (gp.levelConfig != null) ? gp.levelConfig.levelName : "PARADISE // BEYOND WALLS";
        g2.drawString(title, 35, 45);

        g2.setColor(new Color(140, 170, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        String subtitle = (gp.levelConfig != null) ? gp.levelConfig.subTitle : ("LEVEL 0" + gp.currentLevel + " • THE FALLEN COURTYARD");
        g2.drawString(subtitle, 35, 62);

        g2.setColor(new Color(255, 215, 0));
        int totalPoints = (gp.levelConfig != null) ? gp.levelConfig.pointTiles.length : 5;
        g2.drawString("CAPTURE POINTS: " + gp.capturedPoints + " / " + totalPoints, 35, 85);

        // Monster Count Tracker
        if (gp.areMonstersAlive() && gp.monsters != null) {
            int aliveCount = 0;
            for (Boss m : gp.monsters) {
                if (m != null && m.alive) aliveCount++;
            }
            g2.setColor(new Color(255, 100, 100));
            g2.setFont(new Font("SansSerif", Font.BOLD, 11));
            g2.drawString("BLOOD MONSTERS REMAINING: " + aliveCount, 35, 108);
        }
    }

    private void drawHealth(Graphics2D g2) {
        int panelX = gp.screenWidth - 150;
        int panelY = 20;
        int panelW = 130;
        int panelH = 46;

        drawNinePatch(g2, panelTex, panelX, panelY, panelW, panelH, 4, 4, 4, 5);

        g2.setColor(Color.WHITE);
        g2.setFont(pixelFont(11f));
        g2.drawString("HEALTH", panelX + 14, panelY + 18);

        g2.setColor(new Color(40, 30, 25));
        g2.fillRoundRect(panelX + 14, panelY + 26, 100, 8, 4, 4);

        int currentBarWidth = (int) ((gp.playerHealth / (double) gp.maxHealth) * 100);
        float healthRatio = gp.playerHealth / (float) gp.maxHealth;
        Color barColor;
        if (healthRatio > 0.5f) barColor = new Color(80, 220, 80);
        else if (healthRatio > 0.25f) barColor = new Color(220, 180, 40);
        else barColor = new Color(220, 60, 60);
        g2.setColor(barColor);
        g2.fillRoundRect(panelX + 14, panelY + 26, Math.max(0, currentBarWidth), 8, 4, 4);

        g2.setColor(new Color(220, 220, 220));
        g2.setFont(pixelFont(9f));
        g2.drawString(gp.playerHealth + " / " + gp.maxHealth, panelX + 80, panelY + 42);
    }

    private void drawStamina(Graphics2D g2) {
        int panelX = gp.screenWidth - 150;
        int panelY = 74;
        int panelW = 130;
        int panelH = 46;

        drawNinePatch(g2, panelTex, panelX, panelY, panelW, panelH, 4, 4, 4, 5);

        g2.setColor(Color.WHITE);
        g2.setFont(pixelFont(11f));
        g2.drawString("STAMINA", panelX + 14, panelY + 18);

        g2.setColor(new Color(40, 30, 25));
        g2.fillRoundRect(panelX + 14, panelY + 26, 100, 8, 4, 4);

        int currentBarWidth = (int) ((gp.currentStamina / (double) gp.maxStamina) * 100);
        g2.setColor(new Color(0, 220, 255));
        g2.fillRoundRect(panelX + 14, panelY + 26, Math.max(0, currentBarWidth), 8, 4, 4);
    }

    private void drawDoorLockHint(Graphics2D g2) {
        if (!gp.doorLocked || !gp.areMonstersAlive()) return;
        String text = "THE BLOOD HORDE BLOCKS THE WEST GATE — SLAY THEM TO PASS";
        g2.setFont(pixelFont(15f));
        int w = g2.getFontMetrics().stringWidth(text) + 40;
        int h = 36;
        int x = gp.screenWidth / 2 - w / 2;
        int y = gp.screenHeight - 130;

        g2.setColor(new Color(40, 8, 8, 210));
        g2.fillRoundRect(x, y, w, h, 10, 10);
        g2.setColor(new Color(255, 120, 90));
        g2.drawString(text, x + 20, y + 24);
    }

    private void drawStatusMessage(Graphics2D g2) {
        if (gp.statusMessageTimer <= 0 || gp.statusMessage.isEmpty()) return;
        int alpha = Math.min(255, gp.statusMessageTimer * 4);
        g2.setFont(pixelFont(18f));
        int w = g2.getFontMetrics().stringWidth(gp.statusMessage) + 48;
        int h = 40;
        int x = gp.screenWidth / 2 - w / 2;
        int y = gp.screenHeight - 176;

        g2.setColor(new Color(10, 14, 24, Math.min(215, alpha)));
        g2.fillRoundRect(x, y, w, h, 12, 12);
        g2.setColor(new Color(255, 220, 120, alpha));
        g2.drawString(gp.statusMessage, x + 24, y + 27);
    }

    private void drawTransition(Graphics2D g2) {
        g2.setColor(new Color(4, 7, 16, 175));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setColor(new Color(255, 215, 90));
        g2.setFont(pixelFont(34f));
        String title = "THE WALL IS CLEARED";
        int tw = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, gp.screenWidth / 2 - tw / 2, gp.screenHeight / 2 - 16);

        g2.setColor(Color.WHITE);
        g2.setFont(pixelFont(14f));
        String sub = "ASCENDING TO THE NEXT WALL...";
        int sw2 = g2.getFontMetrics().stringWidth(sub);
        g2.drawString(sub, gp.screenWidth / 2 - sw2 / 2, gp.screenHeight / 2 + 26);
    }

    private void drawDialogue(Graphics2D g2) {
        if (gp.npc == null || !gp.dialogueActive) return;
        String line = gp.npc.line(gp.dialogueIndex);

        int boxW = gp.screenWidth - 60;
        int boxH = 104;
        int boxX = 30;
        int boxY = gp.screenHeight - boxH - 22;

        drawNinePatch(g2, panelTex, boxX, boxY, boxW, boxH, 6, 6, 6, 8);
        g2.setColor(new Color(0, 0, 0, 70));
        g2.fillRoundRect(boxX + 8, boxY + 8, boxW - 16, boxH - 16, 8, 8);

        g2.setColor(new Color(255, 200, 90));
        g2.setFont(pixelFont(15f));
        g2.drawString(gp.npc.name, boxX + 22, boxY + 26);

        g2.setColor(new Color(240, 235, 225));
        g2.setFont(pixelFont(14f));
        String[] wrapped = wrapText(line, g2.getFontMetrics(), boxW - 44);
        int textY = boxY + 52;
        for (String piece : wrapped) {
            g2.drawString(piece, boxX + 22, textY);
            textY += 20;
        }

        g2.setColor(new Color(150, 210, 170));
        g2.setFont(pixelFont(11f));
        g2.drawString("Press [T] to Continue", boxX + boxW - 160, boxY + boxH - 12);
    }

    private String[] wrapText(String text, java.awt.FontMetrics fm, int maxWidth) {
        java.util.ArrayList<String> out = new java.util.ArrayList<>();
        for (String word : text.split(" ")) {
            if (out.isEmpty()) {
                out.add(word);
            } else {
                String last = out.get(out.size() - 1);
                if (fm.stringWidth(last + " " + word) <= maxWidth) {
                    out.set(out.size() - 1, last + " " + word);
                } else {
                    out.add(word);
                }
            }
        }
        return out.toArray(new String[0]);
    }

    private void drawMiniMap(Graphics2D g2) {
        int mapDiameter = 136;
        int mapX = gp.screenWidth - mapDiameter - 20;
        int mapY = gp.screenHeight - mapDiameter - 20;
        int centerX = mapX + mapDiameter / 2;
        int centerY = mapY + mapDiameter / 2;

        g2.setColor(new Color(30, 60, 90, 140));
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(mapX - 2, mapY - 2, mapDiameter + 4, mapDiameter + 4);

        java.awt.Shape oldClip = g2.getClip();
        java.awt.geom.Ellipse2D.Double circleClip = new java.awt.geom.Ellipse2D.Double(mapX, mapY, mapDiameter, mapDiameter);
        g2.setClip(circleClip);

        g2.setColor(new Color(20, 55, 95));
        g2.fillOval(mapX, mapY, mapDiameter, mapDiameter);

        double scale = (double) mapDiameter / (double) gp.worldWidth;
        double wallRadius = (gp.levelConfig != null) ? gp.levelConfig.wallRadius : 20.0;
        int centerCol = (gp.levelConfig != null) ? gp.levelConfig.worldCols / 2 : 25;

        g2.setColor(new Color(215, 185, 125));
        int beachX = mapX + (int) (centerCol * gp.tileSize * scale);
        g2.fillRect(beachX, mapY, mapDiameter - (beachX - mapX), mapDiameter);

        int wallPixelRadius = (int) (wallRadius * gp.tileSize * scale);
        g2.setColor(new Color(35, 75, 55));
        g2.fillOval(centerX - wallPixelRadius, centerY - wallPixelRadius, wallPixelRadius * 2, wallPixelRadius * 2);

        g2.setColor(new Color(110, 115, 125));
        g2.setStroke(new BasicStroke(3));
        g2.drawOval(centerX - wallPixelRadius, centerY - wallPixelRadius, wallPixelRadius * 2, wallPixelRadius * 2);

        g2.setColor(new Color(230, 180, 80));
        g2.fillRect(centerX + wallPixelRadius - 3, centerY - 3, 7, 7);
        g2.setColor(new Color(46, 204, 113));
        g2.fillRect(centerX - wallPixelRadius - 3, centerY - 3, 7, 7);

        if (gp.mapBuildings != null) {
            g2.setColor(new Color(130, 130, 140));
            for (Building b : gp.mapBuildings) {
                if (b != null) {
                    int bx = mapX + (int) (b.worldX * scale);
                    int by = mapY + (int) (b.worldY * scale);
                    int bw = Math.max(3, (int) (b.drawWidth * scale));
                    int bh = Math.max(3, (int) (b.drawHeight * scale));
                    g2.fillRoundRect(bx, by, bw, bh, 2, 2);
                }
            }
        }

        if (gp.capturePoints != null) {
            float pulse = (float) (Math.sin(gp.animationFrame * 0.1) * 2);
            for (CapturePoint cp : gp.capturePoints) {
                if (cp != null) {
                    int px = mapX + (int) (cp.worldX * scale);
                    int py = mapY + (int) (cp.worldY * scale);
                    g2.setColor(new Color(255, 215, 0, 90));
                    g2.fillOval(px - (int) pulse, py - (int) pulse, 4 + (int) (pulse * 2), 4 + (int) (pulse * 2));
                    g2.setColor(Color.YELLOW);
                    g2.fillOval(px, py, 4, 4);
                }
            }
        }

        if (gp.ghosts != null) {
            g2.setColor(new Color(255, 75, 75));
            for (Ghost ghost : gp.ghosts) {
                if (ghost != null && ghost.alive) {
                    int gx = mapX + (int) (ghost.worldX * scale);
                    int gy = mapY + (int) (ghost.worldY * scale);
                    g2.fillOval(gx, gy, 4, 4);
                }
            }
        }

        // Monsters on minimap
        if (gp.monsters != null) {
            g2.setColor(new Color(255, 50, 50));
            for (Boss m : gp.monsters) {
                if (m != null && m.alive) {
                    int mx = mapX + (int) (m.worldX * scale);
                    int my = mapY + (int) (m.worldY * scale);
                    g2.fillOval(mx - 3, my - 3, 9, 9);
                }
            }
        }

        if (gp.npc != null) {
            int nx = mapX + (int) (gp.npc.worldX * scale);
            int ny = mapY + (int) (gp.npc.worldY * scale);
            g2.setColor(new Color(80, 220, 120));
            g2.fillRect(nx - 2, ny - 2, 5, 5);
        }

        int playerDotX = mapX + (int) (gp.playerX * scale);
        int playerDotY = mapY + (int) (gp.playerY * scale);
        if (gp.isHiding) g2.setColor(new Color(46, 204, 113));
        else g2.setColor(new Color(0, 230, 255));
        g2.fillOval(playerDotX - 1, playerDotY - 1, 6, 6);

        g2.setClip(oldClip);

        g2.setColor(new Color(80, 150, 220, 220));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(mapX, mapY, mapDiameter, mapDiameter);

        g2.setColor(new Color(15, 25, 40, 240));
        g2.fillRoundRect(centerX - 22, mapY - 7, 44, 15, 7, 7);
        g2.setColor(new Color(100, 180, 255));
        g2.setStroke(new BasicStroke(1));
        g2.drawRoundRect(centerX - 22, mapY - 7, 44, 15, 7, 7);

        g2.setFont(new Font("SansSerif", Font.BOLD, 8));
        g2.setColor(Color.WHITE);
        g2.drawString("MAP", centerX - 9, mapY + 4);
    }

    private void drawGameOver(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setColor(Color.RED);
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2.drawString("WALL BREACHED", gp.screenWidth / 2 - 160, gp.screenHeight / 2 - 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("Press SPACE or R to Restart", gp.screenWidth / 2 - 100, gp.screenHeight / 2 + 30);
    }

    private void drawVictory(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 220));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        g2.setColor(new Color(255, 215, 0));
        g2.setFont(new Font("SansSerif", Font.BOLD, 36));
        g2.drawString("PARADIS RESTORED", gp.screenWidth / 2 - 180, gp.screenHeight / 2 - 20);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 14));
        g2.drawString("You cleared all 3 Walls!", gp.screenWidth / 2 - 80, gp.screenHeight / 2 + 30);
    }
}