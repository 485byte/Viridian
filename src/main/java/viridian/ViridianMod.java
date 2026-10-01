package viridian.content;

import arc.util.Log;
import mindustry.type.Category;
import mindustry.world.Block;

public class ViridianBlocks {

    public static Block testBlock;

    public static void load() {

        Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

        testBlock = new Block("viridian-test-block");

        testBlock.size = 1;
        testBlock.health = 100;
        testBlock.category = Category.effect;

        Log.info("=== VIRIDIAN: BLOCK CREATED: " + testBlock.name + " ===");
        Log.info("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
    }
}
