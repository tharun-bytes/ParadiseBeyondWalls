import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class UI {

    GamePanel gp;
    Font arial_20;
    Font arial_40;

    public UI(GamePanel gp) {
        this.gp = gp;
        arial_20 = new Font("Arial", Font.BOLD, 18);
        arial_40 = new Font("Arial", Font.BOLD, 36);
    }

    public void draw(Graphics2D g2) {
        g2.setFont(arial_20);

        // Draw Quest Box in Top-Left
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(15, 15, 300, 95, 10, 10);

        g2.setColor(Color.YELLOW);
        g2.drawString("Level 1: Wall Maria", 25, 40);

        g2.setColor(Color.WHITE);
        g2.drawString("Ship Hull Scrap: " + gp.scrapMetal + " / 5", 25, 68);

        // Hearts / Lives
        g2.setColor(Color.RED);
        g2.drawString("Lives: " + "♥ ".repeat(Math.max(0, gp.playerHealth)), 25, 95);

        // Game Over Screen
        if (gp.playerHealth <= 0) {
            g2.setColor(new Color(0, 0, 0, 200));
            g2.fillRect(0, 0, gp.screenWidth, gp.screenHeight);

            g2.setFont(arial_40);
            g2.setColor(Color.RED);
            g2.drawString("DEVOURED BY TITANS", gp.screenWidth / 2 - 200, gp.screenHeight / 2);
        }

        if (gp.scrapMetal >= 5) {
            g2.setColor(Color.GREEN);
            g2.drawString("Gate Unlocked! Proceed to Wall Rose", 25, 135);
        }
    }
}