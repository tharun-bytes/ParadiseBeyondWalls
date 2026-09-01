package paradise.core;

public class LevelConfig {
    public final String levelName;
    public final String subTitle;
    public final int worldCols;
    public final int worldRows;
    public final double wallRadius;
    public final int[][] pointTiles;
    public final int[][] ghostTiles;
    public final int ghostSpeed;
    public final boolean hasSword;
    public final BossData boss;
    public final NpcData npc;

    public static class BossData {
        public final String name;
        public final int hp;
        public final int speed;
        public final int damage;
        public final int spawnCol;
        public final int spawnRow;

        public BossData(String name, int hp, int speed, int damage, int spawnCol, int spawnRow) {
            this.name = name;
            this.hp = hp;
            this.speed = speed;
            this.damage = damage;
            this.spawnCol = spawnCol;
            this.spawnRow = spawnRow;
        }
    }

    public static class NpcData {
        public final String name;
        public final int col;
        public final int row;
        public final String[] lines;

        public NpcData(String name, int col, int row, String... lines) {
            this.name = name;
            this.col = col;
            this.row = row;
            this.lines = lines;
        }
    }

    public LevelConfig(String levelName, String subTitle, int worldCols, int worldRows, double wallRadius,
                       int[][] pointTiles, int[][] ghostTiles, int ghostSpeed,
                       boolean hasSword, BossData boss, NpcData npc) {
        this.levelName = levelName;
        this.subTitle = subTitle;
        this.worldCols = worldCols;
        this.worldRows = worldRows;
        this.wallRadius = wallRadius;
        this.pointTiles = pointTiles;
        this.ghostTiles = ghostTiles;
        this.ghostSpeed = ghostSpeed;
        this.hasSword = hasSword;
        this.boss = boss;
        this.npc = npc;
    }

    public static LevelConfig forNumber(int level) {
        switch (level) {
            case 1:
                return new LevelConfig(
                        "PARADISE // BEYOND WALLS",
                        "LEVEL 01 • THE FALLEN COURTYARD",
                        50, 50, 20.0,
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
                        2,
                        false,
                        null,
                        new NpcData(
                                "Old Scout", 22, 20,
                                "You're inside Wall Maria's courtyard. Press W/A/S/D to move.",
                                "TIP: Press [E] near a house to HIDE from the wraiths.",
                                "Grab the 5 glowing capture points, then the WEST gate will open.",
                                "Rest at a house when your heart beats fast. Survive, soldier."
                        )
                );

            case 2:
                return new LevelConfig(
                        "PARADISE // WALL ROSE",
                        "LEVEL 02 • THE SHADOW PLAZA",
                        60, 60, 24.0,
                        new int[][]{
                                {30, 19},
                                {19, 30},
                                {41, 30},
                                {30, 41},
                                {22, 22},
                                {38, 38},
                                {30, 30}
                        },
                        new int[][]{
                                {18, 18},
                                {42, 18},
                                {26, 42},
                                {36, 36}
                        },
                        3,
                        true,
                        new BossData("THE COLOSSAL TITAN", 40, 2, 1, 36, 22),
                        new NpcData(
                                "Scout Captain", 24, 24,
                                "Soldier! Welcome to Wall Rose — this courtyard is three times larger.",
                                "You now carry a BLADE. Press [J] to SWING it and cut down the wraiths.",
                                "TIP: Keep [Shift] ready to DASH, and [E] to hide when the hunt closes in.",
                                "Capture all 7 glowing points — the Colossal will prowl the dark courtyard.",
                                "WARN: The COLOSSAL TITAN guards the WEST gate and takes many hits.",
                                "Slay it with your sword, then step through the WEST gate to Wall Sina."
                        )
                );

            case 3:
            default:
                return new LevelConfig(
                        "PARADISE // WALL SINA",
                        "LEVEL 03 • THE INNER CITADEL",
                        60, 60, 26.0,
                        new int[][]{
                                {30, 19},
                                {20, 24},
                                {40, 24},
                                {20, 36},
                                {40, 36},
                                {30, 43},
                                {30, 30}
                        },
                        new int[][]{
                                {18, 18},
                                {42, 18},
                                {18, 42},
                                {42, 42}
                        },
                        4,
                        true,
                        null,
                        new NpcData(
                                "Veteran Guard", 26, 27,
                                "Wall Sina at last... the final ring before true freedom.",
                                "Capture all 7 points to open the WEST gate.",
                                "These wraiths are the swiftest yet — keep moving, keep swinging.",
                                "Once you step through, this nightmare ends. Finish it."
                        )
                );
        }
    }
}