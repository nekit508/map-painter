package com.github.nekit508.mappainter.gui.utils;

public class Utils {
    public static boolean in(char c, char[] group) {
        for (int i = 0; i < group.length; i++)
            if (c == group[i])
                return true;

        return false;
    }
}
