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
                        // Spread wide across the map, but safely dodging the buildings!
                        new int[][] {{15, 18}, {32, 18}, {25, 44}, {18, 32}, {32, 32}},
                        new int[][] {{12, 12}, {38, 38}});
            case 2:
                return new LevelConfig(2, "The Shattered District", 3,
                        // 7 points scattered in a wide ring
                        new int[][] {{16, 16}, {33, 16}, {25, 18}, {18, 25}, {32, 25}, {16, 38}, {33, 38}},
                        new int[][] {{10, 10}, {40, 10}, {25, 40}});
            case 3:
                return new LevelConfig(3, "Beyond the Last Wall", 4,
                        // 10 points pushed to the absolute edges of the map
                        new int[][] {{14, 18}, {20, 18}, {28, 18}, {35, 18}, {14, 32}, {35, 32}, {18, 42}, {25, 42}, {32, 42}, {25, 25}},
                        new int[][] {{12, 12}, {38, 12}, {12, 38}, {38, 38}});
            default:
                throw new IllegalArgumentException("Unknown level: " + level);
        }
    }
}