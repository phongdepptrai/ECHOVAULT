package com.echovault.combat.projectile;

import com.echovault.core.Registry;

public class ProjectileDef {
    public float speed = 400f;
    public float damage = 1f;
    public float radius = 5f;
    public float lifetime = 5f;
    public String motionType = "straight";
    public String visualType = "circle";
    public boolean bouncing = false;
    public int bounceCount = 0;

    // Extra params
    public float turnSpeed = 0f; // Homing
    public float accel = 0f;

    // Copy constructor or builder pattern could be useful, but public fields are fine for MVP Defs
    public ProjectileDef copy() {
        ProjectileDef d = new ProjectileDef();
        d.speed = speed;
        d.damage = damage;
        d.radius = radius;
        d.lifetime = lifetime;
        d.motionType = motionType;
        d.visualType = visualType;
        d.bouncing = bouncing;
        d.bounceCount = bounceCount;
        d.turnSpeed = turnSpeed;
        d.accel = accel;
        return d;
    }
}
