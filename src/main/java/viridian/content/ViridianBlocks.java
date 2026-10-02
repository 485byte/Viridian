package viridian.content;

import arc.util.Log;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.type.Category;
import mindustry.ui.Bar;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

        heart = new Block("viridian-heart") {

            {
                buildType = () -> new HeartBuild();
            }

            public class HeartBuild extends Building {

                // =========================
                // BIO-ENERGY
                // =========================

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;

                // =========================
                // TIMERS
                // =========================

                private float energyTimer = 0f;
                private float recoveryTimer = 0f;

                // =========================
                // SETTINGS
                // =========================

                public static final float energyConsumption = 4f;

                public static final float recoveryDelay = 10f;
                public static final float recoveryCost = 1f;
                public static final float recoveryPercent = 0.04f;

                public static final float starvationDamage = 0.05f;


                // =========================
                // DAMAGE DETECTION
                // =========================

                @Override
                public void damage(float amount) {

                    super.damage(amount);

                    if (amount > 0f) {
                        recoveryTimer = 0f;
                    }
                }


                // =========================
                // UPDATE
                // =========================

                @Override
                public void updateTile() {

                    // =================================
                    // 1. ENERGY TIMER
                    // =================================

                    energyTimer += arc.util.Time.delta;

                    if (energyTimer >= 60f) {

                        energyTimer -= 60f;

                        // 4 BIO-ENERGY / SECOND
                        bioEnergy -= energyConsumption;

                        if (bioEnergy < 0f) {
                            bioEnergy = 0f;
                        }

                        Log.info(
                            "=== VIRIDIAN HEART ENERGY: "
                            + bioEnergy
                            + " ==="
                        );
                    }


                    // =================================
                    // 2. NO ENERGY = LOSE HP
                    // =================================

                    if (bioEnergy <= 0f) {

                        health -= maxHealth * starvationDamage
                            * arc.util.Time.delta / 60f;

                        recoveryTimer = 0f;

                        if (health <= 0f) {
                            kill();
                            return;
                        }

                        return;
                    }


                    // =================================
                    // 3. RECOVERY TIMER
                    // =================================

                    if (health < maxHealth) {

                        recoveryTimer += arc.util.Time.delta;

                        if (recoveryTimer >= recoveryDelay * 60f) {

                            if (bioEnergy >= recoveryCost) {

                                // PAY 1 ENERGY
                                bioEnergy -= recoveryCost;

                                // HEAL 4% MAX HP
                                health += maxHealth * recoveryPercent;

                                if (health > maxHealth) {
                                    health = maxHealth;
                                }

                                // RESET TIMER
                                recoveryTimer = 0f;

                                Log.info(
                                    "=== VIRIDIAN HEART RECOVERY ==="
                                );
                            }
                        }

                    } else {

                        recoveryTimer = 0f;
                    }
                }
            }


            // =========================
            // BARS
            // =========================

            @Override
            public void setBars() {

                super.setBars();

                addBar(
                    "bio-energy",
                    (Building entity) -> {

                        if (!(entity instanceof HeartBuild)) {

                            return new Bar(
                                "Bio-Energy",
                                Pal.powerBar,
                                () -> 0f
                            );
                        }

                        HeartBuild heartBuild =
                            (HeartBuild)entity;

                        return new Bar(
                            "Bio-Energy",
                            Pal.powerBar,
                            () -> heartBuild.maxBioEnergy <= 0f
                                ? 0f
                                : heartBuild.bioEnergy
                                    / heartBuild.maxBioEnergy
                        );
                    }
                );
            }
        };


        // =========================
        // BLOCK SETTINGS
        // =========================

        heart.size = 2;
        heart.health = 1000;
        heart.destructible = true;

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );


        // =========================
        // LOG
        // =========================

        Log.info(
            "=== VIRIDIAN: HEART CREATED: "
            + heart.name
            + " ==="
        );

        Log.info(
            "=== VIRIDIAN: BLOCK SETUP COMPLETE ==="
        );
    }
}
