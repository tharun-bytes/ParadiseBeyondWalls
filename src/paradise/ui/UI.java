package paradise.ui;

import paradise.core.GamePanel;
import paradise.core.GameState;
import paradise.entity.Ghost;
import paradise.object.Building;
import paradise.object.CapturePoint;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;

public class UI {

    private final GamePanel gp;

    public UI(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2) {
        if (gp.gameState == GameState.PLAYING || gp.gameState == GameState.LEVEL_TRANSITION) {
            drawHeader(g2);
            drawHealth(g2);
            drawStamina(g2);
            drawMiniMap(g2);
        } else if (gp.gameState == GameState.PAUSED) {
            drawPauseMenu(g2);
        } else if (gp.gameState == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else if (gp.gameState == GameState.VICTORY) {
            drawVictory(g2);
        }
    }

    private void drawPauseMenu(Graphics2D g2) {
        // Dim the frozen game behind the menu
        g2.setColor(new Color(20, 15, 10, 190));
        g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

        int panelWidth = 340;
        int panelHeight = 300;
        int panelX = gp.screenWidth / 2 - panelWidth / 2;
        int panelY = gp.screenHeight / 2 - panelHeight / 2;

        // --- Layered pixel-art frame (outer glow -> border -> brown panel -> inset line) ---
        Color outerFrame = new Color(255, 148, 68);
        Color frameShadow = new Color(120, 60, 20);
        Color panelBg = new Color(92, 60, 44);
        Color panelInnerBorder = new Color(58, 34, 24);

        int corner = 10;
        g2.setColor(outerFrame);
        g2.fillRoundRect(panelX, panelY, panelWidth, panelHeight, corner, corner);

        int b1 = 6;
        g2.setColor(frameShadow);
        g2.fillRoundRect(panelX + b1, panelY + b1, panelWidth - b1 * 2, panelHeight - b1 * 2, corner - 2, corner - 2);

        int b2 = 10;
        g2.setColor(panelBg);
        g2.fillRoundRect(panelX + b2, panelY + b2, panelWidth - b2 * 2, panelHeight - b2 * 2, corner - 4, corner - 4);

        int b3 = 14;
        g2.setColor(panelInnerBorder);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(panelX + b3, panelY + b3, panelWidth - b3 * 2, panelHeight - b3 * 2, corner - 6, corner - 6);

        // --- Title ---
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 26));
        String title = "PAUSED";
        int titleWidth = g2.getFontMetrics().stringWidth(title);
        g2.drawString(title, panelX + panelWidth / 2 - titleWidth / 2, panelY + 55);

        // --- Buttons ---
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

        // --- Hint ---
        g2.setFont(new Font("SansSerif", Font.PLAIN, 11));
        g2.setColor(new Color(210, 190, 170));
        String hint = "W/S or \u2191\u2193  \u2022  Enter to Select  \u2022  Esc to Resume";
        int hintWidth = g2.getFontMetrics().stringWidth(hint);
        g2.drawString(hint, panelX + panelWidth / 2 - hintWidth / 2, panelY + panelHeight - 16);
    }

    /** A chunky, beveled pixel-art style button used by the pause menu. */
    private void drawPixelButton(Graphics2D g2, int x, int y, int w, int h, String label, boolean selected) {
        Color border = selected ? new Color(255, 200, 120) : new Color(140, 70, 25);
        Color fill = selected ? new Color(240, 130, 40) : new Color(200, 105, 40);
        Color textColor = selected ? Color.WHITE : new Color(255, 235, 210);

        int corner = 8;
        g2.setColor(border);
        g2.fillRoundRect(x - 2, y - 2, w + 4, h + 4, corner, corner);
        g2.setColor(fill);
        g2.fillRoundRect(x, y, w, h, corner - 2, corner - 2);

        // Subtle top highlight strip for a beveled, pixel-art feel
        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRoundRect(x + 3, y + 3, w - 6, h / 3, corner - 4, corner - 4);

        g2.setFont(new Font("SansSerif", Font.BOLD, 16));
        g2.setColor(textColor);
        int textWidth = g2.getFontMetrics().stringWidth(label);
        int textAscent = g2.getFontMetrics().getAscent();
        g2.drawString(label, x + w / 2 - textWidth / 2, y + h / 2 + textAscent / 2 - 4);

        if (selected) {
            g2.setColor(Color.WHITE);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            g2.drawString("\u25B8", x - 16, y + h / 2 + 6);
        }
    }


    private void drawHeader(Graphics2D g2) {
        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(20, 20, 320, 80, 12, 12);

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
    }

    private void drawHealth(Graphics2D g2) {
        int panelX = gp.screenWidth - 150;
        int panelY = 20;
        int panelW = 130;
        int panelH = 56;

        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        g2.setColor(new Color(140, 170, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("HEALTH", panelX + 15, panelY + 18);

        int heartSize = 18;
        int spacing = 24;
        int heartY = panelY + 26;
        for (int i = 0; i < gp.maxHealth; i++) {
            int heartX = panelX + 15 + i * spacing;
            boolean filled = i < gp.playerHealth;
            Color color = filled ? new Color(235, 60, 70) : new Color(80, 45, 50);
            drawHeart(g2, heartX, heartY, heartSize, color, filled);
        }
    }

    private void drawStamina(Graphics2D g2) {
        int panelX = gp.screenWidth - 150;
        int panelY = 84;
        int panelW = 130;
        int panelH = 46;

        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(panelX, panelY, panelW, panelH, 12, 12);

        g2.setColor(new Color(140, 170, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("STAMINA", panelX + 15, panelY + 18);

        g2.setColor(new Color(60, 70, 85));
        g2.fillRoundRect(panelX + 15, panelY + 28, 100, 8, 4, 4);

        int currentBarWidth = (int) ((gp.currentStamina / (double) gp.maxStamina) * 100);
        g2.setColor(new Color(0, 220, 255));
        g2.fillRoundRect(panelX + 15, panelY + 28, Math.max(0, currentBarWidth), 8, 4, 4);
    }

    /** Draws a proper vector heart (two circular lobes + a triangular point) instead of a text glyph,
     *  which renders inconsistently across fonts/platforms. */
    private void drawHeart(Graphics2D g2, int x, int y, int size, Color color, boolean filled) {
        int r = size / 4;
        Ellipse2D.Double leftLobe = new Ellipse2D.Double(x, y, 2 * r, 2 * r);
        Ellipse2D.Double rightLobe = new Ellipse2D.Double(x + 2 * r, y, 2 * r, 2 * r);
        int[] xPoints = {x, x + 4 * r, x + 2 * r};
        int[] yPoints = {y + r, y + r, y + size};
        Polygon point = new Polygon(xPoints, yPoints, 3);

        Area heart = new Area(leftLobe);
        heart.add(new Area(rightLobe));
        heart.add(new Area(point));

        g2.setColor(color);
        if (filled) {
            g2.fill(heart);
        } else {
            g2.setStroke(new BasicStroke(1.8f));
            g2.draw(heart);
        }
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

        g2.setColor(new Color(215, 185, 125));
        int beachX = mapX + (int) (42 * gp.tileSize * scale);
        g2.fillRect(beachX, mapY, mapDiameter - (beachX - mapX), mapDiameter);

        int wallPixelRadius = (int) (20.0 * gp.tileSize * scale);
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
                if (ghost != null) {
                    int gx = mapX + (int) (ghost.worldX * scale);
                    int gy = mapY + (int) (ghost.worldY * scale);
                    g2.fillOval(gx, gy, 4, 4);
                }
            }
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