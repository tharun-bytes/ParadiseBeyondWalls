package paradise.core;

import paradise.entity.Ghost;
import paradise.input.KeyHandler;
import paradise.object.CapturePoint;
import paradise.ui.UI;
import paradise.world.CollisionChecker;
import paradise.world.TileManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

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
    public final int playerSize = 34;

    public final KeyHandler keyHandler = new KeyHandler();
    public final TileManager tileManager = new TileManager(this);
    public final CollisionChecker collisionChecker = new CollisionChecker(this);
    public final UI ui = new UI(this);

    public int playerX;
    public int playerY;
    public final int playerSpeed = 4;
    public int playerHealth;
    public int currentLevel;
    public int capturedPoints;
    public int animationFrame;
    public GameState gameState;
    public LevelConfig levelConfig;
    public CapturePoint[] capturePoints;
    public Ghost[] ghosts;

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
        restartGame();
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

        movePlayer();
        if (hitCooldown > 0) hitCooldown--;

        for (Ghost ghost : ghosts) {
            ghost.update();
            if (hitCooldown == 0 && playerTouches(ghost.worldX, ghost.worldY, ghost.size)) {
                harmPlayer();
                break;
            }
        }
        collectCapturePoints();
    }

    private void movePlayer() {
        int horizontal = (keyHandler.rightPressed ? playerSpeed : 0) - (keyHandler.leftPressed ? playerSpeed : 0);
        int vertical = (keyHandler.downPressed ? playerSpeed : 0) - (keyHandler.upPressed ? playerSpeed : 0);
        tryMovePlayer(horizontal, 0);
        tryMovePlayer(0, vertical);
    }

    private void tryMovePlayer(int deltaX, int deltaY) {
        if ((deltaX != 0 || deltaY != 0) && collisionChecker.canMove(playerX, playerY, playerSize, playerSize, deltaX, deltaY)) {
            playerX += deltaX;
            playerY += deltaY;
        }
    }

    private boolean playerTouches(int entityX, int entityY, int entitySize) {
        int playerCenterX = playerX + playerSize / 2;
        int playerCenterY = playerY + playerSize / 2;
        int entityCenterX = entityX + entitySize / 2;
        int entityCenterY = entityY + entitySize / 2;
        return Math.hypot(playerCenterX - entityCenterX, playerCenterY - entityCenterY) < (playerSize + entitySize) / 2 - 7;
    }

    private void harmPlayer() {
        playerHealth--;
        hitCooldown = 85;
        movePlayerToSpawn();
        if (playerHealth <= 0) {
            gameState = GameState.GAME_OVER;
            keyHandler.clearMovement();
        }
    }

    private void collectCapturePoints() {
        for (int index = 0; index < capturePoints.length; index++) {
            CapturePoint point = capturePoints[index];
            if (point == null) continue;
            if (playerTouches(point.worldX + 7, point.worldY + 7, tileSize - 14)) {
                capturePoints[index] = null;
                capturedPoints++;
            }
        }
        if (capturedPoints == levelConfig.pointTiles.length) {
            gameState = GameState.LEVEL_TRANSITION;
            transitionTimer = 0;
            keyHandler.clearMovement();
        }
    }

    public void restartGame() {
        playerHealth = 3;
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
        gameState = GameState.PLAYING;
        keyHandler.clearMovement();
    }

    private void movePlayerToSpawn() {
        playerX = 25 * tileSize + 7;
        playerY = 25 * tileSize + 7;
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
        for (CapturePoint point : capturePoints) {
            if (point != null) point.draw(graphics2D, this, animationFrame);
        }
        for (Ghost ghost : ghosts) {
            ghost.draw(graphics2D, animationFrame);
        }
        drawPlayer(graphics2D);
        ui.draw(graphics2D);
        graphics2D.dispose();
    }

    private void drawPlayer(Graphics2D graphics) {
        int x = playerScreenX + (tileSize - playerSize) / 2;
        int y = playerScreenY + (tileSize - playerSize) / 2;
        if (hitCooldown > 0 && (animationFrame / 6) % 2 == 0) return;

        graphics.setColor(new Color(241, 184, 77, 80));
        graphics.fillOval(x - 5, y + 19, playerSize + 10, playerSize - 5);
        graphics.setColor(new Color(246, 223, 164));
        graphics.fillOval(x + 8, y + 2, 18, 18);
        graphics.setColor(new Color(42, 65, 98));
        graphics.fillRoundRect(x + 5, y + 17, 24, 18, 9, 9);
        graphics.setColor(new Color(255, 207, 84));
        graphics.fillRect(x + 4, y, 26, 6);
        graphics.setColor(new Color(20, 31, 49));
        graphics.fillOval(x + 13, y + 9, 3, 4);
        graphics.fillOval(x + 21, y + 9, 3, 4);
    }
}
