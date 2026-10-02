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

    // =========================================================
    // BLOCKS
    // =========================================================

    public static Block heart;
    public static Block energySource;
    public static Block energyDrain;
    public static Block vessel;


    // =========================================================
    // LOAD
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
        // VESSEL
        // =====================================================

        vessel = new Block("viridian-vessel") {

            {
                update = true;
                buildType = () -> new VesselBuild();
            }
        };

        vessel.size = 1;
        vessel.health = 750;
        vessel.destructible = true;

        // Có thể cháy.
        vessel.flammability = 1f;

        // Không có explosiveness,
        // nên Vessel không có cơ chế nổ.

        vessel.update = true;

        vessel.requirements(
            Category.effect,
            with(Items.copper, 5)
        );

        Log.info(
            "=== VIRIDIAN: VESSEL CREATED ==="
        );


        // =====================================================
        // COMPLETE
        // =====================================================

        Log.info(
            "=== VIRIDIAN: BLOCK SETUP COMPLETE ==="
        );
    }


    // =========================================================
    // HEART BUILD
    // =========================================================

    public static class HeartBuild extends Building {

        public float bioEnergy = 100f;
        public float maxBioEnergy = 100f;

        private float energyTimer = 0f;
        private float recoveryTimer = 0f;


        public static final float energyConsumption = 4f;

        public static final float recoveryDelay = 10f;

        public static final float recoveryCost = 1f;

        public static final float recoveryPercent = 0.04f;

        public static final float starvationDamage = 0.05f;


        // =====================================================
        // ADD ENERGY
        // =====================================================

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // =====================================================
        // REMOVE ENERGY
        // =====================================================

        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if (bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        // =====================================================
        // DAMAGE
        // =====================================================

        @Override
        public void damage(float amount) {

            super.damage(amount);

            if (amount > 0f) {
                recoveryTimer = 0f;
            }
        }


        // =====================================================
        // UPDATE
        // =====================================================

        @Override
        public void updateTile() {

            // -------------------------------------------------
            // ENERGY CONSUMPTION
            // -------------------------------------------------

            energyTimer += Time.delta;

            if (energyTimer >= 60f) {

                energyTimer -= 60f;

                removeBioEnergy(
                    energyConsumption
                );

                Log.info(
                    "=== VIRIDIAN HEART ENERGY: "
                    + bioEnergy
                    + " / "
                    + maxBioEnergy
                    + " ==="
                );
            }


            // -------------------------------------------------
            // NO ENERGY
            // -------------------------------------------------

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


            // -------------------------------------------------
            // RECOVERY
            // -------------------------------------------------

            if (health < maxHealth) {

                recoveryTimer += Time.delta;

                if (recoveryTimer >= recoveryDelay * 60f) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(
                            recoveryCost
                        );

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


    // =========================================================
    // ENERGY SOURCE BUILD
    // =========================================================

    public static class EnergySourceBuild extends Building {

        public static final float energyOutput = 500000f;


        @Override
        public void updateTile() {

            for (Building other : proximity) {

                if (other.block == heart
                    && other instanceof HeartBuild) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    heartBuild.addBioEnergy(
                        energyOutput
                        * Time.delta
                        / 60f
                    );
                }
            }
        }
    }


    // =========================================================
    // ENERGY DRAIN BUILD
    // =========================================================

    public static class EnergyDrainBuild extends Building {

        public static final float energyDrain = 1000000f;


        @Override
        public void updateTile() {

            for (Building other : proximity) {

                if (other.block == heart
                    && other instanceof HeartBuild) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    heartBuild.removeBioEnergy(
                        energyDrain
                        * Time.delta
                        / 60f
                    );
                }
            }
        }
    }


    // =========================================================
    // VESSEL BUILD
    // =========================================================

    public static class VesselBuild extends Building {

        // -----------------------------------------------------
        // BIO-ENERGY
        // -----------------------------------------------------

        public float bioEnergy = 3f;

        public static final float maxBioEnergy = 3f;


        // -----------------------------------------------------
        // SETTINGS
        // -----------------------------------------------------

        // Tốn 1 Bio-Energy mỗi giây để duy trì.
        public static final float energyConsumption = 1f;

        // Tốn 1 Bio-Energy mỗi lần hồi máu.
        public static final float recoveryCost = 1f;

        // 10 giây không bị damage thì có thể hồi.
        public static final float recoveryDelay = 10f;

        // Hồi 4% max HP.
        public static final float recoveryPercent = 0.04f;

        // Hết năng lượng thì mất 5% HP mỗi giây.
        public static final float starvationDamage = 0.05f;


        // -----------------------------------------------------
        // TIMERS
        // -----------------------------------------------------

        private float energyTimer = 0f;

        private float recoveryTimer = 0f;


        // =====================================================
        // ADD ENERGY
        // =====================================================

        public void addBioEnergy(float amount) {

            bioEnergy += amount;

            if (bioEnergy > maxBioEnergy) {
                bioEnergy = maxBioEnergy;
            }
        }


        // =====================================================
        // REMOVE ENERGY
        // =====================================================

        public void removeBioEnergy(float amount) {

            bioEnergy -= amount;

            if (bioEnergy < 0f) {
                bioEnergy = 0f;
            }
        }


        // =====================================================
        // DAMAGE
        // =====================================================

        @Override
        public void damage(float amount) {

            super.damage(amount);

            if (amount > 0f) {
                recoveryTimer = 0f;
            }
        }


        // =====================================================
        // UPDATE
        // =====================================================

        @Override
        public void updateTile() {

            // -------------------------------------------------
            // MAINTENANCE
            // -------------------------------------------------

            energyTimer += Time.delta;

            if (energyTimer >= 60f) {

                energyTimer -= 60f;

                removeBioEnergy(
                    energyConsumption
                );
            }


            // -------------------------------------------------
            // NO ENERGY
            // -------------------------------------------------

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


            // -------------------------------------------------
            // RECOVERY
            // -------------------------------------------------

            if (health < maxHealth) {

                recoveryTimer += Time.delta;

                if (recoveryTimer >= recoveryDelay * 60f) {

                    if (bioEnergy >= recoveryCost) {

                        removeBioEnergy(
                            recoveryCost
                        );

                        health += maxHealth
                            * recoveryPercent;

                        if (health > maxHealth) {
                            health = maxHealth;
                        }

                        recoveryTimer = 0f;
                    }
                }

            } else {

                recoveryTimer = 0f;
            }


            // -------------------------------------------------
            // HEART CONNECTION
            // -------------------------------------------------

            for (Building other : proximity) {

                if (other.block == heart
                    && other instanceof HeartBuild) {

                    HeartBuild heartBuild =
                        (HeartBuild)other;

                    // Heart -> Vessel
                    // 1 Bio-Energy / second

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
}
