
package viridian.content;

import arc.math.geom.Geometry;
import arc.math.geom.Point2;
import arc.util.Time;

import mindustry.Vars;
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

        // HEART
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

        // VESSEL
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

        // ENERGY SOURCE
        energySource = new Block("viridian-energy-source") {{
            size = 1;
            health = 250;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 5));

            buildType = EnergySourceBuild::new;
        }};

        // ENERGY DRAIN
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
            bioEnergy = Math.min(maxBioEnergy, bioEnergy + amount);
        }

        public void removeBioEnergy(float amount) {
            bioEnergy = Math.max(0f, bioEnergy - amount);
        }

        @Override
        public void damage(float amount) {
            super.damage(amount);
            recoveryTimer = 0f;
        }

        @Override
        public void placed() {
            super.placed();
            BioEnergyGraph.rebuild();
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            BioEnergyGraph.rebuild();
        }

        @Override
        public void onRemoved() {
            super.onRemoved();
            Time.run(1f, BioEnergyGraph::rebuild);
        }

        @Override
        public void updateTile() {

            energyTimer += Time.delta;

            if(energyTimer >= 60f) {
                removeBioEnergy(consumption);
                energyTimer -= 60f;
            }

            if(bioEnergy <= 0f) {
                damage(maxHealth * starvationDamage * Time.delta / 60f);
                recoveryTimer = 0f;
                return;
            }

            if(health < maxHealth) {

                recoveryTimer += Time.delta;

                if(recoveryTimer >= recoveryDelay * 60f
                    && bioEnergy >= recoveryCost) {

                    removeBioEnergy(recoveryCost);
                    heal(maxHealth * recoveryPercent);
                    recoveryTimer = 0f;
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

        // Maximum transfer rate, in Bio-Energy per second.
        public static float transferRatePerSecond = 100000f;

        // Minimum energy retained when returning energy to Heart.
        public static float heartReturnReserve = 1f;

        public float bioEnergy = 0f;
        public float maxBioEnergy = 3f;

        public float consumption = 1f;

        public float recoveryCost = 1f;
        public float recoveryDelay = 10f;
        public float recoveryPercent = 0.04f;

        public float starvationDamage = 0.05f;

        public BioEnergyGraph bioGraph;

        private float energyTimer = 0f;
        private float recoveryTimer = 0f;

        public void addBioEnergy(float amount) {
            bioEnergy = Math.min(maxBioEnergy, bioEnergy + amount);
        }

        public void removeBioEnergy(float amount) {
            bioEnergy = Math.max(0f, bioEnergy - amount);
        }

        @Override
        public void damage(float amount) {
            super.damage(amount);
            recoveryTimer = 0f;
        }

        @Override
        public void placed() {
            super.placed();
            BioEnergyGraph.register(this);
        }

        @Override
        public void onProximityUpdate() {
            super.onProximityUpdate();
            BioEnergyGraph.rebuild();
        }

        @Override
        public void onRemoved() {
            BioEnergyGraph.unregister(this);
            super.onRemoved();
        }

        @Override
        public void updateTile() {

            if(bioGraph != null) {
                bioGraph.update();
            }

            energyTimer += Time.delta;

            if(energyTimer >= 60f) {
                removeBioEnergy(consumption);
                energyTimer -= 60f;
            }

            if(bioEnergy <= 0f) {
                damage(maxHealth * starvationDamage * Time.delta / 60f);
                recoveryTimer = 0f;
                return;
            }

            if(health < maxHealth) {

                recoveryTimer += Time.delta;

                if(recoveryTimer >= recoveryDelay * 60f
                    && bioEnergy >= recoveryCost) {

                    removeBioEnergy(recoveryCost);
                    heal(maxHealth * recoveryPercent);
                    recoveryTimer = 0f;
                }

            } else {
                recoveryTimer = 0f;
            }
        }
    }

    // =========================================================
    // ENERGY SOURCE
    // =========================================================

    public static class EnergySourceBuild extends Building {

        // 1,000,000 Bio-Energy per second.
        public float production = 1000000f;

        @Override
        public void updateTile() {

            float amount = production * Time.delta / 60f;

            if(amount <= 0f) return;

            // Prefer a connected Vessel network.
            for(Point2 point : Geometry.d8) {

                Building other = Vars.world.build(
                    tileX() + point.x,
                    tileY() + point.y
                );

                if(other instanceof VesselBuild) {

                    VesselBuild target = (VesselBuild)other;

                    if(target.bioGraph != null) {
                        target.bioGraph.addEnergy(amount);
                    } else {
                        target.addBioEnergy(amount);
                    }

                    return;
                }
            }

            // If no Vessel is adjacent, supply Heart directly.
            for(Point2 point : Geometry.d8) {

                Building other = Vars.world.build(
                    tileX() + point.x,
                    tileY() + point.y
                );

                if(other instanceof HeartBuild) {
                    ((HeartBuild)other).addBioEnergy(amount);
                    return;
                }
            }
        }
    }

    // =========================================================
    // ENERGY DRAIN
    // =========================================================

    public static class EnergyDrainBuild extends Building {

        // 2,000,000 Bio-Energy removed per second.
        public float drain = 2000000f;

        @Override
        public void updateTile() {

            float amount = drain * Time.delta / 60f;

            if(amount <= 0f) return;

            // Prefer draining the connected Vessel network.
            for(Point2 point : Geometry.d8) {

                Building other = Vars.world.build(
                    tileX() + point.x,
                    tileY() + point.y
                );

                if(other instanceof VesselBuild) {

                    VesselBuild target = (VesselBuild)other;

                    if(target.bioGraph != null) {
                        target.bioGraph.removeEnergy(amount);
                    } else {
                        target.removeBioEnergy(amount);
                    }

                    return;
                }
            }

            // If no Vessel is adjacent, drain Heart directly.
            for(Point2 point : Geometry.d8) {

                Building other = Vars.world.build(
                    tileX() + point.x,
                    tileY() + point.y
                );

                if(other instanceof HeartBuild) {
                    ((HeartBuild)other).removeBioEnergy(amount);
                    return;
                }
            }
        }
    }
}
