package com.echovault.anim;

import java.util.HashMap;
import java.util.Map;

public class AnimationSet {
    // Key format: "state_direction" e.g., "idle_down", "attack_right"
    private Map<String, AnimationClip> clips = new HashMap<>();

    public void addClip(String state, String dir, AnimationClip clip) {
        clips.put(state + "_" + dir, clip);
    }

    public AnimationClip getClip(String state, String dir) {
        // Fallback to down or idle if missing
        if (clips.containsKey(state + "_" + dir)) return clips.get(state + "_" + dir);
        if (clips.containsKey(state + "_down")) return clips.get(state + "_down");
        if (clips.containsKey("idle_" + dir)) return clips.get("idle_" + dir);
        return clips.get("idle_down");
    }
}
