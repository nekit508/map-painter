package com.github.nekit508.mappainter.graphics;

import arc.Core;
import arc.Events;
import arc.func.FloatFloatf;
import arc.graphics.Color;
import arc.graphics.Gl;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.Shader;
import arc.math.Mathf;
import arc.math.geom.QuadTree;
import arc.math.geom.Rect;
import arc.math.geom.Vec2;
import arc.struct.ObjectSet;
import arc.util.Tmp;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.Vars.world;

public class DirectionalLighting {
    /** Angles in degrees. */
    protected float sunElevation = 59, sunAzimuth = 37;
    protected boolean sunPosChanged = true;
    protected float heightScale, xOffsetScale, yOffsetScale;
    protected boolean enabled = false;

    protected Shader shadowsBufferShader = MPShaders.shadowsBufferShader;
    protected Shader shadowShader = MPShaders.shadowShader;
    protected MPShaders.NormalsShader normalsShader = MPShaders.normalsShader;

    protected BlockShadowQuadTree tree;

    protected ShadowsBatch batch;
    protected FrameBuffer shadowBuffer;
    protected FrameBuffer normalsBuffer;

    protected FloatFloatf heightToAlphaScale = h -> (float) (1 / Math.exp(h / heightScale * Vars.tilesize * 0.015));

    /*protected SpriteBatchWrapper batchWrapper; // TODO very buggy sprite capturing
    protected Seq<Seq<SpriteBatchWrapper.DrawRecord>> records = new Seq<>();*/

    protected Color normalsBufferFiller = new Color(0f, 0f, 1f, 1f);

