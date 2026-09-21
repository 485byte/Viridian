package viridian.content;

import mindustry.world.Block;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.content.Items;

public class ViridianBlocks {

    public static Block testBlock;

    public static void load() {

        System.out.println("=== VIRIDIAN: BLOCK LOAD START ===");

        testBlock = new Block("viridian-test-block");

        System.out.println(
            "=== VIRIDIAN: BLOCK CREATED: " + testBlock.name + " ==="
        );

        testBlock.category = Category.effect;

        testBlock.requirements(
            ItemStack.with(Items.copper, 1)
        );

        System.out.println("=== VIRIDIAN: BLOCK SETUP COMPLETE ===");
    }
}
