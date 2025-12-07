package com.echovault.core;

import java.util.HashMap;
import java.util.Map;

public class Registry {
    // We will use Object for definitions to keep it generic in this MVP,
    // or specific maps if we implement the Def classes first.
    // For now, let's use Maps that will be populated by specific Factories.

    public static final Map<String, Object> projectiles = new HashMap<>();
    public static final Map<String, Object> enemies = new HashMap<>();
    public static final Map<String, Object> patterns = new HashMap<>();

    // Init all defaults
    public static void initDefaults() {
        // Basic Enemy Bullet
        com.echovault.combat.projectile.ProjectileDef basic = new com.echovault.combat.projectile.ProjectileDef();
        basic.speed = 200f;
        basic.visualType = "circle";
        projectiles.put("basic_enemy", basic);

        // Fast
        com.echovault.combat.projectile.ProjectileDef fast = basic.copy();
        fast.speed = 400f;
        fast.visualType = "diamond";
        projectiles.put("fast_enemy", fast);

        // Spiral
        com.echovault.combat.projectile.ProjectileDef spiral = basic.copy();
        spiral.motionType = "spiral";
        spiral.visualType = "pulse";
        projectiles.put("spiral_bullet", spiral);

        // Homing
        com.echovault.combat.projectile.ProjectileDef homing = basic.copy();
        homing.motionType = "homing";
        homing.turnSpeed = 180f;
        homing.visualType = "diamond";
        projectiles.put("homing_bullet", homing);
    }

    // Clear all registries
    public static void clear() {
        projectiles.clear();
        enemies.clear();
        patterns.clear();
    }
}
