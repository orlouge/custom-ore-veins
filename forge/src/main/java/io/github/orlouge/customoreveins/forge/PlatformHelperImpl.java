package io.github.orlouge.customoreveins.forge;

import io.github.orlouge.customoreveins.CustomOreVeinManager;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

public class PlatformHelperImpl {
    public static final CustomOreVeinManager CUSTOM_ORE_VEIN_MANAGER = new CustomOreVeinManager();
    public static Path getConfigDirectory() {
        return FMLPaths.CONFIGDIR.get();
    }

    public static CustomOreVeinManager getCustomOreVeinManager() {
        return CUSTOM_ORE_VEIN_MANAGER;
    }
}
