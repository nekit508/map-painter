package com.github.nekit508.mappainter.world.blocks.misc;

import arc.audio.Sound;
import arc.func.Cons2;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import com.github.bsideup.jabel.Desugar;
import mindustry.Vars;
import mindustry.entities.EntityGroup;
import mindustry.gen.*;
import mindustry.graphics.Layer;
import mindustry.logic.Ranged;
import mindustry.world.Block;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

// TODO shitty
public class Radar extends Block {
    /** Inclusive */
    public float maxRadius = 8000, minRadius = 80;
    public float scanInterval = 60;
    public Sound clickSound = Sounds.click;

    public Radar(String name) {
        super(name);

        update = true;
        solid = true;
        group = BlockGroup.turrets;
        envEnabled = Env.any;
        canOverdrive = false;

        configurable = true;

        config(Boolean.class, (Cons2<RadarBuild, Boolean>) (b, v) -> {
            b.drawScan = v;
        });
    }

    public class RadarBuild extends Building implements Ranged {
        public final Seq<RadioObjectSignal> signals = new Seq<>();
        protected float scanTime;
        protected boolean drawScan = true;

        @Override
        public void created() {
            updateClipRadius(Math.max(Vars.world.unitHeight(), Vars.world.unitWidth()));
            super.created();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            scanTime += edelta();

            if (scanTime > scanInterval) {
                scanTime %= scanInterval;

                scan();
            }
        }

        @Override
        public boolean configTapped() {
            configure(!drawScan);
            clickSound.at(this);
            return false;
        }

        protected Color draw$temp$color = new Color(1f, 1f, 1f, 1f);
        @Override
        public void draw() {
            super.draw();

            Draw.draw(Layer.end, () -> {
                float maxSize = 0;
                for (RadioObjectSignal signal : signals) {
                    if (signal.size > maxSize)
                        maxSize = signal.size;
                }

                if (drawScan) {
                    for (RadioObjectSignal signal : signals) {
                        Draw.color(draw$temp$color.fromHsv(240f * (1 - signal.size / maxSize), 0.8f, 0.7f).a(0.5f));
                        Fill.circle(signal.x, signal.y, Mathf.sqrt(signal.size) * 0.5f);
                    }
                }
            });
        }

        @Override
        public void drawSelect() {
            super.drawSelect();

            Draw.color(Color.red);
            Lines.dashCircle(x, y, actualMinRadius());
            Lines.dashCircle(x, y, actualMaxRadius());
        }

        protected void scan() {
            signals.clear();
            scan(Groups.unit);
            scan(Groups.build);
        }

        protected <T extends Entityc & Posc> void scan(EntityGroup<T> group) {
            float mnd = actualMinRadius();
            float mxd = actualMaxRadius();

            group.each(entity -> {
                var dist = entity.dst2(this);
                if (dist >= mnd * mnd && dist <= mxd * mxd) {
                    scan(entity);
                }
            });
        }

        protected <T extends Entityc & Posc> void scan(T entity) {
            var size = 0f;
            if (entity instanceof RadioObject radio) {
                size = radio.actualSize();
            } else if (entity instanceof Buildingc building) {
                size = building.hitSize() * building.hitSize();
            } else if (entity instanceof Unitc unit) {
                size = unit.hitSize() * unit.hitSize();
            }

            if (size > 0) {
                signals.add(new RadioObjectSignal(entity.x(), entity.y(), size));
            }
        }

        protected float actualMaxRadius() {
            return maxRadius * efficiency;
        }

        protected float actualMinRadius() {
            return minRadius;
        }

        @Override
        public float range() {
            return actualMaxRadius();
        }

        @Override
        public void read(Reads read, byte revision) {
            super.read(read, revision);
            scanTime = read.f();
            drawScan = read.bool();
        }

        @Override
        public void write(Writes write) {
            super.write(write);
            write.f(scanTime);
            write.bool(drawScan);
        }
    }

    @Desugar
    public record RadioObjectSignal(float x, float y, float size) {}

    public interface RadioObject {
        default float sizeScale() {
            return 1f;
        }

        float baseSize();

        default float actualSize() {
            return baseSize() * sizeScale();
        }
    }
}
