package paradise.world;

import paradise.core.GamePanel;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;

public class TileManager {
    private final GamePanel gp;
    public int[][] mapTileNum;

    public static final int TILE_WATER = 0;
    public static final int TILE_SAND = 1;
    public static final int TILE_GRASS = 2;
    public static final int TILE_WALL = 3;
    public static final int TILE_GATE_FLOOR = 4;
    public static final int TILE_GATE_PILLAR = 5;

    public TileManager(GamePanel gp) {
        this.gp = gp;
        this.mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];
        createLevelMap(1);
    }

    public void createLevelMap(int level) {
        int centerX = gp.maxWorldCol / 2;
        int centerY = gp.maxWorldRow / 2;
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

        // Keep east and west passages wide open with walkable sand tiles
        for (int col = 40; col <= 48; col++) {
            for (int row = 22; row <= 27; row++) {
                mapTileNum[col][row] = TILE_SAND;
            }
        }

        for (int col = 0; col <= 7; col++) {
            for (int row = 22; row <= 27; row++) {
                mapTileNum[col][row] = TILE_SAND;
            }
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
                        g2.setColor(new Color(9, 24, 46));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);

                        g2.setColor(new Color(40, 110, 165, 130));

                        int waveOffset1 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + col * 0.6) * 3);
                        int waveOffset2 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + row * 0.6 + 2) * 3);
                        g2.drawLine(screenX + 6, screenY + 14 + waveOffset1, screenX + 28, screenY + 14 + waveOffset1);
                        g2.drawLine(screenX + 20, screenY + 34 + waveOffset2, screenX + 42, screenY + 34 + waveOffset2);
                        break;

                    case TILE_SAND:
                    case TILE_GATE_FLOOR:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(96, 84, 62));
                        else g2.setColor(new Color(88, 77, 56));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_GRASS:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(13, 28, 21));
                        else g2.setColor(new Color(16, 33, 24));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_WALL:
                    case TILE_GATE_PILLAR:
                        g2.setColor(new Color(42, 45, 54));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(22, 24, 30));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 1, screenY + 1, gp.tileSize - 2, gp.tileSize - 2);
                        g2.setColor(new Color(64, 68, 78));
                        g2.drawLine(screenX + 4, screenY + 16, screenX + gp.tileSize - 4, screenY + 16);
                        g2.drawLine(screenX + 4, screenY + 32, screenX + gp.tileSize - 4, screenY + 32);
                        break;
                }
            }
        }
    }
}