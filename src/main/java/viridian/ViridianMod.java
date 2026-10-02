package viridian;

import arc.util.Log;
import mindustry.mod.Mod;
import viridian.content.ViridianBlocks;

public class ViridianMod extends Mod {

    public ViridianMod() {
        Log.info("=== VIRIDIAN: MOD CONSTRUCTOR ===");
    }

    @Override
    public void loadContent() {

        Log.info("=== VIRIDIAN: LOAD CONTENT START ===");

        ViridianBlocks.load();

        Log.info("=== VIRIDIAN: LOAD CONTENT END ===");
    }
}
