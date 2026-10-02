package viridian.content;

import arc.util.Log;
import arc.util.Time;
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
                // RECOVERY
                // =========================

                public float recoveryTimer = 0f;

                public static final float recoveryDelay = 10f;
                public static final float recoveryCost = 1f;
                public static final float recoveryPercent = 0.04f;


                // =========================
                // ENERGY
                // =========================

                public static final float energyConsumption = 4f;


                // =========================
                // STARVATION
                // =========================

                public static final float starvationDamage = 0.05f;


                @Override
                public void damage(float amount) {

                    super.damage(amount);

                    if (amount > 0f) {
                        recoveryTimer = 0f;
                    }
                }


                @Override
                public void updateTile() {

                    // =========================
                    // 1. BASIC ENERGY CONSUMPTION
                    // =========================

                    bioEnergy -= energyConsumption * Time.delta;


                    if (bioEnergy < 0f) {
                        bioEnergy = 0f;
                    }


                    // =========================
                    // 2. NO ENERGY = LOSE HP
                    // =========================

                    if (bioEnergy <= 0f) {

                        health -= maxHealth
                            * starvationDamage
                            * Time.delta;

                        if (health <= 0f) {
                            kill();
                            return;
                        }

                        // Starvation damage does not count
                        // as external damage.
                        // Recovery must wait again after
                        // energy becomes available.
                        recoveryTimer = 0f;

                    } else {

                        // =========================
                        // 3. DAMAGED HEART
                        // =========================

                        if (health < maxHealth) {

                            recoveryTimer += Time.delta;


                            // =========================
                            // 4. RECOVERY AFTER 10 SECONDS
                            // =========================

                            if (recoveryTimer >= recoveryDelay) {

                                if (bioEnergy >= recoveryCost) {

                                    bioEnergy -= recoveryCost;

                                    health += maxHealth
                                        * recoveryPercent
                                        * Time.delta;

                                    if (health > maxHealth) {
                                        health = maxHealth;
                                    }
                                }
                            }

                        } else {

                            recoveryTimer = 0f;
                        }
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

                        HeartBuild heartBuild = (HeartBuild)entity;

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
