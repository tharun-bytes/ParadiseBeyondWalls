package paradise.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shiftPressed;
    public boolean ePressed;
    private boolean restartRequested;

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = true;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = true;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_SHIFT) shiftPressed = true;
        if (code == KeyEvent.VK_E) ePressed = true;
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER || code == KeyEvent.VK_R) restartRequested = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_SHIFT) shiftPressed = false;
    }

    public boolean consumeRestartRequest() {
        boolean r = restartRequested;
        restartRequested = false;
        return r;
    }

    public boolean consumeEPressed() {
        boolean p = ePressed;
        ePressed = false;
        return p;
    }

    public void clearMovement() {
        upPressed = downPressed = leftPressed = rightPressed = shiftPressed = ePressed = false;
    }
}