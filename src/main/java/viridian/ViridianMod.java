package viridian.content;

import mindustry.world.Block;
import mindustry.type.Category;
import mindustry.type.ItemStack;
import mindustry.content.Items;

public class ViridianBlocks {

    public static Block testBlock;

    public static void load() {
        testBlock = new Block("viridian-test-block");

        testBlock.category = Category.effect;
        testBlock.requirements(
            Category.effect,
            ItemStack.with(Items.copper, 1)
        );
    }
}
