package io.github.orlouge.customoreveins.fabric;

import io.github.orlouge.customoreveins.CustomOreVeinManager;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class PlatformHelperImpl {
    public static CustomOreVeinManagerFabric CUSTOM_ORE_VEIN_MANAGER = new CustomOreVeinManagerFabric();

    public static Path getConfigDirectory() {
        return FabricLoader.getInstance().getConfigDir();
    }

    public static CustomOreVeinManager getCustomOreVeinManager() {
        return CUSTOM_ORE_VEIN_MANAGER.getCustomOreVeinManager();
    }

    public static class CustomOreVeinManagerFabric implements IdentifiableResourceReloadListener {

        private CustomOreVeinManager customOreVeinManager = null;

        @Override
        public Identifier getFabricId() {
            return CustomOreVeinManager.ID;
        }

        @Override
        public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Executor prepareExecutor, Executor applyExecutor) {
            return customOreVeinManager.reload(synchronizer, manager, prepareExecutor, applyExecutor);
        }

        public void updateRegistryAccess(DynamicRegistryManager dynamicRegistryManager) {
            customOreVeinManager = new CustomOreVeinManager(dynamicRegistryManager);
        }

        public CustomOreVeinManager getCustomOreVeinManager() {
            return customOreVeinManager;
        }
    }
}
