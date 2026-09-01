package paradise.core;

<<<<<<< HEAD
import paradise.effects.Fireflies;
=======
import paradise.effects.AmbientParticles;
>>>>>>> 015ac1b9982e4947967e2876880e54e71eb73acf
import paradise.entity.Ghost;
import paradise.input.KeyHandler;
import paradise.object.Building;
import paradise.object.CapturePoint;
import paradise.object.Tree;
import paradise.ui.UI;
import paradise.world.CollisionChecker;
import paradise.world.TileManager;

import javax.swing.JPanel;
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

    // Stamina & Dash System
    public final int maxStamina = 100;
    public double currentStamina = 100;
    public boolean isDashing = false;

    // Hiding mechanic
    public boolean isHiding = false;
    public Building currentNearbyBuilding = null;

    public final KeyHandler keyHandler = new KeyHandler();
    public final TileManager tileManager = new TileManager(this);
    public final CollisionChecker collisionChecker = new CollisionChecker(this);
    public final UI ui = new UI(this);
    public final Sound sound = new Sound();
    public final Sound se = new Sound();
<<<<<<< HEAD
    public final Fireflies fireflies = new Fireflies(this);
=======
    public final AmbientParticles ambientParticles = new AmbientParticles(this);
>>>>>>> 015ac1b9982e4947967e2876880e54e71eb73acf

    public int playerX;
    public int playerY;
    public final int baseSpeed = 4;
    public int playerHealth;
    public final int maxHealth = 3;

    // Updated Animation Variables
    public String direction = "down";
    public BufferedImage[] runDown = new BufferedImage[8];
    public BufferedImage[] runLeft = new BufferedImage[8];
    public BufferedImage[] runRight = new BufferedImage[8];
    public BufferedImage[] runUp = new BufferedImage[8];

    public int currentLevel;
    public int capturedPoints;
    public int animationFrame;
    public GameState gameState;
    public LevelConfig levelConfig;
    public CapturePoint[] capturePoints;
    public Ghost[] ghosts;

    public Tree[] mapTrees;
    public Building[] mapBuildings;

    private static final int FPS = 60;
    private static final int TRANSITION_DURATION = 95;
    private Thread gameThread;
    private int hitCooldown;
    private int transitionTimer;
    public final String[] pauseMenuOptions = {"Resume", "Restart", "Quit"};
    public int pauseMenuIndex = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(new Color(4, 7, 16));
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);

        loadPlayerImages();

        setupBuildings();
        setupTrees();
        fireflies.init();
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

    private void setupBuildings() {
        mapBuildings = new Building[7];
        mapBuildings[0] = new Building(21 * tileSize, 10 * tileSize, 1);
        mapBuildings[1] = new Building(32 * tileSize, 14 * tileSize, 2);
        mapBuildings[2] = new Building(36 * tileSize, 24 * tileSize, 3);
        mapBuildings[3] = new Building(30 * tileSize, 34 * tileSize, 4);
        mapBuildings[4] = new Building(18 * tileSize, 34 * tileSize, 5);
        mapBuildings[5] = new Building(8 * tileSize, 26 * tileSize, 6);
        mapBuildings[6] = new Building(12 * tileSize, 14 * tileSize, 7);
    }

    private final int[][] GREEN_MODELS = {{0, 64}, {32, 64}, {128, 64}, {160, 64}, {192, 64}};
    private final int[][] SNOW_MODELS = {{96, 224}, {128, 224}, {160, 224}};
    private final int[][] GOLD_MODELS = {{0, 0}, {32, 0}, {64, 0}};

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
        animationFrame++;
        ambientParticles.update();

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

        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (keyHandler.consumeRestartRequest()) restartGame();
            return;
        }

        if (keyHandler.shiftPressed && currentStamina > 0.8 && (keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed)) {
            isDashing = true;
            currentStamina = Math.max(0, currentStamina - 0.7);
        } else {
            isDashing = false;
            currentStamina = Math.min(maxStamina, currentStamina + 0.35);
        }

        if (ghosts != null) {
            for (Ghost g : ghosts) if (g != null) g.update();
        }

        if (hitCooldown > 0) hitCooldown--;

        checkHouseProximity();

        if (keyHandler.consumeEPressed()) {
            if (isHiding) {
                isHiding = false;
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
        playSE(2);

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
                playSE(1);
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
                playSE(4);
                keyHandler.clearMovement();
            }
        }
    }

    public void restartGame() {
        playerHealth = maxHealth;
        currentStamina = 100;
        isHiding = false;
        loadLevel(1);
    }

    private void loadLevel(int level) {
        currentLevel = level;
        levelConfig = LevelConfig.forNumber(level);
        tileManager.createLevelMap(level);
        capturedPoints = 0;
        capturePoints = new CapturePoint[levelConfig.pointTiles.length];
        ghosts = new Ghost[levelConfig.ghostTiles.length];
        for (int i = 0; i < capturePoints.length; i++) {
            int[] t = levelConfig.pointTiles[i];
            int[] safeTile = findSafeCaptureTile(t[0], t[1]);
            capturePoints[i] = new CapturePoint(this, safeTile[0], safeTile[1]);
        }
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
                || tileType == paradise.world.TileManager.TILE_GATE_PILLAR) {
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

        for (CapturePoint point : capturePoints) {
            if (point != null) point.draw(g2, this, animationFrame);
        }
        for (Ghost ghost : ghosts) {
            ghost.draw(g2, animationFrame);
        }

        if (!isHiding) {
            drawPlayer(g2);
        }

<<<<<<< HEAD
        drawNightOverlay(g2);
        paradise.object.VillageDecor.drawLampGlow(g2, this, animationFrame);
        fireflies.draw(g2);

=======
        // 2. Ambient foreground atmosphere (drifting leaves/petals on the wind)
        ambientParticles.draw(g2);

        // 3. Clear UI Overlay (No dark filter)
>>>>>>> 015ac1b9982e4947967e2876880e54e71eb73acf
        drawInteractionPrompt(g2);
        ui.draw(g2);
        g2.dispose();
    }

    private void drawNightOverlay(Graphics2D g2) {
        g2.setColor(new Color(6, 9, 26, 145));
        g2.fillRect(0, 0, screenWidth, screenHeight);
    }

    private void drawInteractionPrompt(Graphics2D g2) {
        g2.setFont(new Font("Arial", Font.BOLD, 14));
        if (isHiding) {
            g2.setColor(new Color(0, 0, 0, 180));
            g2.fillRoundRect(screenWidth / 2 - 110, screenHeight - 80, 220, 36, 10, 10);
            g2.setColor(Color.GREEN);
            g2.drawString("HIDDEN: Press [E] to Exit", screenWidth / 2 - 90, screenHeight - 57);
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

        boolean isMoving = keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed;
        int currentFrameIndex = isMoving ? (animationFrame / 5) % 8 : 0;

        BufferedImage currentFrame = null;

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