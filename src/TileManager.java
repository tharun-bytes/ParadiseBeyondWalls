import java.awt.Color;
import java.awt.Graphics2D;

public class TileManager {

    GamePanel gp;
    public Tile[] tile;
    public int[][] mapTileNum;

    public TileManager(GamePanel gp) {
        this.gp = gp;

        tile = new Tile[10];
        mapTileNum = new int[gp.maxWorldCol][gp.maxWorldRow];

        getTileTypes();
        createWallMariaMap();
    }

    public void getTileTypes() {
        // Tile 0: Walkable Grass/Farmland (Wall Maria)
        tile[0] = new Tile();
        tile[0].color = new Color(34, 139, 34); // Forest Green
        tile[0].collision = false;

        // Tile 1: Giant Outer Wall Barrier
        tile[1] = new Tile();
        tile[1].color = new Color(100, 100, 100); // Stone Gray
        tile[1].collision = true;

        // Tile 2: Ruined Barn / Obstacle
        tile[2] = new Tile();
        tile[2].color = new Color(139, 69, 19); // Wood Brown
        tile[2].collision = true;
    }

    public void createWallMariaMap() {
        // 50x50 Wall Maria Map
        for (int col = 0; col < gp.maxWorldCol; col++) {
            for (int row = 0; row < gp.maxWorldRow; row++) {

                // Border Walls
                if (col == 0 || col == gp.maxWorldCol - 1 || row == 0 || row == gp.maxWorldRow - 1) {
                    mapTileNum[col][row] = 1;
                }
                // Cluster of ruined buildings near center
                else if ((col >= 20 && col <= 22) && (row >= 20 && row <= 22)) {
                    mapTileNum[col][row] = 2;
                }
                // Open fields
                else {
                    mapTileNum[col][row] = 0;
                }
            }
        }
    }

    public void draw(Graphics2D g2) {
        for (int worldCol = 0; worldCol < gp.maxWorldCol; worldCol++) {
            for (int worldRow = 0; worldRow < gp.maxWorldRow; worldRow++) {

                int tileNum = mapTileNum[worldCol][worldRow];

                int worldX = worldCol * gp.tileSize;
                int worldY = worldRow * gp.tileSize;

                // Screen position relative to centered player
                int screenX = worldX - gp.playerX + gp.playerScreenX;
                int screenY = worldY - gp.playerY + gp.playerScreenY;

                // Only draw visible tiles
                if (worldX + gp.tileSize > gp.playerX - gp.playerScreenX &&
                        worldX - gp.tileSize < gp.playerX + gp.playerScreenX &&
                        worldY + gp.tileSize > gp.playerY - gp.playerScreenY &&
                        worldY - gp.tileSize < gp.playerY + gp.playerScreenY) {

                    g2.setColor(tile[tileNum].color);
                    g2.fillRect(screenX, screenY, gp.tileSize, gp.tileSize);

                    // Grid lines
                    g2.setColor(new Color(0, 0, 0, 40));
                    g2.drawRect(screenX, screenY, gp.tileSize, gp.tileSize);
                }
            }
        }
    }
}