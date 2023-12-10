package io.github.orlouge.customoreveins.fabric;

import io.github.orlouge.customoreveins.CustomOreVeinManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;

import java.nio.file.Path;

public class PlatformHelperImpl {
    public static final CustomOreVeinManagerFabric CUSTOM_ORE_VEIN_MANAGER = new CustomOreVeinManagerFabric();

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static CustomOreVeinManager getCustomOreVeinManager() {
        return CUSTOM_ORE_VEIN_MANAGER;
    }

    public static class CustomOreVeinManagerFabric extends CustomOreVeinManager implements IdentifiableResourceReloadListener {
        @Override
        public Identifier getFabricId() {
            return ID;
        }
    }
}
