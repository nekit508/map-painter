package com.github.nekit508.mappainter.ui.dialogs;

import arc.Core;
import arc.Events;
import arc.files.Fi;
import arc.func.Boolf;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.PixmapIO;
import arc.graphics.Pixmaps;
import arc.graphics.g2d.PixmapRegion;
import arc.graphics.g2d.TextureAtlas;
import arc.graphics.g2d.TextureRegion;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Button;
import arc.scene.ui.Image;
import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.scene.utils.Elem;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Align;
import arc.util.Log;
import arc.util.Nullable;
import com.github.nekit508.mappainter.ui.scene.CollapserWithHeader;
import com.github.nekit508.mappainter.ui.scene.OverlayCollapser;
import mindustry.Vars;
import mindustry.ctype.Content;
import mindustry.ctype.UnlockableContent;
import mindustry.game.EventType;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.type.UnitType;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.ui.dialogs.FileChooser;
import mindustry.world.Block;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicReference;

public class SpriteReloaderDialog extends BaseDialog {
    protected Table list, info, watchers;
    protected Button selectedButton;
    protected String searchText = "";

    protected ObjectMap<TextureRegion, SpriteWatcher> spriteWatchers = new ObjectMap<>();

    public SpriteReloaderDialog(String name) {
        super(name);

        shown(this::shown);
        hidden(this::hidden);

        Events.run(EventType.Trigger.update, () -> {
            for (var entry : spriteWatchers)
                entry.value.update();
        });

        addCloseButton();

        cont.table(list -> this.list = list).width(300).growY();
        cont.table(info -> this.info = info).grow();
        cont.table(watchers -> this.watchers = watchers).width(300).growY();
    }

    protected void hidden() {
        list.reset();
        info.reset();
        selectedButton = null;
    }

    protected void rebuildContentList() {
        var bloat1 = new AtomicReference<Table>();

        Runnable bloat2 = () -> {
            var pane = bloat1.get();
            pane.reset();
            pane.top();
            var contents = Vars.content.getContentMap();
            for (Seq<Content> content : contents)
                for (Content c : content)
                    if (c instanceof UnlockableContent uc && uc.name.contains(searchText))
                        pane.button(uc.localizedName, new TextureRegionDrawable(uc.fullIcon), Styles.clearTogglet, Vars.iconMed, () -> {}).with(b -> {
                            b.userObject = uc;
                            b.clicked(() -> {
                                if (selectedButton != b && selectedButton != null)
                                    selectedButton.setChecked(false);

                                if (b.isChecked())
                                    select(b);
                                else
                                    select(null);
                            });
                        }).width(300).row();
        };

        list.reset();
        list.defaults().growX();
        list.field(searchText, str -> {
            searchText = str;
            bloat2.run();
        }).row();
        list.pane(pane -> {
            bloat1.set(pane);
            bloat2.run();
        }).top().growY();
    }

    protected void select(Button button) {
        if (button == selectedButton) return;
        selectedButton = button;
        rebuildContentInfo();
    }

    protected void rebuildWatchersList() {
        watchers.reset();
        watchers.top().right();
        watchers.button(Icon.save, Styles.emptyi, Vars.iconMed, () -> {
            FileChooser.setLastDirectory(Vars.dataDirectory.child("map-painter").child("sprite-watchers-presets"));
            Vars.platform.showFileChooser(false, "bin", this::saveWatchersList);
        }).tooltip("save watchers list").size(Vars.iconMed);
        watchers.button(Icon.upload, Styles.emptyi, Vars.iconMed, () -> {
            FileChooser.setLastDirectory(Vars.dataDirectory.child("map-painter").child("sprite-watchers-presets"));
            Vars.platform.showFileChooser(true, "bin", fi -> {
                loadWatchersList(fi);
                rebuildWatchersList();
            });
        }).tooltip("load watchers list").size(Vars.iconMed);
        watchers.labelWrap("Active watchers").with(l -> l.setAlignment(Align.center)).height(Vars.iconMed).growX().row();
        watchers.pane(list -> {
            list.top();
            list.defaults().growX();

            for (var entry : spriteWatchers) {
                var watcher = entry.value;

                spriteImage(list, watcher.region);
                list.button(Icon.trash, Styles.emptyi, Vars.iconMed, () -> removeSpriteWatcherOf(watcher.region)).tooltip("remove watcher").size(Vars.iconMed);
                list.label(() -> watcher.fi.absolutePath()).tooltip(tooltip -> tooltip.label(() -> watcher.fi.absolutePath()).expand());

                list.row();
            }
        }).growY().top();
    }

