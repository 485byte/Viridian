package viridian.content;

import arc.Core;
import arc.graphics.Color;
import arc.util.Log;
import arc.util.Time;
import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;
    public static Block vessel;
    public static Block energySource;
    public static Block energyDrain;

    public static void load() {

        // =========================================================
        // HEART
        // =========================================================

        heart = new Block("viridian-heart") {

            {
                update = true;
                buildType = () -> new HeartBuild();
            }
        };

        heart.size = 2;
        heart.health = 1000;
        heart.destructible = true;
        heart.requirements(Category.effect, with(Items.copper, 10));


        // =========================================================
        // VESSEL
        // =========================================================

        vessel = new Block("viridian-vessel") {

            {
                update = true;
                buildType = () -> new VesselBuild();
            }
        };

        vessel.size = 1;
        vessel.health = 750;
        vessel.destructible = true;
        vessel.update = true;

        vessel.requirements(Category.effect, with(Items.copper, 5));


        // =========================================================
        // ENERGY SOURCE
        // =========================================================

        energySource = new Block("viridian-energy-source") {

            {
                update = true;
                buildType = () -> new EnergySourceBuild();
            }
        };

        energySource.size = 1;
        energySource.health = 250;
        energySource.destructible = true;

        energySource.requirements(
            Category.effect,
            with(Items.copper, 5)
        );


        // =========================================================
        // ENERGY DRAIN
        // =========================================================

        energyDrain = new Block("viridian-energy-drain") {

            {
                update = true;
                buildType = () -> new EnergyDrainBuild();
            }
        };

        energyDrain.size = 1;
        energyDrain.health = 250;
        energyDrain.destructible = true;

        energyDrain.requirements(
            Category.effect,
            with(Items.copper, 5)
        );


        Log.info("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
    }


    // =============================================================
    // HEART BUILD
    // =============================================================

    public static class HeartBuild extends Building {

        public float bioEnergy = 100f;

        public static final float maxBioEnergy = 100f;

        public static final float energyConsumption = 4f;

        public static final float recoveryCost = 1f;

        public static final float recoveryDelay = 10f;

        public static final float recoveryPercent = 0.04f;

        public static final float starvationDamage = 0.05f;

        private float energyTimer = 0f;

        private float recoveryTimer = 0f;


        // ---------------------------------------------------------
        // ADD ENERGY
        // ---------------------------------------------------------

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // ---------------------------------------------------------
        // REMOVE ENERGY
        // ---------------------------------------------------------

        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if (bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        // ---------------------------------------------------------
        // DAMAGE
        // ---------------------------------------------------------

        @Override
        public void damage(float amount) {

            super.damage(amount);

            if (amount > 0f) {
                recoveryTimer = 0f;
            }
        }


        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        @Override
        public void updateTile() {

            // =====================================================
            // ENERGY CONSUMPTION
            // =====================================================

            energyTimer += Time.delta;

            if (energyTimer >= 60f) {

                energyTimer -= 60f;

                removeBioEnergy(energyConsumption);

                Log.info(
                    "Viridian Heart energy: " + bioEnergy
                );
            }


            // =====================================================
            // STARVATION
            // =====================================================

            if (bioEnergy <= 0f) {

                health -=
                    maxHealth *
                    starvationDamage *
                    Time.delta / 60f;

                recoveryTimer = 0f;

                if (health <= 0f) {

                    kill();

                    return;
                }

                return;
            }


            // =====================================================
            // RECOVERY
            // =====================================================

            if (health < maxHealth) {

                recoveryTimer += Time.delta;

                if (recoveryTimer >= recoveryDelay * 60f) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(recoveryCost);

                        health +=
                            maxHealth *
                            recoveryPercent;

                        if (health > maxHealth) {
                            health = maxHealth;
                        }

                        recoveryTimer = 0f;

                        Log.info(
                            "Viridian Heart recovered. HP: "
                            + health
                        );
                    }
                }

            } else {

                recoveryTimer = 0f;
            }
        }
    }


    // =============================================================
    // VESSEL BUILD
    // =============================================================

    public static class VesselBuild extends Building {

        // ---------------------------------------------------------
        // ENERGY
        // ---------------------------------------------------------

        public float bioEnergy = 3f;

        public static final float maxBioEnergy = 3f;

        public static final float energyConsumption = 1f;

        public static final float recoveryCost = 1f;

        public static final float recoveryDelay = 10f;

        public static final float recoveryPercent = 0.04f;

        public static final float starvationDamage = 0.05f;


        private float energyTimer = 0f;

        private float recoveryTimer = 0f;


        // ---------------------------------------------------------
        // ADD ENERGY
        // ---------------------------------------------------------

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // ---------------------------------------------------------
        // REMOVE ENERGY
        // ---------------------------------------------------------

        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if (bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        // ---------------------------------------------------------
        // DAMAGE
        // ---------------------------------------------------------

        @Override
        public void damage(float amount) {

            super.damage(amount);

            if (amount > 0f) {
                recoveryTimer = 0f;
            }
        }


        // ---------------------------------------------------------
        // UPDATE
        // ---------------------------------------------------------

        @Override
        public void updateTile() {

            // =====================================================
            // ENERGY CONSUMPTION
            // =====================================================

            energyTimer += Time.delta;

            if (energyTimer >= 60f) {

                energyTimer -= 60f;

                removeBioEnergy(energyConsumption);
            }


            // =====================================================
            // STARVATION
            // =====================================================

            if (bioEnergy <= 0f) {

                health -=
                    maxHealth *
                    starvationDamage *
                    Time.delta / 60f;

                recoveryTimer = 0f;

                if (health <= 0f) {

                    kill();

                    return;
                }

                return;
            }


            // =====================================================
            // RECOVERY
            // =====================================================

            if (health < maxHealth) {

                recoveryTimer += Time.delta;

                if (recoveryTimer >= recoveryDelay * 60f) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(recoveryCost);

                        health +=
                            maxHealth *
                            recoveryPercent;

                        if (health > maxHealth) {
                            health = maxHealth;
                        }

                        recoveryTimer = 0f;
                    }
                }

            } else {

                recoveryTimer = 0f;
            }


            // =====================================================
            // HEART → VESSEL
            // =====================================================

            for (Building other : proximity) {

                if (
                    other.block == heart &&
                    other instanceof HeartBuild
                ) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    float transfer =
                        Time.delta / 60f;

                    if (heartBuild.bioEnergy >= transfer) {

                        heartBuild.removeBioEnergy(
                            transfer
                        );

                        addBioEnergy(
                            transfer
                        );
                    }

                    break;
                }
            }
        }
    }


    // =============================================================
    // ENERGY SOURCE BUILD
    // =============================================================

    public static class EnergySourceBuild extends Building {

        @Override
        public void updateTile() {

            for (Building other : proximity) {

                if (
                    other.block == heart &&
                    other instanceof HeartBuild
                ) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    float amount =
                        500000f *
                        Time.delta /
                        60f;

                    heartBuild.addBioEnergy(
                        amount
                    );
                }


                if (
                    other.block == vessel &&
                    other instanceof VesselBuild
                ) {

                    VesselBuild vesselBuild =
                        (VesselBuild)other;

                    float amount =
                        500000f *
                        Time.delta /
                        60f;

                    vesselBuild.addBioEnergy(
                        amount
                    );
                }
            }
        }
    }


    // =============================================================
    // ENERGY DRAIN BUILD
    // =============================================================

    public static class EnergyDrainBuild extends Building {

        @Override
        public void updateTile() {

            for (Building other : proximity) {

                if (
                    other.block == heart &&
                    other instanceof HeartBuild
                ) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    float amount =
                        1000000f *
                        Time.delta /
                        60f;

                    heartBuild.removeBioEnergy(
                        amount
                    );
                }


                if (
                    other.block == vessel &&
                    other instanceof VesselBuild
                ) {

                    VesselBuild vesselBuild =
                        (VesselBuild)other;

                    float amount =
                        1000000f *
                        Time.delta /
                        60f;

                    vesselBuild.removeBioEnergy(
                        amount
                    );
                }
            }
        }
    }
}
