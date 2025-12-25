package com.github.nekit508.mappainter.core;

import arc.graphics.Color;
import com.github.nekit508.betterfloors.core.BetterFloorsCore;
import com.github.nekit508.emkb.core.EMKBCore;
import com.github.nekit508.mappainter.content.MPDistribution;
import com.github.nekit508.mappainter.content.MPFx;
import com.github.nekit508.mappainter.content.MPOilProduction;
import com.github.nekit508.mappainter.control.MPAxisBindings;
import com.github.nekit508.mappainter.control.MPControl;
import com.github.nekit508.mappainter.control.MPKeyBindings;
import com.github.nekit508.mappainter.files.InternalFileTree;
import com.github.nekit508.mappainter.graphics.DirectionalLighting;
import com.github.nekit508.mappainter.graphics.MPRenderer;
import com.github.nekit508.mappainter.graphics.MPShaders;
import com.github.nekit508.mappainter.graphics.figure.*;
import com.github.nekit508.mappainter.net.packets.MPPackets;
import com.github.nekit508.mappainter.register.BaseRegistries;
import com.github.nekit508.mappainter.ui.MPUI;
import com.github.nekit508.mappainter.world.blocks.misc.NormalBlock;
import mindustry.content.Fx;
import mindustry.content.Items;
import mindustry.entities.bullet.*;
import mindustry.entities.pattern.ShootBarrel;
import mindustry.entities.pattern.ShootPattern;
import mindustry.gen.Sounds;
import mindustry.graphics.Pal;
import mindustry.io.SaveVersion;
import mindustry.mod.Mod;
import mindustry.type.Category;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.draw.DrawMulti;
import mindustry.world.draw.DrawTurret;
import mindustry.world.meta.Env;

import static mindustry.type.ItemStack.with;

public class MPCore extends Mod {
    public static MPRenderer renderer;
    public static MPControl control;
    public static MPFiguresManager figuresManager;

    public static InternalFileTree files = new InternalFileTree(MPCore.class);

    public static BetterFloorsCore betterFloorsCore;
    public static boolean loadBetterFloors = false;

    public static MPCore instance;

    public static DirectionalLighting directionalLighting;

    public MPCore() {
        instance = this;

        if (loadBetterFloors)
            betterFloorsCore = new BetterFloorsCore();
    }

