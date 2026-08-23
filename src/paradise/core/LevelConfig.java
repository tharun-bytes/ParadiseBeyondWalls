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
                        // Guaranteed wide open courtyards (no walls or buildings):
                        new int[][]{{15, 12}, {30, 12}, {15, 28}, {30, 28}, {24, 20}},
                        new int[][]{{18, 16}, {28, 24}, {20, 32}}
                );
            case 2:
                return new LevelConfig(
                        "WALL ROSE // INNER DEFENSE LINE",
                        "LEVEL 02 • THE BREACHED DISTRICT",
                        3,
                        new int[][]{{12, 14}, {38, 14}, {12, 28}, {38, 28}, {25, 16}, {25, 32}},
                        new int[][]{{16, 16}, {32, 16}, {16, 32}, {32, 32}}
                );
            case 3:
                return new LevelConfig(
                        "WALL SINA // THE ROYAL CITADEL",
                        "LEVEL 03 • THE SANCTUARY GATE",
                        4,
                        new int[][]{{14, 14}, {34, 14}, {14, 34}, {34, 34}, {24, 24}, {24, 12}, {24, 36}},
                        new int[][]{{18, 18}, {30, 18}, {18, 30}, {30, 30}, {24, 16}}
                );
            default:
                return forNumber(1);
        }
    }
}