package paradise.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class KeyHandler implements KeyListener {
    public boolean upPressed, downPressed, leftPressed, rightPressed;
    public boolean shiftPressed;
    public boolean ePressed;
    public boolean jPressed;
    public boolean tPressed;
    private boolean restartRequested;
    private boolean escPressed;
    private boolean enterPressed;
    private boolean navUpPressed, navDownPressed;

    // Name-input capture buffer
    public final StringBuilder nameBuffer = new StringBuilder();
    private char pendingChar = 0;
    private boolean backspacePressed = false;

    @Override
    public void keyTyped(KeyEvent e) {
        char c = e.getKeyChar();
        if (Character.isLetterOrDigit(c) || c == ' ') {
            if (nameBuffer.length() < 15) {
                pendingChar = Character.toUpperCase(c);
            }
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code == KeyEvent.VK_W || code == KeyEvent.VK_UP) { upPressed = true; navUpPressed = true; }
        if (code == KeyEvent.VK_S || code == KeyEvent.VK_DOWN) { downPressed = true; navDownPressed = true; }
        if (code == KeyEvent.VK_A || code == KeyEvent.VK_LEFT) leftPressed = true;
        if (code == KeyEvent.VK_D || code == KeyEvent.VK_RIGHT) rightPressed = true;
        if (code == KeyEvent.VK_SHIFT) shiftPressed = true;
        if (code == KeyEvent.VK_E) ePressed = true;
        if (code == KeyEvent.VK_SPACE) jPressed = true;
        if (code == KeyEvent.VK_T) tPressed = true;
        if (code == KeyEvent.VK_ESCAPE) escPressed = true;
        if (code == KeyEvent.VK_BACK_SPACE) backspacePressed = true;
        if (code == KeyEvent.VK_SPACE || code == KeyEvent.VK_ENTER) enterPressed = true;
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

    public boolean consumeJPressed() {
        boolean p = jPressed;
        jPressed = false;
        return p;
    }

    public boolean consumeTPressed() {
        boolean p = tPressed;
        tPressed = false;
        return p;
    }

    public boolean consumeEscPressed() {
        boolean p = escPressed;
        escPressed = false;
        return p;
    }

    public boolean consumeEnterPressed() {
        boolean p = enterPressed;
        enterPressed = false;
        return p;
    }

    public boolean consumeNavUp() {
        boolean p = navUpPressed;
        navUpPressed = false;
        return p;
    }

    public boolean consumeNavDown() {
        boolean p = navDownPressed;
        navDownPressed = false;
        return p;
    }

    public void clearMovement() {
        upPressed = downPressed = leftPressed = rightPressed = shiftPressed = ePressed = false;
    }

    /** Returns the most recently typed character (0 if none), consumed in the process. */
    public char consumeTypedChar() {
        char c = pendingChar;
        pendingChar = 0;
        return c;
    }

    /** Apply a backspace to the name buffer if one was pressed this frame. */
    public void consumeBackspace() {
        backspacePressed = false;
        if (nameBuffer.length() > 0) nameBuffer.deleteCharAt(nameBuffer.length() - 1);
    }

    public boolean isBackspacePressed() {
        return backspacePressed;
    }

    public void clearNameBuffer() {
        nameBuffer.setLength(0);
    }
}