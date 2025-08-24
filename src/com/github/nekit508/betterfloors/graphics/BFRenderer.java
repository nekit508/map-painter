package com.github.nekit508.betterfloors.graphics;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.graphics.*;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.FrameBuffer;
import arc.graphics.gl.PixmapTextureData;
import arc.graphics.gl.Shader;
import arc.struct.FloatSeq;
import arc.struct.IntMap;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Log;
import arc.util.io.Reads;
import com.github.nekit508.VertexesBuilder;
import com.github.nekit508.betterfloors.core.BetterFloorsCore;
import com.github.nekit508.betterfloors.tools.BFPreset;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.graphics.Layer;
import mindustry.world.blocks.environment.Floor;

import java.util.Arrays;

public class BFRenderer {
    public Shader mainRenderShader, bufferShader;

    public FrameBuffer buffer;
    public TextureRegion bufferTextureRegion;

    public Texture mapTexture, bordersTexture;
    public TextureRegion mapTextureRegion, bordersTextureRegion;

    public ObjectMap<Floor, Mesh> floorMeshes;

    public int maxTextureSize;

    public BFRenderer() {
        Events.run(EventType.Trigger.draw, () -> {
            bufferRender();
            render();
        });
    }

    public void load() {
        bufferShader =  new Shader(
                "attribute vec4 a_position;\n" +
                        "attribute vec2 a_texCoord0;\n" +
                        "varying vec2 v_texCoords;\n" +
                        "void main(){\n" +
                        "   v_texCoords = a_texCoord0;\n" +
                        "   gl_Position = a_position;\n" +
                        "}",
                "uniform sampler2D u_texture;\n" +
                        "varying vec2 v_texCoords;\n" +
                        "void main(){\n" +
                        "  gl_FragColor = texture2D(u_texture, v_texCoords);\n" +
                        "}"
        );

        mainRenderShader = new Shader(
                BetterFloorsCore.files.child("shaders/better-floor/better-floor.vert").readString(),
                BetterFloorsCore.files.child("shaders/better-floor/better-floor.frag").readString()
        );

        maxTextureSize = Gl.getInt(Gl.maxTextureSize);
        Log.info("[BFRenderer] Max texture size: @", maxTextureSize);

        buffer = new FrameBuffer(Core.graphics.getWidth(), Core.graphics.getHeight());
        bufferTextureRegion = new TextureRegion();
        bufferTextureRegion.set(buffer.getTexture());

        BFPreset preset = new BFPreset();
        var potentialFiles = Vars.dataDirectory.child("presets").list("." + BFPreset.binaryExtensions);
        if (potentialFiles.length == 0) {
            preset.fillWithCurrentContent();
            preset.writeReadable(null);
            Log.info("[BFRenderer] Created new preset.");
        } else {
            Fi fi = potentialFiles[0];
            Log.info("[BFRenderer] Loading preset @.", fi.nameWithoutExtension());
            Reads reads = fi.reads();
            preset.read(reads);
            reads.close();
        }

        Pixmap mapPixmap = PixmapIO.readPNG(BetterFloorsCore.files.child("map.png"));
        mapTexture = new Texture(new PixmapTextureData(mapPixmap, false, true));
        mapTextureRegion = new TextureRegion(mapTexture);

        int[][] tiles = new int[mapPixmap.width][mapPixmap.height];
        int[][] bordersTiles = new int[mapPixmap.width][mapPixmap.height];
        boolean[][] handledTiles = new boolean[mapPixmap.width][mapPixmap.height];
        for (boolean[] handledTile : handledTiles) Arrays.fill(handledTile, false);
        IntMap<Seq<FloatSeq>> borders = new IntMap<>();

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                int color = mapPixmap.get(i, j) >>> 8;
                tiles[i][j] = color;

                if (!borders.containsKey(color))
                    borders.put(color, new Seq<>());
            }
        }

        for (int i = 0; i < tiles.length; i++) {
            for (int j = 0; j < tiles[i].length; j++) {
                int tile = tiles[i][j];
                int border = 0;

                if (i == 0 || tiles[i-1][j] != tile)
                    border |= 0b0001;
                if (i == tiles.length - 1 || tiles[i+1][j] != tile)
                    border |= 0b0010;
                if (j == 0 || tiles[i][j-1] != tile)
                    border |= 0b0100;
                if (j == tiles[i].length - 1 || tiles[i][j+1] != tile)
                    border |= 0b1000;

                bordersTiles[i][j] = border;
            }
        }

        Pixmap bordersPixmap = new Pixmap(bordersTiles.length, bordersTiles[0].length);
        var color = new Color(0, 0, 0, 1);
        for (int i = 0; i < bordersTiles.length; i++)
            for (int j = 0; j < bordersTiles[i].length; j++) {
                int border = bordersTiles[i][j];
                boolean up = (border & 0b0001) != 0;
                boolean left = (border & 0b0010) != 0;
                boolean down = (border & 0b0100) != 0;
                boolean right = (border & 0b1000) != 0;
                color.r = up ? 0.5f : down ? 1f : 0f;
                color.b = left ? 0.5f : right ? 1f : 0f;

                bordersPixmap.set(i, j, color);
            }
        bordersTexture = new Texture(new PixmapTextureData(bordersPixmap, false, true));
        bordersTextureRegion = new TextureRegion(bordersTexture);

        IntMap<VertexesBuilder> dataBuildersMap = new IntMap<>();
        for (int i = 0; i < handledTiles.length; i++) {
            for (int j = 0; j < handledTiles[i].length; j++) {
                if (!handledTiles[i][j]) {
                    handledTiles[i][j] = true;
                    int x = i;
                    int y = j;

                    int key = tiles[i][j];
                    if (!dataBuildersMap.containsKey(key))
                        dataBuildersMap.put(key, new VertexesBuilder(2));

                    var dataBuilder = dataBuildersMap.get(key);
                    dataBuilder.consumeVertexes(new float[]{x, y}, new float[]{x + 1, y}, new float[]{x + 1, y + 1});
                    dataBuilder.consumeVertexes(new float[]{x, y}, new float[]{x, y + 1}, new float[]{x + 1, y + 1});
                }
            }
        }

        var floorsIds = dataBuildersMap.keys();
        floorMeshes = new ObjectMap<>();
        while (floorsIds.hasNext) {
            var floorId = floorsIds.next();
            var dataBuilder = dataBuildersMap.get(floorId);

            var data = dataBuilder.build();

            var mesh = new Mesh(true, data.left.length / 2, data.right.length, VertexAttribute.position);
            mesh.setVertices(data.left);
            mesh.setIndices(data.right);

            floorMeshes.put((Floor) Vars.content.block(floorId), mesh);
        }
    }

    public void init() {

    }

    /** Render from cache onto buffer. */
    public void bufferRender() {
        boolean resized = buffer.resizeCheck(Core.graphics.getWidth(), Core.graphics.getHeight());
        if (resized) bufferTextureRegion.set(buffer.getTexture());

        buffer.begin(Color.red);

        var floors = floorMeshes.keys();
        for (Floor floor : floors) {
            var mesh = floorMeshes.get(floor);
            floor.region.texture.bind();
            mesh.render(mainRenderShader, Gl.triangles);
        }

        /*Draw.rect(mapTextureRegion, Vars.world.unitWidth()/2f, Vars.world.unitHeight()/2f, Vars.world.unitWidth(), Vars.world.unitHeight());
        Draw.rect(bordersTextureRegion, Vars.world.unitWidth()/2f, Vars.world.unitHeight()/2f, Vars.world.unitWidth(), Vars.world.unitHeight());*/

        buffer.end();
    }

    /** Blit buffer onto screen. */
    public void render() {
        Draw.z(Layer.floor + 1);

        Draw.rect(bufferTextureRegion, Core.camera.position.x, Core.camera.position.y, Core.camera.width, -Core.camera.height);
    }
}