    @Override
    public void loadContent() {
        figuresManager = new MPFiguresManager();
        SaveVersion.addCustomChunk("map-painter-figures-custom-chunk", figuresManager);

        MPShaders.load();
        MPPackets.init();

        control = new MPControl();
        directionalLighting = new DirectionalLighting();

        if (loadBetterFloors)
            betterFloorsCore.loadContent();

        MPFx.load();

        MPDistribution.init();
        MPOilProduction.init();

        BaseRegistries.blocks.register("radar", () -> new NormalBlock("radar"){{
            size = 2;
            health = 180;

            requirements(Category.effect, ItemStack.with(Items.silicon, 140, Items.lead, 60, Items.titanium, 30, Items.copper, 85));
        }});

        BaseRegistries.blocks.register("mlrs", () -> new ItemTurret("mlrs"){{
            requirements(Category.turret, with(Items.copper, 20));

            ammo(
                    Items.surgeAlloy, new MissileBulletType(9f, 18){{
                        velocityScaleRandMin = 0.9f;
                        velocityScaleRandMax = 1.1f;

                        homingPower = 0;
                        width = 4f;
                        height = 10f;
                        shrinkY = 0f;
                        splashDamageRadius = 16;
                        splashDamage = 120;
                        hitEffect = Fx.blastExplosion;
                        despawnEffect = Fx.blastExplosion;
                        ammoMultiplier = 4f;
                        hitColor = backColor = trailColor = Pal.power;
                        frontColor = Pal.powerLight;

                        collidesAir = false;

                        fragBullet = new MultiBulletType(
                                new LaserBulletType(){{
                                    colors = new Color[]{Pal.power, Pal.powerLight};
                                    randomAngleOffset = 5f;
                                    length = 64;
                                    damage = 20f;
                                    width = 8f;
                                    pierceCap = 4;
                                }},
                                new LaserBulletType(){{
                                    colors = new Color[]{Color.cyan};
                                    randomAngleOffset = 15f;
                                    length = 48;
                                    damage = 7f;
                                    width = 4f;
                                    pierceCap = 2;
                                }},
                                new LaserBulletType(){{
                                    randomAngleOffset = 30f;
                                    length = 24;
                                    damage = 8f;
                                    width = 2f;
                                    pierceCap = 3;
                                }}
                        );
                        fragRandomSpread = 5f;
                        fragBullets = 3;
                        fragSpread = 15;
                    }}
            );

            shoot = new ShootBarrel(){{
                barrels = new float[]{
                        -4, -1.25f, 0,
                        0, 0, 0,
                        4, -1.25f, 0,
                        -1, -1.25f, 0,
                        1, -1.25f, 0,
                        -2, -1.25f, 0,
                        2, -1.25f, 0,
                        -3, -1.25f, 0,
                        3, -1.25f, 0
                };
                shots = 9;
                shotDelay = 5f;
            }};

            targetAir = false;

            recoil = 0;

            shootY = 4.5f;
            reload = 120f;
            inaccuracy = 5f;
            range = 1600f;
            minRange = 240f;
            consumeAmmoOnce = false;
            size = 2;
            scaledHealth = 300;
            shootSound = Sounds.missile;
            envEnabled |= Env.any;

            limitRange(5f);
        }});

        BaseRegistries.blocks.register("flak-aa", () -> new ItemTurret("flak-aa"){{
            requirements(Category.turret, with(Items.copper, 20));

            ammo(
                    Items.plastanium, new FlakBulletType(18f, 18){{
                        velocityScaleRandMin = 0.85f;
                        velocityScaleRandMax = 1.15f;

                        homingPower = 0;
                        width = 8f;
                        height = 16f;
                        splashDamageRadius = 160;
                        explodeRange = 160;
                        explodeDelay = 3;
                        splashDamage = 30;
                        flakDelay = 1f;
                        hitEffect = Fx.flakExplosionBig;
                        despawnEffect = Fx.flakExplosionBig;
                        ammoMultiplier = 1;
                        hitColor = backColor = trailColor = Pal.powerLight;
                        frontColor = Pal.powerLight;
                        hitSound = Sounds.explosion;

                        fragBullet = new ShrapnelBulletType(){{
                            serrations = 1;
                            length = 130;
                            damage = 75f;
                            width = 5f;
                            pierceCap = 2;
                            collidesGround = false;
                            collidesAir = true;
                        }};
                        fragRandomSpread = 270;
                        fragBullets = 15;
                    }}
            );

            shoot = new ShootPattern(){{
                shots = 8;
                shotDelay = 9f;
            }};

            targetAir = true;
            targetGround = false;
            targetBlocks = false;

            shootY = 12f;
            reload = 180f;
            inaccuracy = 5f;
            range = 800f;
            minRange = 80f;
            consumeAmmoOnce = true;
            size = 3;
            scaledHealth = 300;
            shootSound = Sounds.shootBig;
            envEnabled |= Env.any;
            ammoPerShot = 20;

            ammoUseEffect = Fx.casing2;

            recoil = 3f;
            recoilTime = 9f;

            limitRange(minRange);

            drawer = new DrawMulti(
                    new DrawTurret()
            );
        }});

        BaseRegistries.items.resolve();
        BaseRegistries.liquids.resolve();
        BaseRegistries.blocks.resolve();
        BaseRegistries.sectorPresets.resolve();
        BaseRegistries.planets.resolve();

        figuresManager.addType(new LineFigureType("line"));
        figuresManager.addType(new HandWrittenFigureType("hand-written"));
        figuresManager.addType(new ArrowFigureType("arrow"));
        figuresManager.addType(new ImageFigureType("image"));
        figuresManager.addType(new CircleFigureType("circle"));

        new Item("dummy-item"){
            {
                hidden = true;
            }

            @Override
            public void load() {
                figuresManager.figureTypes.each(FigureType::load);
            }
        };
    }

    @Override
    public void init() {
        if (loadBetterFloors)
            betterFloorsCore.init();

        MPUI.init();

        EMKBCore.dialog.addKeybindings(MPKeyBindings.values());
        EMKBCore.dialog.addKeybindings(MPAxisBindings.values());

        renderer = new MPRenderer();

        control.init();
    }
}
