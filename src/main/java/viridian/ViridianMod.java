package viridian;

import mindustry.mod.Mod;
import viridian.content.ViridianBlocks;

public class ViridianMod extends Mod {

    public ViridianMod() {
        System.out.println("=== VIRIDIAN: MOD CONSTRUCTOR ===");
    }

    @Override
    public void loadContent() {
        System.out.println("=== VIRIDIAN: LOAD CONTENT START ===");

        ViridianBlocks.load();

        System.out.println("=== VIRIDIAN: LOAD CONTENT END ===");
    }
}
