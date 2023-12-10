package io.github.orlouge.customoreveins.fabric;

import io.github.orlouge.customoreveins.CustomOreVeinsMod;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.DynamicRegistrySetupCallback;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.registry.*;
import net.minecraft.resource.ResourceType;

public class CustomOreVeinsFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        CustomOreVeinsMod.init();
        DynamicRegistrySetupCallback.EVENT.register(view -> {
            if (view.asDynamicRegistryManager().getOptional(RegistryKeys.NOISE_PARAMETERS).isPresent()) {
                DynamicRegistryManager mgr = view.asDynamicRegistryManager();
                PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER.registryAccess = () -> mgr;
            }
        });
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER);
    }
}
