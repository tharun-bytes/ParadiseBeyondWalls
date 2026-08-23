package paradise.world;

import paradise.core.GamePanel;

public class CollisionChecker {
    private final GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public boolean canMove(int worldX, int worldY, int width, int height, int deltaX, int deltaY) {
        int nextX = worldX + deltaX;
        int nextY = worldY + deltaY;

        if (nextX < 0 || nextX + width > gp.worldWidth || nextY < 0 || nextY + height > gp.worldHeight) {
            return false;
        }

        int leftCol = nextX / gp.tileSize;
        int rightCol = (nextX + width - 1) / gp.tileSize;
        int topRow = nextY / gp.tileSize;
        int bottomRow = (nextY + height - 1) / gp.tileSize;

        if (leftCol < 0 || rightCol >= gp.maxWorldCol || topRow < 0 || bottomRow >= gp.maxWorldRow) {
            return false;
        }

        return isWalkable(leftCol, topRow)
                && isWalkable(rightCol, topRow)
                && isWalkable(leftCol, bottomRow)
                && isWalkable(rightCol, bottomRow);
    }

    private boolean isWalkable(int col, int row) {
        int tileType = gp.tileManager.mapTileNum[col][row];
        // Solid impassable tiles:
        return tileType != TileManager.TILE_WALL
                && tileType != TileManager.TILE_WATER
                && tileType != TileManager.TILE_GATE_PILLAR;
    }
}