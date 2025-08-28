package io.github.orlouge.customoreveins;

import net.minecraft.registry.*;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.dimension.DimensionType;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CustomOreVeinManager extends JsonDataLoader<CustomOreVein> {
    public static final Identifier ID = Identifier.of("worldgen/custom_ore_veins");
    public static final RegistryKey<Registry<CustomOreVein>> CUSTOM_ORE_VEINS_REGISTRY_KEY = RegistryKey.ofRegistry(ID);
    private Map<Identifier, CustomOreVein> customOreVeins = Map.of();
    public CustomOreVeinManager(DynamicRegistryManager dynamicRegistryManager) {
        super(dynamicRegistryManager, CustomOreVein.CODEC, CUSTOM_ORE_VEINS_REGISTRY_KEY);
    }

    @Override
    protected void apply(Map<Identifier, CustomOreVein> prepared, ResourceManager manager, Profiler profiler) {
        Map<Identifier, CustomOreVein> veins = new HashMap<>();
        prepared.forEach((identifier, customOreVein) -> {
            if (customOreVein == null) return;
            veins.put(identifier, customOreVein);
            System.out.println("Loaded " + identifier);
        });

        this.customOreVeins = veins;
    }

    public Collection<CustomOreVein> getCustomOreVeins(RegistryEntry<DimensionType> dimension) {
        if (dimension == null || dimension.getKey().isEmpty()) return Collections.emptyList();
        return customOreVeins.values().stream().filter(cov -> cov.dimension().equals(dimension.getKey().get())).toList();
    }
}
