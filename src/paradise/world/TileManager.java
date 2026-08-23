package paradise.world;

import paradise.core.GamePanel;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;

public class TileManager {
    private final GamePanel gp;
    public int[][] mapTileNum;

    public BufferedImage[] gateFrames = new BufferedImage[4];

    public static final int TILE_WATER = 0;
    public static final int TILE_SAND = 1;
    public static final int TILE_GRASS = 2;
    public static final int TILE_WALL = 3;
    public static final int TILE_GATE_FLOOR = 4;
    public static final int TILE_GATE_PILLAR = 5;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        loadGateSprites();
        createLevelMap(1);
    }

    private void loadGateSprites() {
        BufferedImage sheet = null;
        try {
            // Check direct file system first
            File file = new File("src/paradise/object/gate_sprites.png");
            if (file.exists()) {
                sheet = ImageIO.read(file);
            } else {
                InputStream is = getClass().getResourceAsStream("/paradise/object/gate_sprites.png");
                if (is != null) {
                    sheet = ImageIO.read(is);
                }
            }

            if (sheet != null) {
                int frameWidth = sheet.getWidth() / 4;
                int frameHeight = sheet.getHeight();

                for (int i = 0; i < 4; i++) {
                    gateFrames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
                }
                System.out.println("Gate sprites loaded successfully!");
            } else {
                System.out.println("Could not find gate_sprites.png at src/paradise/object/gate_sprites.png");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createLevelMap(int level) {
        int centerX = gp.maxWorldCol / 2; // 25
        int centerY = gp.maxWorldRow / 2; // 25
        double wallRadius = 20.0;

        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {
                double distance = Math.hypot(col - centerX, row - centerY);

                if (distance > wallRadius + 2.5) {
                    if (col >= 41) {
                        mapTileNum[col][row] = TILE_SAND;
                    } else {
                        mapTileNum[col][row] = TILE_WATER;
                    }
                } else if (distance >= wallRadius - 1.2 && distance <= wallRadius + 1.2) {
                    mapTileNum[col][row] = TILE_WALL;
                } else if (distance < wallRadius - 1.2) {
                    mapTileNum[col][row] = TILE_GRASS;
                } else {
                    mapTileNum[col][row] = TILE_SAND;
                }
            }
        }

        // East Gate Pathway (Rows 23-26, Cols 40-48)
        for (int col = 40; col <= 48; col++) {
            mapTileNum[col][22] = TILE_GATE_PILLAR;
            mapTileNum[col][23] = TILE_GATE_FLOOR;
            mapTileNum[col][24] = TILE_GATE_FLOOR;
            mapTileNum[col][25] = TILE_GATE_FLOOR;
            mapTileNum[col][26] = TILE_GATE_FLOOR;
            mapTileNum[col][27] = TILE_GATE_PILLAR;
        }

        // West Gate Pathway (Rows 23-26, Cols 0-7)
        for (int col = 0; col <= 7; col++) {
            mapTileNum[col][22] = TILE_GATE_PILLAR;
            mapTileNum[col][23] = TILE_GATE_FLOOR;
            mapTileNum[col][24] = TILE_GATE_FLOOR;
            mapTileNum[col][25] = TILE_GATE_FLOOR;
            mapTileNum[col][26] = TILE_GATE_FLOOR;
            mapTileNum[col][27] = TILE_GATE_PILLAR;
        }
    }

    public void draw(Graphics2D g2) {
        int startCol = Math.max(0, (gp.playerX - gp.playerScreenX) / gp.tileSize);
        int endCol = Math.min(gp.maxWorldCol, (gp.playerX + gp.playerScreenX + gp.tileSize) / gp.tileSize + 1);
        int startRow = Math.max(0, (gp.playerY - gp.playerScreenY) / gp.tileSize);
        int endRow = Math.min(gp.maxWorldRow, (gp.playerY + gp.playerScreenY + gp.tileSize) / gp.tileSize + 1);

        for (int col = startCol; col < endCol; col++) {
            for (int row = startRow; row < endRow; row++) {
                int tileType = mapTileNum[col][row];
                int screenX = col * gp.tileSize - gp.playerX + gp.playerScreenX;
                int screenY = row * gp.tileSize - gp.playerY + gp.playerScreenY;

                switch (tileType) {
                    case TILE_WATER:
                        g2.setColor(new Color(24, 75, 120));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(40, 110, 165, 130));
                        g2.drawLine(screenX + 6, screenY + 14, screenX + 28, screenY + 14);
                        g2.drawLine(screenX + 20, screenY + 34, screenX + 42, screenY + 34);
                        break;

                    case TILE_SAND:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(225, 195, 135));
                        else g2.setColor(new Color(215, 185, 125));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_GRASS:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(34, 70, 52));
                        else g2.setColor(new Color(40, 82, 60));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_WALL:
                        g2.setColor(new Color(75, 80, 90));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(45, 50, 58));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 1, screenY + 1, gp.tileSize - 2, gp.tileSize - 2);
                        g2.setColor(new Color(105, 110, 120));
                        g2.drawLine(screenX + 4, screenY + 16, screenX + gp.tileSize - 4, screenY + 16);
                        g2.drawLine(screenX + 4, screenY + 32, screenX + gp.tileSize - 4, screenY + 32);
                        break;

                    case TILE_GATE_FLOOR:
                        g2.setColor(new Color(130, 115, 95));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(90, 80, 68));
                        g2.drawRect(screenX + 2, screenY + 2, gp.tileSize - 4, gp.tileSize - 4);
                        break;

                    case TILE_GATE_PILLAR:
                        g2.setColor(new Color(45, 50, 60));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(210, 160, 50));
                        g2.fillOval(screenX + 12, screenY + 12, 24, 24);
                        g2.setColor(Color.WHITE);
                        g2.fillOval(screenX + 18, screenY + 18, 12, 12);
                        break;
                }
            }
        }

        // Draw the East Beach Gate spanning rows 23-26 (Open Frame 3)
        drawGateSprite(g2, 44, 23, 3);

        // Draw the West Exit Gate spanning rows 23-26
        boolean allCollected = (gp.levelConfig != null && gp.capturedPoints >= gp.levelConfig.pointTiles.length);
        int westGateFrame = allCollected ? 3 : 0;
        drawGateSprite(g2, 4, 23, westGateFrame);
    }

    private void drawGateSprite(Graphics2D g2, int tileCol, int tileRow, int frameIndex) {
        int worldX = tileCol * gp.tileSize;
        int worldY = tileRow * gp.tileSize;
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        // Perfectly covers the 4-tile wide opening
        int gateWidth = gp.tileSize * 2;
        int gateHeight = gp.tileSize * 4;

        if (gp.isOnScreen(worldX, worldY, gateWidth, gateHeight)) {
            if (gateFrames != null && gateFrames[frameIndex] != null) {
                g2.drawImage(gateFrames[frameIndex], screenX, screenY, gateWidth, gateHeight, null);
            } else {
                // High-visibility fallback if sprite file is missing
                g2.setColor(new Color(40, 45, 55, 230));
                g2.fillRoundRect(screenX, screenY, gateWidth, gateHeight, 16, 16);
                g2.setColor(new Color(220, 175, 50));
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(screenX, screenY, gateWidth, gateHeight, 16, 16);
            }
        }
    }
}