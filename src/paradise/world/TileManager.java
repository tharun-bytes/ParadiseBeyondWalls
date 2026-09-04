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
    public static final int TILE_GATE_DOOR = 6;
    public static final int TILE_EXIT_GATE_DOOR = 7;

    // Geometry of the entrance gate (east side, player spawn), computed per level
    // so GamePanel can check proximity and CollisionChecker can gate movement.
    public int gateCol0, gateCol1;
    public int gateDoorRowStart, gateDoorRowEnd;

    // Geometry of the exit gate (west side, level clear), mirrors the entrance gate.
    public int exitGateCol0, exitGateCol1;
    public int exitGateDoorRowStart, exitGateDoorRowEnd;

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

        // Entrance gate: a real closed gate spanning the wall ring on the east corridor,
        // flanked by pillars. It blocks the way in until the player opens it (see GamePanel).
        gateCol0 = eastStart;
        gateCol1 = eastStart + 1;
        gateDoorRowStart = centerY - 1;
        gateDoorRowEnd = centerY + 1;
        for (int col = gateCol0; col <= gateCol1; col++) {
            for (int row = centerY - 4; row <= centerY + 3; row++) {
                if (col < 0 || col >= cols || row < 0 || row >= rows) continue;
                if (row >= gateDoorRowStart && row <= gateDoorRowEnd) {
                    mapTileNum[col][row] = TILE_GATE_DOOR;
                } else {
                    mapTileNum[col][row] = TILE_GATE_PILLAR;
                }
            }
        }

        // Exit gate: mirrors the entrance gate on the west corridor. It stays closed
        // until the level's objective is cleared and the player opens it with [E].
        exitGateCol0 = westStart - 1;
        exitGateCol1 = westStart;
        exitGateDoorRowStart = centerY - 1;
        exitGateDoorRowEnd = centerY + 1;
        for (int col = exitGateCol0; col <= exitGateCol1; col++) {
            for (int row = centerY - 4; row <= centerY + 3; row++) {
                if (col < 0 || col >= cols || row < 0 || row >= rows) continue;
                if (row >= exitGateDoorRowStart && row <= exitGateDoorRowEnd) {
                    mapTileNum[col][row] = TILE_EXIT_GATE_DOOR;
                } else {
                    mapTileNum[col][row] = TILE_GATE_PILLAR;
                }
            }
        }
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
                        g2.setColor(new Color(158, 162, 171));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(138, 142, 150));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 1, screenY + 1, gp.tileSize - 2, gp.tileSize - 2);
                        g2.setColor(new Color(180, 184, 192));
                        g2.drawLine(screenX + 4, screenY + 16, screenX + gp.tileSize - 4, screenY + 16);
                        g2.drawLine(screenX + 4, screenY + 32, screenX + gp.tileSize - 4, screenY + 32);
                        break;

                    case TILE_GATE_PILLAR:
                        // Reinforced stone pillar flanking the gate — darker and riveted,
                        // so the gate reads as a distinct structure rather than plain wall.
                        g2.setColor(new Color(96, 98, 108));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);
                        g2.setColor(new Color(66, 68, 78));
                        g2.setStroke(new BasicStroke(2));
                        g2.drawRect(screenX + 2, screenY + 2, gp.tileSize - 4, gp.tileSize - 4);
                        g2.setColor(new Color(150, 130, 60));
                        g2.fillOval(screenX + gp.tileSize / 2 - 3, screenY + 9, 6, 6);
                        g2.fillOval(screenX + gp.tileSize / 2 - 3, screenY + gp.tileSize - 15, 6, 6);
                        break;

                    case TILE_GATE_DOOR: {
                        // Floor beneath the gate — sand, matching the rest of the corridor.
                        if ((col + row) % 2 == 0) g2.setColor(new Color(238, 221, 173));
                        else g2.setColor(new Color(230, 210, 160));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);

                        // Metal gate slab. It rises out of view as gp.entranceGateProgress
                        // goes from 0 (closed) to 1 (fully open), like a portcullis lifting.
                        double progress = Math.max(0.0, Math.min(1.0, gp.entranceGateProgress));
                        int doorHeight = (int) Math.round(gp.tileSize * (1.0 - progress));
                        if (doorHeight > 0) {
                            g2.setColor(new Color(72, 68, 60));
                            g2.fillRect(screenX, screenY, gp.tileSize, doorHeight);

                            g2.setColor(new Color(112, 104, 88));
                            g2.setStroke(new BasicStroke(2));
                            for (int lx = screenX + 6; lx < screenX + gp.tileSize; lx += 10) {
                                g2.drawLine(lx, screenY, lx, screenY + doorHeight);
                            }

                            if (doorHeight > 10) {
                                g2.setColor(new Color(150, 130, 60));
                                g2.fillOval(screenX + gp.tileSize / 2 - 3, screenY + doorHeight - 10, 6, 6);
                            }
                        }
                        break;
                    }

                    case TILE_EXIT_GATE_DOOR: {
                        // Same look as the entrance gate, but driven by gp.exitGateProgress —
                        // this one only opens once the level's objective is cleared.
                        if ((col + row) % 2 == 0) g2.setColor(new Color(238, 221, 173));
                        else g2.setColor(new Color(230, 210, 160));
                        g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);

                        double exitProgress = Math.max(0.0, Math.min(1.0, gp.exitGateProgress));
                        int exitDoorHeight = (int) Math.round(gp.tileSize * (1.0 - exitProgress));
                        if (exitDoorHeight > 0) {
                            g2.setColor(new Color(72, 68, 60));
                            g2.fillRect(screenX, screenY, gp.tileSize, exitDoorHeight);

                            g2.setColor(new Color(112, 104, 88));
                            g2.setStroke(new BasicStroke(2));
                            for (int lx = screenX + 6; lx < screenX + gp.tileSize; lx += 10) {
                                g2.drawLine(lx, screenY, lx, screenY + exitDoorHeight);
                            }

                            if (exitDoorHeight > 10) {
                                g2.setColor(new Color(150, 130, 60));
                                g2.fillOval(screenX + gp.tileSize / 2 - 3, screenY + exitDoorHeight - 10, 6, 6);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }
}