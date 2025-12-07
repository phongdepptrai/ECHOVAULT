package com.echovault.tools;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.utils.Array;

public class SheetPacker {

    public static Pixmap pack(Array<Pixmap> frames, int cols) {
        if (frames.size == 0) return null;

        int w = frames.get(0).getWidth();
        int h = frames.get(0).getHeight();

        int rows = (int)Math.ceil((float)frames.size / cols);

        Pixmap sheet = new Pixmap(w * cols, h * rows, Pixmap.Format.RGBA8888);
        sheet.setColor(0);
        sheet.fill(); // Clear

        for (int i = 0; i < frames.size; i++) {
            int r = i / cols;
            int c = i % cols;
            sheet.drawPixmap(frames.get(i), c * w, r * h);
        }

        return sheet;
    }
}
