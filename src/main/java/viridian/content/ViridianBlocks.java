package viridian.content;

import arc.math.geom.Point2;
import arc.math.geom.Geometry;
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

    public static void load() {

        // =========================
        // HEART
        // =========================

        heart = new Block("viridian-heart") {{
            size = 2;
            health = 1000;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 10));

            buildType = HeartBuild::new;

            addBar("bio-energy", build ->
                new Bar(
                    "Bio-Energy",
                    Pal.powerBar,
                    () -> {
                        HeartBuild h = (HeartBuild)build;
                        return h.maxBioEnergy <= 0f
                            ? 0f
                            : h.bioEnergy / h.maxBioEnergy;
                    }
                )
            );
        }};

        // =========================
        // VESSEL
        // =========================

        vessel = new Block("viridian-vessel") {{
            size = 1;
            health = 750;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 5));

            buildType = VesselBuild::new;

            addBar("bio-energy", build ->
                new Bar(
                    "Bio-Energy",
                    Pal.powerBar,
                    () -> {
                        VesselBuild v = (VesselBuild)build;
                        return v.maxBioEnergy <= 0f
                            ? 0f
                            : v.bioEnergy / v.maxBioEnergy;
                    }
                )
            );
        }};

        // =========================
        // ENERGY SOURCE
        // =========================

        energySource = new Block("viridian-energy-source") {{
            size = 1;
            health = 250;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 5));

            buildType = EnergySourceBuild::new;
        }};

        // =========================
        // ENERGY DRAIN
        // =========================

        energyDrain = new Block("viridian-energy-drain") {{
            size = 1;
            health = 250;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 5));

            buildType = EnergyDrainBuild::new;
        }};
    }


    // =========================================================
    // HELPER
    // =========================================================

    public static Building getAdjacentBuilding(Building building, int dx, int dy) {

        Tile tile = Vars.world.tile(
            building.tileX() + dx,
            building.tileY() + dy
        );

        if(tile == null) return null;

        return tile.build;
    }


    // =========================================================
    // HEART BUILD
    // =========================================================

    public static class HeartBuild extends Building {

        public float bioEnergy = 100f;
        public float maxBioEnergy = 100f;

        public float consumption = 4f;

        public float recoveryCost = 1f;
        public float recoveryDelay = 10f;
        public float recoveryPercent = 0.04f;

        public float starvationDamage = 0.05f;

        private float energyTimer = 0f;
        private float recoveryTimer = 0f;


        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if(bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if(bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        @Override
        public void damage(float amount) {

            super.damage(amount);

            // Bị damage -> reset thời gian hồi phục
            recoveryTimer = 0f;
        }


        @Override
        public void updateTile() {

            // =====================================
            // ENERGY CONSUMPTION
            // =====================================

            energyTimer += Time.delta;

            if(energyTimer >= 60f) {

                removeBioEnergy(consumption);

                energyTimer -= 60f;
            }


            // =====================================
            // STARVATION
            // =====================================

            if(bioEnergy <= 0f) {

                // 5% MAX HP mỗi giây
                damage(maxHealth * starvationDamage * Time.delta / 60f);

                // Không hồi phục khi hết năng lượng
                recoveryTimer = 0f;

                return;
            }


            // =====================================
            // RECOVERY
            // =====================================

            if(health < maxHealth) {

                recoveryTimer += Time.delta;

                if(recoveryTimer >= recoveryDelay * 60f) {

                    if(bioEnergy >= recoveryCost) {

                        removeBioEnergy(recoveryCost);

                        heal(maxHealth * recoveryPercent);

                        recoveryTimer = 0f;
                    }
                }

            } else {

                recoveryTimer = 0f;
            }
        }
    }


    // =========================================================
    // VESSEL BUILD
    // =========================================================

    public static class VesselBuild extends Building {

        // Vessel bắt đầu rỗng để có thể test truyền năng lượng.
        public float bioEnergy = 0f;

        public float maxBioEnergy = 3f;

        public float consumption = 1f;

        public float recoveryCost = 1f;
        public float recoveryDelay = 10f;
        public float recoveryPercent = 0.04f;

        public float starvationDamage = 0.05f;

        // 100k Bio-Energy / second
        public float transferRate = 100000f;

        private float energyTimer = 0f;
        private float recoveryTimer = 0f;


        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if(bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if(bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        @Override
        public void damage(float amount) {

            super.damage(amount);

            // Bị damage -> reset thời gian hồi phục
            recoveryTimer = 0f;
        }


        @Override
        public void updateTile() {

            // =====================================
            // ENERGY CONSUMPTION
            // =====================================

            energyTimer += Time.delta;

            if(energyTimer >= 60f) {

                removeBioEnergy(consumption);

                energyTimer -= 60f;
            }


            // =====================================
            // STARVATION
            // =====================================

            if(bioEnergy <= 0f) {

                damage(maxHealth * starvationDamage * Time.delta / 60f);

                recoveryTimer = 0f;

                return;
            }


            // =====================================
            // RECOVERY
            // =====================================

            if(health < maxHealth) {

                recoveryTimer += Time.delta;

                if(recoveryTimer >= recoveryDelay * 60f) {

                    if(bioEnergy >= recoveryCost) {

                        removeBioEnergy(recoveryCost);

                        heal(maxHealth * recoveryPercent);

                        recoveryTimer = 0f;
                    }
                }

            } else {

                recoveryTimer = 0f;
            }


            // =====================================
            // ENERGY TRANSFER
            // =====================================

            /*
             * Geometry.d8 gồm 8 hướng xung quanh Vessel:
             *
             *      ↖  ↑  ↗
             *      ←  V  →
             *      ↙  ↓  ↘
             *
             * Vì vậy Vessel có thể truyền năng lượng
             * theo cả 8 hướng.
             */

            for(Point2 point : Geometry.d8) {

                Building other = getAdjacentBuilding(
                    this,
                    point.x,
                    point.y
                );

                if(other == null) continue;


                // -----------------------------
                // HEART -> VESSEL
                // -----------------------------

                transferFromHeart(other);


                // -----------------------------
                // VESSEL -> VESSEL
                // -----------------------------

                transferFromVessel(other);
            }
        }


        // =====================================================
        // HEART -> THIS VESSEL
        // =====================================================

        private void transferFromHeart(Building other) {

            if(!(other instanceof HeartBuild)) {
                return;
            }

            HeartBuild heart = (HeartBuild)other;


            // Vessel đã đầy
            if(bioEnergy >= maxBioEnergy) {
                return;
            }


            // Heart hết năng lượng
            if(heart.bioEnergy <= 0f) {
                return;
            }


            float freeSpace =
                maxBioEnergy - bioEnergy;


            float amount =
                transferRate * Time.delta / 60f;


            if(amount > freeSpace) {
                amount = freeSpace;
            }


            if(amount > heart.bioEnergy) {
                amount = heart.bioEnergy;
            }


            if(amount <= 0f) {
                return;
            }


            heart.removeBioEnergy(amount);

            addBioEnergy(amount);
        }


        // =====================================================
        // VESSEL -> VESSEL
        // =====================================================

        private void transferFromVessel(Building other) {

            if(!(other instanceof VesselBuild)) {
                return;
            }

            VesselBuild source = (VesselBuild)other;


            // Không truyền cho chính mình
            if(source == this) {
                return;
            }


            // Nếu source không có nhiều hơn mình,
            // không cần truyền.
            if(source.bioEnergy <= bioEnergy) {
                return;
            }


            // Vessel này đã đầy
            if(bioEnergy >= maxBioEnergy) {
                return;
            }


            float need =
                maxBioEnergy - bioEnergy;


            float amount =
                transferRate * Time.delta / 60f;


            if(amount > need) {
                amount = need;
            }


            if(amount > source.bioEnergy) {
                amount = source.bioEnergy;
            }


            if(amount <= 0f) {
                return;
            }


            source.removeBioEnergy(amount);

            addBioEnergy(amount);
        }
    }


    // =========================================================
    // ENERGY SOURCE
    // =========================================================

    public static class EnergySourceBuild extends Building {

        public float production = 500000f;


        @Override
        public void updateTile() {

            for(Point2 point : Geometry.d8) {

                Building other = getAdjacentBuilding(
                    this,
                    point.x,
                    point.y
                );

                if(other instanceof HeartBuild) {

                    ((HeartBuild)other).addBioEnergy(
                        production * Time.delta / 60f
                    );

                } else if(other instanceof VesselBuild) {

                    ((VesselBuild)other).addBioEnergy(
                        production * Time.delta / 60f
                    );
                }
            }
        }
    }


    // =========================================================
    // ENERGY DRAIN
    // =========================================================

    public static class EnergyDrainBuild extends Building {

        public float drain = 1000000f;


        @Override
        public void updateTile() {

            for(Point2 point : Geometry.d8) {

                Building other = getAdjacentBuilding(
                    this,
                    point.x,
                    point.y
                );

                if(other instanceof HeartBuild) {

                    ((HeartBuild)other).removeBioEnergy(
                        drain * Time.delta / 60f
                    );

                } else if(other instanceof VesselBuild) {

                    ((VesselBuild)other).removeBioEnergy(
                        drain * Time.delta / 60f
                    );
                }
            }
        }
    }
}
