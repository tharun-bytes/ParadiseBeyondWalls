package paradise.core;

public class LevelConfig {
    public final String levelName;
    public final String subTitle;
    public final int worldCols;
    public final int worldRows;
    public final double wallRadius;
    public final int[][] pointTiles;
    public final int[][] ghostTiles;
    public final int[][] monsterSpawns; // Replaces the single boss
    public final int ghostSpeed;
    public final boolean hasSword;
    public final NpcData npc;

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
                       int[][] pointTiles, int[][] ghostTiles, int[][] monsterSpawns, int ghostSpeed,
                       boolean hasSword, NpcData npc) {
        this.levelName = levelName;
        this.subTitle = subTitle;
        this.worldCols = worldCols;
        this.worldRows = worldRows;
        this.wallRadius = wallRadius;
        this.pointTiles = pointTiles;
        this.ghostTiles = ghostTiles;
        this.monsterSpawns = monsterSpawns;
        this.ghostSpeed = ghostSpeed;
        this.hasSword = hasSword;
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
                                {25, 12}, {18, 25}, {32, 25}, {25, 38}, {25, 25}
                        },
                        new int[][]{
                                {20, 15}, {30, 35} // Ghosts kept for the stealth tutorial
                        },
                        new int[][]{}, // No blood monsters in level 1
                        2,
                        false,
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
                        "LEVEL 02 • THE BLOOD HORDE",
                        60, 60, 24.0,
                        new int[][]{
                                {30, 19}, {19, 30}, {41, 30}, {30, 41}, {22, 22}, {38, 38}, {30, 30}
                        },
                        new int[][]{}, // Ghosts completely removed from Level 2
                        new int[][]{
                                // 7 Blood Monsters spawned across the plaza
                                {20, 20}, {40, 40}, {20, 40}, {40, 20}, {30, 25}, {25, 35}, {35, 35}
                        },
                        0,
                        true,
                        new NpcData(
                                "Scout Captain", 24, 24,
                                "Soldier! Welcome to Wall Rose — this courtyard is crawling with Blood Monsters.",
                                "You now carry a BLADE. Press [J] to SWING it and cut them down.",
                                "TIP: Keep [Shift] ready to DASH, and swing carefully.",
                                "Capture all 7 glowing points to unlock the WEST gate.",
                                "Slay the horde, then step through the gate to Wall Sina."
                        )
                );

            case 3:
            default:
                return new LevelConfig(
                        "PARADISE // WALL SINA",
                        "LEVEL 03 • THE INNER CITADEL",
                        60, 60, 26.0,
                        new int[][]{
                                {30, 19}, {20, 24}, {40, 24}, {20, 36}, {40, 36}, {30, 43}, {30, 30}
                        },
                        new int[][]{}, // Ghosts completely removed from Level 3
                        new int[][]{
                                // 10 Blood Monsters for a harder final level
                                {20, 20}, {40, 40}, {20, 40}, {40, 20}, {30, 30},
                                {25, 25}, {35, 35}, {25, 35}, {35, 25}, {30, 45}
                        },
                        0,
                        true,
                        new NpcData(
                                "Veteran Guard", 26, 27,
                                "Wall Sina at last... the final ring before true freedom.",
                                "Capture all 7 points to open the WEST gate.",
                                "The Blood Monsters are swarming. Keep moving, keep swinging.",
                                "Once you step through, this nightmare ends. Finish it."
                        )
                );
        }
    }
}