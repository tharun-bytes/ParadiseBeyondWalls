package paradise.ui;

import paradise.core.GamePanel;
import paradise.core.GameState;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

/** HUD and modal overlays; it intentionally contains no game-state mutations. */
public class UI {
    private final GamePanel game;
    private final Font brandFont = new Font("SansSerif", Font.BOLD, 17);
    private final Font bodyFont = new Font("SansSerif", Font.BOLD, 14);
    private final Font smallFont = new Font("SansSerif", Font.PLAIN, 12);
    private final Font modalFont = new Font("SansSerif", Font.BOLD, 34);

    public UI(GamePanel game) {
        this.game = game;
    }

    public void draw(Graphics2D graphics) {
        drawHud(graphics);
        if (game.gameState != GameState.PLAYING) drawOverlay(graphics);
    }

    private void drawHud(Graphics2D graphics) {
        graphics.setColor(new Color(8, 15, 28, 220));
        graphics.fillRoundRect(16, 16, 334, 112, 14, 14);
        graphics.setColor(new Color(255, 203, 76));
        graphics.fillRoundRect(16, 16, 6, 112, 6, 6);

        graphics.setFont(brandFont);
        graphics.setColor(new Color(244, 246, 250));
        graphics.drawString("PARADISE // BEYOND WALLS", 34, 42);
        graphics.setFont(smallFont);
        graphics.setColor(new Color(154, 191, 211));
        graphics.drawString(String.format("LEVEL %02d  •  %s", game.currentLevel, game.levelConfig.title.toUpperCase()), 34, 62);

        graphics.setFont(bodyFont);
        graphics.setColor(Color.WHITE);
        graphics.drawString("CAPTURE POINTS", 34, 88);
        String count = game.capturedPoints + " / " + game.levelConfig.pointTiles.length;
        graphics.drawString(count, 292, 88);
        graphics.setColor(new Color(30, 48, 67));
        graphics.fillRoundRect(34, 97, 294, 10, 5, 5);
        int progress = game.levelConfig.pointTiles.length == 0 ? 0 : 294 * game.capturedPoints / game.levelConfig.pointTiles.length;
        graphics.setColor(new Color(255, 202, 76));
        graphics.fillRoundRect(34, 97, progress, 10, 5, 5);

        graphics.setColor(new Color(8, 15, 28, 220));
        graphics.fillRoundRect(game.screenWidth - 167, 16, 151, 72, 14, 14);
        graphics.setFont(smallFont);
        graphics.setColor(new Color(154, 191, 211));
        graphics.drawString("INTEGRITY", game.screenWidth - 148, 40);
        graphics.setFont(bodyFont);
        graphics.setColor(new Color(255, 104, 104));
        graphics.drawString(hearts(), game.screenWidth - 148, 64);

        graphics.setColor(new Color(8, 15, 28, 190));
        graphics.fillRoundRect(16, game.screenHeight - 40, 317, 24, 12, 12);
        graphics.setFont(smallFont);
        graphics.setColor(new Color(222, 234, 241));
        graphics.drawString("MOVE  WASD / ARROWS     AVOID THE GHOSTS", 29, game.screenHeight - 24);
    }

    private String hearts() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) result.append(i < game.playerHealth ? "\u2665 " : "\u2661 ");
        return result.toString();
    }

    private void drawOverlay(Graphics2D graphics) {
        graphics.setColor(new Color(5, 10, 19, 182));
        graphics.fillRect(0, 0, game.screenWidth, game.screenHeight);

        String title;
        String message;
        Color accent;
        if (game.gameState == GameState.LEVEL_TRANSITION) {
            title = "SECTOR SECURED";
            message = game.currentLevel == GamePanel.LEVEL_COUNT ? "The final gate is opening..." : "Preparing the next wall...";
            accent = new Color(255, 211, 82);
        } else if (game.gameState == GameState.VICTORY) {
            title = "YOU ESCAPED";
            message = "Every capture point has been stabilised. Press R or Enter to play again.";
            accent = new Color(109, 235, 169);
        } else {
            title = "LOST TO THE MIST";
            message = "The ghosts closed in. Press R or Enter to restart.";
            accent = new Color(255, 112, 112);
        }

        graphics.setColor(new Color(11, 22, 38, 240));
        int cardWidth = 530;
        int cardHeight = 170;
        int cardX = (game.screenWidth - cardWidth) / 2;
        int cardY = (game.screenHeight - cardHeight) / 2;
        graphics.fillRoundRect(cardX, cardY, cardWidth, cardHeight, 18, 18);
        graphics.setColor(accent);
        graphics.fillRoundRect(cardX, cardY, cardWidth, 7, 7, 7);
        graphics.setFont(modalFont);
        drawCentered(graphics, title, game.screenWidth / 2, cardY + 72, accent);
        graphics.setFont(bodyFont);
        drawCentered(graphics, message, game.screenWidth / 2, cardY + 112, new Color(231, 238, 243));
    }

    private void drawCentered(Graphics2D graphics, String text, int centerX, int baselineY, Color color) {
        FontMetrics metrics = graphics.getFontMetrics();
        graphics.setColor(color);
        graphics.drawString(text, centerX - metrics.stringWidth(text) / 2, baselineY);
    }
}
