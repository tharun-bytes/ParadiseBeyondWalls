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

public class UI {

    private final GamePanel gp;

    public UI(GamePanel gp) {
        this.gp = gp;
    }

    public void draw(Graphics2D g2) {
        if (gp.gameState == GameState.PLAYING || gp.gameState == GameState.LEVEL_TRANSITION) {
            drawHeader(g2);
            drawIntegrity(g2);
            drawMiniMap(g2);
        } else if (gp.gameState == GameState.GAME_OVER) {
            drawGameOver(g2);
        } else if (gp.gameState == GameState.VICTORY) {
            drawVictory(g2);
        }
    }

    private void drawHeader(Graphics2D g2) {
        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(20, 20, 320, 80, 12, 12);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        g2.drawString("PARADISE // BEYOND WALLS", 35, 45);

        g2.setColor(new Color(140, 170, 210));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 10));
        g2.drawString("LEVEL 0" + gp.currentLevel + " • THE FALLEN COURTYARD", 35, 62);

        g2.setColor(new Color(255, 215, 0));
        int totalPoints = gp.levelConfig != null ? gp.levelConfig.pointTiles.length : 5;
        g2.drawString("CAPTURE POINTS: " + gp.capturedPoints + " / " + totalPoints, 35, 85);
    }

    private void drawIntegrity(Graphics2D g2) {
        g2.setColor(new Color(15, 25, 40, 220));
        g2.fillRoundRect(gp.screenWidth - 140, 20, 120, 60, 12, 12);

        g2.setColor(new Color(140, 170, 210));
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString("INTEGRITY", gp.screenWidth - 125, 40);

        // Draw Health Hearts
        g2.setColor(Color.RED);
        for (int i = 0; i < gp.playerHealth; i++) {
            g2.drawString("❤", gp.screenWidth - 125 + (i * 20), 62);
        }
    }

    private void drawMiniMap(Graphics2D g2) {
        int mapDiameter = 130;
        int mapX = gp.screenWidth - mapDiameter - 20;
        int mapY = gp.screenHeight - mapDiameter - 20;
        int centerX = mapX + mapDiameter / 2;
        int centerY = mapY + mapDiameter / 2;

        // Outer ambient glow ring
        g2.setColor(new Color(30, 60, 90, 140));
        g2.setStroke(new BasicStroke(4));
        g2.drawOval(mapX - 2, mapY - 2, mapDiameter + 4, mapDiameter + 4);

        // Save original clipping mask so everything stays clipped inside the circle
        java.awt.Shape oldClip = g2.getClip();
        java.awt.geom.Ellipse2D.Double circleClip = new java.awt.geom.Ellipse2D.Double(mapX, mapY, mapDiameter, mapDiameter);
        g2.setClip(circleClip);

        // Circular background
        g2.setColor(new Color(12, 22, 34, 230));
        g2.fillOval(mapX, mapY, mapDiameter, mapDiameter);

        // Grid crosshair
        g2.setColor(new Color(255, 255, 255, 20));
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(centerX, mapY, centerX, mapY + mapDiameter);
        g2.drawLine(mapX, centerY, mapX + mapDiameter, centerY);

        // Scale factor: World Pixels to Map Pixels
        double scale = (double) mapDiameter / (double) gp.worldWidth;

        // Draw Buildings
        if (gp.mapBuildings != null) {
            g2.setColor(new Color(100, 115, 130));
            for (Building b : gp.mapBuildings) {
                if (b != null) {
                    int bx = mapX + (int) (b.worldX * scale);
                    int by = mapY + (int) (b.worldY * scale);
                    int bw = Math.max(4, (int) (b.drawWidth * scale));
                    int bh = Math.max(4, (int) (b.drawHeight * scale));
                    g2.fillRoundRect(bx, by, bw, bh, 2, 2);
                }
            }
        }

        // Draw Exit / Gate
        g2.setColor(new Color(46, 204, 113));
        int gateX = mapX;
        int gateY = mapY + (int) ((24 * gp.tileSize) * scale);
        g2.fillRect(gateX, gateY, 4, (int) (2 * gp.tileSize * scale));

        // Draw Animated Capture Points (Pulsing Glow)
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

        // Draw Ghosts
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

        // Draw Player with Expanding Pulse Ring
        int playerDotX = mapX + (int) (gp.playerX * scale);
        int playerDotY = mapY + (int) (gp.playerY * scale);

        int rippleSize = (gp.animationFrame % 40) / 3;
        int rippleAlpha = Math.max(0, 180 - (rippleSize * 12));

        if (gp.isHiding) {
            g2.setColor(new Color(46, 204, 113, rippleAlpha));
            g2.drawOval(playerDotX - rippleSize, playerDotY - rippleSize, 6 + (rippleSize * 2), 6 + (rippleSize * 2));
            g2.setColor(new Color(46, 204, 113));
        } else {
            g2.setColor(new Color(0, 230, 255, rippleAlpha));
            g2.drawOval(playerDotX - rippleSize, playerDotY - rippleSize, 6 + (rippleSize * 2), 6 + (rippleSize * 2));
            g2.setColor(new Color(0, 230, 255));
        }
        g2.fillOval(playerDotX, playerDotY, 6, 6);

        // Restore canvas clip
        g2.setClip(oldClip);

        // Metallic Border
        g2.setColor(new Color(80, 150, 220, 220));
        g2.setStroke(new BasicStroke(2));
        g2.drawOval(mapX, mapY, mapDiameter, mapDiameter);

        // Top Pill Badge
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