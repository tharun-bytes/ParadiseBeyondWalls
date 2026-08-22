public class CollisionChecker {

    GamePanel gp;

    public CollisionChecker(GamePanel gp) {
        this.gp = gp;
    }

    public void checkTile(GamePanel gp) {
        // Hitbox boundaries in world coordinates
        int playerLeftWorldX = gp.playerX + 8;
        int playerRightWorldX = gp.playerX + gp.tileSize - 8;
        int playerTopWorldY = gp.playerY + 16;
        int playerBottomWorldY = gp.playerY + gp.tileSize;

        // Convert coordinates to grid column and row numbers
        int playerLeftCol = playerLeftWorldX / gp.tileSize;
        int playerRightCol = playerRightWorldX / gp.tileSize;
        int playerTopRow = playerTopWorldY / gp.tileSize;
        int playerBottomRow = playerBottomWorldY / gp.tileSize;

        int tileNum1, tileNum2;

        if (gp.keyH.upPressed) {
            int checkTopRow = (playerTopWorldY - gp.playerSpeed) / gp.tileSize;
            tileNum1 = gp.tileM.mapTileNum[playerLeftCol][checkTopRow];
            tileNum2 = gp.tileM.mapTileNum[playerRightCol][checkTopRow];
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.collisionOn = true;
            }
        }
        if (gp.keyH.downPressed) {
            int checkBottomRow = (playerBottomWorldY + gp.playerSpeed) / gp.tileSize;
            tileNum1 = gp.tileM.mapTileNum[playerLeftCol][checkBottomRow];
            tileNum2 = gp.tileM.mapTileNum[playerRightCol][checkBottomRow];
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.collisionOn = true;
            }
        }
        if (gp.keyH.leftPressed) {
            int checkLeftCol = (playerLeftWorldX - gp.playerSpeed) / gp.tileSize;
            tileNum1 = gp.tileM.mapTileNum[checkLeftCol][playerTopRow];
            tileNum2 = gp.tileM.mapTileNum[checkLeftCol][playerBottomRow];
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.collisionOn = true;
            }
        }
        if (gp.keyH.rightPressed) {
            int checkRightCol = (playerRightWorldX + gp.playerSpeed) / gp.tileSize;
            tileNum1 = gp.tileM.mapTileNum[checkRightCol][playerTopRow];
            tileNum2 = gp.tileM.mapTileNum[checkRightCol][playerBottomRow];
            if (gp.tileM.tile[tileNum1].collision || gp.tileM.tile[tileNum2].collision) {
                gp.collisionOn = true;
            }
        }
    }
}