package paradise.core;

public class LevelConfig {
    public final String levelName;
    public final String subTitle;
    public final int ghostSpeed;
    public final int[][] pointTiles;
    public final int[][] ghostTiles;

    public LevelConfig(String levelName, String subTitle, int ghostSpeed, int[][] pointTiles, int[][] ghostTiles) {
        this.levelName = levelName;
        this.subTitle = subTitle;
        this.ghostSpeed = ghostSpeed;
        this.pointTiles = pointTiles;
        this.ghostTiles = ghostTiles;
    }

    public static LevelConfig forNumber(int level) {
        switch (level) {
            case 1:
                return new LevelConfig(
                        "PARADISE // BEYOND WALLS",
                        "LEVEL 01 • THE FALLEN COURTYARD",
                        2,
                        // Clean open coordinates away from building hitboxes:
                        new int[][]{{14, 10}, {25, 14}, {15, 30}, {38, 16}, {30, 36}},
                        new int[][]{{20, 15}, {15, 25}, {30, 20}}
                );
            case 2:
                return new LevelConfig(
                        "WALL ROSE // INNER DEFENSE LINE",
                        "LEVEL 02 • THE BREACHED DISTRICT",
                        3,
                        // Open pathway coordinates:
                        new int[][]{{12, 12}, {25, 10}, {40, 15}, {14, 38}, {32, 28}, {22, 38}},
                        new int[][]{{12, 18}, {25, 8}, {38, 25}, {18, 32}}
                );
            case 3:
                return new LevelConfig(
                        "WALL SINA // THE ROYAL CITADEL",
                        "LEVEL 03 • THE SANCTUARY GATE",
                        4,
                        // Strategic central courtyard spots:
                        new int[][]{{16, 16}, {32, 16}, {16, 32}, {32, 32}, {24, 24}, {24, 10}, {24, 40}},
                        new int[][]{{10, 12}, {40, 12}, {10, 38}, {40, 38}, {25, 18}}
                );
            default:
                return forNumber(1);
        }
    }
}