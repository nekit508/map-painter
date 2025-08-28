package com.github.nekit508.mappainter.graphics;

public class MPShaders {
    public static ShadowsBufferShader shadowsBufferShader;
    public static ShadowShader shadowShader;

    public static void load() {
        shadowsBufferShader = new ShadowsBufferShader();
        shadowShader = new ShadowShader();
    }

    public static class ShadowsBufferShader extends MPShader {
        public ShadowsBufferShader() {
            super("shadowsBuffer/vert", "shadowsBuffer/frag");
        }
    }

    public static class ShadowShader extends MPShader {
        public ShadowShader() {
            super("shadow/vert", "shadow/frag");
        }
    }
}
