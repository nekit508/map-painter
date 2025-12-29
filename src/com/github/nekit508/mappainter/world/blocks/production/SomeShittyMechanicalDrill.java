package com.github.nekit508.mappainter.world.blocks.production;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Fill;
import arc.graphics.g2d.Lines;
import arc.graphics.g2d.TextureRegion;
import arc.math.Mathf;
import arc.math.geom.Position;
import arc.struct.EnumSet;
import arc.struct.Seq;
import arc.util.Time;
import mindustry.game.Team;
import mindustry.gen.Building;
import mindustry.graphics.Layer;
import mindustry.world.Block;
import mindustry.world.Tile;
import mindustry.world.meta.BlockFlag;
import mindustry.world.meta.BlockGroup;
import mindustry.world.meta.Env;

public class SomeShittyMechanicalDrill extends Block {
    public TextureRegion arm1Region, arm2Region;

    public SomeShittyMechanicalDrill(String name) {
        super(name);

        update = true;
        solid = true;
        group = BlockGroup.drills;
        hasLiquids = true;
        liquidCapacity = 5f;
        hasItems = true;
        //ambientSound = Sounds.drill;
        ambientSoundVolume = 0.018f;
        envEnabled |= Env.space;
        flags = EnumSet.of(BlockFlag.drill);
    }

    @Override
    public void load() {
        super.load();

        arm1Region = Core.atlas.find(name + "-arm1");
        arm2Region = Core.atlas.find(name + "-arm2");;
    }

    public class SomeShittyMechanicalDrillBuild extends Building {
        public Arm arm1, arm2, arm3;

        @Override
        public Building init(Tile tile, Team team, boolean shouldAdd, int rotation) {
            arm1 = new Arm(arm1Region, this, 8 * 4);
            arm2 = new Arm(arm2Region, arm1, 8 * 4);
            arm3 = new Arm(arm2Region, arm2, 0);
            return super.init(tile, team, shouldAdd, rotation);
        }

        @Override
        public void draw() {
            super.draw();

            arm1.update();

            arm1.draw();
        }

        @Override
        public void updateTile() {
            super.updateTile();

            arm3.rotation += Time.delta * 5;
        }
    }

    public static class Arm implements Position {
        public TextureRegion region;

        public Position parent;
        public Seq<Arm> children = new Seq<>();
        public float length;

        public float rotation = 0;
        public float ar;
        // position of start (center)
        public float x = 0, y = 0;
        // position of end
        public float ex = 0, ey = 0;

        public Arm(TextureRegion region, Position parent, float length) {
            if (parent instanceof Arm arm)
                arm.children.add(this);
            this.region = region;
            this.parent = parent;
            this.length = length;
        }

        public void draw() {
            Draw.z(Layer.blockOver);
            /*Draw.alpha(0.5f);
            Draw.rect(region, x, y, rotation + ar);*/

            //Draw.alpha(0.5f);
            Draw.color(Color.red);
            Fill.circle(x, y, 2);
            Draw.color(Color.green);
            Fill.circle(ex, ey, 2);
            Draw.color(Color.blue);
            Lines.stroke(1);
            Lines.line(x, y, ex, ey);

            children.each(Arm::draw);
        }

        public void update() {
            x = parent.getX();
            y = parent.getY();

            if (parent instanceof Arm arm)
                ar = arm.getRotation();
            else ar = 0;

            ex = x + Mathf.cosDeg(rotation + ar) * length;
            ey = y + Mathf.sinDeg(rotation + ar) * length;

            children.each(Arm::update);
        }

        @Override
        public float getX() {
            return ex;
        }

        @Override
        public float getY() {
            return ey;
        }

        public float getRotation() {
            return rotation + ar;
        }
    }
}
