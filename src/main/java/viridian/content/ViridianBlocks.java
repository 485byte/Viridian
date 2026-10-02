package viridian.content;

import arc.util.Log;
import arc.util.Time;

import mindustry.Vars;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.graphics.Pal;
import mindustry.ui.Bar;
import mindustry.type.Category;
import mindustry.world.Block;
import mindustry.world.Tile;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;
    public static Block vessel;
    public static Block energySource;
    public static Block energyDrain;


    // =============================================================
    // LOAD
    // =============================================================

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
    // GET ADJACENT BUILDING
    // =============================================================

    public static Building getAdjacentBuilding(
        Building building,
        int dx,
        int dy
    ) {

        Tile tile = Vars.world.tile(
            building.tileX() + dx,
            building.tileY() + dy
        );

        if (tile == null) {
            return null;
        }

        return tile.build;
    }


    // =============================================================
    // HEART BUILD
    // =============================================================

    public static class HeartBuild extends Building {

        // ---------------------------------------------------------
        // ENERGY
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

                removeBioEnergy(
                    energyConsumption
                );
            }


            // =====================================================
            // STARVATION
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

        // 100,000 Bio-Energy per second.
        public static final float transferRate = 100000f;


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

                removeBioEnergy(
                    energyConsumption
                );
            }


            // =====================================================
            // STARVATION
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
            // FIND NEIGHBORS
            // =====================================================

            Building north =
                getAdjacentBuilding(
                    this,
                    0,
                    1
                );

            Building south =
                getAdjacentBuilding(
                    this,
                    0,
                    -1
                );

            Building east =
                getAdjacentBuilding(
                    this,
                    1,
                    0
                );

            Building west =
                getAdjacentBuilding(
                    this,
                    -1,
                    0
                );


            // =====================================================
            // HEART → VESSEL
            // =====================================================

            transferFromHeart(north);
            transferFromHeart(south);
            transferFromHeart(east);
            transferFromHeart(west);


            // =====================================================
            // VESSEL → VESSEL
            // =====================================================

            transferFromVessel(north);
            transferFromVessel(south);
            transferFromVessel(east);
            transferFromVessel(west);
        }


        // =========================================================
        // HEART → VESSEL
        // =========================================================

        private void transferFromHeart(
            Building other
        ) {

            if (!(other instanceof HeartBuild)) {
                return;
            }

            HeartBuild source =
                (HeartBuild)other;


            float amount =
                transferRate *
                Time.delta /
                60f;


            // Don't exceed Vessel capacity.

            float freeSpace =
                maxBioEnergy -
                bioEnergy;

            if (amount > freeSpace) {
                amount = freeSpace;
            }


            // Don't take more than Heart has.

            if (amount > source.bioEnergy) {
                amount = source.bioEnergy;
            }


            if (amount <= 0f) {
                return;
            }


            source.removeBioEnergy(
                amount
            );

            addBioEnergy(
                amount
            );
        }


        // =========================================================
        // VESSEL → VESSEL
        // =========================================================

        private void transferFromVessel(
            Building other
        ) {

            if (!(other instanceof VesselBuild)) {
                return;
            }

            VesselBuild target =
                (VesselBuild)other;


            // -----------------------------------------------------
            // Target must have less energy.
            // -----------------------------------------------------

            if (target.bioEnergy >= bioEnergy) {
                return;
            }


            // -----------------------------------------------------
            // Calculate how much target needs.
            // -----------------------------------------------------

            float needed =
                maxBioEnergy -
                target.bioEnergy;


            // -----------------------------------------------------
            // Transfer up to 100,000/s.
            // -----------------------------------------------------

            float amount =
                transferRate *
                Time.delta /
                60f;


            if (amount > needed) {
                amount = needed;
            }


            // -----------------------------------------------------
            // Don't transfer more than source has.
            // -----------------------------------------------------

            if (amount > bioEnergy) {
                amount = bioEnergy;
            }


            if (amount <= 0f) {
                return;
            }


            // -----------------------------------------------------
            // TRANSFER
            // -----------------------------------------------------

            removeBioEnergy(
                amount
            );

            target.addBioEnergy(
                amount
            );
        }
    }


    // =============================================================
    // ENERGY SOURCE
    // =============================================================

    public static class EnergySourceBuild
        extends Building {

        @Override
        public void updateTile() {

            for (int dx = -1; dx <= 1; dx++) {

                for (int dy = -1; dy <= 1; dy++) {

                    if (
                        Math.abs(dx) +
                        Math.abs(dy) != 1
                    ) {
                        continue;
                    }


                    Building other =
                        getAdjacentBuilding(
                            this,
                            dx,
                            dy
                        );


                    // SOURCE → HEART

                    if (
                        other instanceof HeartBuild
                    ) {

                        HeartBuild target =
                            (HeartBuild)other;

                        target.addBioEnergy(
                            500000f *
                            Time.delta /
                            60f
                        );
                    }


                    // SOURCE → VESSEL

                    if (
                        other instanceof VesselBuild
                    ) {

                        VesselBuild target =
                            (VesselBuild)other;

                        target.addBioEnergy(
                            500000f *
                            Time.delta /
                            60f
                        );
                    }
                }
            }
        }
    }


    // =============================================================
    // ENERGY DRAIN
    // =============================================================

    public static class EnergyDrainBuild
        extends Building {

        @Override
        public void updateTile() {

            for (int dx = -1; dx <= 1; dx++) {

                for (int dy = -1; dy <= 1; dy++) {

                    if (
                        Math.abs(dx) +
                        Math.abs(dy) != 1
                    ) {
                        continue;
                    }


                    Building other =
                        getAdjacentBuilding(
                            this,
                            dx,
                            dy
                        );


                    // DRAIN ← HEART

                    if (
                        other instanceof HeartBuild
                    ) {

                        HeartBuild target =
                            (HeartBuild)other;

                        target.removeBioEnergy(
                            1000000f *
                            Time.delta /
                            60f
                        );
                    }


                    // DRAIN ← VESSEL

                    if (
                        other instanceof VesselBuild
                    ) {

                        VesselBuild target =
                            (VesselBuild)other;

                        target.removeBioEnergy(
                            1000000f *
                            Time.delta /
                            60f
                        );
                    }
                }
            }
        }
    }
}
