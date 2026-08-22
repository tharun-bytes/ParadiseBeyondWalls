import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class GamePanel extends JPanel implements Runnable {

    // Screen Settings
    final int originalTileSize = 16;
    final int scale = 3;
    public final int tileSize = originalTileSize * scale; // 48x48

    public final int maxScreenCol = 16;
    public final int maxScreenRow = 12;
    public final int screenWidth = tileSize * maxScreenCol;   // 768 px
    public final int screenHeight = tileSize * maxScreenRow; // 576 px

    // World Map Settings
    public final int maxWorldCol = 50;
    public final int maxWorldRow = 50;
    public final int worldWidth = tileSize * maxWorldCol;
    public final int worldHeight = tileSize * maxWorldRow;

    // Fixed Screen Center for Player Camera
    public final int playerScreenX = screenWidth / 2 - (tileSize / 2);
    public final int playerScreenY = screenHeight / 2 - (tileSize / 2);

    // Player State
    public int playerX = tileSize * 25;
    public int playerY = tileSize * 25;
    public int playerSpeed = 4;
    public int playerHealth = 3; // 3 Lives
    public boolean collisionOn = false;

    // Inventory
    public int scrapMetal = 0;

    // Systems & Entities
    final int FPS = 60;
    public KeyHandler keyH = new KeyHandler();
    public TileManager tileM = new TileManager(this);
    public CollisionChecker cChecker = new CollisionChecker(this);
    public AssetSetter aSetter = new AssetSetter(this);
    public UI ui = new UI(this);
    public SuperObject[] obj = new SuperObject[10];
    public Titan[] titans = new Titan[5]; // Stores active Titans
    Thread gameThread;

    public GamePanel() {
        this.setPreferredSize(new Dimension(screenWidth, screenHeight));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(keyH);
        this.setFocusable(true);

        // Spawn items and enemies
        aSetter.setObject();
        aSetter.setTitans();
    }

    public void startGameThread() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        double drawInterval = 1000000000.0 / FPS;
        double nextDrawTime = System.nanoTime() + drawInterval;

        while (gameThread != null) {
            update();
            repaint();

            try {
                double remainingTime = (nextDrawTime - System.nanoTime()) / 1000000.0;
                if (remainingTime < 0) remainingTime = 0;
                Thread.sleep((long) remainingTime);
                nextDrawTime += drawInterval;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void update() {
        // 1. Move Player
        collisionOn = false;
        cChecker.checkTile(this);

        if (!collisionOn) {
            if (keyH.upPressed) playerY -= playerSpeed;
            if (keyH.downPressed) playerY += playerSpeed;
            if (keyH.leftPressed) playerX -= playerSpeed;
            if (keyH.rightPressed) playerX += playerSpeed;
        }

        // 2. Update Titans & Check Hit Collision
        for (int i = 0; i < titans.length; i++) {
            if (titans[i] != null) {
                titans[i].update();

                // If Titan touches player, reset player to safe spawn and reduce health
                if (Math.abs(playerX - titans[i].worldX) < 40 && Math.abs(playerY - titans[i].worldY) < 40) {
                    playerHealth--;
                    playerX = tileSize * 25; // Respawn in safe center
                    playerY = tileSize * 25;
                }
            }
        }

        // 3. Collect Items
        pickUpObject();
    }

    public void pickUpObject() {
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                if (Math.abs(playerX - obj[i].worldX) < 30 && Math.abs(playerY - obj[i].worldY) < 30) {
                    scrapMetal++;
                    obj[i] = null;
                }
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // 1. Draw Map
        tileM.draw(g2);

        // 2. Draw Items
        for (int i = 0; i < obj.length; i++) {
            if (obj[i] != null) {
                obj[i].draw(g2, this);
            }
        }

        // 3. Draw Titans
        for (int i = 0; i < titans.length; i++) {
            if (titans[i] != null) {
                titans[i].draw(g2);
            }
        }

        // 4. Draw Player
        g2.setColor(Color.WHITE);
        g2.fillRect(playerScreenX, playerScreenY, tileSize, tileSize);

        // 5. Draw UI HUD
        ui.draw(g2);

        g2.dispose();
    }
}