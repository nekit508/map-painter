package com.github.nekit508.mappainter.ui.dialogs;

import arc.Events;
import arc.files.Fi;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.PixmapIO;
import arc.graphics.Texture;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.graphics.gl.PixmapTextureData;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.math.geom.Vec3;
import arc.scene.Element;
import arc.scene.event.ClickListener;
import arc.scene.event.InputEvent;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import com.github.nekit508.emkb.control.keys.keyboard.KeyBinding;
import com.github.nekit508.mappainter.control.MPKeyBindings;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.FileChooser;

import java.util.Objects;

public class NormalMapTester extends BaseDialog {
    protected Table left, center, right;
    protected NormalMapViewer normalMapViewer;

    protected KeyBinding binding = MPKeyBindings.openONormalMapTester;
    protected NormalViewer normalViewer;

    public NormalMapTester(String title) {
        super(title);

        addCloseButton();
        makeButtonOverlay();

        shown(this::shown);
        hidden(this::hidden);

        Events.run(EventType.Trigger.update, () -> {
            if (binding.active())
                toggle();
        });
    }

    protected void shown() {
        build();
    }

    protected void hidden() {
        destroy();
    }

    protected void build() {
        cont.defaults().growY();

        normalViewer = new NormalViewer();

        cont.table(left -> {
            this.left = left;
            left.left().top();

            left.button(Icon.file, Styles.emptyi, () -> {
                FileChooser.setLastDirectory(Vars.dataDirectory.child("map-painter").child("sprite-watchers-presets"));
                Vars.platform.showFileChooser(true, "png", fi -> {
                    normalMapViewer.load(fi);
                });
            });
        }).width(200).top();
        cont.image().color(Color.yellow).fillY();
        cont.table(center -> {
            this.center = center;
            center.center();

            center.add(normalMapViewer = new NormalMapViewer(normalViewer)).expand();
        }).grow().top();
        cont.image().color(Color.yellow).fillY();
        cont.table(right -> {
            this.right = right;
            right.right().top();

            right.add(normalViewer).expand().fillX();
        }).width(400).top();
    }

    protected void destroy() {
        cont.reset();
        normalMapViewer.dump();
    }

    public static class NormalMapViewer extends Element {
        protected TextureRegion normalMap;
        protected Pixmap normalMapPixmap;
        protected Vec2 pixelPos = new Vec2(-1, -1);
        protected NormalViewer viewer;

        protected Color tmp$Color = new Color();

        protected int scale = 4;

        public NormalMapViewer(NormalViewer viewer) {
            this.viewer = viewer;

            addListener(new ClickListener(KeyCode.mouseLeft){
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    super.clicked(event, x, y);

                    if (normalMapPixmap == null) return;

                    x /= scale;
                    y /= scale;

                    pixelPos.set(x, y);
                    viewer.rebuild(tmp$Color.rgba8888(normalMapPixmap.get((int) x, normalMapPixmap.getHeight() - (int) y)));
                }
            });
        }

        @Override
        public void draw() {
            validate();
            if (normalMapPixmap != null) {
                var w = normalMapPixmap.getWidth() * scale;
                var h = normalMapPixmap.getHeight() * scale;

                Draw.color();
                Draw.rect(normalMap, x + w/2f, y + h/2f, w, h);

                Draw.color(Color.cyan);
                Lines.rect(x + pixelPos.x * scale, y + pixelPos.y * scale, scale, scale);
            }
        }

        protected void load(Fi fi) {
            dump();

            normalMapPixmap = PixmapIO.readPNG(fi);
            normalMap = new TextureRegion(new Texture(new PixmapTextureData(normalMapPixmap, false, false)));

            invalidateHierarchy();
        }

        protected void dump() {
            if (normalMapPixmap != null) {
                normalMap.texture.dispose();
                normalMapPixmap.dispose();
            }
        }

        @Override
        public float getMinWidth(){
            return 0;
        }

        @Override
        public float getMinHeight(){
            return 0;
        }

        @Override
        public float getPrefWidth(){
            if(normalMapPixmap != null) return normalMapPixmap.getWidth() * scale;
            return 0;
        }

        @Override
        public float getPrefHeight(){
            if(normalMapPixmap != null) return normalMapPixmap.getHeight() * scale;
            return 0;
        }
    }

    public static class NormalViewer extends Table {
        protected Color color = new Color();
        protected Vec3 vec = new Vec3();

        protected void rebuild(final Color newColor) {
            if (Objects.equals(color, newColor)) return;
            color.set(newColor);

            reset();
            if (color == null) return;

            recalculateNormal();

            defaults().growX();
            image().color(color).height(5).row();
            labelWrap(Strings.format(
                    "color: @ @ @",
                    (int) (color.r * 255),
                    (int) (color.g * 255),
                    (int) (color.b * 255)
            )).row();

            labelWrap(Strings.format(
                    "normal: @ @ @",
                    Strings.fixed(vec.x, 2),
                    Strings.fixed(vec.y, 2),
                    Strings.fixed(vec.z, 2)
            )).row();

            image().color(Color.yellow).row();
            add(new Element(){
                @Override
                public void draw() {
                    super.draw();
                    Draw.color(Color.white);
                    Lines.line(
                            x + 64,
                            y + 64,
                            x + 64 + vec.x * 64,
                            y + 64 + vec.y * 64
                    );
                }

                @Override
                public float getPrefHeight() {
                    return 128;
                }

                @Override
                public float getPrefWidth() {
                    return 128;
                }
            }).expandY().row();
            image().color(Color.yellow);
        }

        protected void recalculateNormal() {
            vec.set(color.r, color.g, color.b).scl(2).sub(1).nor();
        }
    }
}
