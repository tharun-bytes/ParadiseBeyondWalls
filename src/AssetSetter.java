public class AssetSetter {

    GamePanel gp;

    public AssetSetter(GamePanel gp) {
        this.gp = gp;
    }

    public void setObject() {
        // Place 5 Scrap Metal pieces around Wall Maria
        gp.obj[0] = new OBJ_Scrap();
        gp.obj[0].worldX = 23 * gp.tileSize;
        gp.obj[0].worldY = 18 * gp.tileSize;

        gp.obj[1] = new OBJ_Scrap();
        gp.obj[1].worldX = 27 * gp.tileSize;
        gp.obj[1].worldY = 22 * gp.tileSize;

        gp.obj[2] = new OBJ_Scrap();
        gp.obj[2].worldX = 18 * gp.tileSize;
        gp.obj[2].worldY = 25 * gp.tileSize;

        gp.obj[3] = new OBJ_Scrap();
        gp.obj[3].worldX = 30 * gp.tileSize;
        gp.obj[3].worldY = 28 * gp.tileSize;

        gp.obj[4] = new OBJ_Scrap();
        gp.obj[4].worldX = 22 * gp.tileSize;
        gp.obj[4].worldY = 32 * gp.tileSize;
    }

    public void setTitans() {
        // Spawn 3 Pure Titans in Wall Maria
        gp.titans[0] = new Titan(gp, 15, 15);
        gp.titans[1] = new Titan(gp, 33, 20);
        gp.titans[2] = new Titan(gp, 20, 35);
    }
}