package paradise.core;

import paradise.entity.Ghost;
import paradise.input.KeyHandler;
import paradise.object.Building;
import paradise.object.CapturePoint;
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
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;
    public final int playerScreenX = screenWidth / 2 - tileSize / 2;
    public final int playerScreenY = screenHeight / 2 - tileSize / 2;

    public final int playerSize = 28;
    public int characterScale = 2;

    public final int maxStamina = 100;
    public double currentStamina = 100;
    public boolean isDashing = false;

    public boolean isHiding = false;
    public Building currentNearbyBuilding = null;

    // Lore Variables
    public boolean isReadingLore = false;
    public String currentLoreText = "";
    public ArrayList<LoreNote> loreNotes = new ArrayList<>();
    public LoreNote currentNearbyNote = null;

    // Cinematic Intro Variables
    public boolean showStoryScreen = true;
    private boolean voicePlayed = false;
    private int storyTimer = 0;
    private final int storyDuration = 3600;
    private BufferedImage darknessFilter;

    // Pause Menu Variables
    public boolean isPaused = false;
    public int pauseCommandNum = 0;
    private int pauseCooldown = 0;

    public final KeyHandler keyHandler = new KeyHandler();
    public final TileManager tileManager = new TileManager(this);
    public final CollisionChecker collisionChecker = new CollisionChecker(this);
    public final UI ui = new UI(this);
    public final Sound sound = new Sound();

    public int playerX;
    public int playerY;
    public final int baseSpeed = 4;
    public int playerHealth;

    public String direction = "down";
    public BufferedImage[] runUp = new BufferedImage[8];
    public BufferedImage[] runDown = new BufferedImage[8];
    public BufferedImage[] runLeft = new BufferedImage[8];
    public BufferedImage[] runRight = new BufferedImage[8];

    public int currentLevel;
    public int capturedPoints;
    public int animationFrame;
    public GameState gameState;
    public LevelConfig levelConfig;
    public CapturePoint[] capturePoints;
    public Ghost[] ghosts;

    public Tree[] mapTrees;
    public Building[] mapBuildings;
    public ArrayList<Firefly> fireflies = new ArrayList<>();

    private static final int FPS = 60;
    private static final int TRANSITION_DURATION = 95;
    private Thread gameThread;
    private int hitCooldown;
    private int transitionTimer;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(new Color(10, 18, 31));
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);

        try {
            runDown = loadAndSlice("src/paradise/entity/run1.png");
            runUp = loadAndSlice("src/paradise/entity/run4.png");
            runRight = loadAndSlice("src/paradise/entity/run3.png");
            runLeft = loadAndSlice("src/paradise/entity/run2.png");
        } catch (IOException e) {
            System.out.println("Could not load one or more directional run animations!");
        }

        setupBuildings();
        setupTrees();
        setupLoreNotes();
        setupLighting();
        restartGame();
    }

    private BufferedImage[] loadAndSlice(String filePath) throws IOException {
        BufferedImage sheet = ImageIO.read(new File(filePath));
        BufferedImage[] frames = new BufferedImage[8];
        int frameWidth = sheet.getWidth() / 8;
        int frameHeight = sheet.getHeight();
        for (int i = 0; i < 8; i++) {
            frames[i] = sheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
        }
        return frames;
    }

    private void setupBuildings() {
        mapBuildings = new Building[7];
        mapBuildings[0] = new Building(14 * tileSize, 14 * tileSize, 1);
        mapBuildings[1] = new Building(32 * tileSize, 14 * tileSize, 2);
        mapBuildings[2] = new Building(14 * tileSize, 32 * tileSize, 3);
        mapBuildings[3] = new Building(32 * tileSize, 32 * tileSize, 4);
        mapBuildings[4] = new Building(20 * tileSize, 18 * tileSize, 5);
        mapBuildings[5] = new Building(28 * tileSize, 18 * tileSize, 6);
        mapBuildings[6] = new Building(24 * tileSize, 30 * tileSize, 7);

        fireflies.clear();
        for (Building b : mapBuildings) {
            if (b != null) {
                int anchorX = b.worldX + (tileSize * 2);
                int anchorY = b.worldY + (tileSize * 2);
                for (int i = 0; i < 15; i++) {
                    fireflies.add(new Firefly(anchorX, anchorY));
                }
            }
        }
    }

    private void setupLoreNotes() {
        loreNotes.clear();
        loreNotes.add(new LoreNote(24 * tileSize, 16 * tileSize, "Expedition Log 01:\n\nThe fog rolled in faster than we could react.\nOur instruments are dead.\nSomething is moving in the trees."));
        loreNotes.add(new LoreNote(34 * tileSize, 25 * tileSize, "Expedition Log 07:\n\nThey don't bleed. They don't breathe.\nThey just drain you until you're nothing but dust.\nDon't let them touch you."));
        loreNotes.add(new LoreNote(15 * tileSize, 25 * tileSize, "Expedition Log 14:\n\nI've found the energy monoliths.\nIf I can capture them all, the central gate might open.\nI'm so tired..."));
    }

    private final int[][] GREEN_MODELS = {{0, 64}, {32, 64}, {128, 64}, {160, 64}, {192, 64}};
    private final int[][] SNOW_MODELS = {{96, 224}, {128, 224}, {160, 224}};

    private void setupTrees() {
        ArrayList<Tree> treeList = new ArrayList<>();
        boolean[][] occupied = new boolean[maxWorldCol][maxWorldRow];
        Random rand = new Random(42);

        class ForestHelper {
            void plant(int col, int row, int[][] pool) {
                if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow || occupied[col][row]) return;
                if (col >= 38 && col <= 49 && row >= 21 && row <= 28) return;
                if (col >= 0 && col <= 9 && row >= 21 && row <= 28) return;

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
        }

        ForestHelper f = new ForestHelper();
        f.plantCluster(28, 38, 6, 16, 0.35, SNOW_MODELS);
        f.plantCluster(28, 38, 32, 42, 0.35, SNOW_MODELS);
        f.plantCluster(10, 20, 6, 16, 0.35, GREEN_MODELS);
        f.plantCluster(10, 20, 32, 42, 0.35, GREEN_MODELS);
        mapTrees = treeList.toArray(new Tree[0]);
    }

    private void setupLighting() {
        darknessFilter = new BufferedImage(screenWidth, screenHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = (Graphics2D) darknessFilter.getGraphics();

        Color fogColor = new Color(5, 15, 25, 245);
        g2.setColor(fogColor);
        g2.fillRect(0, 0, screenWidth, screenHeight);

        int centerX = playerScreenX + (tileSize / 2);
        int centerY = playerScreenY + (tileSize / 2);
        int radius = 220;

        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(centerX, centerY);
        float[] distance = {0.0f, 0.6f, 1.0f};
        Color[] colors = {
                new Color(0, 0, 0, 0),
                new Color(0, 0, 0, 150),
                fogColor
        };

        java.awt.RadialGradientPaint paint = new java.awt.RadialGradientPaint(center, radius, distance, colors);

        g2.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC, 1f));
        g2.setPaint(paint);
        g2.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
        g2.dispose();
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

    public void update() {
        if (showStoryScreen) {
            if (!voicePlayed) {
                sound.playVoice("story_voice");
                voicePlayed = true;
            }
            if (keyHandler.consumeEnterPressed() || keyHandler.consumeEscapePressed()) {
                showStoryScreen = false;
                sound.stopVoice();
                keyHandler.clearMovement();
                return;
            }
            storyTimer++;
            if (storyTimer >= storyDuration) {
                showStoryScreen = false;
                keyHandler.clearMovement();
            }
            return;
        }

        if (isReadingLore) {
            if (keyHandler.consumeEPressed() || keyHandler.consumeEnterPressed() || keyHandler.consumeEscapePressed()) {
                isReadingLore = false;
            }
            return;
        }

        if (keyHandler.consumeEscapePressed()) {
            isPaused = !isPaused;
            if (isPaused) keyHandler.clearMovement();
        }

        if (isPaused) {
            if (keyHandler.upPressed && pauseCooldown == 0) {
                pauseCommandNum = (pauseCommandNum == 0) ? 1 : 0;
                pauseCooldown = 15;
            }
            if (keyHandler.downPressed && pauseCooldown == 0) {
                pauseCommandNum = (pauseCommandNum == 1) ? 0 : 1;
                pauseCooldown = 15;
            }
            if (pauseCooldown > 0) pauseCooldown--;

            if (keyHandler.consumeEnterPressed()) {
                if (pauseCommandNum == 0) {
                    isPaused = false;
                } else if (pauseCommandNum == 1) {
                    System.exit(0);
                }
            }
            return;
        }

        animationFrame++;

        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (keyHandler.consumeRestartRequest()) restartGame();
            return;
        }

        if (gameState == GameState.LEVEL_TRANSITION) {
            transitionTimer++;
            if (transitionTimer >= TRANSITION_DURATION) {
                if (currentLevel == LEVEL_COUNT) gameState = GameState.VICTORY;
                else loadLevel(currentLevel + 1);
            }
            return;
        }

        boolean isMoving = keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed;

        if (isMoving && keyHandler.shiftPressed && currentStamina > 0) {
            isDashing = true;
            currentStamina = Math.max(0, currentStamina - 0.5);
        } else {
            isDashing = false;
            if (isMoving) currentStamina = Math.min(maxStamina, currentStamina + 0.2);
            else currentStamina = Math.min(maxStamina, currentStamina + 0.6);
        }

        if (ghosts != null) {
            for (Ghost g : ghosts) if (g != null) g.update();
        }
        for (Firefly f : fireflies) f.update();
        if (hitCooldown > 0) hitCooldown--;

        checkHouseProximity();
        checkLoreProximity();

        if (keyHandler.consumeEPressed()) {
            if (isHiding) {
                isHiding = false;
            } else if (currentNearbyNote != null) {
                isReadingLore = true;
                currentLoreText = currentNearbyNote.text;
                keyHandler.clearMovement();
            } else if (currentNearbyBuilding != null) {
                isHiding = true;
                keyHandler.clearMovement();
            }
        }

        if (!isHiding) {
            movePlayer();

            if (hitCooldown == 0 && ghosts != null) {
                for (Ghost ghost : ghosts) {
                    if (ghost != null && playerTouchesGhost(ghost)) {
                        harmPlayer();
                        break;
                    }
                }
            }
            collectCapturePoints();
            checkDoorTransition();
        }
    }

    private void checkLoreProximity() {
        currentNearbyNote = null;
        Rectangle playerBox = new Rectangle(playerX - 10, playerY - 10, playerSize + 20, playerSize + 20);
        for (LoreNote note : loreNotes) {
            Rectangle noteBox = new Rectangle(note.worldX, note.worldY, tileSize, tileSize);
            if (playerBox.intersects(noteBox)) {
                currentNearbyNote = note;
                break;
            }
        }
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
        else if (keyHandler.downPressed) direction = "down";
        else if (keyHandler.leftPressed) direction = "left";
        else if (keyHandler.rightPressed) direction = "right";

        int speed = isDashing ? baseSpeed + 1 : baseSpeed - 1;
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
        playerX += deltaX;
        playerY += deltaY;
    }

    private boolean playerTouchesGhost(Ghost ghost) {
        Rectangle pBox = new Rectangle(playerX, playerY, playerSize, playerSize);
        Rectangle gBox = new Rectangle(ghost.worldX + 6, ghost.worldY + 6, ghost.size - 12, ghost.size - 12);
        return pBox.intersects(gBox);
    }

    private void harmPlayer() {
        playerHealth--;
        hitCooldown = 120;
        sound.playSFX("damage");
        if (ghosts != null) {
            for (Ghost ghost : ghosts) if (ghost != null) ghost.resetToSpawn();
        }
        movePlayerToSpawn();
        if (playerHealth <= 0) {
            gameState = GameState.GAME_OVER;
            keyHandler.clearMovement();
        }
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
                sound.playSFX("pickup");
            }
        }
    }

    private void checkDoorTransition() {
        if (capturedPoints == levelConfig.pointTiles.length) {
            int playerCol = (playerX + playerSize / 2) / tileSize;
            int playerRow = (playerY + playerSize / 2) / tileSize;
            if (playerCol <= 4 && (playerRow >= 23 && playerRow <= 26)) {
                gameState = GameState.LEVEL_TRANSITION;
                transitionTimer = 0;
                sound.playSFX("door");
                keyHandler.clearMovement();
            }
        }
    }

    public void restartGame() {
        playerHealth = 3;
        currentStamina = 100;
        isHiding = false;
        sound.playMusic("bgm");
        loadLevel(1);
    }

    public boolean isTileFreeForCapture(int col, int row) {
        if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow) return false;
        int tileType = tileManager.mapTileNum[col][row];
        if (tileType == TileManager.TILE_WATER || tileType == TileManager.TILE_WALL) {
            return false;
        }

        int x = col * tileSize;
        int y = row * tileSize;
        Rectangle tileBox = new Rectangle(x, y, tileSize, tileSize);
        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(tileBox)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void loadLevel(int level) {
        currentLevel = level;
        levelConfig = LevelConfig.forNumber(level);
        tileManager.createLevelMap(level);
        capturedPoints = 0;

        capturePoints = new CapturePoint[levelConfig.pointTiles.length];
        for (int i = 0; i < capturePoints.length; i++) {
            int[] t = levelConfig.pointTiles[i];
            int col = t[0];
            int row = t[1];
            while (!isTileFreeForCapture(col, row) && row < maxWorldRow - 2) row++;
            capturePoints[i] = new CapturePoint(this, col, row);
        }

        ghosts = new Ghost[levelConfig.ghostTiles.length];
        for (int i = 0; i < ghosts.length; i++) {
            int[] t = levelConfig.ghostTiles[i];
            ghosts[i] = new Ghost(this, t[0], t[1], levelConfig.ghostSpeed);
        }

        movePlayerToSpawn();
        hitCooldown = 70;
        isHiding = false;
        gameState = GameState.PLAYING;
        keyHandler.clearMovement();
    }

    private void movePlayerToSpawn() {
        playerX = 46 * tileSize;
        playerY = 25 * tileSize;
        direction = "down";
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

        if (showStoryScreen) {
            drawStoryScreen(g2);
            g2.dispose();
            return;
        }

        tileManager.draw(g2);

        if (mapBuildings != null) {
            for (Building b : mapBuildings) if (b != null) b.draw(g2, this);
        }

        if (mapTrees != null) {
            for (Tree t : mapTrees) if (t != null) t.draw(g2, this);
        }

        for (CapturePoint point : capturePoints) {
            if (point != null) point.draw(g2, this, animationFrame);
        }

        for (Ghost ghost : ghosts) {
            ghost.draw(g2, animationFrame);
        }

        if (!isHiding) {
            drawPlayer(g2);
        }

        g2.drawImage(darknessFilter, 0, 0, null);

        for (LoreNote note : loreNotes) {
            note.draw(g2, this);
        }

        for (Firefly f : fireflies) {
            f.draw(g2, this);
        }

        drawInteractionPrompt(g2);
        ui.draw(g2);
        drawStaminaBar(g2);

        if (isPaused) drawPauseScreen(g2);
        if (isReadingLore) drawLoreScreen(g2);

        g2.dispose();
    }

    private void drawLoreScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        int boxX = screenWidth / 2 - 250;
        int boxY = screenHeight / 2 - 150;
        int boxW = 500;
        int boxH = 300;

        g2.setColor(new Color(240, 235, 210));
        g2.fillRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        g2.setColor(new Color(60, 40, 20));
        g2.setStroke(new BasicStroke(4));
        g2.drawRoundRect(boxX, boxY, boxW, boxH, 15, 15);

        g2.setFont(new Font("Serif", Font.ITALIC, 22));
        g2.setColor(new Color(40, 30, 10));

        String[] lines = currentLoreText.split("\n");
        int textY = boxY + 60;
        for (String line : lines) {
            g2.drawString(line, boxX + 40, textY);
            textY += 35;
        }

        g2.setFont(new Font("Arial", Font.BOLD, 14));
        g2.setColor(new Color(150, 100, 50));
        g2.drawString("Press [E] to Close", boxX + boxW - 150, boxY + boxH - 20);
    }

    private void drawPauseScreen(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        g2.setFont(new Font("Arial", Font.BOLD, 54));
        g2.setColor(Color.WHITE);
        String text = "PAUSED";
        int x = screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2;
        int y = screenHeight / 2 - 80;
        g2.drawString(text, x, y);

        g2.setFont(new Font("Arial", Font.BOLD, 28));
        text = "Resume";
        x = screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2;
        y += 100;
        g2.drawString(text, x, y);
        if (pauseCommandNum == 0) g2.drawString(">", x - 30, y);

        text = "Exit Game";
        x = screenWidth / 2 - g2.getFontMetrics().stringWidth(text) / 2;
        y += 50;
        g2.drawString(text, x, y);
        if (pauseCommandNum == 1) g2.drawString(">", x - 30, y);
    }

    private void drawStoryScreen(Graphics2D g2) {
        if (storyTimer < 3300) {
            g2.setColor(Color.BLACK);
            g2.fillRect(0, 0, screenWidth, screenHeight);

            g2.setFont(new Font("Arial", Font.BOLD, 26));
            g2.setColor(new Color(220, 220, 220));
            g2.drawString("PARADISE BEYOND WALLS", screenWidth / 2 - 170, 80);

            g2.setFont(new Font("Arial", Font.PLAIN, 18));
            g2.setColor(Color.WHITE);

            String[] storyLines = {
                    "The expedition was supposed to be a one-way trip to salvation,",
                    "a final desperate voyage to chart a fabled, untouched sanctuary",
                    "known only as Paradise.",
                    "",
                    "But as your vessel breached the coordinates, a sudden squall of",
                    "thick, glowing fog swallowed the ship. The crushing weight of",
                    "the black waves tore the hull apart in seconds.",
                    "",
                    "You awaken choking on saltwater, washed ashore on a jagged",
                    "coastline. The rest of your crew is nowhere to be found.",
                    "",
                    "This is not a sanctuary. It is an ancient containment zone,",
                    "patrolled by restless, wandering phantoms that drain your life.",
                    "",
                    "Master your exhaustion, seek out the dormant energy monoliths,",
                    "and unlock the heavy iron gates to escape this cursed prison."
            };

            int y = 140;
            for (String line : storyLines) {
                g2.drawString(line, 100, y);
                y += 24;
            }

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.setColor(new Color(100, 100, 100));
            g2.drawString("Press [ENTER] to Skip", screenWidth - 180, screenHeight - 30);

        }
        else {
            g2.setColor(new Color(15, 0, 0));
            g2.fillRect(0, 0, screenWidth, screenHeight);

            int rightX = screenWidth / 2 - 140;
            int rightY = screenHeight / 2 - 40;

            g2.setFont(new Font("Arial", Font.BOLD, 30));
            g2.setColor(new Color(200, 50, 50));
            g2.drawString("READY FOR", rightX + 35, rightY);
            g2.drawString("THE NIGHTMARE?", rightX - 15, rightY + 45);

            int secondsLeft = (storyDuration - storyTimer) / 60;
            g2.setFont(new Font("Arial", Font.ITALIC, 22));
            g2.setColor(new Color(150, 150, 150));
            g2.drawString("Begins in " + secondsLeft + "...", rightX + 50, rightY + 110);
        }
    }

    private void drawStaminaBar(Graphics2D g2) {
        int barWidth = 200;
        int barHeight = 16;
        int x = 20;
        int y = screenHeight - 40;

        g2.setFont(new Font("Arial", Font.BOLD, 12));
        g2.setColor(Color.WHITE);
        g2.drawString("STAMINA", x, y - 8);

        g2.setColor(new Color(30, 30, 30, 200));
        g2.fillRoundRect(x, y, barWidth, barHeight, 8, 8);

        int fillWidth = (int) ((currentStamina / maxStamina) * barWidth);
        if (currentStamina > 60) g2.setColor(new Color(40, 200, 255, 220));
        else if (currentStamina > 25) g2.setColor(new Color(255, 200, 0, 220));
        else g2.setColor(new Color(255, 50, 50, 220));

        g2.fillRoundRect(x, y, fillWidth, barHeight, 8, 8);

        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2));
        g2.drawRoundRect(x, y, barWidth, barHeight, 8, 8);
    }

    private void drawInteractionPrompt(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        if (isHiding) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(Color.GREEN);
            g2.drawString("HIDDEN: Press [E] to Exit", screenWidth / 2 - 90, screenHeight - 57);
        } else if (currentNearbyNote != null) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(new Color(255, 255, 150));
            g2.drawString("Press [E] to Read Journal", screenWidth / 2 - 90, screenHeight - 57);
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

        BufferedImage[] currentAnim = runDown;
        boolean flipHorizontal = false;

        switch(direction) {
            case "up": currentAnim = runUp; break;
            case "down": currentAnim = runDown; break;
            case "right": currentAnim = runRight; break;
            case "left": currentAnim = runLeft; flipHorizontal = false; break;
        }

        if (currentAnim != null && currentAnim[0] != null) {
            boolean isMoving = keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed;
            int currentFrame = isMoving ? (animationFrame / 5) % 8 : 0;

            int originalWidth = currentAnim[currentFrame].getWidth();
            int originalHeight = currentAnim[currentFrame].getHeight();
            int drawWidth = originalWidth * characterScale;
            int drawHeight = originalHeight * characterScale;
            int drawX = x - (drawWidth - playerSize) / 2;
            int drawY = y - (drawHeight - playerSize) / 2;

            if (!flipHorizontal) {
                graphics.drawImage(currentAnim[currentFrame], drawX, drawY, drawWidth, drawHeight, null);
            } else {
                graphics.drawImage(currentAnim[currentFrame], drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
            }
        } else {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(x, y, playerSize, playerSize);
        }
    }

    public class LoreNote {
        int worldX, worldY;
        String text;

        public LoreNote(int x, int y, String text) {
            this.worldX = x;
            this.worldY = y;
            this.text = text;
        }

        public void draw(Graphics2D g2, GamePanel gp) {
            int screenX = worldX - gp.playerX + gp.playerScreenX;
            int screenY = worldY - gp.playerY + gp.playerScreenY;

            if (screenX > -tileSize && screenX < gp.screenWidth && screenY > -tileSize && screenY < gp.screenHeight) {
                // Draw glowing paper
                g2.setColor(new Color(255, 255, 200, 150));
                g2.fillRoundRect(screenX + 16, screenY + 16, 16, 16, 4, 4);
                g2.setColor(new Color(255, 255, 255));
                g2.drawRoundRect(screenX + 16, screenY + 16, 16, 16, 4, 4);
            }
        }
    }

    public class Firefly {
        double worldX, worldY, anchorX, anchorY, angle, speed;
        int size;
        float alpha, alphaSpeed;
        boolean fadingIn;

        public Firefly(int anchorX, int anchorY) {
            this.anchorX = anchorX; this.anchorY = anchorY;
            this.worldX = anchorX + (Math.random() * 200 - 100);
            this.worldY = anchorY + (Math.random() * 200 - 100);
            this.angle = Math.random() * Math.PI * 2;
            this.speed = 0.2 + Math.random() * 0.4;
            this.size = 2 + (int)(Math.random() * 4);
            this.alpha = (float)Math.random();
            this.alphaSpeed = 0.005f + (float)(Math.random() * 0.01f);
            this.fadingIn = Math.random() > 0.5;
        }

        public void update() {
            worldX += Math.cos(angle) * speed;
            worldY += Math.sin(angle) * speed;
            angle += (Math.random() - 0.5) * 0.15;
            double dist = Math.sqrt(Math.pow(worldX - anchorX, 2) + Math.pow(worldY - anchorY, 2));
            if (dist > 150) angle += Math.PI;

            if (fadingIn) {
                alpha += alphaSpeed;
                if (alpha >= 0.8f) { alpha = 0.8f; fadingIn = false; }
            } else {
                alpha -= alphaSpeed;
                if (alpha <= 0.1f) { alpha = 0.1f; fadingIn = true; }
            }
        }

        public void draw(Graphics2D g2, GamePanel gp) {
            int screenX = (int)worldX - gp.playerX + gp.playerScreenX;
            int screenY = (int)worldY - gp.playerY + gp.playerScreenY;

            if (screenX > -10 && screenX < gp.screenWidth + 10 && screenY > -10 && screenY < gp.screenHeight + 10) {
                g2.setColor(new Color(220, 255, 120, (int)(alpha * 255)));
                g2.fillOval(screenX, screenY, size, size);
                g2.setColor(new Color(200, 255, 100, (int)(alpha * 70)));
                g2.fillOval(screenX - size, screenY - size, size * 3, size * 3);
            }
        }
    }
}