package viridian.content;

import mindustry.world.Block;
import mindustry.type.Category;

public class ViridianBlocks {

    public static Block testBlock;

    public static void load() {
        testBlock = new Block("viridian-test-block");

        testBlock.category = Category.effect;
    }
}
