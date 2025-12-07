package com.echovault.bosses;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.echovault.Enemy;
import com.echovault.Player;
import com.echovault.RoomManager;
import com.echovault.utils.Telegraph;
import java.util.ArrayList;
import java.util.List;

public abstract class Boss extends Enemy {

    protected List<Phase> phases = new ArrayList<>();
    protected int currentPhaseIndex = 0;
    protected Phase currentPhase;
    protected String name;

    public Boss(float x, float y, float radius, float hp, String name) {
        super(x, y, radius, hp);
        this.name = name;
        this.isElite = true; // Bosses are elite
        this.color = Color.GOLD; // Default override
    }

    public abstract void initPhases();

    @Override
    public void update(float delta, Player player, RoomManager roomManager) {
        super.update(delta, player, roomManager);

        if (phases.isEmpty()) initPhases();

        // Phase Transition Check
        float hpPct = hp / maxHp;
        if (currentPhaseIndex < phases.size() - 1) {
             Phase next = phases.get(currentPhaseIndex + 1);
             if (hpPct <= next.hpThreshold) {
                 if (currentPhase != null) currentPhase.onExit(this, roomManager);
                 currentPhaseIndex++;
                 currentPhase = next;
                 currentPhase.onEnter(this, roomManager);
             }
        }

        if (currentPhase == null && !phases.isEmpty()) {
            currentPhase = phases.get(0);
            currentPhase.onEnter(this, roomManager);
        }

        if (currentPhase != null) {
            currentPhase.update(delta, this, player, roomManager);
        }
    }

    @Override
    public void render(ShapeRenderer sr) {
        super.render(sr);
        // Boss visual size
        sr.setColor(Color.RED);
        sr.circle(position.x, position.y, radius * 0.5f);
    }

    public Telegraph activeTelegraph;

    public String getName() { return name; }

    // --- Inner Classes for Framework ---

    public static abstract class Phase {
        public float hpThreshold; // e.g. 0.66 for 66%

        public Phase(float hpThreshold) {
            this.hpThreshold = hpThreshold;
        }

        public abstract void onEnter(Boss boss, RoomManager rm);
        public abstract void onExit(Boss boss, RoomManager rm);
        public abstract void update(float delta, Boss boss, Player p, RoomManager rm);
    }
}
