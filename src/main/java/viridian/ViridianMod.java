package viridian;

import mindustry.mod.Mod;
import viridian.content.ViridianBlocks;

public class ViridianMod extends Mod {

    public ViridianMod() {
        System.out.println("Viridian Java loaded!");
    }

    @Override
    public void loadContent() {
        ViridianBlocks.load();
    }
}
