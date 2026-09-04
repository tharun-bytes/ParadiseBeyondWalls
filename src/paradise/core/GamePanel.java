package paradise.core;

import paradise.effects.Fireflies;
import paradise.entity.Boss;
import paradise.entity.Ghost;
import paradise.entity.NPC;
import paradise.entity.ThrownBlade;
import paradise.input.KeyHandler;
import paradise.object.Building;
import paradise.object.CapturePoint;
import paradise.object.HealthPickup;
import paradise.object.Ruins;
import paradise.object.Tree;
import paradise.ui.UI;
import paradise.world.CollisionChecker;
import paradise.world.TileManager;

import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class GamePanel extends JPanel implements Runnable {
    public static final int LEVEL_COUNT = 3;

    public final int tileSize = 48;
    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;
    public final int screenHeight = tileSize * maxScreenRow;
    public int maxWorldCol = 50;
    public int maxWorldRow = 50;
    public int worldWidth = tileSize * 50;
    public int worldHeight = tileSize * 50;
    public final int playerScreenX = screenWidth / 2 - tileSize / 2;
    public final int playerScreenY = screenHeight / 2 - tileSize / 2;

    public final int playerSize = 28;
    public int characterScale = 2;

    public final int maxStamina = 100;
    public double currentStamina = 100;
    public boolean isDashing = false;

    public boolean isHiding = false;
    public Building currentNearbyBuilding = null;

    public final KeyHandler keyHandler = new KeyHandler();
    public final TileManager tileManager = new TileManager(this);
    public final CollisionChecker collisionChecker = new CollisionChecker(this);
    public final UI ui = new UI(this);
    public final Sound sound = new Sound();
    public final Sound se = new Sound();

    public final Fireflies fireflies = new Fireflies(this);

    public int playerX;
    public int playerY;
    public final int baseSpeed = 4;
    public int playerHealth;
    public final int maxHealth = 100;

    public String direction = "down";
    public BufferedImage[] runDown = new BufferedImage[8];
    public BufferedImage[] runLeft = new BufferedImage[8];
    public BufferedImage[] runRight = new BufferedImage[8];
    public BufferedImage[] runUp = new BufferedImage[8];

    public boolean hasSword;
    public int attackCooldown;
    public java.util.ArrayList<ThrownBlade> thrownBlades = new java.util.ArrayList<>();

    public static final int THROW_COOLDOWN = 25;
    private static final int BLADE_SPEED = 9;
    private static final int BLADE_DISTANCE = 6 * 48;
    public static final int DASH_STRIKE_DAMAGE = 1;
    private boolean dashStrikeDone = false;

    public int currentLevel;
    public int capturedPoints;
    public int animationFrame;
    public GameState gameState;
    public LevelConfig levelConfig;
    public CapturePoint[] capturePoints;
    public Ghost[] ghosts;

    public Boss[] monsters;
    public NPC npc;
    public java.util.ArrayList<HealthPickup> healthPickups = new java.util.ArrayList<>();

    public Tree[] mapTrees;
    public Building[] mapBuildings;
    public Ruins[] mapRuins;
    private java.util.ArrayList<int[]> ruinCaptureTiles = new java.util.ArrayList<>();

    public boolean dialogueActive;
    public int dialogueIndex;
    public String statusMessage = "";
    public int statusMessageTimer;
    public boolean doorLocked;
    // Entrance gate (east side, player spawn): closed on arrival, opens when the
    // player stands near it and presses [E], with a short rise animation + sound.
    public boolean entranceGateOpen = false;
    public boolean entranceGateOpening = false;
    public double entranceGateProgress = 0.0;
    public boolean nearEntranceGate = false;

    // Exit gate (west side, level clear): same idea as the entrance gate, but it
    // only responds to [E] once the level's objective (points + monsters) is done.
    public boolean exitGateOpen = false;
    public boolean exitGateOpening = false;
    public double exitGateProgress = 0.0;
    public boolean nearExitGate = false;

    private static final int GATE_OPEN_DURATION = 75;

    // New: score, player name, NPC intro lock
    public int score = 0;
    public String playerName = "";
    public boolean npcIntroDone = false;
    public boolean enteringName = false;

    // Wave system (used for level 1's 3 waves of enemies)
    public int currentWave = 0;
    public boolean waveSpawned = false;

    private static final int FPS = 60;
    private static final int TRANSITION_DURATION = 95;
    private Thread gameThread;
    private int hitCooldown;
    public int transitionTimer;
    public final String[] pauseMenuOptions = {"Resume", "Restart", "Quit"};
    public int pauseMenuIndex = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(new Color(135, 206, 235));
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);

        loadPlayerImages();
        restartGame();
        playMusic(0);
    }

    private void loadPlayerImages() {
        runDown = loadSpriteSheet("src/paradise/entity/run1.png", 8);
        runLeft = loadSpriteSheet("src/paradise/entity/run2.png", 8);
        runRight = loadSpriteSheet("src/paradise/entity/run3.png", 8);
        runUp = loadSpriteSheet("src/paradise/entity/run4.png", 8);
    }

    private BufferedImage[] loadSpriteSheet(String path, int frames) {
        BufferedImage[] sprites = new BufferedImage[frames];
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            int width = sheet.getWidth() / frames;
            int height = sheet.getHeight();
            for (int i = 0; i < frames; i++) {
                sprites[i] = sheet.getSubimage(i * width, 0, width, height);
            }
        } catch (IOException e) {
            System.out.println("Error loading: " + path);
        }
        return sprites;
    }

    public void playMusic(int i) {
        sound.setFile(i);
        sound.play();
        sound.loop();
    }

    public void stopMusic() {
        sound.stop();
    }

    public void playSE(int i) {
        se.setFile(i);
        se.play();
    }

    private final int[][] GREEN_MODELS = {{0, 64}, {32, 64}, {128, 64}, {160, 64}, {192, 64}};
    private final int[][] SNOW_MODELS = {{96, 224}, {128, 224}, {160, 224}};

    private boolean isNearBossSpawn(int col, int row) {
        if (levelConfig == null || levelConfig.monsterSpawns == null) return false;
        for (int[] spawn : levelConfig.monsterSpawns) {
            if (Math.hypot(col - spawn[0], row - spawn[1]) < 4) return true;
        }
        return false;
    }

    private void setupBuildings(int level) {
        if (level == 1) {
            mapBuildings = new Building[7];
            mapBuildings[0] = new Building(21 * tileSize, 10 * tileSize, 1);
            mapBuildings[1] = new Building(32 * tileSize, 14 * tileSize, 2);
            mapBuildings[2] = new Building(36 * tileSize, 24 * tileSize, 3);
            mapBuildings[3] = new Building(30 * tileSize, 34 * tileSize, 4);
            mapBuildings[4] = new Building(18 * tileSize, 34 * tileSize, 5);
            mapBuildings[5] = new Building(8 * tileSize, 26 * tileSize, 6);
            mapBuildings[6] = new Building(12 * tileSize, 14 * tileSize, 7);
            return;
        }

        int centerCol = levelConfig.worldCols / 2;
        int centerRow = levelConfig.worldRows / 2;
        int ringRadius = (int) Math.round(levelConfig.wallRadius * 0.5);
        int houses = (level == 2) ? 10 : 12;
        ArrayList<Building> list = new ArrayList<>();
        for (int i = 0; i < houses; i++) {
            double angle = 2 * Math.PI * i / houses - Math.PI / 2;
            int col = centerCol + (int) Math.round(Math.cos(angle) * ringRadius);
            int row = centerRow + (int) Math.round(Math.sin(angle) * ringRadius);
            if (isNearBossSpawn(col, row)) continue;
            list.add(new Building(col * tileSize, row * tileSize, 1 + (i % 7)));
        }
        mapBuildings = list.toArray(new Building[0]);
    }

    private void setupTrees(int level) {
        ArrayList<Tree> treeList = new ArrayList<>();
        boolean[][] occupied = new boolean[maxWorldCol][maxWorldRow];
        Random rand = new Random(42 + level);

        class ForestHelper {
            boolean inCorridor(int col, int row) {
                int centerY = levelConfig.worldRows / 2;
                if (row < centerY - 4 || row > centerY + 3) return false;
                int west = (int) (levelConfig.worldCols / 2 - levelConfig.wallRadius);
                int east = (int) (levelConfig.worldCols / 2 + levelConfig.wallRadius);
                return (col >= west - 4 && col <= west + 6) || (col >= east - 6 && col <= east + 8);
            }

            void plant(int col, int row, int[][] pool) {
                if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow || occupied[col][row]) return;
                if (inCorridor(col, row)) return;
                if (isNearBossSpawn(col, row)) return;

                int treeX = col * tileSize;
                int treeY = row * tileSize;
                Rectangle tBox = new Rectangle(treeX, treeY, tileSize, tileSize);
                if (mapBuildings != null) {
                    for (Building b : mapBuildings) {
                        if (b != null && new Rectangle(b.worldX, b.worldY, b.drawWidth, b.drawHeight).intersects(tBox)) return;
                    }
                }
                occupied[col][row] = true;
                int[] crop = pool[rand.nextInt(pool.length)];
                treeList.add(new Tree(treeX, treeY, crop[0], crop[1]));
            }

            void plantCluster(int sC, int eC, int sR, int eR, double d, int[][] pool) {
                for (int c = sC; c <= eC; c++) {
                    for (int r = sR; r <= eR; r++) {
                        if (rand.nextDouble() < d) plant(c, r, pool);
                    }
                }
            }

            void plantRing(double r0, double r1, double density, int[][] pool) {
                int centerCol = levelConfig.worldCols / 2;
                int centerRow = levelConfig.worldRows / 2;
                for (int c = 1; c < maxWorldCol - 1; c++) {
                    for (int r = 1; r < maxWorldRow - 1; r++) {
                        double dist = Math.hypot(c - centerCol, r - centerRow);
                        if (dist >= r0 && dist <= r1 && rand.nextDouble() < density) plant(c, r, pool);
                    }
                }
            }
        }

        ForestHelper f = new ForestHelper();

        if (level == 1) {
            f.plantCluster(28, 38, 6, 16, 0.35, SNOW_MODELS);
            f.plantCluster(28, 38, 32, 42, 0.35, SNOW_MODELS);
            f.plantCluster(10, 20, 6, 16, 0.35, GREEN_MODELS);
            f.plantCluster(10, 20, 32, 42, 0.35, GREEN_MODELS);
        } else {
            double wallR = levelConfig.wallRadius;
            f.plantRing(0.55 * wallR, 0.9 * wallR, 0.20, SNOW_MODELS);
            f.plantRing(0.55 * wallR, 0.9 * wallR, 0.15, GREEN_MODELS);
            f.plantRing(0.25 * wallR, 0.42 * wallR, 0.06, GREEN_MODELS);
        }
        mapTrees = treeList.toArray(new Tree[0]);
    }

    private void setupRuins(int level) {
        String[] ruinPool = {
                "Blue-gray_ruins1.png", "Blue-gray_ruins2.png", "Blue-gray_ruins3.png",
                "Blue-gray_ruins4.png", "Blue-gray_ruins5.png",
                "Brown-gray_ruins1.png", "Brown-gray_ruins2.png", "Brown-gray_ruins3.png",
                "Brown-gray_ruins4.png", "Brown-gray_ruins5.png",
                "Brown_ruins1.png", "Brown_ruins2.png", "Brown_ruins3.png",
                "Brown_ruins4.png", "Brown_ruins5.png",
                "Sand_ruins1.png", "Sand_ruins2.png", "Sand_ruins3.png",
                "Sand_ruins4.png", "Sand_ruins5.png",
                "Snow_ruins1.png", "Snow_ruins2.png", "Snow_ruins3.png",
                "Snow_ruins4.png", "Snow_ruins5.png",
                "White_ruins1.png", "White_ruins2.png", "White_ruins3.png",
                "White_ruins4.png", "White_ruins5.png",
                "Yellow_ruins1.png", "Yellow_ruins2.png", "Yellow_ruins3.png",
                "Yellow_ruins4.png", "Yellow_ruins5.png"
        };

        // Keep capture points clear of rubble so the player can always reach objectives
        ruinCaptureTiles = new java.util.ArrayList<>();
        if (capturePoints != null) {
            for (CapturePoint p : capturePoints) {
                if (p != null) {
                    int c = (p.worldX + tileSize / 2) / tileSize;
                    int r = (p.worldY + tileSize / 2) / tileSize;
                    ruinCaptureTiles.add(new int[]{c, r});
                }
            }
        }

        ArrayList<Ruins> ruinList = new ArrayList<>();
        Random rand = new Random(100 + level);
        int centerCol = levelConfig.worldCols / 2;
        int centerRow = levelConfig.worldRows / 2;
        double wallR = levelConfig.wallRadius;

        // Ring of rubble between the inner plaza and the outer wall
        for (int attempt = 0; attempt < 700 && ruinList.size() < 34; attempt++) {
            int col = 1 + rand.nextInt(Math.max(1, maxWorldCol - 2));
            int row = 1 + rand.nextInt(Math.max(1, maxWorldRow - 2));

            double dist = Math.hypot(col - centerCol, row - centerRow);
            if (dist < 0.55 * wallR || dist > 0.9 * wallR) continue;

            if (!canPlaceRuin(col, row)) continue;

            int ruinX = col * tileSize;
            int ruinY = row * tileSize;
            Ruins candidate = new Ruins(ruinX, ruinY, ruinPool[rand.nextInt(ruinPool.length)]);

            // Reject if the ruin's real hitbox overlaps a building, tree, or capture point
            if (ruinOverlapsAnything(candidate, ruinList)) continue;

            ruinList.add(candidate);
        }

        mapRuins = ruinList.toArray(new Ruins[0]);
    }

    // Checks whether a ruin's actual collision box hits buildings, trees, or capture points
    private boolean ruinOverlapsAnything(Ruins candidate, ArrayList<Ruins> existing) {
        if (candidate.hitbox == null) return true;

        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b == null) continue;
                Rectangle bBox = new Rectangle(b.worldX, b.worldY, b.drawWidth, b.drawHeight);
                if (bBox.intersects(candidate.hitbox)) return true;
            }
        }
        if (mapTrees != null) {
            for (Tree t : mapTrees) {
                if (t == null) continue;
                Rectangle tBox = new Rectangle(t.worldX - tileSize, t.worldY - tileSize, tileSize * 2, tileSize * 2);
                if (tBox.intersects(candidate.hitbox)) return true;
            }
        }
        for (int[] ct : ruinCaptureTiles) {
            Rectangle pointBox = new Rectangle(ct[0] * tileSize, ct[1] * tileSize, tileSize * 4, tileSize * 4);
            if (pointBox.intersects(candidate.hitbox)) return true;
        }
        for (Ruins r : existing) {
            if (r != null && r.hitbox != null && r.hitbox.intersects(candidate.hitbox)) return true;
        }

        return false;
    }

    private boolean canPlaceRuin(int col, int row) {
        if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow) return false;

        int tile = tileManager.mapTileNum[col][row];
        if (tile == TileManager.TILE_WALL || tile == TileManager.TILE_WATER) return false;
        if (tile == TileManager.TILE_GATE_FLOOR
                || tile == TileManager.TILE_GATE_PILLAR
                || tile == TileManager.TILE_GATE_DOOR) return false; // keep gate areas clear

        int centerY = levelConfig.worldRows / 2;
        if (row >= centerY - 5 && row <= centerY + 4) return false; // keep corridors clear
        if (isNearBossSpawn(col, row)) return false;

        return true;
    }

    public void startGameThread() {
        gameThread = new Thread(this, "game-loop");
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime();

        while (gameThread != null) {
            update();
            repaint();
            nextDrawTime += drawInterval;
            long waitMillis = (long) ((nextDrawTime - System.nanoTime()) / 1_000_000.0);
            if (waitMillis > 0) {
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    public boolean areMonstersAlive() {
        if (monsters == null) return false;
        for (Boss m : monsters) {
            if (m != null && m.alive) return true;
        }
        return false;
    }

    // ---------- WAVE SYSTEM ----------
    public int getWaveCount(int level) {
        // Level 1 has 3 waves; other levels have a single wave
        return (level == 1) ? 3 : 1;
    }

    public int getCurrentWaveIndex() {
        return currentWave;
    }

    // Returns the spawn positions for the given wave of a level.
    private int[][] getWaveSpawns(int level, int wave) {
        if (level == 1) {
            switch (wave) {
                case 1:
                    return new int[][]{{21, 14}, {29, 36}};      // Wave 1: 2 Demons
                case 2:
                    return new int[][]{{33, 20}, {17, 30}, {25, 33}}; // Wave 2: 3 Demons
                case 3:
                default:
                    return new int[][]{{22, 22}, {28, 28}, {32, 22}, {18, 28}}; // Wave 3: 4 Demons
            }
        }
        // Levels 2 & 3 use the config's monster spawns as the single wave
        return levelConfig != null ? levelConfig.monsterSpawns : new int[][]{};
    }

    private void spawnNextWave() {
        currentWave++;
        int[][] positions = getWaveSpawns(currentLevel, currentWave);
        if (positions == null || positions.length == 0) {
            monsters = new Boss[0];
            waveSpawned = true;
            return;
        }
        monsters = new Boss[positions.length];
        for (int i = 0; i < positions.length; i++) {
            int[] safeTile = findSafeBossSpawn(positions[i][0], positions[i][1]);
            monsters[i] = new Boss(this, safeTile[0], safeTile[1], "Demon A", 2, 1, 15);
        }
        waveSpawned = true;
        if (getWaveCount(currentLevel) > 1) {
            flashStatusMessage("WAVE " + currentWave + " / " + getWaveCount(currentLevel) + " — THE DEMONS ARRIVE!");
        }
    }

    private void updateWaves() {
        if (!waveSpawned) return;
        // If all monsters in the current wave are dead and more waves remain, spawn the next
        if (!areMonstersAlive() && currentWave < getWaveCount(currentLevel)) {
            spawnNextWave();
        }
    }

    public void update() {
        animationFrame++;
        if (gameState == GameState.PLAYING && keyHandler.consumeEscPressed()) {
            gameState = GameState.PAUSED;
            pauseMenuIndex = 0;
            keyHandler.clearMovement();
            return;
        }
        if (gameState == GameState.PAUSED) {
            updatePauseMenu();
            return;
        }
        if (gameState == GameState.LEVEL_TRANSITION) {
            transitionTimer++;
            if (transitionTimer >= TRANSITION_DURATION) advanceLevel();
            return;
        }
        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (keyHandler.consumeRestartRequest()) restartGame();
            return;
        }
        if (dialogueActive) {
            updateDialogue();
            return;
        }
        if (enteringName) {
            updateNameInput();
            return;
        }
        if (statusMessageTimer > 0) statusMessageTimer--;

        boolean moving = isMoving();
        if (keyHandler.shiftPressed && currentStamina > 0.8 && moving) {
            isDashing = true;
            currentStamina = Math.max(0, currentStamina - 0.7);
        } else {
            isDashing = false;
            currentStamina = Math.min(maxStamina, currentStamina + 0.35);
        }

        if (attackCooldown > 0) attackCooldown--;
        if (!isHiding && attackCooldown == 0 && keyHandler.consumeJPressed()) {
            throwBlade();
            attackCooldown = THROW_COOLDOWN;
            playSE(3);
        }

        if (isDashing) {
            performDashStrike();
        } else {
            dashStrikeDone = false;
        }

        updateThrownBlades();

        if (ghosts != null) {
            for (Ghost g : ghosts) if (g != null && g.alive) g.update();
        }

        if (monsters != null) {
            for (Boss m : monsters) {
                if (m != null && m.alive) m.update();
            }
        }

        if (npc != null) npc.updateAnimation();

        if (hitCooldown > 0) hitCooldown--;

        checkHouseProximity();
        checkEntranceGateProximity();
        checkExitGateProximity();

        if (keyHandler.consumeEPressed()) {
            if (isHiding) {
                isHiding = false;
            } else if (currentNearbyBuilding != null) {
                isHiding = true;
                keyHandler.clearMovement();
            } else if (nearEntranceGate && !entranceGateOpen && !entranceGateOpening) {
                entranceGateOpening = true;
                playSE(4);
            } else if (nearExitGate && isExitReady() && !exitGateOpen && !exitGateOpening) {
                exitGateOpening = true;
                playSE(4);
            }
        }

        if (entranceGateOpening) {
            entranceGateProgress = Math.min(1.0, entranceGateProgress + 1.0 / GATE_OPEN_DURATION);
            if (entranceGateProgress >= 1.0) {
                entranceGateOpening = false;
                entranceGateOpen = true;
            }
        }

        if (exitGateOpening) {
            exitGateProgress = Math.min(1.0, exitGateProgress + 1.0 / GATE_OPEN_DURATION);
            if (exitGateProgress >= 1.0) {
                exitGateOpening = false;
                exitGateOpen = true;
            }
        }

        if (!isHiding) {
            movePlayer();

            if (hitCooldown == 0 && ghosts != null) {
                for (Ghost ghost : ghosts) {
                    if (ghost != null && ghost.alive && playerTouchesGhost(ghost)) {
                        harmPlayer();
                        break;
                    }
                }
            }

            collectCapturePoints();
            collectHealthPickups();
        }

        updateHealthPickups();

        updateWaves();

        doorLocked = (capturedPoints == levelConfig.pointTiles.length) && areMonstersAlive();

        if (!isHiding) checkDoorTransition();
    }

    private void checkEntranceGateProximity() {
        if (entranceGateOpen) {
            nearEntranceGate = false;
            return;
        }
        int gateCol = (tileManager.gateCol0 + tileManager.gateCol1) / 2;
        int gateRow = levelConfig.worldRows / 2;
        int gateCenterX = gateCol * tileSize + tileSize / 2;
        int gateCenterY = gateRow * tileSize + tileSize / 2;
        int playerCenterX = playerX + playerSize / 2;
        int playerCenterY = playerY + playerSize / 2;
        double dist = Math.hypot(playerCenterX - gateCenterX, playerCenterY - gateCenterY);
        nearEntranceGate = dist < tileSize * 3.0;
    }

    private void checkExitGateProximity() {
        if (exitGateOpen) {
            nearExitGate = false;
            return;
        }
        int gateCol = (tileManager.exitGateCol0 + tileManager.exitGateCol1) / 2;
        int gateRow = levelConfig.worldRows / 2;
        int gateCenterX = gateCol * tileSize + tileSize / 2;
        int gateCenterY = gateRow * tileSize + tileSize / 2;
        int playerCenterX = playerX + playerSize / 2;
        int playerCenterY = playerY + playerSize / 2;
        double dist = Math.hypot(playerCenterX - gateCenterX, playerCenterY - gateCenterY);
        nearExitGate = dist < tileSize * 3.0;
    }
    private boolean isMoving() {
        return keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed;
    }

    private void updatePauseMenu() {
        if (keyHandler.consumeEscPressed()) {
            gameState = GameState.PLAYING;
            return;
        }
        if (keyHandler.consumeNavUp()) {
            pauseMenuIndex = (pauseMenuIndex - 1 + pauseMenuOptions.length) % pauseMenuOptions.length;
        }
        if (keyHandler.consumeNavDown()) {
            pauseMenuIndex = (pauseMenuIndex + 1) % pauseMenuOptions.length;
        }
        if (keyHandler.consumeEnterPressed()) {
            switch (pauseMenuIndex) {
                case 0:
                    gameState = GameState.PLAYING;
                    break;
                case 1:
                    restartGame();
                    break;
                case 2:
                    System.exit(0);
                    break;
            }
        }
    }

    private void updateDialogue() {
        if (keyHandler.consumeTPressed()) {
            dialogueIndex++;
            if (npc == null || dialogueIndex >= npc.lineCount()) {
                dialogueActive = false;
                dialogueIndex = 0;
                // The intro (name + instructions) has been fully read -> NPC can't be re-visited
                npcIntroDone = true;
            }
        }
    }

    private void updateNameInput() {
        // Handle backspace
        if (keyHandler.isBackspacePressed()) {
            keyHandler.consumeBackspace();
        }

        // Append any freshly typed characters
        char c = keyHandler.consumeTypedChar();
        if (c != 0) {
            keyHandler.nameBuffer.append(c);
        }

        // Submit the name with Enter / Space
        if (keyHandler.consumeEnterPressed()) {
            String name = keyHandler.nameBuffer.toString().trim();
            if (name.isEmpty()) {
                playerName = "Soldier";
            } else {
                playerName = name;
            }
            enteringName = false;
            gameState = GameState.PLAYING;
            // Start the explanation dialogue, which will use the player's name
            dialogueActive = true;
            dialogueIndex = 0;
            keyHandler.clearMovement();
        }
    }

    // Returns a name-aware rendering of a dialogue line: substitutes {name} placeholders
    public String renderLine(String raw) {
        if (raw == null) return "";
        String name = (playerName == null || playerName.isEmpty()) ? "Soldier" : playerName;
        return raw.replace("{name}", name);
    }

    private void throwBlade() {
        int startX = playerX + playerSize / 2;
        int startY = playerY + playerSize / 2;
        int vx = 0, vy = 0;
        switch (direction) {
            case "up": vy = -1; break;
            case "down": vy = 1; break;
            case "left": vx = -1; break;
            default: vx = 1; break;
        }
        thrownBlades.add(new ThrownBlade(startX, startY, vx * BLADE_SPEED, vy * BLADE_SPEED, BLADE_DISTANCE));
    }

    private void updateThrownBlades() {
        java.util.Iterator<ThrownBlade> it = thrownBlades.iterator();
        while (it.hasNext()) {
            ThrownBlade t = it.next();
            t.update();
            if (t.life <= 0) {
                it.remove();
                continue;
            }

            boolean hit = false;
            if (ghosts != null) {
                for (Ghost g : ghosts) {
                    if (g != null && g.alive) {
                        Rectangle ghostBox = new Rectangle(g.worldX + 6, g.worldY + 6, g.size - 12, g.size - 12);
                        if (t.box().intersects(ghostBox)) {
                            g.kill();
                            playSE(1);
                            addScore(50);
                            hit = true;
                            break;
                        }
                    }
                }
            }

            if (!hit && monsters != null) {
                for (Boss m : monsters) {
                    if (m != null && m.alive && t.box().intersects(m.hitbox())) {
                        m.takeDamage(1);
                        playSE(1);
                        if (!m.alive) {
                            int col = (m.worldX + m.size / 2) / tileSize;
                            int row = (m.worldY + m.size / 2) / tileSize;
                            healthPickups.add(new HealthPickup(this, col, row, 25));
                            addScore(100);
                        }
                        if (!areMonstersAlive() && currentWave >= getWaveCount(currentLevel)) {
                            flashStatusMessage("HORDE CLEARED! THE WAY IS OPEN!");
                            addScore(200);
                        }
                        hit = true;
                        break;
                    }
                }
            }

            if (hit) it.remove();
        }
    }

    private void performDashStrike() {
        if (dashStrikeDone) return;
        Rectangle dashBox = new Rectangle(playerX - 8, playerY - 8, playerSize + 16, playerSize + 16);

        boolean struck = false;
        if (ghosts != null) {
            for (Ghost g : ghosts) {
                if (g != null && g.alive) {
                    Rectangle ghostBox = new Rectangle(g.worldX + 6, g.worldY + 6, g.size - 12, g.size - 12);
                    if (dashBox.intersects(ghostBox)) {
                        g.kill();
                        playSE(1);
                        addScore(50);
                        struck = true;
                    }
                }
            }
        }

        if (monsters != null) {
            for (Boss m : monsters) {
                if (m != null && m.alive && dashBox.intersects(m.hitbox())) {
                    m.takeDamage(DASH_STRIKE_DAMAGE);
                    playSE(1);
                    if (!m.alive) {
                        int col = (m.worldX + m.size / 2) / tileSize;
                        int row = (m.worldY + m.size / 2) / tileSize;
                        healthPickups.add(new HealthPickup(this, col, row, 25));
                        addScore(100);
                    }
                    if (!areMonstersAlive() && currentWave >= getWaveCount(currentLevel)) {
                        flashStatusMessage("HORDE CLEARED! THE WAY IS OPEN!");
                        addScore(200);
                    }
                    struck = true;
                }
            }
        }

        dashStrikeDone = true;
    }

    private void addScore(int amount) {
        score += amount;
    }

    private void flashStatusMessage(String message) {
        statusMessage = message;
        statusMessageTimer = 160;
    }

    private void checkHouseProximity() {
        currentNearbyBuilding = null;
        Rectangle playerBox = new Rectangle(playerX - 10, playerY - 10, playerSize + 20, playerSize + 20);
        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && new Rectangle(b.worldX, b.worldY, b.drawWidth, b.drawHeight).intersects(playerBox)) {
                    currentNearbyBuilding = b;
                    break;
                }
            }
        }
    }

    private void movePlayer() {
        if (keyHandler.upPressed) direction = "up";
        if (keyHandler.downPressed) direction = "down";
        if (keyHandler.leftPressed) direction = "left";
        if (keyHandler.rightPressed) direction = "right";

        int speed = isDashing ? baseSpeed + 3 : baseSpeed;
        int horizontal = (keyHandler.rightPressed ? speed : 0) - (keyHandler.leftPressed ? speed : 0);
        int vertical = (keyHandler.downPressed ? speed : 0) - (keyHandler.upPressed ? speed : 0);
        tryMovePlayer(horizontal, 0);
        tryMovePlayer(0, vertical);
    }

    private void tryMovePlayer(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) return;
        if (!collisionChecker.canMove(playerX, playerY, playerSize, playerSize, deltaX, deltaY)) return;

        Rectangle futureBox = new Rectangle(playerX + deltaX, playerY + deltaY, playerSize, playerSize);
        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(futureBox)) return;
            }
        }

        if (mapRuins != null) {
            for (Ruins r : mapRuins) {
                if (r != null && r.hitbox != null && r.hitbox.intersects(futureBox)) return;
            }
        }

        if (monsters != null) {
            Rectangle currentBox = new Rectangle(playerX, playerY, playerSize, playerSize);
            for (Boss m : monsters) {
                if (m == null || !m.alive) continue;
                // Allow the player to escape out of a monster that is overlapping them;
                // only block walking into a monster they are not already touching.
                if (!currentBox.intersects(m.hitbox()) && m.hitbox().intersects(futureBox)) return;
            }
        }

        playerX += deltaX;
        playerY += deltaY;
    }

    private boolean playerTouchesGhost(Ghost ghost) {
        Rectangle pBox = new Rectangle(playerX, playerY, playerSize, playerSize);
        Rectangle gBox = new Rectangle(ghost.worldX + 6, ghost.worldY + 6, ghost.size - 12, ghost.size - 12);
        return pBox.intersects(gBox);
    }

    private void harmPlayer() {
        playerHealth -= 10;
        hitCooldown = 120;
        playSE(2);

        if (playerHealth <= 0) {
            respawnPlayer();
            return;
        }
    }

    public void harmPlayerFromBoss(int damage, int bossCenterX, int bossCenterY, Boss source) {
        if (hitCooldown > 0) return;
        playerHealth -= damage;
        hitCooldown = 120;
        playSE(2);

        int dx = playerX + playerSize / 2 - bossCenterX;
        int dy = playerY + playerSize / 2 - bossCenterY;
        int knockX = (dx == 0 ? 0 : dx > 0 ? tileSize : -tileSize);
        int knockY = (dy == 0 ? 0 : dy > 0 ? tileSize : -tileSize);
        knockbackPlayer(knockX, 0, source);
        knockbackPlayer(0, knockY, source);

        if (playerHealth <= 0) {
            respawnPlayer();
            return;
        }
    }

    private void knockbackPlayer(int deltaX, int deltaY, Boss excludeBoss) {
        if (deltaX == 0 && deltaY == 0) return;
        if (!collisionChecker.canMove(playerX, playerY, playerSize, playerSize, deltaX, deltaY)) return;

        Rectangle futureBox = new Rectangle(playerX + deltaX, playerY + deltaY, playerSize, playerSize);
        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(futureBox)) return;
            }
        }

        if (mapRuins != null) {
            for (Ruins r : mapRuins) {
                if (r != null && r.hitbox != null && r.hitbox.intersects(futureBox)) return;
            }
        }

        if (monsters != null) {
            Rectangle currentBox = new Rectangle(playerX, playerY, playerSize, playerSize);
            for (Boss m : monsters) {
                if (m != null && m.alive && m != excludeBoss && !currentBox.intersects(m.hitbox()) && m.hitbox().intersects(futureBox)) return;
            }
        }

        playerX += deltaX;
        playerY += deltaY;
    }

    private void collectCapturePoints() {
        Rectangle playerBox = new Rectangle(playerX, playerY, playerSize, playerSize);
        for (int i = 0; i < capturePoints.length; i++) {
            CapturePoint point = capturePoints[i];
            if (point == null) continue;
            Rectangle pBox = new Rectangle(point.worldX, point.worldY, tileSize, tileSize);
            if (playerBox.intersects(pBox)) {
                capturePoints[i] = null;
                capturedPoints++;
                playSE(1);
                addScore(50);
            }
        }
    }

    private void collectHealthPickups() {
        Rectangle playerBox = new Rectangle(playerX, playerY, playerSize, playerSize);
        java.util.Iterator<HealthPickup> it = healthPickups.iterator();
        while (it.hasNext()) {
            HealthPickup h = it.next();
            Rectangle hBox = new Rectangle(h.worldX, h.worldY, tileSize, tileSize);
            if (playerBox.intersects(hBox)) {
                playerHealth = Math.min(maxHealth, playerHealth + h.healAmount);
                it.remove();
                playSE(1);
            }
        }
    }

    private void updateHealthPickups() {
        java.util.Iterator<HealthPickup> it = healthPickups.iterator();
        while (it.hasNext()) {
            HealthPickup h = it.next();
            h.update();
            if (h.isExpired()) it.remove();
        }
    }

    private boolean isExitReady() {
        return capturedPoints == levelConfig.pointTiles.length && !areMonstersAlive();
    }

    private void checkDoorTransition() {
        if (!exitGateOpen) return;

        int westStart = levelConfig.worldCols / 2 - (int) levelConfig.wallRadius;
        int centerRow = levelConfig.worldRows / 2;
        int playerCol = (playerX + playerSize / 2) / tileSize;
        int playerRow = (playerY + playerSize / 2) / tileSize;
        if (playerCol <= westStart + 3 && playerRow >= centerRow - 4 && playerRow <= centerRow + 3) {
            gameState = GameState.LEVEL_TRANSITION;
            transitionTimer = 0;
            playSE(4);
            keyHandler.clearMovement();
        }
    }
    private void advanceLevel() {
        if (currentLevel >= LEVEL_COUNT) {
            gameState = GameState.VICTORY;
            keyHandler.clearMovement();
        } else {
            loadLevel(currentLevel + 1);
        }
    }

    public void restartGame() {
        playerHealth = maxHealth;
        currentStamina = 100;
        isHiding = false;
        score = 0;
        playerName = "";
        npcIntroDone = false;
        enteringName = false;
        loadLevel(1);
    }

    private void loadLevel(int level) {
        currentLevel = level;
        levelConfig = LevelConfig.forNumber(level);
        maxWorldCol = levelConfig.worldCols;
        maxWorldRow = levelConfig.worldRows;
        worldWidth = maxWorldCol * tileSize;
        worldHeight = maxWorldRow * tileSize;

        playerHealth = maxHealth;
        currentStamina = maxStamina;

        tileManager.createLevelMap(level);
        setupBuildings(level);
        setupTrees(level);
        setupRuins(level);
        fireflies.init();

        capturedPoints = 0;
        capturePoints = new CapturePoint[levelConfig.pointTiles.length];
        for (int i = 0; i < capturePoints.length; i++) {
            int[] t = levelConfig.pointTiles[i];
            int[] safeTile = findSafeCaptureTile(t[0], t[1]);
            capturePoints[i] = new CapturePoint(this, safeTile[0], safeTile[1]);
        }

        if (levelConfig.ghostTiles != null) {
            ghosts = new Ghost[levelConfig.ghostTiles.length];
            for (int i = 0; i < ghosts.length; i++) {
                int[] t = levelConfig.ghostTiles[i];
                int[] safeTile = findSafeCaptureTile(t[0], t[1]);
                ghosts[i] = new Ghost(this, safeTile[0], safeTile[1], levelConfig.ghostSpeed);
            }
        } else {
            ghosts = new Ghost[0];
        }

        if (level == 1) {
            // Level 1 uses a 3-wave enemy system
            currentWave = 0;
            waveSpawned = false;
            spawnNextWave();
        } else if (levelConfig.monsterSpawns != null) {
            // Levels 2 & 3 spawn their fixed horde as a single wave
            currentWave = 1;
            waveSpawned = true;
            monsters = new Boss[levelConfig.monsterSpawns.length];
            for (int i = 0; i < monsters.length; i++) {
                int[] t = levelConfig.monsterSpawns[i];
                int[] safeTile = findSafeBossSpawn(t[0], t[1]);
                monsters[i] = new Boss(this, safeTile[0], safeTile[1], "Demon A", 2, 1, 15);
            }
        } else {
            currentWave = 0;
            waveSpawned = true;
            monsters = new Boss[0];
        }

        if (levelConfig.npc != null) {
            int[] ns = findSafeCaptureTile(levelConfig.npc.col, levelConfig.npc.row);
            npc = new NPC(this, ns[0], ns[1], levelConfig.npc.name, levelConfig.npc.lines);
        } else {
            npc = null;
        }

        hasSword = false;
        attackCooldown = 0;
        thrownBlades.clear();
        dashStrikeDone = false;

        dialogueActive = false;
        dialogueIndex = 0;
        statusMessageTimer = 0;
        doorLocked = false;
        entranceGateOpen = false;
        entranceGateOpening = false;
        entranceGateProgress = 0.0;
        nearEntranceGate = false;
        exitGateOpen = false;
        exitGateOpening = false;
        exitGateProgress = 0.0;
        nearExitGate = false;
        enteringName = false;
        npcIntroDone = false;
        keyHandler.clearNameBuffer();

        movePlayerToSpawn();
        hitCooldown = 70;
        isHiding = false;
        gameState = GameState.PLAYING;
        keyHandler.clearMovement();
    }

    private int[] findSafeCaptureTile(int col, int row) {
        if (isTileFreeForCapture(col, row)) return new int[]{col, row};

        for (int radius = 1; radius <= 12; radius++) {
            for (int dc = -radius; dc <= radius; dc++) {
                for (int dr = -radius; dr <= radius; dr++) {
                    if (Math.max(Math.abs(dc), Math.abs(dr)) != radius) continue;
                    int c = col + dc;
                    int r = row + dr;
                    if (isTileFreeForCapture(c, r)) return new int[]{c, r};
                }
            }
        }
        return new int[]{col, row};
    }

    private boolean isTileFreeForCapture(int col, int row) {
        if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow) return false;

        int tileType = tileManager.mapTileNum[col][row];
        if (tileType == paradise.world.TileManager.TILE_WALL
                || tileType == paradise.world.TileManager.TILE_WATER
                || tileType == paradise.world.TileManager.TILE_GATE_PILLAR
                || tileType == paradise.world.TileManager.TILE_GATE_DOOR) {
            return false;
        }

        Rectangle tileBox = new Rectangle(col * tileSize, row * tileSize, tileSize, tileSize);
        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(tileBox)) return false;
            }
        }
        return true;
    }

    // True if the 2x2 tile block anchored at (col,row) is fully walkable and free of
    // buildings/ruins so a Demon's 2x2 body can sit and move there.
    public boolean isAreaFreeForBoss(int col, int row) {
        if (col < 0 || col + 1 >= maxWorldCol || row < 0 || row + 1 >= maxWorldRow) return false;
        for (int dc = 0; dc <= 1; dc++) {
            for (int dr = 0; dr <= 1; dr++) {
                if (!isTileFreeForCapture(col + dc, row + dr)) return false;
            }
        }
        // Also ensure no ruin rubble sits in the 2x2 footprint
        Rectangle area = new Rectangle(col * tileSize, row * tileSize, tileSize * 2, tileSize * 2);
        if (mapRuins != null) {
            for (Ruins r : mapRuins) {
                if (r != null && r.hitbox != null && r.hitbox.intersects(area)) return false;
            }
        }
        return true;
    }

    // Finds a tile whose 2x2 block is clear enough for a Demon, searching outward.
    public int[] findSafeBossSpawn(int col, int row) {
        if (isAreaFreeForBoss(col, row)) return new int[]{col, row};
        for (int radius = 1; radius <= 12; radius++) {
            for (int dc = -radius; dc <= radius; dc++) {
                for (int dr = -radius; dr <= radius; dr++) {
                    if (Math.max(Math.abs(dc), Math.abs(dr)) != radius) continue;
                    int c = col + dc;
                    int r = row + dr;
                    if (isAreaFreeForBoss(c, r)) return new int[]{c, r};
                }
            }
        }
        return new int[]{col, row};
    }

    private void movePlayerToSpawn() {
        int centerCol = levelConfig.worldCols / 2;
        int centerRow = levelConfig.worldRows / 2;
        int east = centerCol + (int) levelConfig.wallRadius;
        // Spawn a few tiles outside (east of) the entrance gate, facing it, instead
        // of on top of it — the gate itself starts closed and blocks the way in.
        playerX = (east + 4) * tileSize;
        playerY = centerRow * tileSize;
        direction = "left";
    }

    private void respawnPlayer() {
        playerHealth = maxHealth;
        hitCooldown = 120;
        if (ghosts != null) {
            for (Ghost ghost : ghosts) if (ghost != null) ghost.resetToSpawn();
        }
        movePlayerToSpawn();
        keyHandler.clearMovement();
    }

    public boolean isOnScreen(int worldX, int worldY, int width, int height) {
        return worldX + width > playerX - playerScreenX
                && worldX < playerX + playerScreenX + tileSize
                && worldY + height > playerY - playerScreenY
                && worldY < playerY + playerScreenY + tileSize;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2 = (Graphics2D) graphics.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tileManager.draw(g2);
        paradise.object.VillageDecor.drawPaths(g2, this);
        paradise.object.VillageDecor.drawWell(g2, this);

        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null) {
                    b.draw(g2, this);
                    paradise.object.VillageDecor.drawFence(g2, this, b);
                }
            }
        }

        paradise.object.VillageDecor.drawLampPosts(g2, this, animationFrame);

        if (mapTrees != null) {
            for (Tree t : mapTrees) if (t != null) t.draw(g2, this);
        }

        if (mapRuins != null) {
            for (Ruins r : mapRuins) if (r != null) r.draw(g2, this);
        }

        for (CapturePoint point : capturePoints) {
            if (point != null) point.draw(g2, this, animationFrame);
        }

        for (HealthPickup h : healthPickups) {
            h.draw(g2, this, animationFrame);
        }

        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                if (ghost != null && ghost.alive) ghost.draw(g2, animationFrame);
            }
        }

        if (monsters != null) {
            for (Boss m : monsters) {
                if (m != null) m.draw(g2, animationFrame);
            }
        }

        if (npc != null) npc.draw(g2, animationFrame);

        if (!isHiding) {
            drawPlayer(g2);
        }

        for (ThrownBlade t : thrownBlades) {
            t.draw(g2, playerX, playerY, playerScreenX, playerScreenY);
        }

        drawNightOverlay(g2);
        paradise.object.VillageDecor.drawLampGlow(g2, this, animationFrame);

        fireflies.draw(g2);

        drawInteractionPrompt(g2);
        ui.draw(g2);
        g2.dispose();
    }

    private void drawNightOverlay(Graphics2D g2) {
        // Daytime: no dark overlay
    }

    private void drawInteractionPrompt(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        if (isHiding) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(Color.GREEN);
            g2.drawString("HIDDEN: Press [E] to Exit", screenWidth / 2 - 90, screenHeight - 57);
        } else if (npc != null && npc.nearPlayer(64) && gameState == GameState.PLAYING && !npcIntroDone) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(new Color(120, 255, 170));
            g2.drawString("Press [T] to Talk", screenWidth / 2 - 78, screenHeight - 57);
        } else if (npc != null && npc.nearPlayer(64) && gameState == GameState.PLAYING && npcIntroDone) {
            g2.setColor(new Color(0, 0, 0, 140));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(new Color(180, 180, 180));
            g2.drawString("The Scout has left you.", screenWidth / 2 - 82, screenHeight - 57);
        } else if (currentNearbyBuilding != null) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(Color.YELLOW);
            g2.drawString("Press [E] to Hide in House", screenWidth / 2 - 95, screenHeight - 57);
        }
    }

    private void drawPlayer(Graphics2D graphics) {
        int x = playerScreenX + (tileSize - playerSize) / 2;
        int y = playerScreenY + (tileSize - playerSize) / 2;

        if (hitCooldown > 0 && (animationFrame / 6) % 2 == 0) return;

        if (isDashing) {
            graphics.setColor(new Color(0, 200, 255, 60));
            graphics.fillOval(x - 4, y + 4, playerSize + 8, playerSize + 4);
        }

        BufferedImage currentFrame = null;
        boolean isMoving = isMoving();
        int currentFrameIndex = isMoving ? (animationFrame / 5) % 8 : 0;

        switch (direction) {
            case "up": currentFrame = runUp[currentFrameIndex]; break;
            case "down": currentFrame = runDown[currentFrameIndex]; break;
            case "left": currentFrame = runLeft[currentFrameIndex]; break;
            case "right": currentFrame = runRight[currentFrameIndex]; break;
        }

        if (currentFrame != null) {
            int drawWidth = currentFrame.getWidth() * characterScale;
            int drawHeight = currentFrame.getHeight() * characterScale;

            int drawX = x - (drawWidth - playerSize) / 2;
            int drawY = y - (drawHeight - playerSize) / 2;

            graphics.drawImage(currentFrame, drawX, drawY, drawWidth, drawHeight, null);
        } else {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(x, y, playerSize, playerSize);
        }
    }
}