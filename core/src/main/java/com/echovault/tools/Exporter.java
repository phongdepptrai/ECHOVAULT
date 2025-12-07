package com.echovault.tools;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class Exporter {

    public static void export(FileHandle outputDir, int frameSize, boolean perDirSheets, String[] onlyAnims, String[] onlyDirs, long seed) {
        if (!outputDir.exists()) outputDir.mkdirs();

        ProceduralPlayerRenderer renderer = new ProceduralPlayerRenderer(frameSize);

        Map<String, Object> jsonRoot = new LinkedHashMap<>();
        jsonRoot.put("frameWidth", frameSize);
        jsonRoot.put("frameHeight", frameSize);
        Map<String, Object> animsJson = new LinkedHashMap<>();
        jsonRoot.put("animations", animsJson);

        AnimationType[] animTypes = AnimationType.values();
        Direction[] dirTypes = Direction.values();

        // Filter Anims
        ArrayList<AnimationType> selectedAnims = new ArrayList<>();
        if (onlyAnims == null || onlyAnims.length == 0 || (onlyAnims.length==1 && onlyAnims[0].equals("all"))) {
            for(AnimationType t : animTypes) selectedAnims.add(t);
        } else {
            for (String s : onlyAnims) {
                try { selectedAnims.add(AnimationType.valueOf(s.toUpperCase())); } catch(Exception e){}
            }
        }

        // Filter Dirs
        ArrayList<Direction> selectedDirs = new ArrayList<>();
        if (onlyDirs == null || onlyDirs.length == 0) {
            for(Direction d : dirTypes) selectedDirs.add(d);
        } else {
            for (String s : onlyDirs) {
                try { selectedDirs.add(Direction.valueOf(s.toUpperCase())); } catch(Exception e){}
            }
        }

        for (AnimationType anim : selectedAnims) {
            Map<String, Object> dirMap = new LinkedHashMap<>();
            animsJson.put(anim.name().toLowerCase(), dirMap);

            AnimationSpec.AnimDef def = AnimationSpec.get(anim);

            for (Direction dir : selectedDirs) {
                Array<Pixmap> frames = new Array<>();
                for (int i = 0; i < def.frameCount; i++) {
                    frames.add(renderer.renderFrame(anim, dir, i, seed));
                }

                String filename = "player_" + anim.name().toLowerCase() + "_" + dir.name().toLowerCase() + ".png";

                // Pack
                Pixmap sheet = SheetPacker.pack(frames, def.frameCount); // 1 row per dir

                // Save PNG
                PixmapIO.writePNG(outputDir.child(filename), sheet);
                sheet.dispose();
                for(Pixmap p : frames) p.dispose();

                // JSON Metadata
                Map<String, Object> meta = new LinkedHashMap<>();
                meta.put("fps", def.fps);
                meta.put("frameCount", def.frameCount);
                meta.put("sheet", filename);

                // Since we pack linearly:
                ArrayList<Map<String, Integer>> frameRects = new ArrayList<>();
                for (int i=0; i<def.frameCount; i++) {
                     Map<String, Integer> r = new LinkedHashMap<>();
                     r.put("x", i * frameSize);
                     r.put("y", 0);
                     r.put("w", frameSize);
                     r.put("h", frameSize);
                     frameRects.add(r);
                }
                meta.put("frames", frameRects);

                dirMap.put(dir.name().toLowerCase(), meta);
            }
        }

        // Write JSON
        Json json = new Json();
        json.setOutputType(JsonWriter.OutputType.json);
        outputDir.child("player_animations.json").writeString(json.prettyPrint(jsonRoot), false);
    }
}
