import java.awt.Color;
import java.awt.Graphics2D;
import java.util.Random;

public class Titan {

    GamePanel gp;
    public int worldX, worldY;
    public int speed = 2; // Slower than player speed (4)
    public int size;

    public String direction = "down";
    public int actionLockCounter = 0;

    public Titan(GamePanel gp, int startCol, int startRow) {
        this.gp = gp;
        this.worldX = startCol * gp.tileSize;
        this.worldY = startRow * gp.tileSize;
        this.size = (int)(gp.tileSize * 1.5);
    }

    public void setAction() {
        // Line of Sight Math
        int diffX = Math.abs((worldX + size / 2) - (gp.playerX + gp.tileSize / 2));
        int diffY = Math.abs((worldY + size / 2) - (gp.playerY + gp.tileSize / 2));
        double distance = Math.hypot(diffX, diffY);

        // Chase mode within 250px
        if (distance < 250) {
            if (gp.playerX > worldX) worldX += speed;
            if (gp.playerX < worldX) worldX -= speed;
            if (gp.playerY > worldY) worldY += speed;
            if (gp.playerY < worldY) worldY -= speed;
        }
        // Patrol mode
        else {
            actionLockCounter++;
            if (actionLockCounter == 120) {
                Random random = new Random();
                int i = random.nextInt(100) + 1;

                if (i <= 25) direction = "up";
                else if (i <= 50) direction = "down";
                else if (i <= 75) direction = "left";
                else direction = "right";

                actionLockCounter = 0;
            }

            if (direction.equals("up") && worldY > gp.tileSize) worldY -= 1;
            if (direction.equals("down") && worldY < gp.worldHeight - gp.tileSize * 2) worldY += 1;
            if (direction.equals("left") && worldX > gp.tileSize) worldX -= 1;
            if (direction.equals("right") && worldX < gp.worldWidth - gp.tileSize * 2) worldX += 1;
        }
    }

    public void update() {
        setAction();
    }

    public void draw(Graphics2D g2) {
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        if (worldX + size > gp.playerX - gp.playerScreenX &&
                worldX - size < gp.playerX + gp.playerScreenX &&
                worldY + size > gp.playerY - gp.playerScreenY &&
                worldY - size < gp.playerY + gp.playerScreenY) {

            // Red Titan body
            g2.setColor(new Color(178, 34, 34));
            g2.fillRect(screenX, screenY, size, size);

            // Glowing yellow eyes
            g2.setColor(Color.YELLOW);
            g2.fillRect(screenX + 12, screenY + 12, 10, 10);
            g2.fillRect(screenX + size - 22, screenY + 12, 10, 10);
        }
    }
}