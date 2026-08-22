package paradise.world;

import paradise.core.GamePanel;

import java.awt.Color;
import java.awt.Graphics2D;

/** Generates a different walled arena for every level. */
public class TileManager {
    private static final int GROUND = 0;
    private static final int WALL = 1;

    private final GamePanel game;
    private final Tile[] tiles = {
            new Tile(new Color(31, 79, 76), false),
            new Tile(new Color(53, 63, 81), true)
    };
    private final int[][] mapTiles;

    public TileManager(GamePanel game) {
        this.game = game;
        this.mapTiles = new int[game.maxWorldCol][game.maxWorldRow];
    }

    public void createLevelMap(int level) {
        for (int column = 0; column < game.maxWorldCol; column++) {
            for (int row = 0; row < game.maxWorldRow; row++) {
                mapTiles[column][row] = isBorder(column, row) ? WALL : GROUND;
            }
        }

        switch (level) {
            case 1:
                buildLevelOne();
                break;
            case 2:
                buildLevelTwo();
                break;
            case 3:
                buildLevelThree();
                break;
            default:
                throw new IllegalArgumentException("Unknown level: " + level);
        }
    }

    private boolean isBorder(int column, int row) {
        return column == 0 || row == 0 || column == game.maxWorldCol - 1 || row == game.maxWorldRow - 1;
    }

    private void buildLevelOne() {
        horizontalWall(7, 19, 10, 13);
        verticalWall(33, 8, 19, 15);
        horizontalWall(11, 24, 29, 18);
        verticalWall(20, 34, 43, 39);
    }

    private void buildLevelTwo() {
        horizontalWall(8, 22, 13, 14);
        verticalWall(28, 8, 21, 17);
        horizontalWall(28, 42, 27, 36);
        verticalWall(17, 29, 42, 35);
        horizontalWall(6, 14, 38, 10);
    }

    private void buildLevelThree() {
        horizontalWall(6, 17, 8, 11);
        verticalWall(21, 9, 20, 15);
        horizontalWall(8, 22, 25, 16);
        horizontalWall(27, 42, 31, 34);
        verticalWall(34, 34, 43, 39);
        horizontalWall(7, 25, 41, 19);
    }

    private void horizontalWall(int startColumn, int endColumn, int row, int doorwayColumn) {
        for (int column = startColumn; column <= endColumn; column++) {
            if (column != doorwayColumn) mapTiles[column][row] = WALL;
        }
    }

    private void verticalWall(int column, int startRow, int endRow, int doorwayRow) {
        for (int row = startRow; row <= endRow; row++) {
            if (row != doorwayRow) mapTiles[column][row] = WALL;
        }
    }

    public boolean isBlocked(int column, int row) {
        return mapTiles[column][row] == WALL;
    }

    public void draw(Graphics2D graphics) {
        for (int column = 0; column < game.maxWorldCol; column++) {
            for (int row = 0; row < game.maxWorldRow; row++) {
                int worldX = column * game.tileSize;
                int worldY = row * game.tileSize;
                if (!game.isOnScreen(worldX, worldY, game.tileSize, game.tileSize)) continue;

                int screenX = worldX - game.playerX + game.playerScreenX;
                int screenY = worldY - game.playerY + game.playerScreenY;
                if (mapTiles[column][row] == WALL) {
                    drawWall(graphics, screenX, screenY);
                } else {
                    drawGround(graphics, screenX, screenY, column, row);
                }
            }
        }
    }

    private void drawGround(Graphics2D graphics, int x, int y, int column, int row) {
        int variation = Math.floorMod(column * 13 + row * 7, 3);
        Color base;
        switch (variation) {
            case 0:
                base = new Color(31, 79, 76);
                break;
            case 1:
                base = new Color(34, 86, 80);
                break;
            default:
                base = new Color(27, 72, 71);
                break;
        }
        graphics.setColor(base);
        graphics.fillRect(x, y, game.tileSize, game.tileSize);
        graphics.setColor(new Color(118, 187, 142, 35));
        graphics.fillOval(x + 8, y + 10, 4, 4);
        graphics.fillOval(x + 31, y + 28, 3, 3);
        graphics.setColor(new Color(0, 0, 0, 20));
        graphics.drawRect(x, y, game.tileSize, game.tileSize);
    }

    private void drawWall(Graphics2D graphics, int x, int y) {
        graphics.setColor(tiles[WALL].color);
        graphics.fillRect(x, y, game.tileSize, game.tileSize);
        graphics.setColor(new Color(121, 139, 160));
        graphics.fillRect(x + 3, y + 3, game.tileSize - 6, 5);
        graphics.setColor(new Color(25, 31, 44));
        graphics.drawRect(x, y, game.tileSize - 1, game.tileSize - 1);
        graphics.drawLine(x, y + game.tileSize / 2, x + game.tileSize, y + game.tileSize / 2);
        graphics.drawLine(x + game.tileSize / 2, y, x + game.tileSize / 2, y + game.tileSize / 2);
        graphics.drawLine(x + game.tileSize / 4, y + game.tileSize / 2, x + game.tileSize / 4, y + game.tileSize);
        graphics.drawLine(x + game.tileSize * 3 / 4, y + game.tileSize / 2, x + game.tileSize * 3 / 4, y + game.tileSize);
    }
}
