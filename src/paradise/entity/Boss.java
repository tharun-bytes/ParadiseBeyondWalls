package paradise.entity;

import paradise.core.GamePanel;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

public class Boss {
    private final GamePanel gp;
    public int worldX, worldY;
    public String name;
    public int maxHp, hp;
    public int speed, damage;
    public boolean alive = true;
    public int size;

    private boolean enraged = false;
    private int attackCooldown = 0;
    private int chaseDelay = 0;
    private boolean active = false;

    // Animation variables
    private BufferedImage[] idleFrames;
    private BufferedImage[] attackFrames;
    private BufferedImage[] deathFrames;
    private int spriteCounter = 0;
    private int spriteIndex = 0;
    private boolean isAttacking = false;
    private int deathCounter = 0;

    public Boss(GamePanel gp, int col, int row, String name, int hp, int speed, int damage) {
        this.gp = gp;
        this.worldX = col * gp.tileSize;
        this.worldY = row * gp.tileSize;
        this.name = name;
        this.maxHp = hp;
        this.hp = hp;
        this.speed = speed;
        this.damage = damage;
        this.size = gp.tileSize * 3; // Bosses are 3x3 tiles large

        loadBossImages();
    }

    // --- Added to fix UI.java build error ---
    public String getName() {
        return name;
    }

    private void loadBossImages() {
        // Loads your specific Demon A files
        idleFrames = loadStrip("src/paradise/entity/Demon_A_Idle.png");
        attackFrames = loadStrip("src/paradise/entity/Demon_A_Attack01.png");
        deathFrames = loadStrip("src/paradise/entity/Demon_A_Death.png");
    }

    private BufferedImage[] loadStrip(String path) {
        try {
            BufferedImage sheet = ImageIO.read(new File(path));
            // Automatically calculate frame count based on the 100px width rule for this asset pack
            int frameCount = sheet.getWidth() / 100;
            BufferedImage[] sprites = new BufferedImage[frameCount];
            for (int i = 0; i < frameCount; i++) {
                sprites[i] = sheet.getSubimage(i * 100, 0, 100, 100);
            }
            return sprites;
        } catch (Exception e) {
            System.out.println("Could not load Boss image: " + path);
            return null;
        }
    }

    public Rectangle hitbox() {
        // Slightly smaller hitbox so the player can get close without instantly taking damage
        return new Rectangle(worldX + 16, worldY + 16, size - 32, size - 32);
    }

    private boolean moveBoss(int deltaX, int deltaY) {
        if (deltaX == 0 && deltaY == 0) return true;
        if (!canBossMove(deltaX, deltaY)) return false;
        worldX += deltaX;
        worldY += deltaY;
        return true;
    }

    private boolean canBossMove(int deltaX, int deltaY) {
        int nextX = worldX + deltaX;
        int nextY = worldY + deltaY;

        if (!gp.collisionChecker.canMove(worldX, worldY, size, size, deltaX, deltaY)) return false;

        Rectangle futureBox = new Rectangle(nextX + 8, nextY + 8, size - 16, size - 16);
        if (gp.mapBuildings != null) {
            for (paradise.object.Building b : gp.mapBuildings) {
                if (b != null && b.hitbox != null && b.hitbox.intersects(futureBox)) return false;
            }
        }

        if (gp.monsters != null) {
            for (Boss m : gp.monsters) {
                if (m != null && m != this && m.alive && m.hitbox().intersects(futureBox)) return false;
            }
        }

        return true;
    }

    public void takeDamage(int amount) {
        if (!alive) return;
        hp -= amount;

        if (hp <= 0) {
            alive = false;
            spriteIndex = 0; // Reset index to start the death animation from the beginning
        } else if (hp <= maxHp / 2 && !enraged) {
            enraged = true;
            speed += 1; // Boss moves faster when at half health
        }
    }

