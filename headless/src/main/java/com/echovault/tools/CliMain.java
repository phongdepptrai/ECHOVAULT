package com.echovault.tools;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.badlogic.gdx.files.FileHandle;

import java.util.HashMap;
import java.util.Map;

public class CliMain {
    public static void main(String[] args) {
        // Parse Args
        Map<String, String> argMap = new HashMap<>();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String[] parts = arg.substring(2).split("=");
                if (parts.length == 2) {
                    argMap.put(parts[0], parts[1]);
                } else {
                    argMap.put(parts[0], "true");
                }
            }
        }

        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ApplicationAdapter() {
            @Override
            public void create() {
                String outPath = argMap.getOrDefault("out", "export");
                int frameSize = Integer.parseInt(argMap.getOrDefault("frameSize", "64"));
                String onlyAnimsStr = argMap.getOrDefault("only", "all");
                String[] onlyAnims = onlyAnimsStr.equals("all") ? null : onlyAnimsStr.split(",");
                // seed, dirs...

                System.out.println("Starting Export to: " + outPath);

                Exporter.export(Gdx.files.local(outPath), frameSize, true, onlyAnims, null, 12345);

                System.out.println("Export Complete.");
                Gdx.app.exit();
            }
        }, config);
    }
}
