package viridian.content;

import arc.graphics.Color;
import arc.util.Log;
import arc.util.Time;

import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
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

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );


        // =========================================================
        // HEART BIO-ENERGY BAR
        // =========================================================

        heart.addBar(
            "bio-energy",
            (HeartBuild entity) -> new Bar(
                "Bio-Energy",
                Pal.powerBar,
                () -> entity.bioEnergy / HeartBuild.maxBioEnergy
            )
        );


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

        vessel.requirements(
            Category.effect,
            with(Items.copper, 5)
        );


        // =========================================================
        // VESSEL BIO-ENERGY BAR
        // =========================================================

        vessel.addBar(
            "bio-energy",
            (VesselBuild entity) -> new Bar(
                "Bio-Energy",
                Pal.powerBar,
                () -> entity.bioEnergy / VesselBuild.maxBioEnergy
            )
        );


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

        // ---------------------------------------------------------
        // BIO-ENERGY
        // ---------------------------------------------------------

        public float bioEnergy = 100f;

        public static final float maxBioEnergy = 100f;

        public static final float energyConsumption = 4f;


        // ---------------------------------------------------------
        // RECOVERY
        // ---------------------------------------------------------

        public static final float recoveryCost = 1f;

        public static final float recoveryDelay = 10f;

        public static final float recoveryPercent = 0.04f;


        // ---------------------------------------------------------
        // STARVATION
        // ---------------------------------------------------------

        public static final float starvationDamage = 0.05f;


        private float energyTimer = 0f;

        private float recoveryTimer = 0f;


        // ---------------------------------------------------------
        // ADD BIO-ENERGY
        // ---------------------------------------------------------

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // ---------------------------------------------------------
        // REMOVE BIO-ENERGY
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

                removeBioEnergy(
                    energyConsumption
                );

                Log.info(
                    "Viridian Heart energy: "
                    + bioEnergy
                );
            }


            // =====================================================
            // NO ENERGY = LOSE HP
            // =====================================================

            if (bioEnergy <= 0f) {

                health -=
                    maxHealth *
                    starvationDamage *
                    Time.delta /
                    60f;

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

                if (
                    recoveryTimer >=
                    recoveryDelay * 60f
                ) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(
                            recoveryCost
                        );

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
        // BIO-ENERGY
        // ---------------------------------------------------------

        public float bioEnergy = 3f;

        public static final float maxBioEnergy = 3f;

        public static final float energyConsumption = 1f;


        // ---------------------------------------------------------
        // RECOVERY
        // ---------------------------------------------------------

        public static final float recoveryCost = 1f;

        public static final float recoveryDelay = 10f;

        public static final float recoveryPercent = 0.04f;


        // ---------------------------------------------------------
        // STARVATION
        // ---------------------------------------------------------

        public static final float starvationDamage = 0.05f;


        // ---------------------------------------------------------
        // TRANSFER
        // ---------------------------------------------------------

        public static final float transferRate = 1f;


        private float energyTimer = 0f;

        private float recoveryTimer = 0f;


        // ---------------------------------------------------------
        // ADD BIO-ENERGY
        // ---------------------------------------------------------

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // ---------------------------------------------------------
        // REMOVE BIO-ENERGY
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

                removeBioEnergy(
                    energyConsumption
                );
            }


            // =====================================================
            // NO ENERGY = LOSE HP
            // =====================================================

            if (bioEnergy <= 0f) {

                health -=
                    maxHealth *
                    starvationDamage *
                    Time.delta /
                    60f;

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

                if (
                    recoveryTimer >=
                    recoveryDelay * 60f
                ) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(
                            recoveryCost
                        );

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

                    if (
                        heartBuild.bioEnergy >=
                        transfer
                    ) {

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


            // =====================================================
            // VESSEL → VESSEL
            // =====================================================

            for (Building other : proximity) {

                if (
                    other.block != vessel ||
                    !(other instanceof VesselBuild)
                ) {
                    continue;
                }

                VesselBuild target =
                    (VesselBuild)other;


                // -------------------------------------------------
                // Only one side is allowed to transfer.
                // This prevents A <-> B from transferring twice.
                // -------------------------------------------------

                if (id <= target.id) {
                    continue;
                }


                // -------------------------------------------------
                // Only transfer when this Vessel has more energy.
                // -------------------------------------------------

                if (
                    bioEnergy <=
                    target.bioEnergy
                ) {
                    continue;
                }


                // -------------------------------------------------
                // Transfer 1 Bio-Energy/s.
                // -------------------------------------------------

                float amount =
                    transferRate *
                    Time.delta /
                    60f;


                // Don't give more than the target can hold.

                float freeSpace =
                    maxBioEnergy -
                    target.bioEnergy;


                if (amount > freeSpace) {
                    amount = freeSpace;
                }


                // Don't give more than this Vessel has.

                if (amount > bioEnergy) {
                    amount = bioEnergy;
                }


                if (amount > 0f) {

                    removeBioEnergy(
                        amount
                    );

                    target.addBioEnergy(
                        amount
                    );
                }
            }
        }
    }


    // =============================================================
    // ENERGY SOURCE
    // =============================================================

    public static class EnergySourceBuild extends Building {

        @Override
        public void updateTile() {

            for (Building other : proximity) {

                // -------------------------------------------------
                // SOURCE → HEART
                // -------------------------------------------------

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


                // -------------------------------------------------
                // SOURCE → VESSEL
                // -------------------------------------------------

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
    // ENERGY DRAIN
    // =============================================================

    public static class EnergyDrainBuild extends Building {

        @Override
        public void updateTile() {

            for (Building other : proximity) {

                // -------------------------------------------------
                // DRAIN ← HEART
                // -------------------------------------------------

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


                // -------------------------------------------------
                // DRAIN ← VESSEL
                // -------------------------------------------------

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
