package com.github.nekit508.mappainter.world.blocks.production;

import arc.Core;
import arc.func.Prov;
import arc.graphics.g2d.TextureRegion;
import arc.util.Structs;
import mindustry.gen.Building;

import java.lang.reflect.Constructor;

public class ArmType {
    public String name;
    public Prov<? extends Arm> armProv;

    public TextureRegion arm1, arm2;

    public float arm1Length = 8 * 4, arm2Length = 8 * 4;
    //public

    public ArmType(String name) {
        this.name = name;
        initArmType();
    }

    public <T extends Arm> T createArm() {
        return (T) null;
    }

    public void load() {
        arm1 = Core.atlas.find(name + "-arm1");
        arm2 = Core.atlas.find(name + "-arm2");
    }

    protected void initArmType(){
        try {
            Class<?> current = getClass();

            if (current.isAnonymousClass())
                current = current.getSuperclass();

            while(armProv == null && ArmType.class.isAssignableFrom(current)){
                Class<?> type = Structs.find(current.getDeclaredClasses(), t -> ArmType.Arm.class.isAssignableFrom(t) && !t.isInterface());
                if(type != null) {
                    Constructor<? extends ArmType.Arm> cons = (Constructor<? extends ArmType.Arm>) type.getDeclaredConstructor(type.getDeclaringClass());
                    armProv = () -> {
                        try {
                            return cons.newInstance(this);
                        } catch(Exception e) {
                            throw new RuntimeException(e);
                        }
                    };
                }

                current = current.getSuperclass();
            }

        } catch(Throwable exception) {
            throw new RuntimeException(exception);
        }

        if(armProv == null){
            throw new RuntimeException("Arm not found in " + getClass() + ".");
        }
    }

    public class Arm {
        public Building building;

        public void init(Building building) {
            this.building = building;
        }

        public void draw() {
            //building.x
        }
    }
}
