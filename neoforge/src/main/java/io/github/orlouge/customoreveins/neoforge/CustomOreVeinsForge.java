package io.github.orlouge.customoreveins.neoforge;

import io.github.orlouge.customoreveins.CustomOreVeinsMod;
import net.minecraft.registry.DynamicRegistryManager;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

@Mod(CustomOreVeinsMod.MOD_ID)
public class CustomOreVeinsForge {
    public CustomOreVeinsForge() {
        CustomOreVeinsMod.init();
        NeoForge.EVENT_BUS.addListener(this::reloadListenerEventHandler);
    }

    private void reloadListenerEventHandler(AddReloadListenerEvent event) {
        DynamicRegistryManager registry = event.getRegistryAccess();
        PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER.registryAccess = () -> registry;
        event.addListener(PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER);
    }
}
