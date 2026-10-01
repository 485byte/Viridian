package viridian.content;

import arc.util.Log;
import mindustry.world.Block;
import mindustry.type.Category;

public class ViridianBlocks {

public static Block testBlock;

public static void load() {

    Log.info("=== VIRIDIAN: BLOCK LOAD START ===");

    testBlock = new Block("viridian-test-block");

    Log.info("=== VIRIDIAN: BLOCK CREATED: " + testBlock.name + " ===");

    testBlock.category = Category.effect;

    Log.info("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
}

}
