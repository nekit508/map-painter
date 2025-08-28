package com.github.nekit508.mappainter.graphics;

import arc.graphics.gl.Shader;
import com.github.nekit508.mappainter.core.MPCore;

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
}
