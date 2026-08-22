package paradise.world;

import paradise.core.GamePanel;

/** Resolves movement against the current level's tile walls. */
public class CollisionChecker {
    private final GamePanel game;

    public CollisionChecker(GamePanel game) {
        this.game = game;
    }

    public boolean canMove(int x, int y, int width, int height, int deltaX, int deltaY) {
        int nextLeft = x + deltaX;
        int nextTop = y + deltaY;
        int nextRight = nextLeft + width - 1;
        int nextBottom = nextTop + height - 1;

        int leftColumn = nextLeft / game.tileSize;
        int rightColumn = nextRight / game.tileSize;
        int topRow = nextTop / game.tileSize;
        int bottomRow = nextBottom / game.tileSize;

        if (leftColumn < 0 || topRow < 0 || rightColumn >= game.maxWorldCol || bottomRow >= game.maxWorldRow) {
            return false;
        }

        return !game.tileManager.isBlocked(leftColumn, topRow)
                && !game.tileManager.isBlocked(rightColumn, topRow)
                && !game.tileManager.isBlocked(leftColumn, bottomRow)
                && !game.tileManager.isBlocked(rightColumn, bottomRow);
    }
}
