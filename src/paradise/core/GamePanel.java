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

/** Owns the fixed-rate game loop and connects the game's small systems. */
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

    // Player collision hitbox
    public final int playerSize = 28;

    // Sprite scale setting (2 = clean, balanced size)
    public int characterScale = 2;

    // Hiding state
    public boolean isHiding = false;
    public Building currentNearbyBuilding = null;

    public final KeyHandler keyHandler = new KeyHandler();
    public final TileManager tileManager = new TileManager(this);
    public final CollisionChecker collisionChecker = new CollisionChecker(this);
    public final UI ui = new UI(this);

    public int playerX;
    public int playerY;
    public final int playerSpeed = 4;
    public int playerHealth;

    public boolean facingRight = true;

    public BufferedImage[] playerRunImages = new BufferedImage[8];

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

    public GamePanel() {
        setPreferredSize(new Dimension(screenWidth, screenHeight));
        setBackground(new Color(10, 18, 31));
        setDoubleBuffered(true);
        addKeyListener(keyHandler);
        setFocusable(true);

        try {
            BufferedImage spriteSheet = ImageIO.read(new File("src/paradise/entity/run1.png"));
            int frameWidth = spriteSheet.getWidth() / 8;
            int frameHeight = spriteSheet.getHeight();

            for (int i = 0; i < 8; i++) {
                playerRunImages[i] = spriteSheet.getSubimage(i * frameWidth, 0, frameWidth, frameHeight);
            }
        } catch (IOException e) {
            System.out.println("Could not load run1.png from src/paradise/entity/ folder.");
        }

        setupBuildings();
        setupTrees();
        restartGame();
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
    }

    private final int[][] GREEN_MODELS = {
            {0, 64}, {32, 64}, {128, 64}, {160, 64}, {192, 64}, {224, 64},
            {0, 96}, {32, 96}, {96, 96}, {128, 96}, {160, 96}, {224, 96},
            {0, 128}, {32, 128}, {64, 128}, {96, 128}, {128, 128}, {160, 128}, {192, 128}, {224, 128},
            {0, 160}, {32, 160}, {64, 160}, {96, 160}, {128, 160}, {160, 160}, {192, 160}, {224, 160},
            {0, 192}, {32, 192}, {96, 192}, {128, 192}, {160, 192}, {192, 192}, {224, 192}
    };

    private final int[][] SNOW_MODELS = {
            {96, 224}, {128, 224}, {160, 224}, {192, 224}, {224, 224},
            {0, 256}, {32, 256}, {64, 256}, {96, 256}, {128, 256}
    };

    private final int[][] GOLD_MODELS = {
            {0, 0}, {32, 0}, {64, 0}, {96, 0}, {128, 0}, {160, 0}, {192, 0},
            {0, 32}, {32, 32}, {64, 32}, {96, 32}, {128, 32}, {160, 32},
            {64, 64}, {96, 64}, {64, 96}, {192, 96}
    };

    private void setupTrees() {
        ArrayList<Tree> treeList = new ArrayList<>();
        boolean[][] occupied = new boolean[maxWorldCol][maxWorldRow];
        Random rand = new Random(42);

        class ForestHelper {
            void plant(int col, int row, int[][] modelPool) {
                if (col < 0 || col >= maxWorldCol || row < 0 || row >= maxWorldRow) return;
                if (occupied[col][row]) return;

                // Keep East Beach Gate and shore completely open
                if (col >= 38 && col <= 49 && row >= 21 && row <= 28) return;
                // Keep West Exit Gate completely open
                if (col >= 0 && col <= 9 && row >= 21 && row <= 28) return;

                int treeX = col * tileSize;
                int treeY = row * tileSize;

                Rectangle treeBox = new Rectangle(treeX, treeY, tileSize, tileSize);
                if (mapBuildings != null) {
                    for (Building b : mapBuildings) {
                        if (b != null) {
                            Rectangle buildingBox = new Rectangle(b.worldX - 10, b.worldY - 10, b.drawWidth + 20, b.drawHeight + 20);
                            if (buildingBox.intersects(treeBox)) {
                                return;
                            }
                        }
                    }
                }

                occupied[col][row] = true;
                int[] crop = modelPool[rand.nextInt(modelPool.length)];
                treeList.add(new Tree(treeX, treeY, crop[0], crop[1]));
            }

            void plantCluster(int startCol, int endCol, int startRow, int endRow, double density, int[][] modelPool) {
                for (int c = startCol; c <= endCol; c++) {
                    for (int r = startRow; r <= endRow; r++) {
                        if (rand.nextDouble() < density) {
                            plant(c, r, modelPool);
                        }
                    }
                }
            }
        }

        ForestHelper forest = new ForestHelper();
        forest.plantCluster(28, 38, 6, 16, 0.35, SNOW_MODELS);
        forest.plantCluster(28, 38, 32, 42, 0.35, SNOW_MODELS);
        forest.plantCluster(10, 20, 6, 16, 0.35, GREEN_MODELS);
        forest.plantCluster(10, 20, 32, 42, 0.35, GREEN_MODELS);

        for (int c = 12; c < 38; c++) {
            for (int r = 12; r < 38; r++) {
                if (rand.nextDouble() < 0.05) {
                    forest.plant(c, r, GREEN_MODELS);
                }
            }
        }

        forest.plant(20, 20, GOLD_MODELS);
        forest.plant(21, 20, GOLD_MODELS);
        forest.plant(29, 21, GOLD_MODELS);
        forest.plant(21, 30, GOLD_MODELS);
        forest.plant(30, 29, GOLD_MODELS);

        mapTrees = treeList.toArray(new Tree[0]);
    }

    public void startGameThread() {
        gameThread = new Thread(this, "paradise-game-loop");
        gameThread.start();
    }

    @Override
    public void run() {
        final double drawInterval = 1_000_000_000.0 / FPS;
        double nextDrawTime = System.nanoTime();

        while (gameThread != null) {
            update();
            repaint();
            nextDrawTime += drawInterval;
            long waitMillis = (long) ((nextDrawTime - System.nanoTime()) / 1_000_000.0);
            if (waitMillis > 0) {
                try {
                    Thread.sleep(waitMillis);
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public void update() {
        animationFrame++;

        if (gameState == GameState.GAME_OVER || gameState == GameState.VICTORY) {
            if (keyHandler.consumeRestartRequest()) restartGame();
            return;
        }

        if (gameState == GameState.LEVEL_TRANSITION) {
            transitionTimer++;
            if (transitionTimer >= TRANSITION_DURATION) {
                if (currentLevel == LEVEL_COUNT) {
                    gameState = GameState.VICTORY;
                } else {
                    loadLevel(currentLevel + 1);
                }
            }
            return;
        }

        // 1. Ghosts always update & patrol
        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                if (ghost != null) {
                    ghost.update();
                }
            }
        }

        if (hitCooldown > 0) hitCooldown--;

        checkHouseProximity();

        // 2. Handle 'E' press to hide / exit house
        if (keyHandler.consumeEPressed()) {
            if (isHiding) {
                isHiding = false;
            } else if (currentNearbyBuilding != null) {
                isHiding = true;
                keyHandler.clearMovement();
            }
        }

        // 3. Process movement, ghost damage, and point collection when outside
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

    private void checkHouseProximity() {
        currentNearbyBuilding = null;
        Rectangle playerBox = new Rectangle(playerX - 10, playerY - 10, playerSize + 20, playerSize + 20);

        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null) {
                    Rectangle bBox = new Rectangle(b.worldX, b.worldY, b.drawWidth, b.drawHeight);
                    if (bBox.intersects(playerBox)) {
                        currentNearbyBuilding = b;
                        break;
                    }
                }
            }
        }
    }

    private void movePlayer() {
        if (keyHandler.leftPressed) facingRight = false;
        if (keyHandler.rightPressed) facingRight = true;

        int horizontal = (keyHandler.rightPressed ? playerSpeed : 0) - (keyHandler.leftPressed ? playerSpeed : 0);
        int vertical = (keyHandler.downPressed ? playerSpeed : 0) - (keyHandler.upPressed ? playerSpeed : 0);
        tryMovePlayer(horizontal, 0);
        tryMovePlayer(0, vertical);
    }

    private void tryMovePlayer(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) return;

        if (!collisionChecker.canMove(playerX, playerY, playerSize, playerSize, deltaX, deltaY)) {
            return;
        }

        Rectangle futurePlayerBox = new Rectangle(playerX + deltaX, playerY + deltaY, playerSize, playerSize);

        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(futurePlayerBox)) {
                    return;
                }
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
        hitCooldown = 120; // 2 seconds of invulnerability

        if (ghosts != null) {
            for (Ghost ghost : ghosts) {
                if (ghost != null) {
                    ghost.resetToSpawn();
                }
            }
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

            Rectangle pointBox = new Rectangle(point.worldX, point.worldY, tileSize, tileSize);
            if (playerBox.intersects(pointBox)) {
                capturePoints[i] = null;
                capturedPoints++;
            }
        }
    }

    private void checkDoorTransition() {
        if (capturedPoints == levelConfig.pointTiles.length) {
            int playerCol = (playerX + playerSize / 2) / tileSize;
            int playerRow = (playerY + playerSize / 2) / tileSize;

            // Trigger level progression when stepping onto West Gate exit
            if (playerCol <= 4 && (playerRow >= 23 && playerRow <= 26)) {
                gameState = GameState.LEVEL_TRANSITION;
                transitionTimer = 0;
                keyHandler.clearMovement();
            }
        }
    }

    public void restartGame() {
        playerHealth = 3;
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
        for (int index = 0; index < capturePoints.length; index++) {
            int[] tile = levelConfig.pointTiles[index];
            capturePoints[index] = new CapturePoint(this, tile[0], tile[1]);
        }
        for (int index = 0; index < ghosts.length; index++) {
            int[] tile = levelConfig.ghostTiles[index];
            ghosts[index] = new Ghost(this, tile[0], tile[1], levelConfig.ghostSpeed);
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
        facingRight = false;
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
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        tileManager.draw(graphics2D);

        if (mapBuildings != null) {
            for (Building b : mapBuildings) {
                if (b != null) b.draw(graphics2D, this);
            }
        }

        if (mapTrees != null) {
            for (Tree t : mapTrees) {
                if (t != null) t.draw(graphics2D, this);
            }
        }

        for (CapturePoint point : capturePoints) {
            if (point != null) point.draw(graphics2D, this, animationFrame);
        }
        for (Ghost ghost : ghosts) {
            ghost.draw(graphics2D, animationFrame);
        }

        if (!isHiding) {
            drawPlayer(graphics2D);
        }

        drawInteractionPrompt(graphics2D);
        ui.draw(graphics2D);
        graphics2D.dispose();
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

        if (playerRunImages != null && playerRunImages[0] != null) {

            boolean isMoving = keyHandler.upPressed || keyHandler.downPressed || keyHandler.leftPressed || keyHandler.rightPressed;
            int currentFrame = 0;

            if (isMoving) {
                currentFrame = (animationFrame / 6) % 8;
            }

            int originalWidth = playerRunImages[currentFrame].getWidth();
            int originalHeight = playerRunImages[currentFrame].getHeight();

            int drawWidth = originalWidth * characterScale;
            int drawHeight = originalHeight * characterScale;

            int drawX = x - (drawWidth - playerSize) / 2;
            int drawY = y - (drawHeight - playerSize) / 2;

            if (facingRight) {
                graphics.drawImage(playerRunImages[currentFrame], drawX, drawY, drawWidth, drawHeight, null);
            } else {
                graphics.drawImage(playerRunImages[currentFrame], drawX + drawWidth, drawY, -drawWidth, drawHeight, null);
            }

        } else {
            graphics.setColor(Color.MAGENTA);
            graphics.fillRect(x, y, playerSize, playerSize);
        }
    }
}