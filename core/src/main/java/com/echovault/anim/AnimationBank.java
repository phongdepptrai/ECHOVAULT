package com.echovault.anim;

import java.util.HashMap;
import java.util.Map;

public class AnimationBank {
    private static final Map<String, AnimationSet> bank = new HashMap<>();

    public static void addSet(String key, AnimationSet set) {
        bank.put(key, set);
    }

    public static AnimationSet getSet(String key) {
        return bank.get(key);
    }
}
