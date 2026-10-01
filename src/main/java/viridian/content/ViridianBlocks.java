package viridian.content;

import arc.util.Log;
import mindustry.content.Items;
import mindustry.type.Category;
import mindustry.world.Block;

import static mindustry.type.ItemStack.with;

public class ViridianBlocks {

    public static Block heart;

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

        heart = new Block("viridian-heart");

        heart.size = 2;
        heart.health = 1000;

        heart.requirements(
            Category.effect,
            with(Items.copper, 10)
        );

        Log.info("=== VIRIDIAN: HEART CREATED: " + heart.name + " ===");
        Log.info("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
    }
}
