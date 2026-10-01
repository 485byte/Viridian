package viridian.content;

import arc.graphics.Color;
import arc.util.Log;
import mindustry.content.Items;
import mindustry.gen.Building;
import mindustry.type.Category;
import mindustry.ui.Bar;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

        heart = new Block("viridian-heart") {

            public class HeartBuild extends Building {

                public float bioEnergy = 100f;
                public float maxBioEnergy = 100f;

                @Override
                public void updateTile() {
                }
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
                                Color.yellow,
                                () -> 0f
                            );
                        }

                        HeartBuild heart = (HeartBuild)entity;

                        return new Bar(
                            "Bio-Energy",
                            Color.yellow,
                            () -> heart.maxBioEnergy <= 0f
                                ? 0f
                                : heart.bioEnergy / heart.maxBioEnergy
                        );
                    }
                );
            }
        };

        heart.size = 2;
        heart.health = 1000;
        heart.destructible = true;

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );

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
