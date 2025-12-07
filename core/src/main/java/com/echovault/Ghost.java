package com.echovault;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.List;

public class Ghost extends Player {
    private List<InputFrame> recording;
    private int playbackIndex = 0;

    // Echo Mods
    public boolean friendly = false;
    public boolean forked = false;
    public boolean delayed = false;
    public int delayFrames = 0;

    public Ghost(float x, float y, List<InputFrame> recording, boolean friendly, boolean forked, boolean delayed) {
        super(x, y);
        this.recording = recording;
        this.friendly = friendly;
        this.forked = forked;
        this.delayed = delayed;

        // Setup stats based on mods
        if (friendly) {
            this.team = 0; // Ally (Wait, Player is team 0. Enemy is 1. Friendly Ghost should match Player team to not hurt player, but logic needs to know it hurts enemies.)
            // Collision logic:
            // - Bullets from team 0 (Player) hurt team 1 (Enemy).
            // - Bullets from team 1 (Enemy) hurt team 0 (Player).
            // If Ghost is team 0, it won't hurt Player, and its bullets (team 0) will hurt Enemies. CORRECT.
        } else {
            this.team = 1; // Enemy
        }

        if (delayed) {
            delayFrames = 30; // 0.5s at 60fps
        }

        // Ghost visuals or stats?
        this.hp = 15; // Hardcoded Ghost HP
        this.maxHp = 15;
    }

    @Override
    public void update(float delta, InputFrame dummyInput, RoomManager roomManager) {
        // We ignore dummyInput and read from recording

        int frameToPlay = playbackIndex;
        if (delayed) {
            frameToPlay -= delayFrames;
        }

        InputFrame currentInput;
        if (frameToPlay >= 0 && frameToPlay < recording.size()) {
            currentInput = recording.get(frameToPlay);
        } else {
            currentInput = new InputFrame(); // idle
            // If recording ended, ghost just stands there or we can loop?
            // "replays the previous room’s recorded inputs" -> Usually once.
            // When recording ends, maybe ghost dies? Or stops?
            // Let's make it stop.
        }

        // Handle Forked Echo (Modifies shooting)
        // We override shoot logic in Player, but Player.update calls shoot.
        // We can override shoot method.

        super.update(delta, currentInput, roomManager);

        playbackIndex++;
    }

    @Override
    public void render(ShapeRenderer sr) {
        if (friendly) sr.setColor(0, 1, 0, 0.5f); // Transparent Green
        else sr.setColor(1, 0, 0, 0.5f); // Transparent Red

        sr.circle(position.x, position.y, radius);
        // Maybe draw "GHOST" text or eyes
    }

    // Forked logic needs to override shoot, but shoot is private in Player?
    // Let's check Player.java. I made it private.
    // I should make it protected.

    @Override
    protected void shoot(float angle, RoomManager roomManager) {
        if (forked) {
            // 2 bullets, reduced damage
            Bullet b1 = new Bullet(position.x, position.y, angle - 10, 600f, 0.5f, team);
            Bullet b2 = new Bullet(position.x, position.y, angle + 10, 600f, 0.5f, team);
            roomManager.addBullet(b1);
            roomManager.addBullet(b2);
        } else {
            super.shoot(angle, roomManager);
        }
    }
}
