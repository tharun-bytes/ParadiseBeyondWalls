package paradise.core;

public class LevelConfig {
    public final String levelName;
    public final String subTitle;
    public final int[][] pointTiles;
    public final int[][] ghostTiles;
    public final int ghostSpeed;

    public LevelConfig(String levelName, String subTitle, int[][] pointTiles, int[][] ghostTiles, int ghostSpeed) {
        this.levelName = levelName;
        this.subTitle = subTitle;
        this.pointTiles = pointTiles;
        this.ghostTiles = ghostTiles;
        this.ghostSpeed = ghostSpeed;
    }

    public static LevelConfig forNumber(int level) {
        switch (level) {
            case 1:
                return new LevelConfig(
                        "PARADISE // BEYOND WALLS",
                        "LEVEL 01 • THE FALLEN COURTYARD",
                        new int[][]{
                                {25, 12}, // Center North Clearing
                                {18, 25}, // Center West Clearing
                                {32, 25}, // Center East Clearing
                                {25, 38}, // Center South Clearing
                                {25, 25}  // Central Plaza
                        },
                        new int[][]{
                                {20, 15},
                                {30, 35}
                        },
                        2
                );

            case 2:
                return new LevelConfig(
                        "PARADISE // WALL ROSE",
                        "LEVEL 02 • THE SHADOW PLAZA",
                        new int[][]{
                                {22, 12},
                                {28, 12},
                                {18, 25},
                                {32, 25},
                                {22, 38},
                                {28, 38}
                        },
                        new int[][]{
                                {18, 18},
                                {32, 18},
                                {25, 32}
                        },
                        3
                );

            case 3:
            default:
                return new LevelConfig(
                        "PARADISE // WALL SINA",
                        "LEVEL 03 • THE INNER CITADEL",
                        new int[][]{
                                {25, 10},
                                {16, 20},
                                {34, 20},
                                {16, 30},
                                {34, 30},
                                {25, 40},
                                {25, 25}
                        },
                        new int[][]{
                                {18, 15},
                                {32, 15},
                                {18, 35},
                                {32, 35}
                        },
                        4
                );
        }
    }
}