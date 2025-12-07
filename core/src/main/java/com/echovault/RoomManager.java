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
    private com.badlogic.gdx.math.RandomXS128 rng; // Deterministic RNG

    // Previous room data
    public List<InputFrame> lastRoomRecording;
    public Vector2 lastRoomPlayerStartPos;
    public Vector2 currentRoomPlayerStartPos;

    public RoomManager(Player player) {
        this.player = player;
        this.bounds = new Rectangle(50, 50, 1180, 620); // Arena walls
        this.entities = new ArrayList<>();
        this.bullets = new ArrayList<>();
        this.seed = System.currentTimeMillis();
        this.rng = new com.badlogic.gdx.math.RandomXS128(seed);

        startRoom();
    }

    public void startRoom() {
        entities.clear();
        bullets.clear();
        roomCleared = false;
        itemPhase = false;

        // Reseed for room
        rng.setSeed(seed + roomIndex * 1000);
        MathUtils.random.setSeed(seed + roomIndex * 1000); // Also sync global for helpers

        if (roomIndex > 0 && roomIndex % 6 == 0) {
            // BOSS ROOM
            spawnBoss();
        } else {
            // REGULAR ROOM
            spawnWave();
        }

        // Spawn Ghost if we have recording (Not in boss room? Or yes?)
        if (roomIndex % 6 != 0 && lastRoomRecording != null && !lastRoomRecording.isEmpty()) {
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
            if (lastRoomPlayerStartPos == null) lastRoomPlayerStartPos = new Vector2(640, 360); // default center

            ghost = new Ghost(lastRoomPlayerStartPos.x, lastRoomPlayerStartPos.y, lastRoomRecording, friendly, forked, delayed);
            entities.add(ghost);
        }

        // Save CURRENT start pos for NEXT room
    }

    private void spawnBoss() {
        // Center of room
        float cx = bounds.x + bounds.width/2;
        float cy = bounds.y + bounds.height/2;

        if ((roomIndex / 6) % 2 != 0) {
            // Odd boss index (6, 18..): Curator
            entities.add(new com.echovault.bosses.CuratorBoss(cx, cy));
        } else {
            // Even boss index (12, 24..): Bellwether
            entities.add(new com.echovault.bosses.BellwetherBoss(cx, cy));
        }
    }

    private void spawnWave() {
        int budget = 12 + roomIndex * 2; // Increase diff
        // Cap budget?

        while (budget > 0) {
            // Select archetype
            EnemyArchetype type = EnemyArchetype.values()[rng.nextInt(EnemyArchetype.values().length)];
            int cost = getCost(type);

            if (cost <= budget) {
                float x = rng.nextFloat() * (bounds.width - 100) + bounds.x + 50;
                float y = rng.nextFloat() * (bounds.height - 100) + bounds.y + 50;

                if (Vector2.dst(x, y, player.position.x, player.position.y) < 250) continue;

                Enemy e = createEnemy(type, x, y);

                // Elite chance
                if (roomIndex >= 6 && rng.nextFloat() < 0.15f) {
                    e.makeElite();
                    budget -= 2; // Elites cost more?
                }

                entities.add(e);
                budget -= cost;
            } else {
                budget--; // Avoid infinite loop if only cheap units left
            }
        }
    }

    private int getCost(EnemyArchetype type) {
        switch(type) {
            case SWARMER: return 1;
            case ORBITER: return 2;
            case HOPPER: return 2;
            case CHASER: return 3;
            case WANDERER: return 3;
            case SHIELDED: return 4;
            case TURRET: return 4;
            case BOMBER: return 5;
            case LASER_TELEGRAPH: return 5;
            case SPLITTER_PLUS: return 6;
            default: return 1;
        }
    }

    private Enemy createEnemy(EnemyArchetype type, float x, float y) {
        switch(type) {
            case CHASER: return new com.echovault.enemies.Chaser(x, y);
            case HOPPER: return new com.echovault.enemies.Hopper(x, y);
            case WANDERER: return new com.echovault.enemies.Wanderer(x, y);
            case TURRET: return new com.echovault.enemies.Turret(x, y);
            case ORBITER: return new com.echovault.enemies.Orbiter(x, y);
            case BOMBER: return new com.echovault.enemies.Bomber(x, y);
            case SPLITTER_PLUS: return new com.echovault.enemies.SplitterPlus(x, y);
            case SHIELDED: return new com.echovault.enemies.ShieldedEnemy(x, y);
            case LASER_TELEGRAPH: return new com.echovault.enemies.LaserEnemy(x, y);
            case SWARMER: return new com.echovault.enemies.Swarmer(x, y);
            default: return new com.echovault.enemies.Chaser(x, y);
        }
    }

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

            // Handle new ProjectileEntity logic which needs RoomManager
            if (b instanceof com.echovault.combat.projectile.ProjectileEntity) {
                ((com.echovault.combat.projectile.ProjectileEntity)b).update(delta, this);
            } else {
                b.update(delta);
            }

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
                    // Shield Check
                    if (e instanceof com.echovault.enemies.ShieldedEnemy) {
                        if (((com.echovault.enemies.ShieldedEnemy)e).blocksDamage(b.velocity)) {
                             // Blocked!
                             // Visual effect?
                             b.dead = true;
                             break;
                        }
                    }
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

    public void addEnemy(Entity e) {
        entities.add(e);
    }
}
