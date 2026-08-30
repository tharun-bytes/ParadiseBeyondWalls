package paradise.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shiftPressed;
    public boolean ePressed;
    public boolean escapePressed;
    public boolean enterPressed;
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

        // Pause menu keys
        if (code == KeyEvent.VK_ESCAPE) escapePressed = true;
        if (code == KeyEvent.VK_ENTER) enterPressed = true;

        // Restart game (removed Enter to prevent pause menu conflicts)
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_R) restartRequested = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) upPressed = false;
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) downPressed = false;
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = false;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = false;
        if (code == KeyEvent.VK_SHIFT) shiftPressed = false;

        if (code == KeyEvent.VK_ESCAPE) escapePressed = false;
        if (code == KeyEvent.VK_ENTER) enterPressed = false;
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

    public boolean consumeEscapePressed() {
        boolean p = escapePressed;
        escapePressed = false;
        return p;
    }

    public boolean consumeEnterPressed() {
        boolean p = enterPressed;
        enterPressed = false;
        return p;
    }

    public void clearMovement() {
        upPressed = downPressed = leftPressed = rightPressed = shiftPressed = ePressed = false;
        escapePressed = enterPressed = false;
    }
}