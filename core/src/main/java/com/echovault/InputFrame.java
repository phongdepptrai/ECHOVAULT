package com.echovault;

public class InputFrame {
    public boolean up;
    public boolean down;
    public boolean left;
    public boolean right;
    public boolean shoot;
    public boolean dash;
    public float aimAngle; // degrees

    // Copy constructor
    public InputFrame(InputFrame other) {
        this.up = other.up;
        this.down = other.down;
        this.left = other.left;
        this.right = other.right;
        this.shoot = other.shoot;
        this.dash = other.dash;
        this.aimAngle = other.aimAngle;
    }

    public InputFrame() {}
}