    protected void rebuildContentInfo() {
        info.reset();
        var selectedContent = selectedContent();
        if (selectedContent == null) return;

        var spriteFields = new Seq<Field>();
        findFieldsRecursively(spriteFields, field ->
                        (field.getModifiers() & Modifier.STATIC) == 0 &&
                                (TextureRegion.class.isAssignableFrom(field.getType()) || (field.getType().isArray() && TextureRegion.class.isAssignableFrom(field.getType().getComponentType()))),
                selectedContent, null);

        info.left().top();
        info.pane(list -> {
            list.right();
            list.top();
            list.defaults().width(500).right();

            for (Field spriteField : spriteFields) {
                try {
                    var obj = spriteField.get(selectedContent);

                    if (obj == null || obj instanceof TextureRegion) {
                        OverlayCollapser col;
                        list.stack(col = constructContextInfoFor((TextureRegion) obj),
                                proc(Elem.newButton(spriteField.getName(), Styles.cleart, col::toggle), b -> {
                                    spriteImage(b, (TextureRegion) obj);
                                    b.getCells().reverse();
                                })
                        ).row();
                    } else {
                        list.add(new CollapserWithHeader((colTable, coll) -> {
                            var regions = (TextureRegion[]) obj;
                            for (TextureRegion region : regions) {
                                OverlayCollapser col;
                                colTable.stack(col = constructContextInfoFor(region),
                                        proc(Elem.newButton("", Styles.cleart, col::toggle), b -> {
                                            b.center();
                                            spriteImage(b, region).center();
                                            b.getCells().reverse();
                                        })
                                ).size(500, Vars.iconMed).row();
                            }
                        }, (header, coll) -> {
                            header.labelWrap(spriteField.getName()).expandX();
                            header.button(Icon.up, Styles.clearNonei, Vars.iconMed, () -> {
                                coll.collapser.toggle(coll.animateCollapser);
                            }).update(b -> b.getStyle().imageUp = coll.collapser.isCollapsed() ? Icon.upOpen : Icon.downOpen).size(Vars.iconMed);
                        }, (state, coll) -> {

                        }, true, false)).row();
                    }
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    protected TextureRegion getErrorRegion() {
        return Core.atlas.find("error");
    }

    protected boolean isRegionAdjustable(TextureRegion region) {
        return region != null && region != getErrorRegion();
    }

    protected OverlayCollapser constructContextInfoFor(TextureRegion region) {
        if (!isRegionAdjustable(region))
            return new OverlayCollapser((colTable, col) -> {
                colTable.background(Styles.black9);
                colTable.image().color(Pal.power).fillX().row();
                colTable.label(() -> "Null and error regions are not adjustable.").expandX().row();
                colTable.image().color(Pal.power).fillX().row();
            }, true);

        var outlineData = new OutlineData(true, Color.clear, 0);
        var content = selectedContent();
        if (content instanceof Block b) {
            outlineData.color = b.outlineColor;
            outlineData.radius = b.outlineRadius;
        } else if (content instanceof UnitType u) {
            outlineData.color = u.outlineColor;
            outlineData.radius = u.outlineRadius;
        }

        return new OverlayCollapser((colTable, c) -> {
            colTable.background(Styles.black9);
            colTable.image().color(Pal.power).fillX().colspan(3).row();

            colTable.button(Icon.eyeOff, Styles.cleari, Vars.iconMed, () -> {
                if (!isRegionAdjustable(region)) return;

                if (!spriteWatchers.containsKey(region))
                    Vars.platform.showFileChooser(true, "png", fi -> {
                        addSpriteWatcher(region, fi, outlineData);
                        c.toggle();
                    });
                else
                    removeSpriteWatcherOf(region);
            }).update(b ->
                    b.getStyle().imageUp = (region != null && spriteWatchers.containsKey(region)) ? Icon.eye : Icon.eyeOff
            ).tooltip(tooltip ->
                    tooltip.label(() -> (region != null && spriteWatchers.containsKey(region)) ? spriteWatchers.get(region).fi.absolutePath() : "Add watcher").expand()
            );
            colTable.check("", b -> outlineData.needed = b).with(check -> check.setChecked(outlineData.needed)).tooltip("Generate outline");
            colTable.button("load", Icon.download, Styles.cleart, Vars.iconMed, () -> {
                if (region == null)
                    ;
                else
                    Vars.platform.showFileChooser(true, "png", fi -> {
                        loadSpriteFromInto(fi, region, outlineData);
                        c.toggle();
                    });
            }).width(200).tooltip("Load from file").row();
            colTable.image().color(Pal.power).fillX().colspan(3).row();
        }, true);
    }

    protected void addSpriteWatcher(TextureRegion region, Fi file, OutlineData outlineData) {
        spriteWatchers.put(region, new SpriteWatcher(region, file, outlineData));
        if (isShown()) rebuildWatchersList();
    }

    protected void removeSpriteWatcherOf(TextureRegion region) {
        spriteWatchers.remove(region);
        if (isShown()) rebuildWatchersList();
    }

    protected Cell<Image> spriteImage(Table table, TextureRegion sprite) {
        var drawable = new TextureRegionDrawable(sprite != null ? sprite : Core.atlas.find("error"));
        return table.image(drawable).size(Vars.iconMed).tooltip(tooltip -> {
            tooltip.defaults().center();
            if (sprite instanceof TextureAtlas.AtlasRegion atlasRegion)
                tooltip.stack(
                        new Image(Styles.black9),
                        proc(new Label(atlasRegion.name), l -> l.setAlignment(Align.center))
                ).expandX().row();
            tooltip.stack(new Image(Styles.black9), new Image(drawable)).size(Math.min(
                    drawable.imageSize() * 2,
                    Math.min(Core.graphics.getWidth(), Core.graphics.getHeight()) * 0.5f
            ));
        });
    }

    public void loadWatchersList(Fi file) {
        spriteWatchers.clear();
        var reads = file.reads();

        var size = reads.i();
        for (int i = 0; i < size; i++) {
            var regionName = reads.str();
            var loadedTime = reads.l();
            var fi = new Fi(reads.str());
            var needed = reads.bool();
            var radius = reads.i();
            var color = new Color().rgba8888(reads.i());

            var region = Core.atlas.find(regionName);
            if (!isRegionAdjustable(region)) {
                Log.warn("[SpriteReloaderDialog] Region with name @ not founded in atlas.", regionName);
                continue;
            }

            var watcher = new SpriteWatcher(region, fi, new OutlineData(needed, color, radius));
            watcher.loadedTime = loadedTime;

            spriteWatchers.put(region, watcher);
        }

        reads.close();
    }

    public void saveWatchersList(Fi file) {
        var writes = file.writes();

        Log.warn("[SpriteReloaderDialog] All unnamed regions will be skipped.");
        var keys = spriteWatchers.keys().toSeq().retainAll(k -> k instanceof TextureAtlas.AtlasRegion).map(k -> (TextureAtlas.AtlasRegion) k);
        writes.i(keys.size);
        for (var key : keys) {
            writes.str(key.name);
            var watcher = spriteWatchers.get(key);
            writes.l(watcher.loadedTime);
            writes.str(watcher.fi.absolutePath());
            writes.bool(watcher.data.needed);
            writes.i(watcher.data.radius);
            writes.i(watcher.data.color.rgba8888());
        }

        writes.close();
    }

    public static void loadSpriteFromInto(Fi file, TextureRegion region, OutlineData outline) {
        var sprite = PixmapIO.readPNG(file);

        if (outline.needed) {
            var outlinedSprite = Pixmaps.outline(new PixmapRegion(sprite), outline.color, outline.radius);
            sprite.dispose();
            sprite = outlinedSprite;
        }

        if (region.width != sprite.width || region.height != sprite.height) {
            Log.err(
                    "Wrong sprite sizes. old: @x@ new: @x@.",
                    region.width, region.height,
                    sprite.width, sprite.height
            );
            return;
        }

        region.texture.draw(sprite, region.getX(), region.getY());
        sprite.dispose();
    }

    protected <T> T proc(T obj, Cons<T> cons) {
        cons.get(obj);
        return obj;
    }

    protected void findFieldsRecursively(Seq<Field> out, Boolf<Field> tester, Object object, @Nullable Class<?> clazz) {
        clazz = clazz == null ? object.getClass() : clazz;
        var fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            if (tester.get(field))
                out.add(field);
        }

        var superClass = clazz.getSuperclass();
        if (superClass != null)
            findFieldsRecursively(out, tester, object, superClass);
    }

    protected void shown() {
        selectedButton = null;
        rebuildContentList();
        rebuildWatchersList();
    }

    protected UnlockableContent selectedContent() {
        return selectedButton != null ? (UnlockableContent) selectedButton.userObject : null;
    }

    public static class OutlineData {
        public boolean needed;
        public Color color;
        public int radius;

        public OutlineData(boolean needed, Color color, int radius) {
            this.needed = needed;
            this.color = color;
            this.radius = radius;
        }
    }

    public static class SpriteWatcher {
        public TextureRegion region;
        public Fi fi;
        public OutlineData data;

        protected long loadedTime;

        public SpriteWatcher(TextureRegion region, Fi fi, OutlineData data) {
            this.region = region;
            this.fi = fi;
            this.data = data;
            loadedTime = fi.lastModified();
        }

        public void update() {
            var lm = fi.file().lastModified();
            if (lm != loadedTime && System.currentTimeMillis() - lm >= 1000) {
                reload();
                loadedTime = lm;
            }
        }

        public void reload() {
            SpriteReloaderDialog.loadSpriteFromInto(fi, region, data);
        }
    }
}
