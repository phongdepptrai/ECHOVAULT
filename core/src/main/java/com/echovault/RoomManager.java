package com.echovault;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class RoomManager {
    public int roomIndex = 1;
    public Rectangle bounds;
    public List<Entity> entities;
    public List<Bullet> bullets;
    public Player player;
    public Ghost ghost;

    // State
    public boolean roomCleared = false;
    public boolean itemPhase = false;
    public List<Item> activeItems = new ArrayList<>(); // Choices

    // Spawning logic
    private long seed;

    // Previous room data
    public List<InputFrame> lastRoomRecording;
    public Vector2 lastRoomPlayerStartPos;

    public RoomManager(Player player) {
        this.player = player;
        this.bounds = new Rectangle(50, 50, 1180, 620); // Arena walls
        this.entities = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.seed = System.currentTimeMillis();

        startRoom();
    }

    public void startRoom() {
        entities.clear();
        bullets.clear();
        roomCleared = false;
        itemPhase = false;

        // Spawn Enemies
        int enemyCount = 6 + MathUtils.random(6);
        for (int i = 0; i < enemyCount; i++) {
            float x = MathUtils.random(bounds.x + 50, bounds.x + bounds.width - 50);
            float y = MathUtils.random(bounds.y + 50, bounds.y + bounds.height - 50);
            // Ensure not too close to player start
            if (Vector2.dst(x, y, player.position.x, player.position.y) < 200) {
                 x += 200; // rough fix
            }
            entities.add(new Enemy(x, y, MathUtils.random(2)));
        }

        // Spawn Ghost if we have recording
        if (lastRoomRecording != null && !lastRoomRecording.isEmpty()) {
            boolean friendly = false;
            boolean forked = false;
            boolean delayed = false;

            // Check player items for Echo Mods
            for (Item item : player.items) {
                 if (item.type == ItemType.FRIENDLY_ECHO) friendly = true;
                 if (item.type == ItemType.FORKED_ECHO) forked = true;
                 if (item.type == ItemType.DELAYED_ECHO) delayed = true;
            }

            // Ghost spawn pos is relative to where player STARTED in PREVIOUS room.
            // We need to store that.
            // `lastRoomPlayerStartPos` should be set when room starts (or ends).
            // Actually, we need to know where player started *in the previous room*.
            // So we take `lastRoomPlayerStartPos` passed from prev room.

            if (lastRoomPlayerStartPos == null) lastRoomPlayerStartPos = new Vector2(640, 360); // default center

            ghost = new Ghost(lastRoomPlayerStartPos.x, lastRoomPlayerStartPos.y, lastRoomRecording, friendly, forked, delayed);
            entities.add(ghost);
        }

        // Save CURRENT start pos for NEXT room
        // Player position should be set by Door Logic before calling startRoom usually?
        // Or we capture it now.
        // Wait, if I entered from West door, my pos is West. I record that now.
        // But `lastRoomPlayerStartPos` is for the CURRENT ghost.
        // We need `currentRoomStartPos` to save for NEXT ghost.
        // Let's use `player.position` as the start pos for this room.
        // We will store it in a temp var and promote it to `lastRoomPlayerStartPos` on room transition.
    }

    private Vector2 currentRoomPlayerStartPos;

    public void update(float delta) {
        if (currentRoomPlayerStartPos == null) {
            currentRoomPlayerStartPos = new Vector2(player.position);
            player.inputHistory.clear(); // Start recording for this room
        }

        // Update Entities
        // Use Iterator to allow removal
        Iterator<Entity> it = entities.iterator();
        while (it.hasNext()) {
            Entity e = it.next();
            if (e instanceof Enemy) {
                ((Enemy)e).update(delta, player, this);
            } else if (e instanceof Ghost) {
                ((Ghost)e).update(delta, null, this);
            }

            // Constrain to bounds
            clampToBounds(e);

            if (e.dead) {
                if (e instanceof Enemy) ((Enemy)e).onDeath(this);
                it.remove();
            }
        }

        // Update Bullets
        Iterator<Bullet> bit = bullets.iterator();
        while (bit.hasNext()) {
            Bullet b = bit.next();
            b.update(delta);
            if (!bounds.contains(b.position)) b.dead = true;

            if (b.dead) {
                bit.remove();
                continue;
            }

            // Bullet Collision
            // Player
            if (b.team != 0 && b.getBounds().overlaps(player.getBounds())) {
                player.takeDamage(b.damage);
                b.dead = true;
                bit.remove();
                continue;
            }

            // Enemies/Ghost
            for (Entity e : entities) {
                if (b.team != e.team && b.getBounds().overlaps(e.getBounds())) {
                    e.takeDamage(b.damage);
                    b.dead = true;
                    // bit.remove(); // Done after break
                    break;
                }
            }
            if (b.dead) bit.remove();
        }

        // Check Clear
        if (!roomCleared && countEnemies() == 0) {
            roomCleared = true;
            spawnItems();
        }

        // Door Logic / Room Transition
        if (roomCleared && !itemPhase) { // itemPhase means items are on ground.
             // Wait, "After clearing a room, spawn 1 pickup choice".
             // We spawned items. Player picks one. Then doors open?
             // Simplification: Items are there, doors are also open?
             // Or must pick item to open doors?
             // Let's say doors are open, items are optional.

             checkDoors();
        }

        // Item Pickup Logic
        if (itemPhase) {
            Iterator<Item> iit = activeItems.iterator();
            while (iit.hasNext()) {
                Item item = iit.next();
                if (player.getBounds().overlaps(item.getBounds())) {
                    // Pick up
                    item.apply(player);
                    player.items.add(item);

                    // Clear other items (Choice)
                    activeItems.clear();
                    itemPhase = false; // Choice made
                    break;
                }
            }
        }
    }

    private void spawnItems() {
        itemPhase = true;
        activeItems.clear();
        // Spawn 3 random items
        float cx = 640;
        float cy = 360;

        for (int i = 0; i < 3; i++) {
            ItemType type = ItemType.values()[MathUtils.random(ItemType.values().length - 1)];
            activeItems.add(new Item(cx - 100 + i * 100, cy, type));
        }
    }

    private void checkDoors() {
        // Check if player hits edge of screen
        boolean transition = false;
        Vector2 nextPos = new Vector2(player.position);

        if (player.position.x < bounds.x - 10) { // Left Door
            nextPos.x = bounds.x + bounds.width - 50;
            transition = true;
        } else if (player.position.x > bounds.x + bounds.width + 10) { // Right Door
            nextPos.x = bounds.x + 50;
            transition = true;
        } else if (player.position.y < bounds.y - 10) { // Bottom Door
            nextPos.y = bounds.y + bounds.height - 50;
            transition = true;
        } else if (player.position.y > bounds.y + bounds.height + 10) { // Top Door
            nextPos.y = bounds.y + 50;
            transition = true;
        }

        if (transition) {
            // Prepare Next Room
            lastRoomRecording = new ArrayList<>(player.inputHistory);
            lastRoomPlayerStartPos = currentRoomPlayerStartPos;
            currentRoomPlayerStartPos = null; // Will be set in next update

            player.position.set(nextPos);
            roomIndex++;
            startRoom();
        }
    }

    private void clampToBounds(Entity e) {
        if (e.position.x < bounds.x) e.position.x = bounds.x;
        if (e.position.x > bounds.x + bounds.width) e.position.x = bounds.x + bounds.width;
        if (e.position.y < bounds.y) e.position.y = bounds.y;
        if (e.position.y > bounds.y + bounds.height) e.position.y = bounds.y + bounds.height;
    }

    private int countEnemies() {
        int count = 0;
        for (Entity e : entities) {
            if (e instanceof Enemy) count++;
            // Ghost counts as enemy if not friendly?
            // "When enemies cleared". Ghost is usually a threat.
            // If Ghost is friendly (team 0), don't count it.
            // If Ghost is enemy (team 1), count it.
            if (e instanceof Ghost && e.team == 1) count++;
        }
        return count;
    }

    public void addBullet(Bullet b) {
        bullets.add(b);
    }

    public void addEnemy(Enemy e) {
        entities.add(e);
    }
}
