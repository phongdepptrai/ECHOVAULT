package com.echovault.pattern;

import com.badlogic.gdx.math.Vector2;
import com.echovault.RoomManager;

public interface AttackPattern {
    void execute(RoomManager room, Vector2 source, Vector2 target);
}
