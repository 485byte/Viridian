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

        // =================================================
        // HEART
        // =================================================

        heart = new Block("viridian-heart") {

            {
                // Enable Building updates.
                update = true;

                buildType = () -> new HeartBuild();
            }


            // =================================================
            // HEART BUILDING
            // =================================================

            public class HeartBuild extends Building {

                // =================================================
                // BIO-ENERGY
                // =================================================

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;


                // =================================================
                // TIMERS
                // =================================================

                private float energyTimer = 0f;
                private float recoveryTimer = 0f;


                // =================================================
                // SETTINGS
                // =================================================

                // Bio-Energy consumed per second.
                public static final float energyConsumption = 4f;

                // Seconds before recovery.
                public static final float recoveryDelay = 10f;

                // Bio-Energy required for one recovery.
                public static final float recoveryCost = 1f;

                // Recover 4% of maximum HP.
                public static final float recoveryPercent = 0.04f;

                // Lose 5% maximum HP per second
                // while Bio-Energy is empty.
                public static final float starvationDamage = 0.05f;


                // =================================================
                // DAMAGE DETECTION
                // =================================================

                @Override
                public void damage(float amount) {

                    super.damage(amount);

                    if (amount > 0f) {
                        recoveryTimer = 0f;
                    }
                }


                // =================================================
                // UPDATE
                // =================================================

                @Override
                public void updateTile() {

                    // =================================================
                    // 1. BIO-ENERGY CONSUMPTION
                    // =================================================

                    energyTimer += Time.delta;

                    if (energyTimer >= 60f) {

                        energyTimer -= 60f;

                        // Consume 4 Bio-Energy per second.
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


                    // =================================================
                    // 2. STARVATION
                    // =================================================

                    if (bioEnergy <= 0f) {

                        health -= maxHealth
                            * starvationDamage
                            * Time.delta
                            / 60f;

                        // While starving, recovery timer resets.
                        recoveryTimer = 0f;

                        if (health <= 0f) {
                            kill();
                            return;
                        }

                        return;
                    }


                    // =================================================
                    // 3. RECOVERY TIMER
                    // =================================================

                    if (health < maxHealth) {

                        recoveryTimer += Time.delta;


                        // =================================================
                        // 4. RECOVERY
                        // =================================================

                        if (recoveryTimer >= recoveryDelay * 60f) {

                            if (bioEnergy >= recoveryCost) {

                                // Consume 1 Bio-Energy.
                                bioEnergy -= recoveryCost;

                                // Recover 4% of maximum HP.
                                health += maxHealth
                                    * recoveryPercent;

                                if (health > maxHealth) {
                                    health = maxHealth;
                                }

                                // Start a new recovery cycle.
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


            // =================================================
            // BARS
            // =================================================

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


        // =================================================
        // BLOCK SETTINGS
        // =================================================

        heart.size = 2;

        heart.health = 1000;

        heart.destructible = true;

        // IMPORTANT:
        // Allows HeartBuild.updateTile() to run.
        heart.update = true;


        // =================================================
        // BUILD REQUIREMENTS
        // =================================================

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );


        // =================================================
        // LOG
        // =================================================

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
