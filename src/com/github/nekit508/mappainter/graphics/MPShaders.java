package com.github.nekit508.mappainter.graphics;

import arc.math.Mathf;

public class MPShaders {
    public static ShadowsBufferShader shadowsBufferShader;
    public static ShadowShader shadowShader;
    public static NormalsShader normalsShader;

    public static void load() {
        shadowsBufferShader = new ShadowsBufferShader();
        shadowShader = new ShadowShader();
        normalsShader = new NormalsShader();
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

    // TODO integrate normal map into main render
    public static class NormalsShader extends MPShader {
        //protected Texture normalMap;
        /** Degrees. */
        protected float sunAzimuth;
        protected float sunElevation;

        public NormalsShader() {
            super("normals/vert", "normals/frag");
        }

        @Override
        public void apply() {
            super.apply();
            setUniformf("u_sunAzimuth", sunAzimuth * Mathf.degreesToRadians);
            setUniformf("u_sunElevation", sunElevation * Mathf.degreesToRadians);
        }

        public void setSunState(float azimuth, float elevation) {
            this.sunAzimuth = azimuth;
            this.sunElevation = elevation;
        }
    }
}
