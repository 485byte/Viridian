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
    public static Block energySource;
    public static Block energyDrain;


    // =========================================================
    // LOAD ALL VIRIDIAN BLOCKS
    // =========================================================

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");


        // =====================================================
        // HEART
        // =====================================================

        heart = new Block("viridian-heart") {

            {
                update = true;
                buildType = () -> new HeartBuild();
            }

            public class HeartBuild extends Building {

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;

                private float energyTimer = 0f;
                private float recoveryTimer = 0f;


                // -----------------------------
                // HEART SETTINGS
                // -----------------------------

                public static final float energyConsumption = 4f;

                public static final float recoveryDelay = 10f;

                public static final float recoveryCost = 1f;

                public static final float recoveryPercent = 0.04f;

                public static final float starvationDamage = 0.05f;


                // -----------------------------
                // ADD ENERGY
                // -----------------------------

                public void addBioEnergy(float amount) {

                    bioEnergy += amount;

                    if (bioEnergy > maxBioEnergy) {
                        bioEnergy = maxBioEnergy;
                    }
                }


                // -----------------------------
                // REMOVE ENERGY
                // -----------------------------

                public void removeBioEnergy(float amount) {

                    bioEnergy -= amount;

                    if (bioEnergy < 0f) {
                        bioEnergy = 0f;
                    }
                }


                // -----------------------------
                // DAMAGE
                // -----------------------------

                @Override
                public void damage(float amount) {

                    super.damage(amount);

                    if (amount > 0f) {
                        recoveryTimer = 0f;
                    }
                }


                // -----------------------------
                // UPDATE
                // -----------------------------

                @Override
                public void updateTile() {

                    // =========================================
                    // NORMAL ENERGY CONSUMPTION
                    // =========================================

                    energyTimer += Time.delta;

                    if (energyTimer >= 60f) {

                        energyTimer -= 60f;

                        removeBioEnergy(energyConsumption);

                        Log.info(
                            "=== VIRIDIAN HEART ENERGY: "
                            + bioEnergy
                            + " / "
                            + maxBioEnergy
                            + " ==="
                        );
                    }


                    // =========================================
                    // NO ENERGY = LOSE HP
                    // =========================================

                    if (bioEnergy <= 0f) {

                        health -= maxHealth
                            * starvationDamage
                            * Time.delta
                            / 60f;

                        recoveryTimer = 0f;

                        if (health <= 0f) {

                            kill();
                            return;
                        }

                        return;
                    }


                    // =========================================
                    // RECOVERY
                    // =========================================

                    if (health < maxHealth) {

                        recoveryTimer += Time.delta;

                        if (recoveryTimer >= recoveryDelay * 60f) {

                            if (bioEnergy >= recoveryCost) {

                                removeBioEnergy(recoveryCost);

                                health += maxHealth
                                    * recoveryPercent;

                                if (health > maxHealth) {
                                    health = maxHealth;
                                }

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


            // =============================================
            // HEART BIO-ENERGY BAR
            // =============================================

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


        // Heart properties

        heart.size = 2;

        heart.health = 1000;

        heart.destructible = true;

        heart.update = true;

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );


        Log.info(
            "=== VIRIDIAN: HEART CREATED ==="
        );


        // =====================================================
        // ENERGY SOURCE
        // =====================================================

        energySource = new Block("viridian-energy-source") {

            {
                update = true;
                buildType = () -> new EnergySourceBuild();
            }


            public class EnergySourceBuild extends Building {

                // 500,000 Bio-Energy per second
                public static final float energyOutput =
                    500000f;


                @Override
                public void updateTile() {

                    // Check adjacent buildings

                    for (Building other : proximity) {

                        if (other.block == heart) {

                            if (other instanceof ViridianBlocks.HeartBuild) {

                                ViridianBlocks.HeartBuild heartBuild =
                                    (ViridianBlocks.HeartBuild)other;

                                heartBuild.addBioEnergy(
                                    energyOutput
                                    * Time.delta
                                    / 60f
                                );
                            }
                        }
                    }
                }
            }
        };


        energySource.size = 1;

        energySource.health = 250;

        energySource.destructible = true;

        energySource.update = true;

        energySource.requirements(
            Category.effect,
            with(Items.copper, 5)
        );


        Log.info(
            "=== VIRIDIAN: ENERGY SOURCE CREATED ==="
        );


        // =====================================================
        // ENERGY DRAIN
        // =====================================================

        energyDrain = new Block("viridian-energy-drain") {

            {
                update = true;
                buildType = () -> new EnergyDrainBuild();
            }


            public class EnergyDrainBuild extends Building {

                // 1,000,000 Bio-Energy per second
                public static final float energyDrain =
                    1000000f;


                @Override
                public void updateTile() {

                    // Check adjacent buildings

                    for (Building other : proximity) {

                        if (other.block == heart) {

                            if (other instanceof ViridianBlocks.HeartBuild) {

                                ViridianBlocks.HeartBuild heartBuild =
                                    (ViridianBlocks.HeartBuild)other;

                                heartBuild.removeBioEnergy(
                                    energyDrain
                                    * Time.delta
                                    / 60f
                                );
                            }
                        }
                    }
                }
            }
        };


        energyDrain.size = 1;

        energyDrain.health = 250;

        energyDrain.destructible = true;

        energyDrain.update = true;

        energyDrain.requirements(
            Category.effect,
            with(Items.copper, 5)
        );


        Log.info(
            "=== VIRIDIAN: ENERGY DRAIN CREATED ==="
        );


        // =====================================================
        // COMPLETE
        // =====================================================

        Log.info(
            "=== VIRIDIAN: BLOCK SETUP COMPLETE ==="
        );
    }


    // =========================================================
    // HEART BUILD TYPE
    // =========================================================
    //
    // This class reference allows Source/Drain to recognize
    // the Heart's Building instance.
    //
    // =========================================================

    public static class HeartBuild extends Building {
    }
}
