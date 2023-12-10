package io.github.orlouge.customoreveins.forge;

import io.github.orlouge.customoreveins.CustomOreVeinsMod;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.common.Mod;

@Mod(CustomOreVeinsMod.MOD_ID)
public class CustomOreVeinsForge {
    public CustomOreVeinsForge() {
        CustomOreVeinsMod.init();
        MinecraftForge.EVENT_BUS.addListener(this::reloadListenerEventHandler);
    }

    private void reloadListenerEventHandler(AddReloadListenerEvent event) {
        DynamicRegistryManager registry = event.getRegistryAccess();
        PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER.registryAccess = () -> registry;
        event.addListener(PlatformHelperImpl.CUSTOM_ORE_VEIN_MANAGER);
    }
}
