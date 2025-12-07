package com.echovault.tools;

import java.util.HashMap;
import java.util.Map;

public class AnimationSpec {
    public static class AnimDef {
        public int fps;
        public int frameCount;

        public AnimDef(int fps, int frameCount) {
            this.fps = fps;
            this.frameCount = frameCount;
        }
    }

    private static final Map<AnimationType, AnimDef> specs = new HashMap<>();

    static {
        specs.put(AnimationType.IDLE, new AnimDef(8, 8));
        specs.put(AnimationType.WALK, new AnimDef(12, 8));
        specs.put(AnimationType.SHOOT, new AnimDef(14, 6));
        specs.put(AnimationType.DASH, new AnimDef(18, 6));
        specs.put(AnimationType.HURT, new AnimDef(10, 4));
        specs.put(AnimationType.DEATH, new AnimDef(10, 10));
    }

    public static AnimDef get(AnimationType type) {
        return specs.get(type);
    }
}