    public DirectionalLighting() {
        try {
            batch = new ShadowsBatch(4096);
            shadowBuffer = new FrameBuffer();
            normalsBuffer = new FrameBuffer();

            Events.on(EventType.WorldLoadEvent.class, event -> reload());
            Events.run(EventType.Trigger.draw, this::render);
            Events.run(EventType.Trigger.drawOver, this::draw);

            /*Events.run(EventType.Trigger.preDraw, () -> { // TODO very buggy sprite capturing
                if (batchWrapper == null) {
                    batchWrapper = new SpriteBatchWrapper();
                    records.add(batchWrapper.bind(Layer.flyingUnit-2, Layer.flyingUnit+2));
                    records.add(batchWrapper.bind(Layer.flyingUnitLow-2, Layer.flyingUnitLow+2));
                }

                batchWrapper.begin();
                Draw.batch(batchWrapper);
            });

            Events.run(EventType.Trigger.postDraw, () -> {
                batchWrapper.end();
            });*/
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reload() {
        tree = new BlockShadowQuadTree(new Rect(0, 0, world.unitWidth(), world.unitHeight()));

        for (Tile tile : world.tiles)
            tree.insert(tile);
    }

    protected ObjectSet<Building> tmp$buildings = new ObjectSet<>();
    /** Render shadows into buffer. */
    public void render() {
        recompute();

        if (shadowBuffer.resizeCheck(Core.graphics.getWidth(), Core.graphics.getHeight()))
            shadowBuffer.getTexture().setFilter(Texture.TextureFilter.linear, Texture.TextureFilter.linear);
        Draw.batch(batch, () -> {
            Draw.proj(Core.camera);

            var b = Draw.getBlend();
            Gl.blendEquationSeparate(Gl.max, Gl.max);
            Gl.blendFuncSeparate(Gl.one, Gl.one, Gl.one, Gl.one);
            Draw.sort(false);
            Draw.shader(shadowShader);
            shadowBuffer.begin(Color.clear);

            tree.intersect(Core.camera.bounds(Tmp.r1), this::renderTile);

            /*Log.info(records); // TODO very buggy sprite capturing
            records.each(r -> r.each(this::renderRecord));
            records.each(Seq::clear);*/

            shadowBuffer.end();
            Draw.shader(null);
            Draw.sort(true);
            Gl.blendEquationSeparate(Gl.funcAdd, Gl.funcAdd);
            Draw.blend(b);
        });

        if (normalsBuffer.resizeCheck(Core.graphics.getWidth(), Core.graphics.getHeight()))
            normalsBuffer.getTexture().setFilter(Texture.TextureFilter.linear, Texture.TextureFilter.linear);
        normalsBuffer.begin(normalsBufferFiller);
        tmp$buildings.clear();
        tree.intersect(Core.camera.bounds(Tmp.r1), tile -> {
            if (tile.build != null && tile.block().size == 2)
                tmp$buildings.add(tile.build);
        });
        tmp$buildings.each(this::renderNormal);
        normalsBuffer.end();
    }

    protected void renderNormal(Building building) {
        Draw.rect(Core.atlas.find("map-painter-normal-map"), building.x, building.y);
    }

    /** Blit buffer onto screen. */
    public void draw() {
        Draw.draw(Layer.block - 1, () -> shadowBuffer.blit(shadowsBufferShader));
        Draw.draw(Layer.blockOver + 5, () -> normalsBuffer.blit(normalsShader));
    }

    public void renderTile(Tile tile) {
        if (tile.block() != Blocks.air) {
            var h = computeBlockHeight(tile.block());

            for (int i = 0; i < 4; i++)
                renderCorner(tile, i);

            quad(
                    tile.worldx() + Vars.tilesize * 0.5f, tile.worldy() - Vars.tilesize * 0.5f, h,
                    tile.worldx() + Vars.tilesize * 0.5f, tile.worldy() + Vars.tilesize * 0.5f, h,
                    tile.worldx() - Vars.tilesize * 0.5f, tile.worldy() + Vars.tilesize * 0.5f, h,
                    tile.worldx() - Vars.tilesize * 0.5f, tile.worldy() - Vars.tilesize * 0.5f, h
            );
        }
    }

    public void renderRecord(SpriteBatchWrapper.DrawRecord record) {
        var h = record.z() * 0.05f;

        var t = transformPos(tmp$Vec2$transformPos.set(record.x(), record.y()), h);
        var x = t.x;
        var y = t.y;

        Draw.color(h);
        Draw.rect(record.region(),
                x,
                y,
                record.width(),
                record.height(),
                record.originX(),
                record.originY(),
                record.rotation()
        );
    }

    /** Renders corner of tile if it's needed. */
    protected void renderCorner(Tile tile, int side) {
        var h = computeBlockHeight(tile.block());
        var other = tile.nearby(side);

        if (other != null && other.block() != Blocks.air && h == computeBlockHeight(other.block()))
            return;

        var x = tile.worldx();
        var y = tile.worldy();

        // TODO shiiiit
        var dx1 = switch(side){
            case 0 -> 1;
            case 1 -> 1;
            case 2 -> -1;
            case 3 -> -1;
            default -> null;
        } * 0.5f * Vars.tilesize;
        var dy1 = switch(side){
            case 0 -> -1;
            case 1 -> 1;
            case 2 -> 1;
            case 3 -> -1;
            default -> null;
        } * 0.5f * Vars.tilesize;
        var dx2 = switch((side+1) % 4){
            case 0 -> 1;
            case 1 -> 1;
            case 2 -> -1;
            case 3 -> -1;
            default -> null;
        } * 0.5f * Vars.tilesize;
        var dy2 = switch((side+1) % 4){
            case 0 -> -1;
            case 1 -> 1;
            case 2 -> 1;
            case 3 -> -1;
            default -> null;
        } * 0.5f * Vars.tilesize;

        wall(x + dx1, y + dy1, h, x + dx2, y + dy2, h);
    }

    /** Renders wall with specified height of each corner. */
    protected void wall(float x1, float y1, float h1, float x2, float y2, float h2) {
        var t = transformPos(x1, y1, h1);
        var x1h = t.x;
        var y1h = t.y;

        t = transformPos(x2, y2, h2);
        var x2h = t.x;
        var y2h = t.y;

        Fill.quad(
                x1, y1, heightToAlphaScale.get(0),
                x2, y2, heightToAlphaScale.get(0),
                x2h, y2h, heightToAlphaScale.get(h1),
                x1h, y1h, heightToAlphaScale.get(h2)
        );
    }

    /** Renders wall with specified height of each corner. */
    protected void quad(float x1, float y1, float h1, float x2, float y2, float h2, float x3, float y3, float h3, float x4, float y4, float h4) {
        var t = transformPos(x1, y1, h1);
        x1 = t.x;
        y1 = t.y;

        t = transformPos(x2, y2, h2);
        x2 = t.x;
        y2 = t.y;

        t = transformPos(x3, y3, h3);
        x3 = t.x;
        y3 = t.y;

        t = transformPos(x4, y4, h4);
        x4 = t.x;
        y4 = t.y;

        Fill.quad(
                x1, y1, heightToAlphaScale.get(h1),
                x2, y2, heightToAlphaScale.get(h2),
                x3, y3, heightToAlphaScale.get(h3),
                x4, y4, heightToAlphaScale.get(h4)
        );
    }

    public float sunElevation() {
        return sunElevation;
    }

    public float sunAzimuth() {
        return sunAzimuth;
    }

    public void sunElevation(float sunElevation) {
        if (this.sunElevation == sunElevation) return;
        sunPosChanged = true;
        this.sunElevation = sunElevation;
    }

    public void sunAzimuth(float sunAzimuth) {
        if (this.sunAzimuth == sunAzimuth) return;
        sunPosChanged = true;
        this.sunAzimuth = sunAzimuth;
    }

    Vec2 tmp$Vec2$transformPos = new Vec2();
    protected Vec2 transformPos(float x, float y, float height) {
        return transformPos(tmp$Vec2$transformPos.set(x, y), height);
    }

    protected Vec2 transformPos(Vec2 pos, float height) {
        return pos.add(xOffsetScale * height, yOffsetScale * height);
    }

    public void toggle() {
        enabled(!enabled());
    }

    public void enabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean enabled() {
        return enabled;
    }

    public float computeBlockHeight(Block block) {
        // TODO create interface that provides height of block
        if (false) {
            return 0;
        } else
            return block.size * Vars.tilesize * (block.underBullets ? 0.2f : 1f);
    }

    public void recompute() {
        if (!sunPosChanged) return;

        normalsShader.setSunState(sunAzimuth, sunElevation);

        heightScale = (float) (1 / Math.tan(Mathf.degreesToRadians * sunElevation));
        xOffsetScale = -Mathf.cosDeg(sunAzimuth) * heightScale;
        yOffsetScale = -Mathf.sinDeg(sunAzimuth) * heightScale;

        sunPosChanged = false;
    }

    public float heightScale() {
        return heightScale;
    }

    public float xOffsetScale() {
        return xOffsetScale;
    }

    public float yOffsetScale() {
        return yOffsetScale;
    }

    public class BlockShadowQuadTree extends QuadTree<Tile> {
        public BlockShadowQuadTree(Rect bounds) {
            super(bounds);
        }

        @Override
        public void hitbox(Tile tile){
            var block = tile.block();
            var vh = computeBlockHeight(tile.block()) * heightScale;
            tmp.setCentered(tile.worldx() + block.offset, tile.worldy() + block.offset, (vh + tile.block().size * Vars.tilesize) * 2, (vh + tile.block().size * Vars.tilesize) * 2);
        }

        @Override
        protected QuadTree<Tile> newChild(Rect rect){
            return new BlockShadowQuadTree(rect);
        }
    }
}
