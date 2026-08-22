package paradise.core;

/** Immutable content for one playable level. Coordinates are map tiles. */
public final class LevelConfig {
    public final int number;
    public final String title;
    public final int ghostSpeed;
    public final int[][] pointTiles;
    public final int[][] ghostTiles;

    private LevelConfig(int number, String title, int ghostSpeed, int[][] pointTiles, int[][] ghostTiles) {
        this.number = number;
        this.title = title;
        this.ghostSpeed = ghostSpeed;
        this.pointTiles = pointTiles;
        this.ghostTiles = ghostTiles;
    }

    public static LevelConfig forNumber(int level) {
        switch (level) {
            case 1:
                return new LevelConfig(1, "The Fallen Courtyard", 2,
                        new int[][] {{18, 18}, {27, 22}, {18, 27}, {32, 31}, {22, 34}},
                        new int[][] {{15, 15}, {35, 20}});
            case 2:
                return new LevelConfig(2, "The Shattered District", 3,
                        new int[][] {{11, 18}, {23, 10}, {32, 23}, {37, 33}, {22, 32}, {12, 36}, {28, 40}},
                        new int[][] {{14, 19}, {37, 17}, {21, 39}});
            case 3:
                return new LevelConfig(3, "Beyond the Last Wall", 4,
                        new int[][] {{9, 16}, {17, 11}, {25, 14}, {29, 23}, {40, 26}, {29, 35}, {10, 38}, {24, 39}, {39, 40}, {18, 45}},
                        new int[][] {{14, 14}, {38, 14}, {15, 35}, {38, 39}});
            default:
                throw new IllegalArgumentException("Unknown level: " + level);
        }
    }
}
