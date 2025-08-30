package com.github.nekit508.mappainter.graphics;

import arc.graphics.gl.Shader;
import arc.util.Reflect;
import com.github.nekit508.mappainter.core.MPCore;

import java.lang.reflect.Field;

public class MPShader extends Shader {
    public static String context = "330";

    public MPShader(String vertexShader, String fragmentShader) {
        super(MPCore.files.child("shaders").child(vertexShader + ".vert"), MPCore.files.child("shaders").child(fragmentShader + ".frag"));
    }

    @Override
    protected String preprocess(String source, boolean fragment) {
        var out = super.preprocess(source, fragment);
        out = out.replaceAll("#version [a-zA-Z0-9]*\\s", "#version " + context + "\n");
        return out;
    }

    protected Field tmp$Field;
    public int getProgram() {
        if (tmp$Field == null) {
            tmp$Field = Reflect.get(Shader.class, this, "program");
            tmp$Field.setAccessible(true);
        }
        try {
            return (int) tmp$Field.get(this);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
