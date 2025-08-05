package com.github.nekit508.mappainter.gui.utils;

import arc.util.Log;

public class Logger {
    public boolean enabled;

    public Logger(boolean enabled) {
        this.enabled = enabled;
    }
    public void info(String str, Object... objects) {
        if (enabled)
            Log.info(str, objects);
    }
}