    public void update() {
        if (!alive) return;

        // Radius (in tiles) at which the Demon wakes up and engages the player
        int activationRange = 8 * gp.tileSize;

        int px = gp.playerX;
        int py = gp.playerY;

        int centerX = worldX + size / 2;
        int centerY = worldY + size / 2;
        int playerCenterX = px + gp.playerSize / 2;
        int playerCenterY = py + gp.playerSize / 2;

        long distSq = (long) (centerX - playerCenterX) * (centerX - playerCenterX)
                    + (long) (centerY - playerCenterY) * (centerY - playerCenterY);
        long activationRangeSq = (long) activationRange * activationRange;

        // Stay dormant until the player enters the 8-tile radius, then stay active
        if (!active && distSq <= activationRangeSq) {
            active = true;
        }

        animate();
        if (!active) return;

        if (attackCooldown > 0) {
            attackCooldown--;
            if (attackCooldown < 40) isAttacking = false; // End attack animation early in cooldown
        }

        // Once active, attack whenever the player is within range and the cooldown allows
        if (distSq <= activationRangeSq && attackCooldown == 0) {
            // Player is within 8 tiles -> launch an attack
            isAttacking = true;
            attackCooldown = 60; // 1 second cooldown before the next attack
            spriteIndex = 0; // Restart the attack animation
            gp.harmPlayerFromBoss(damage, centerX, centerY, this);
        } else {
            // Chase the player (stop momentarily while the attack winds up)
            chaseDelay++;
            boolean shouldMove = chaseDelay >= 2; // move every other frame -> half speed
            if (shouldMove) chaseDelay = 0;

            int dx = 0, dy = 0;
            if (worldX < px) dx = shouldMove ? speed : 0;
            if (worldX > px) dx = shouldMove ? -speed : 0;
            if (worldY < py) dy = shouldMove ? speed : 0;
            if (worldY > py) dy = shouldMove ? -speed : 0;

            int absDx = Math.abs(worldX - px);
            int absDy = Math.abs(worldY - py);

            boolean moved;
            if (absDx >= absDy) {
                moved = moveBoss(dx, 0) || moveBoss(0, dy);
            } else {
                moved = moveBoss(0, dy) || moveBoss(dx, 0);
            }

            if (shouldMove && !moved) {
                // Wedged against an obstacle: try strafing perpendicular to the target
                int sx = (dx == 0) ? (worldX < gp.worldWidth / 2 ? speed : -speed) : 0;
                int sy = (dy == 0) ? (worldY < gp.worldHeight / 2 ? speed : -speed) : 0;
                if (!moveBoss(sx, sy)) {
                    moveBoss(-sx, -sy);
                }
            }
        }

        worldX = Math.max(0, Math.min(worldX, gp.worldWidth - size));
        worldY = Math.max(0, Math.min(worldY, gp.worldHeight - size));
    }

    private void animate() {
        spriteCounter++;
        int animationSpeed = enraged ? 5 : 8; // Animate faster when enraged

        if (spriteCounter > animationSpeed) {
            BufferedImage[] currentArray = isAttacking ? attackFrames : idleFrames;
            if (currentArray != null) {
                spriteIndex++;
                if (spriteIndex >= currentArray.length) {
                    spriteIndex = 0;
                }
            }
            spriteCounter = 0;
        }
    }

    public void draw(Graphics2D g2, int frame) {
        int screenX = worldX - gp.playerX + gp.playerScreenX;
        int screenY = worldY - gp.playerY + gp.playerScreenY;

        if (gp.isOnScreen(worldX, worldY, size, size)) {

            BufferedImage imageToDraw = null;

            if (!alive) {
                // Play death animation once, then stay on the last frame
                deathCounter++;
                if (deathCounter > 10 && deathFrames != null && deathFrames.length > 0) {
                    if (spriteIndex < deathFrames.length - 1) {
                        spriteIndex++;
                    }
                    deathCounter = 0;
                }
                if (deathFrames != null && deathFrames.length > 0 && spriteIndex < deathFrames.length) {
                    imageToDraw = deathFrames[spriteIndex];
                }

            } else if (isAttacking && attackFrames != null) {
                if (spriteIndex < attackFrames.length) imageToDraw = attackFrames[spriteIndex];
            } else if (idleFrames != null) {
                if (spriteIndex < idleFrames.length) imageToDraw = idleFrames[spriteIndex];
            }

            // Draw the sprite scaled up slightly to fit your 2x2 boss size
            if (imageToDraw != null) {
                // Flip image horizontally if player is to the left so the boss faces the player
                if (gp.playerX < worldX) {
                    g2.drawImage(imageToDraw, screenX + size + 20, screenY - 20, -size - 40, size + 40, null);
                } else {
                    g2.drawImage(imageToDraw, screenX - 20, screenY - 20, size + 40, size + 40, null);
                }
            } else {
                // Fallback red box if the images fail to load due to a typo in the file path
                g2.setColor(Color.RED);
                g2.fillRect(screenX, screenY, size, size);
            }

            // Draw Health Bar above the boss
            if (alive) {
                int barWidth = size;
                int hpWidth = (int) (((double) hp / maxHp) * barWidth);
                g2.setColor(new Color(30, 30, 30, 200));
                g2.fillRect(screenX, screenY - 15, barWidth, 8);
                g2.setColor(Color.RED);
                g2.fillRect(screenX, screenY - 15, hpWidth, 8);
                g2.setColor(Color.WHITE);
                g2.drawRect(screenX, screenY - 15, barWidth, 8);
            }
        }
    }
}