
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
    public static Block organ;
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

        // ORGAN
        organ = new Block("viridian-organ") {{
            size = 2;
            health = 1000;
            destructible = true;
            update = true;

            requirements(Category.effect, with(Items.copper, 10));

            buildType = OrganBuild::new;

            // Displays the energy level of the connected Vessel network.
            addBar("bio-energy", build ->
                new Bar(
                    "Bio-Energy",
                    Pal.powerBar,
                    () -> ((OrganBuild)build).networkEnergyFraction()
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
    // FIND ADJACENT BUILDINGS
    // =========================================================

    private static VesselBuild findAdjacentVessel(Building building) {

        for(Building other : building.proximity) {
            if(other instanceof VesselBuild) {
                return (VesselBuild)other;
            }
        }

        if(Vars.world != null) {
            for(Point2 point : Geometry.d8) {
                Building other = Vars.world.build(
                    building.tileX() + point.x,
                    building.tileY() + point.y
                );

                if(other instanceof VesselBuild) {
                    return (VesselBuild)other;
                }
            }
        }

        return null;
    }

    private static HeartBuild findAdjacentHeart(Building building) {

        for(Building other : building.proximity) {
            if(other instanceof HeartBuild) {
                return (HeartBuild)other;
            }
        }

        if(Vars.world != null) {
            for(Point2 point : Geometry.d8) {
                Building other = Vars.world.build(
                    building.tileX() + point.x,
                    building.tileY() + point.y
                );

                if(other instanceof HeartBuild) {
                    return (HeartBuild)other;
                }
            }
        }

        return null;
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

        public static float transferRatePerSecond = 100000f;
        public static float heartReturnReserve = 2f;

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
    // ORGAN BUILD
    // =========================================================

    public static class OrganBuild extends Building {

        // Energy consumed per second from the connected Vessel network.
        public float consumption = 5f;

        public float recoveryCost = 1f;
        public float recoveryDelay = 10f;
        public float recoveryPercent = 0.04f;

        public float starvationDamage = 0.05f;

        private float recoveryTimer = 0f;

        // Find the network connected to an adjacent Vessel.
        private BioEnergyGraph connectedGraph() {
            VesselBuild adjacent = findAdjacentVessel(this);

            if(adjacent == null || !adjacent.isAdded()) {
                return null;
            }

            return adjacent.bioGraph;
        }

        // The bar represents the connected network, not stored Organ energy.
        public float networkEnergyFraction() {
            BioEnergyGraph graph = connectedGraph();

            if(graph == null) return 0f;

            float capacity = graph.totalCapacity();
            if(capacity <= 0f) return 0f;

            return Math.min(1f, graph.totalEnergy() / capacity);
        }

        @Override
        public void damage(float amount) {
            super.damage(amount);
            recoveryTimer = 0f;
        }

        @Override
        public void updateTile() {

            BioEnergyGraph graph = connectedGraph();

            // Convert energy per second to energy for this update.
            float energyCost = consumption * Time.delta / 60f;

            boolean powered =
                graph != null && graph.consumeEnergy(energyCost);

            // No connected network or insufficient energy:
            // apply the shared starvation damage rule.
            if(!powered) {
                damage(maxHealth * starvationDamage * Time.delta / 60f);
                recoveryTimer = 0f;
                return;
            }

            // Recovery follows the existing Heart/Vessel rule:
            // wait 10 seconds without taking damage, then spend 1 energy
            // to recover 4% of maximum HP.
            if(health < maxHealth) {
                recoveryTimer += Time.delta;

                if(recoveryTimer >= recoveryDelay * 60f
                    && graph.consumeEnergy(recoveryCost)) {

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

        public float production = 1000000f;

        @Override
        public void updateTile() {

            float amount = production * Time.delta / 60f;
            if(amount <= 0f) return;

            VesselBuild target = findAdjacentVessel(this);

            if(target != null) {
                target.addBioEnergy(amount);
                return;
            }

            HeartBuild heart = findAdjacentHeart(this);

            if(heart != null) {
                heart.addBioEnergy(amount);
            }
        }
    }

    // =========================================================
    // ENERGY DRAIN
    // =========================================================

    public static class EnergyDrainBuild extends Building {

        public float drain = 2000000f;

        @Override
        public void updateTile() {

            float amount = drain * Time.delta / 60f;
            if(amount <= 0f) return;

            VesselBuild target = findAdjacentVessel(this);

            if(target != null) {
                if(target.bioGraph != null) {
                    target.bioGraph.removeEnergy(amount);
                } else {
                    target.removeBioEnergy(amount);
                }

                return;
            }

            HeartBuild heart = findAdjacentHeart(this);

            if(heart != null) {
                heart.removeBioEnergy(amount);
            }
        }
    }
}
