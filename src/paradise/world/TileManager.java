package paradise.world;

import paradise.core.GamePanel;
import paradise.core.LevelConfig;

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
        LevelConfig cfg = gp.levelConfig;
        int cols = (cfg != null) ? cfg.worldCols : gp.maxWorldCol;
        int rows = (cfg != null) ? cfg.worldRows : gp.maxWorldRow;
        double wallRadius = (cfg != null) ? cfg.wallRadius : 20.0;
        mapTileNum = new int[cols][rows];

        int centerX = cols / 2;
        int centerY = rows / 2;

        for (int col = 0; col < cols; col++) {
            for (int row = 0; row < rows; row++) {
                double distance = Math.hypot(col - centerX, row - centerY);

                if (distance > wallRadius + 2.5) {
                    if (col >= centerX) {
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

        // West corridor (the escape gate side)
        int westStart = (int) (centerX - wallRadius);
        carveCorridor(westStart - 3, westStart + 5, centerY, cols, rows);

        // East corridor (beach side, player spawn)
        int eastStart = (int) (centerX + wallRadius);
        carveCorridor(eastStart - 5, eastStart + 7, centerY, cols, rows);
    }

    private void carveCorridor(int colFrom, int colTo, int centerY, int cols, int rows) {
        for (int col = colFrom; col <= colTo; col++) {
            for (int row = centerY - 4; row <= centerY + 3; row++) {
                if (col >= 0 && col < cols && row >= 0 && row < rows) {
                    mapTileNum[col][row] = TILE_SAND;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        int cols = gp.maxWorldCol;
        int rows = gp.maxWorldRow;
        int startCol = Math.max(0, (gp.playerX - gp.playerScreenX) / gp.tileSize);
        int endCol = Math.min(cols, (gp.playerX + gp.playerScreenX + gp.tileSize) / gp.tileSize + 1);
        int startRow = Math.max(0, (gp.playerY - gp.playerScreenY) / gp.tileSize);
        int endRow = Math.min(rows, (gp.playerY + gp.playerScreenY + gp.tileSize) / gp.tileSize + 1);

        for (int col = startCol; col < endCol; col++) {
            for (int row = startRow; row < endRow; row++) {
                int tileType = mapTileNum[col][row];
                int screenX = col * gp.tileSize - gp.playerX + gp.playerScreenX;
                int screenY = row * gp.tileSize - gp.playerY + gp.playerScreenY;

                switch (tileType) {
                    case TILE_WATER:
                        g2.setColor(new Color(64, 164, 223));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);

                        g2.setColor(new Color(200, 235, 255, 150));

                        int waveOffset1 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + col * 0.6) * 3);
                        int waveOffset2 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + row * 0.6 + 2) * 3);
                        g2.drawLine(screenX + 6, screenY + 14 + waveOffset1, screenX + 28, screenY + 14 + waveOffset1);
                        g2.drawLine(screenX + 20, screenY + 34 + waveOffset2, screenX + 42, screenY + 34 + waveOffset2);
                        break;

                    case TILE_SAND:
                    case TILE_GATE_FLOOR:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(238, 221, 173));
                        else g2.setColor(new Color(230, 210, 160));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_GRASS:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(99, 169, 82));
                        else g2.setColor(new Color(107, 180, 89));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_WALL:
                    case TILE_GATE_PILLAR:
                        g2.setColor(new Color(158, 162, 171));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(138, 142, 150));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 1, screenY + 1, gp.tileSize - 2, gp.tileSize - 2);
                        g2.setColor(new Color(180, 184, 192));
                        g2.drawLine(screenX + 4, screenY + 16, screenX + gp.tileSize - 4, screenY + 16);
                        g2.drawLine(screenX + 4, screenY + 32, screenX + gp.tileSize - 4, screenY + 32);
                        break;
                }
            }
        }
    }
}