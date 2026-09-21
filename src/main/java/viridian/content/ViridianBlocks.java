package viridian.content;

import mindustry.world.Block;
import mindustry.type.Category;

public class ViridianBlocks {

    public static Block testBlock;

    public static void load() {

        System.out.println("=== VIRIDIAN: BLOCK LOAD START ===");

        testBlock = new Block("viridian-test-block");

        System.out.println(
            "=== VIRIDIAN: BLOCK CREATED: " + testBlock.name + " ==="
        );

        testBlock.category = Category.effect;

        System.out.println("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
    }
}
