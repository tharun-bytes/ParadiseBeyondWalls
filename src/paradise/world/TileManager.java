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
                        g2.setColor(new Color(24, 75, 120));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(40, 110, 165, 130));
                        int waveOffset1 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + col * 0.6) * 3);
                        int waveOffset2 = (int) Math.round(Math.sin(gp.animationFrame * 0.05 + row * 0.6 + 2) * 3);
                        g2.drawLine(screenX + 6, screenY + 14 + waveOffset1, screenX + 28, screenY + 14 + waveOffset1);
                        g2.drawLine(screenX + 20, screenY + 34 + waveOffset2, screenX + 42, screenY + 34 + waveOffset2);
                        break;

                    case TILE_SAND:
                    case TILE_GATE_FLOOR:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(225, 195, 135));
                        else g2.setColor(new Color(215, 185, 125));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        break;

                    case TILE_GRASS:
                        if ((col + row) % 2 == 0) g2.setColor(new Color(34, 70, 52));
                        else g2.setColor(new Color(40, 82, 60));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        drawGrassBlades(g2, col, row, screenX, screenY);
                        break;

                    case TILE_WALL:
                    case TILE_GATE_PILLAR:
                        g2.setColor(new Color(75, 80, 90));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(45, 50, 58));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 1, screenY + 1, gp.tileSize - 2, gp.tileSize - 2);
                        g2.setColor(new Color(105, 110, 120));
                        g2.drawLine(screenX + 4, screenY + 16, screenX + gp.tileSize - 4, screenY + 16);
                        g2.drawLine(screenX + 4, screenY + 32, screenX + gp.tileSize - 4, screenY + 32);
                        break;
                }
            }
        }
    }

    private static final Color GRASS_BLADE_SHADOW = new Color(58, 110, 74);
    private static final Color GRASS_BLADE_HIGHLIGHT = new Color(84, 148, 100);

    /**
     * Small wind-blown grass tufts on top of a grass tile. Blade positions are
     * derived deterministically from the tile's col/row so they stay put
     * frame-to-frame; only the sway angle animates, driven by a slow "gust"
     * envelope layered with a faster per-blade wobble so the wind feels organic
     * rather than perfectly uniform.
     */
    private void drawGrassBlades(Graphics2D g2, int col, int row, int screenX, int screenY) {
        double gust = 0.5 + 0.5 * Math.sin(gp.animationFrame * 0.015 + col * 0.3);
        double windAngle = Math.sin(gp.animationFrame * 0.07 + row * 0.5) * (2.0 + gust * 4.0);

        int tileSeed = (col * 92821) ^ (row * 68917);
        g2.setStroke(new BasicStroke(1.4f));

        for (int i = 0; i < 3; i++) {
            int bladeSeed = tileSeed + i * 7351;
            int bx = screenX + 6 + Math.floorMod(bladeSeed, gp.tileSize - 12);
            int by = screenY + gp.tileSize - 4 - Math.floorMod(bladeSeed / 7, 8);
            int bladeHeight = 7 + Math.floorMod(bladeSeed / 13, 6);
            double phase = (Math.floorMod(bladeSeed, 100) / 100.0) * Math.PI * 2;
            double sway = windAngle + Math.sin(gp.animationFrame * 0.12 + phase) * 1.5;

            int tipX = bx + (int) Math.round(sway);
            int tipY = by - bladeHeight;

            g2.setColor(((col + row + i) % 2 == 0) ? GRASS_BLADE_SHADOW : GRASS_BLADE_HIGHLIGHT);
            g2.drawLine(bx, by, tipX, tipY);
        }
    }
}